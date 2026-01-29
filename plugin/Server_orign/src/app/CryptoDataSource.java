package app;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;

import app.util.crypto.Encryptor;

public class CryptoDataSource extends BasicDataSource{
	private Encryptor encrpytor;

	public void setEncrpytor(Encryptor encrpytor) {
		this.encrpytor = encrpytor;
	}
	
	@Override
	public synchronized void setUrl(String url){

		url = ConfigFactory.getProperties("P_url");
		
		if ("false".equals(ConfigFactory.getProperties("P_secu"))) {
			try {
				super.setUrl(url);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				//System.out.println(oEncryptor.strGetDecrypt(url));
				super.setUrl(this.encrpytor.strGetDecrypt(url));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void setUsername(String username){

		username = ConfigFactory.getProperties("P_username");
		
		if ("false".equals(ConfigFactory.getProperties("P_secu"))) {
			try {
				super.setUsername(username);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				//System.out.println(oEncryptor.strGetDecrypt(username));
				super.setUsername(this.encrpytor.strGetDecrypt(username));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void setPassword(String password){

		password = ConfigFactory.getProperties("P_password");
		
		if ("false".equals(ConfigFactory.getProperties("P_secu"))) {
			try {
				super.setPassword(password);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				//System.out.println(oEncryptor.strGetDecrypt(password));
				super.setPassword(this.encrpytor.strGetDecrypt(password));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
