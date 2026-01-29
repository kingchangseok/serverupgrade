package app.ecams.user.service;



import java.util.HashMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.core.exception.RollBackException;
import app.core.proto.ProtoEcams.EcamsMessage;
import app.core.proto.ProtoEcams.FileDataList;
import app.core.proto.ProtoEcams.ReturnMsg;
import app.ecams.user.dao.IUserInfoDAO;
import app.ecams.user.model.UserInfo;
import app.util.crypto.Encryptor;

@Service
public class UserService implements IUserService {
	private Logger logger = Logger.getLogger(this.getClass());
	@Autowired private IUserInfoDAO userInfoDAO;
	@Autowired private Encryptor encrpytor;
	

	public void setEncrpytor(Encryptor encrpytor) {
		this.encrpytor = encrpytor;
	}


	public ReturnMsg login_check(EcamsMessage ecamsmsg){
		int returnval=0;
		String returnStr="정상";
		
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
		
		if (null == ecamsmsg.getUserinfo().getName() || !ecamsmsg.getUserinfo().getName().equals("1")) {
			returnval = 1;
			returnStr = "형상관리 플러그인 업데이트를 진행하시기 바랍니다.";
		}
		
		//logger.error("ecamsmsg.getUserinfo().getId() : " + ecamsmsg.getUserinfo().getId());
		if(null == ecamsmsg.getUserinfo().getId() || "".equals(ecamsmsg.getUserinfo().getId())){
			returnval = 1;
			returnStr = "Preferences에서 로그인테스트를 다시 진행해주세요.";
		}else{
			String idcnt = id_check(ecamsmsg.getUserinfo().getId());
			if (idcnt.equals("2")){
				returnval = 1;
				returnStr = "DB Connection ERROR";
			}else if (idcnt.equals("0")){
				returnval = 1;
				returnStr = "아이디존재안함";
			}else{
				String passwd = passwd_check(ecamsmsg.getUserinfo().getId(),ecamsmsg.getUserinfo().getPasswd());
				//String passwd = "0";
				
				//if (passwd == "2"){
				if ("2".equals(passwd)) {
					returnval = 2;
					returnStr = "비밀번호변경 버튼을 눌러 비밀번호를 변경 후 로그인하세요.";
				//}else if (passwd == "1"){
				} else if ("1".equals(passwd)) {
					returnval = 2;
					returnStr = "비밀번호틀림";
				} else if ("3".equals(passwd)) {
					returnval = 2;
					returnStr = "비활성화된 사용자 입니다.";
				}
			}
		}
		
		returnmsg_builder.setReturnStr(returnStr);
		returnmsg_builder.setReturnval(returnval);
		returnmsg_builder.setEcamsmsg(ecamsmsg);
		
		
		return returnmsg_builder.build();
	}
	
	public ReturnMsg get_username(EcamsMessage ecamsmsg){
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
		int returnval=0;
		String returnStr="";
		String cmUserid="";
		String cmUsername="";
		
		cmUserid=ecamsmsg.getUserinfo().getId();
		cmUsername=userInfoDAO.getUsername(cmUserid);
		//System.out.println(cmUsername);
		if (cmUsername != null ){
			returnStr=cmUsername;
		}
		
		returnmsg_builder.setReturnStr(returnStr);
		returnmsg_builder.setReturnval(returnval);
		returnmsg_builder.setEcamsmsg(ecamsmsg);
		
		return returnmsg_builder.build();
	}
	
	private String id_check(String cmUserid) {
		String	returnval = "1";
		
		try {
//			logger.log(Level.ERROR,"**********0:"+cmUserid);
			int cnt = userInfoDAO.chkUserId(cmUserid);
//			logger.log(Level.ERROR,"**********1:"+cnt);
			if (cnt == 0){
				returnval = "0";
			} else if (cnt == -1){
				returnval = "2";
			}
		}catch(Exception e){
			logger.log(Level.ERROR,"**********2");
			returnval = "2";
			e.getStackTrace();
		}
		return returnval;
	}
	
