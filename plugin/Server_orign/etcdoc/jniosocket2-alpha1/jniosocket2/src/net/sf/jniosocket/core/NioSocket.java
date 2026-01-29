// $Id: NioSocket.java,v 1.5 2005/07/12 14:07:31 tomy Exp $
/*
 * Created on 18.01.2005
 *
 *  Copyright (c) Thomas Kläger, 2005. All rights reserved.
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Please refer to the LGPL license at: http://www.gnu.org/copyleft/lesser.txt
 * The latest copy of this software may be found on http://jniosocket.sourceforge.net/
 */
package net.sf.jniosocket.core;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author tomy
 *
 */
public abstract class NioSocket implements DataHandler {

	private static int idCount = 0;

	private NioWorker worker;

	private SocketChannel channel;

	protected boolean debug;

	protected boolean readPending;
	
	protected boolean writePending;

	protected boolean endOfRead = false;

	protected boolean acceptingChannel;

	protected int id;
	
	private int initOps;

	protected List wBuffers = new ArrayList();

	protected int bufferOffset = 0;

	protected ByteBuffer rBuffer;

	protected long totalBytesRead = 0;

	protected SelectionKey sk;

    private NioSocket(SocketChannel channel, int ops) throws IOException {
        this.channel = channel;
        initOps = ops;
        id = idCount++;
        channel.configureBlocking(false);
        createReceiveBuffer();
    }
    
    public void register(NioWorker nioWorker) {
    	this.worker = nioWorker;
        sk = worker.registerChannel(channel, initOps, this);
    }

    /**
     *
     */
    protected void createReceiveBuffer() {
        rBuffer = ByteBuffer.allocate(256);
    }

	protected NioSocket(SocketChannel channel) throws IOException {
        this(channel, SelectionKey.OP_READ);
	}

	protected NioSocket(SocketAddress addr) throws IOException {
        this(SocketChannel.open(), SelectionKey.OP_CONNECT);
		channel.connect(addr);
	}

	/**
	 * @throws IOException
	 *
	 */
	public void handleConnect() throws IOException {
		worker.accountingConnect();
		handleWrite();
	}

	/**
	 * Send the data to the channel, returning the number of bytes written. This
	 * does the actual write, and usually you would not override this.
	 */
	protected int send() throws IOException {
		if (!channel.isOpen())
			return 0;

		int count = 0;
		ByteBuffer last = (ByteBuffer) wBuffers.get(wBuffers.size() - 1);
		if (last.hasRemaining()) {
			ByteBuffer[] data = new ByteBuffer[wBuffers.size()];
			wBuffers.toArray(data);
			count = (int) channel.write(data, 0, data.length);
		}
        cleanupWriteBuffers();
        if (wBuffers.size() == 0)
        	writePending = false;
		return count;
	}

    /**
     *
     */
    private void cleanupWriteBuffers() {
        Iterator i = wBuffers.iterator();
        while (i.hasNext()) {
            ByteBuffer bb = (ByteBuffer) i.next();
            if (!bb.hasRemaining()) {
                i.remove();
            }
        }
    }

	protected int recv() throws IOException {
		if (!channel.isOpen())
			return -1;

        int count = channel.read(rBuffer);
        if (count < 0) {
          // channel has reached end-of-stream
          handleClose();
        }
		return count;
	}

	public void close() throws IOException {
		channel.close();
	}

    public void updateInterestOps() {
    	if (!sk.isValid())
    		return;
   
        int ops = 0;
        if (readable()) {
            ops += SelectionKey.OP_READ;
        }
        if (writeable()) {
            ops += SelectionKey.OP_WRITE;
        }
        if (ops != sk.interestOps()) {
            sk.interestOps(ops);
        }
    }

	/**
	 *
	 */
	private void connResetHandler() {
		// TODO Auto-generated method stub
	}

    protected boolean readable() {
        return true;
    }

	protected boolean writeable() {
		return writePending;
	}

	/**
	 * @throws IOException
	 *
	 */
	public void finishConnect() throws IOException {
		channel.finishConnect();
	}

	/**
	 * @return
	 */
	public boolean isConnected() {
		return channel.isConnected();
	}

	/**
	 * @throws IOException
	 *
	 */
	protected void handleClose() throws IOException {
		close();
	}

	public void setWriteInterest() {
		int ops = sk.interestOps();
		sk.interestOps(ops | SelectionKey.OP_WRITE);
	}

    public void handleException(CancelledKeyException cke) {
    	try {
			close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
    }
    public void handleException(IOException ioe) {
    	try {
			close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
    }
}
