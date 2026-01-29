package app.util.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileToByteArray {
    public static byte[] FileToByteArray( File file) throws IOException{
		FileChannel inChannel = new FileInputStream( file ).getChannel();
		ByteBuffer byteBuffer;
		 byte[] newBytes= null;
		 
		try
		{
			//  inChannel.transferTo(0, inChannel.size(), outChannel);      // original -- apparently has trouble copying large files on Windows
	
		    // magic number for Windows, 64Mb - 32Kb)
		    int size = (int)inChannel.size();
		    
		    byteBuffer = ByteBuffer.allocate(size);
		    newBytes = new byte[size];
		    
		    int nRead,nGet;
		    
		    while ( (nRead=inChannel.read( byteBuffer )) != -1 )
		    {
		        if ( nRead == 0 )
		            continue;
		        byteBuffer.position( 0 );
		        byteBuffer.limit( nRead );

		        while( byteBuffer.hasRemaining( ) )
		        {
		            nGet = Math.min( byteBuffer.remaining( ), size );
		            byteBuffer.get( newBytes, 0, nGet );

		        }
		        byteBuffer.clear( );
		    }
		}
		finally
		{
		    if ( inChannel != null )
		    {
		       inChannel.close();
		    }
		    return newBytes;
		}
    }
}
