// $Id: NioServerSocket.java,v 1.6 2005/06/07 05:58:27 tomy Exp $
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
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @author tomy
 *
 */
public abstract class NioServerSocket implements AcceptHandler {

	private NioWorker nioWorker;
    private ServerSocketChannel serverChannel;
	private SelectionKey sk;

	public NioServerSocket() throws IOException {
		serverChannel = ServerSocketChannel.open();
	}

	public NioServerSocket(SocketAddress addr) throws IOException {
        serverChannel = ServerSocketChannel.open();
        ServerSocket serverSocket = serverChannel.socket();
        serverChannel.configureBlocking(false);
        serverSocket.setReuseAddress( true );
        serverSocket.bind (addr);
	}
	public void register(NioWorker nioWorker) {
		this.nioWorker = nioWorker;
        nioWorker.registerChannel(serverChannel, SelectionKey.OP_ACCEPT, this);
	}

	/* (non-Javadoc)
	 * @see net.sf.jniosocket.core.AcceptHandler#handleAccept()
	 */
	public void handleAccept() throws IOException {
		SocketChannel sc = null;
		NioSocket ns = null;
		sc = accept();
		if (sc == null)
			return; // there was an IOException while accepting
		ns = createSocket(nioWorker, sc);
		ns.handleRead();
		nioWorker.accountingAccept();
	}

    public SocketChannel accept (){
        SocketChannel schannel = null;
        try {
            schannel = serverChannel.accept();
        } catch (Exception exc) {
        	// TODO logging
        	System.err.println( "Nio accept 661 EXC:"+ exc.toString()); exc.printStackTrace();
        }
        return schannel;
    }

    public abstract NioSocket createSocket(NioWorker server, SocketChannel sc) throws IOException;

    public void close() throws IOException {
    	serverChannel.close();
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
