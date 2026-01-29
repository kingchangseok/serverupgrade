// $Id: ChatSocket.java,v 1.5 2005/06/07 05:58:27 tomy Exp $
/*
 * Created on 05.02.2005
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
import java.nio.channels.SocketChannel;
import java.util.Arrays;

/**
 * @author tomy
 *
 */
public abstract class ChatSocket extends NioSocket {

    protected int expected = 0;
    protected byte[] terminator = null;
    protected int terminatorLen = 0;
    
    /**
     * @param worker
     * @param channel
     * @throws IOException
     */
    protected ChatSocket(SocketChannel channel)
            throws IOException {
        super(channel);
    }
    
    /**
     * @param worker
     * @param host
     * @param port
     * @throws IOException
     */
    protected ChatSocket(SocketAddress addr)
            throws IOException {
        super(addr);
    }
    
    /* (non-Javadoc)
     * @see net.sf.jniosocket.core.NioSocket#handleRead()
     */
    public void handleRead() throws IOException {
        recv();
        rBuffer.flip();
        while (rBuffer.hasRemaining()) {
            if (expected > 0) {
                int r = rBuffer.remaining();
                if (r < expected) {
                    collectIncomingData(rBuffer);
                    r -= rBuffer.remaining();
                    expected -= r;
                    break;
                } else {
                    ByteBuffer bb = rBuffer.slice();
                    bb.limit(expected); 
					rBuffer.position(rBuffer.position()+expected);
                    collectIncomingData(bb);
                    foundTerminator();
                }
            } else if (terminator != null) {
                ByteBuffer search = rBuffer.duplicate();
                int index = findTerminator(search);
                if (index >= 0) {
                    if (index > 0) {
                        ByteBuffer bb = rBuffer.slice();
                        bb.limit(index); 
                        collectIncomingData(bb);
                    }
                    rBuffer.position(rBuffer.position()+index+terminatorLen);
                    foundTerminator();
                } else {
                    // check for a prefix of the terminator
                    int len = findPrefixAtEnd(search);
                    if (len > 0) {
                        if (len < rBuffer.remaining()) {
                            ByteBuffer bb = rBuffer.slice();
                            bb.limit(rBuffer.remaining()-len); 
                            collectIncomingData(bb);
                            rBuffer.position(rBuffer.position()+rBuffer.remaining()-len);
                        }
                        break;
                    } else {
                        collectIncomingData(rBuffer);
                        break;
                    }
                }
            } else {
                collectIncomingData(rBuffer);
                break;
            }
        }
        rBuffer.compact();
    }

    /* (non-Javadoc)
     * @see net.sf.jniosocket.core.NioSocket#handleWrite()
     */
    public void handleWrite() throws IOException {
        initiateSend();
    }

    private void initiateSend() throws IOException {
        if (wBuffers.size() > 0 && isConnected()) {
            send();
        }
    }

    public void appendSendData(String data) throws IOException {
        appendSendData(data.getBytes());
    }

    public void appendSendData(byte[] data) throws IOException {
        appendSendData(ByteBuffer.wrap(data));
    }

    public void appendSendData(ByteBuffer data) throws IOException {
        writePending = true;
        wBuffers.add(data);
        initiateSend();
    }

    public void appendSendData(ByteBuffer[] data) throws IOException {
        writePending = true;
        wBuffers.addAll(Arrays.asList(data));
        initiateSend();
    }

    protected abstract void collectIncomingData(ByteBuffer data) throws IOException;
    
    protected abstract void foundTerminator() throws IOException;
    
    protected int findTerminator(ByteBuffer data) {
        byte[] b;
        int offset;
        int count = data.remaining();
        if (data.hasArray()) {
            b = data.array();
            offset = data.arrayOffset()+data.position();
        } else {
            b = new byte[data.remaining()];
            data.get(b);
            offset = 0;
        }
        int i = offset;
        byte first = terminator[0];
        terminatorLen = terminator.length;
        int max = offset+count-terminatorLen;
    startSearchForFirstByte:
        while (true) {
            /* Look for first byte. */
            while (i < max && b[i] != first) {
                i++;
            }
            if (i > max) {
                return -1;
            }

            /* Found first byte, now look at the rest of terminator */
            int j = i + 1;
            int end = j + terminatorLen - 1;
            int k = 1;
            while (j < end) {
                if (b[j++] != terminator[k++]) {
                    i++;
                    /* Look for first byte again. */
                    continue startSearchForFirstByte;
                }
            }
            return i - offset;    /* Found whole termiinator */
        }
    }

    protected int findPrefixAtEnd(ByteBuffer data) {
        byte[] b;
        int offset;
        int count = data.remaining();
        if (data.hasArray()) {
            b = data.array();
            offset = data.arrayOffset()+data.position();
        } else {
            b = new byte[data.remaining()];
            data.get(b);
            offset = 0;
        }
        int max = offset+count;
        int len = terminator.length-1;
        while (len > 0) {
            int i = max-len;
            int j = 0;
            while (i < max && b[i] == terminator[j++])
                i++;
            if (i == max) 
                return len;
            len--;
        }
        return 0;
    }
}
