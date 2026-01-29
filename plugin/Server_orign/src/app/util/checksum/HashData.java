package app.util.checksum;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

public class HashData {

	public static String getHashData(File file) {
		BufferedInputStream fis = null;
		try {
			fis = new BufferedInputStream(new FileInputStream(file));
			return DigestUtils.sha256Hex(fis);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(fis);
		}
		return null;
	}
}