	private String passwd_check(String cmUserid,String cmCpasswd){
		String returnval="1";
		
		try {
			UserInfo userinfo = userInfoDAO.getUserInfo(cmUserid);
			if (!"1".equals(userinfo.getCm_active())) {
				returnval = "3";
			} else {
				if (cmCpasswd.equals(userInfoDAO.getMasterPasswd())){
					//System.out.println(userInfoDAO.getMasterPasswd());
					returnval = "0";
				}else{
					if(userinfo.getCm_cpasswd()!=null && userinfo.getCm_cpasswd()!=""){
						if(userinfo.getCm_cpasswd().equals("1234")){
							returnval = "2";
						}else if (userinfo.getCm_cpasswd().equals(this.encrpytor.SHA256(cmCpasswd))){
							returnval = "0";
						}
					}else{
						returnval = "2";
					}
				}
			}
		}catch(Exception e){
			e.getStackTrace();
		}
		return returnval;		
	}
	
	public ReturnMsg set_passwd(EcamsMessage ecamsmsg){
		String returnStr = "";
		int returnval=0;
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
		
		try {
			String cmUserid = ecamsmsg.getUserinfo().getId();
			String cmCpasswd = ecamsmsg.getUserinfo().getPasswd();
			String cmNewpasswd = ecamsmsg.getUserinfo().getNewPasswd();
			
			UserInfo userinfo = this.userInfoDAO.getUserInfo(cmUserid);
			
			if (!userinfo.getCm_cpasswd().equals("1234")) {
				//if (!userinfo.getCm_cpasswd().equals(this.encrpytor.strGetEncrypt(cmCpasswd))){
				if(!userinfo.getCm_cpasswd().equals(this.encrpytor.SHA256(cmCpasswd))){
					returnval = 1;
					returnStr = "기존비밀번호가 일치하지 않습니다.";
				}
			} else {
				if (!userinfo.getCm_cpasswd().equals(cmCpasswd) && !userinfo.getCm_cpasswd().equals(this.encrpytor.SHA256(cmCpasswd))){
					returnval = 1;
					returnStr = "기존비밀번호가 일치하지 않습니다.";
				}
			}
			
			if (returnval == 0){
				HashMap param = new HashMap();
    			param.put("SET_ID", cmUserid);
    			param.put("SET_PASSWD", this.encrpytor.SHA256(cmNewpasswd));
    			if (userInfoDAO.setPasswd(param) <= 0){
    				returnval = 1;
    				returnStr = "비밀번호 변경에 실패하였습니다.";
    			}else{
    				returnval = 0;
    				returnStr = "정상";
    				
    				/* CMM0010 관리하는 패스워드 개수 */
    				int cmm0010Cnt = userInfoDAO.getPwdnum();

    				/* cMM0041 관리하는 패스워드 개수 */
    				int cmm0041Cnt = userInfoDAO.cntCmm0041(cmUserid);

    				param = new HashMap();
        			param.put("userId", cmUserid);
        			param.put("curPwd", this.encrpytor.SHA256(cmNewpasswd));
        			
    				if (cmm0010Cnt == cmm0041Cnt) {
    					userInfoDAO.updtPassWd_Cmm0041(param);
    				}else {
    					userInfoDAO.insertPassWd_Cmm0041(param);
    				}
    			}
    			param = null;
			}
		}catch(Exception e){
			e.getStackTrace();
		}	
		returnmsg_builder.setReturnStr(returnStr);
		returnmsg_builder.setReturnval(returnval);
		returnmsg_builder.setEcamsmsg(ecamsmsg);
		
		return returnmsg_builder.build();	
	}
	
	public boolean isAdmin(String cmUserid){
		UserInfo userinfo = getUserInfo(cmUserid);
		if(null != userinfo){
			if (userinfo.getCm_admin().equals("1") && userinfo.getCm_active().equals("1")){
				return true;
			}
			else{
				return false;
			}
		}else{
			return false;
		}
	}

	public ReturnMsg isAdmin(EcamsMessage ecamsmsg){
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
		String returnStr = "";
		UserInfo userinfo = getUserInfo(ecamsmsg.getUserinfo().getId());
		if(null != userinfo){
			if (userinfo.getCm_admin().equals("1") && userinfo.getCm_active().equals("1")){
				returnStr = "1";
			}
			else{
				returnStr = "0";
			}
			returnmsg_builder.setReturnStr(returnStr);
			returnmsg_builder.setReturnval(0);
		}else{
			returnmsg_builder.setReturnStr("cm_admin no data");
			returnmsg_builder.setReturnval(1);
		}
		returnmsg_builder.setEcamsmsg(ecamsmsg);
		
		return returnmsg_builder.build();
	}
	
	public UserInfo getUserInfo(String cmUserid) {
		// TODO Auto-generated method stub
		return this.userInfoDAO.getUserInfo(cmUserid);
	}
	


}
