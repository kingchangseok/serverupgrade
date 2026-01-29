package com.azsoft.ecams.util.file;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class EFileToByteArray2 {
    public static List<byte[]> FileToByteArray2(File file) throws IOException{
    	/*
		FileChannel inChannel = new FileInputStream( file ).getChannel();
		ByteBuffer byteBuffer;
		byte[] newBytes= null;
		 
		try
		{
			//  inChannel.transferTo(0, inChannel.size(), outChannel);      // original -- apparently has trouble copying large files on Windows
	
		    // magic number for Windows, 64Mb - 32Kb)
		    int size = (int)inChannel.size();
		    
		    //byteBuffer = ByteBuffer.allocate(size);
		    System.out.println("EFileToByteArray ["+size+"]");
		    
		    byteBuffer = ByteBuffer.allocateDirect(size);
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
		} catch (Exception e){
			e.printStackTrace();
			System.out.println(e);
		}
		finally
		{
		    if ( inChannel != null )
		    {
		       inChannel.close();
		    }
		    return newBytes;
		}
		*/
    	
    	List<byte[]> retList = new ArrayList<byte[]>();
    	FileChannel inChannel = new FileInputStream( file ).getChannel();
		ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
		InputStream in = null;
		BufferedInputStream bis = null;
		
		try{
			int maxFileSize = 1024*1024*1;
			
		    int size = (int)inChannel.size();
		    
		    System.out.println("1 ["+size+"]");
		    
		    byte[] newBytes = new byte[maxFileSize];
		    if (size < maxFileSize) {
		    	newBytes = new byte[size];
		    }
		    System.out.println("2");
		    
		    in=new FileInputStream(file);
		    bis = new BufferedInputStream(in);
		    
		    int nRead;
		    while ((nRead = bis.read(newBytes)) >= 0) {
		    	byteBuffer = new ByteArrayOutputStream();
		    	byteBuffer.write(newBytes, 0, nRead);

		    	System.out.println("byteBuffer size:"+byteBuffer.size());
		    	System.out.println("nRead:"+nRead);
		    	retList.add(byteBuffer.toByteArray());
		    	
		    	byteBuffer = null;
		    }
			//return byteBuffer.toByteArray();
		    return retList;
		} catch (Exception e){
			e.printStackTrace();
			System.out.println(e);
			return null;
		}
		finally
		{
			try {
				in.close();
				bis.close();
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(e);
			}
			retList = null;
		}
		
    }
    
}
