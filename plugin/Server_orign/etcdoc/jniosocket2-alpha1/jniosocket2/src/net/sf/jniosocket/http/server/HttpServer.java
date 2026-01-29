// $Id: HttpServer.java,v 1.4 2005/06/07 05:58:27 tomy Exp $
/*
 * Created on 01.02.2005
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
package net.sf.jniosocket.http.server;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

import net.sf.jniosocket.core.NioWorker;
import net.sf.jniosocket.core.NioServerSocket;
import net.sf.jniosocket.core.NioSocket;

/**
 * @author tomy
 *
 */
public class HttpServer extends NioServerSocket {

	/**
	 * @param nw
	 * @param port
	 * @throws IOException
	 */
	public HttpServer(SocketAddress addr) throws IOException {
		super(addr);
	}

	/* (non-Javadoc)
	 * @see net.sf.jniosocket.core.NioServerSocket#createSocket(net.sf.jniosocket.core.NioWorker, java.nio.channels.SocketChannel)
	 */
	public NioSocket createSocket(NioWorker worker, SocketChannel sc) throws IOException {
		HttpChannel result = new HttpChannel(sc);
		return result;
	}

}
