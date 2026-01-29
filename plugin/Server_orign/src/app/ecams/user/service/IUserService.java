package app.ecams.user.service;



import app.core.proto.ProtoEcams.EcamsMessage;
import app.core.proto.ProtoEcams.ReturnMsg;
import app.ecams.user.model.UserInfo;

public interface IUserService {
	public ReturnMsg login_check(EcamsMessage ecamsmsg);
	public UserInfo getUserInfo(String cmUserid);
	public boolean isAdmin(String cmUserid);
	public ReturnMsg get_username(EcamsMessage ecamsmsg);
	public ReturnMsg isAdmin(EcamsMessage message);
	public ReturnMsg set_passwd(EcamsMessage ecamsmsg);
}
