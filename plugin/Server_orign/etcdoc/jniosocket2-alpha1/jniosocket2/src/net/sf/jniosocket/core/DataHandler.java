// $Id: DataHandler.java,v 1.1 2005/06/07 05:58:00 tomy Exp $
/*
 * Created on 25.05.2005
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

/**
 * @author tomy
 *
 */
public interface DataHandler extends ExceptionHandler {

	public void handleWrite() throws IOException;
	public void handleRead() throws IOException;
	public void updateInterestOps();
}
