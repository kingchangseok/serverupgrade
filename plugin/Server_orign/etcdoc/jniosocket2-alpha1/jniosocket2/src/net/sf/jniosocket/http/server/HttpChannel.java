// $Id: HttpChannel.java,v 1.3 2005/06/07 05:58:27 tomy Exp $
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
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import net.sf.jniosocket.core.ChatSocket;

/**
 * @author tomy
 *
 */
public class HttpChannel extends ChatSocket {

	private StringBuffer request;

	/**
	 * @param worker
	 * @param channel
	 * @throws IOException
	 */
	public HttpChannel(SocketChannel channel) throws IOException {
		super(channel);
		request = new StringBuffer();
        terminator = "\r\n\r\n".getBytes();
	}

    /* (non-Javadoc)
     * @see net.sf.jniosocket.core.ChatSocket#collectIncomingData(java.nio.ByteBuffer)
     */
    protected void collectIncomingData(ByteBuffer data) {
        Charset charset=Charset.forName("ISO-8859-1");
        CharsetDecoder decoder = charset.newDecoder();
        try {
            CharBuffer charBuffer = decoder.decode(data);
            System.out.print(charBuffer.array());
        } catch (CharacterCodingException cce) {
            System.out.println("Exception collection data");
            cce.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see net.sf.jniosocket.core.ChatSocket#foundTerminator()
     */
    protected void foundTerminator() throws IOException {
        System.out.println("foundTerminator");
        appendSendData("You did it!\r\n\r\n");
    }

}
