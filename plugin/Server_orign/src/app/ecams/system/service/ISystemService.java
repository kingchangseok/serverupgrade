package app.ecams.system.service;


import java.util.HashMap;

import app.core.proto.ProtoEcams.EcamsMessage;
import app.core.proto.ProtoEcams.ReturnMsg;

public interface ISystemService {
	ReturnMsg getSysInfo (EcamsMessage ecamsmsg) ;
	ReturnMsg getSysInfo_user (EcamsMessage ecamsmsg) ;
	public HashMap getSysInfo_detail(String syscd) ;
	public HashMap sysdirinfo(String pathcd) ;
}

