// $Id: NioWorker.java,v 1.5 2005/07/12 14:07:31 tomy Exp $
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
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

/**
 * @author tomy
 *
 */
public class NioWorker {

	private Selector s = null;

	public NioWorker() throws IOException {
		s = Selector.open();
	}

	public void poll(long timeout) throws IOException {
		if (s.keys().isEmpty()) return;
		int n = s.select(timeout);
		if (n == 0) return;
		Set selectedKeys = s.selectedKeys();
		
		Iterator i = selectedKeys.iterator();
		while (i.hasNext()) {
			SelectionKey sk = (SelectionKey) i.next();
			i.remove();
            try {
                checkAccept(sk);
                checkConnect(sk);
                checkRead(sk);
                checkWrite(sk);
            } catch (IOException ioe) {
            	ExceptionHandler eh = (ExceptionHandler) sk.attachment();
            	eh.handleException(ioe);
            } catch (CancelledKeyException cke) {
            	ExceptionHandler eh = (ExceptionHandler) sk.attachment();
            	eh.handleException(cke);
			}
		}
	}

    /**
     * @param sk
     * @throws IOException
     */
    private void checkAccept(SelectionKey sk) throws IOException {
        if (sk.isAcceptable()) {
        	AcceptHandler ah = (AcceptHandler) sk.attachment();
        	ah.handleAccept();
        }
    }

    /**
     * @param sk
     * @throws IOException
     */
    private void checkWrite(SelectionKey sk) throws IOException {
        if (sk.isValid() && sk.isWritable()) {
        	DataHandler ns = (DataHandler) sk.attachment();
        	ns.handleWrite();
            ns.updateInterestOps();
        }
    }

    /**
     * @param sk
     * @throws IOException
     */
    private void checkRead(SelectionKey sk) throws IOException {
        if (sk.isValid() && sk.isReadable()) {
            DataHandler ns = (DataHandler) sk.attachment();
        	ns.handleRead();
            ns.updateInterestOps();
        }
    }

    /**
     * @param sk
     * @throws IOException
     */
    private void checkConnect(SelectionKey sk) throws IOException {
        if (sk.isConnectable()) {
            ConnectHandler ns = (ConnectHandler) sk.attachment();
        	ns.finishConnect();
        	if (ns.isConnected()) {
        		ns.handleConnect();
        	}
            ns.updateInterestOps();
        }
    }

	public void loop(long timeout) throws IOException {
		while (!s.keys().isEmpty()) {
			poll(timeout);
		}

	}

	/**
	 * @param schannel
	 * @param op_read
	 * @return
	 */
	public SelectionKey registerChannel(SelectableChannel channel, 
			int ops, ExceptionHandler att) {
        if (channel == null) return null;
        SelectionKey  sk = null;
        try {
            sk = channel.register(s, ops, att);
        } catch (Exception e) {
        }
        return sk;
	}

	private int acceptCount;
	/**
	 *
	 */
	public void accountingAccept() {
		acceptCount++;
	}

	private int connectCount;
	/**
	 *
	 */
	public void accountingConnect() {
		connectCount++;
	}

}
