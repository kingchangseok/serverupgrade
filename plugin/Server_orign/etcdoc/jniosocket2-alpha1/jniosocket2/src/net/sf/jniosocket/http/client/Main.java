// $Id: Main.java,v 1.4 2005/06/07 05:58:27 tomy Exp $
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
package net.sf.jniosocket.http.client;

import java.io.IOException;
import java.net.InetSocketAddress;

import net.sf.jniosocket.core.NioWorker;

/**
 * @author tomy
 *
 */
public class Main {

	private NioWorker h;

	public Main() throws IOException {
		h = new NioWorker();
		HttpClient hc = new HttpClient(new InetSocketAddress("127.0.0.1", 80));
		hc.register(h);
		hc.appendSendData("GET /\r\n\r\n".getBytes());
		for (int i = 0; i < 6; i++)
			h.poll(10000);
	}

	public static void main(String[] args) {
		try {
			Main m = new Main();
		} catch (IOException e) {
			e.printStackTrace();
		}
        System.out.println("Client terminated");
	}
}
