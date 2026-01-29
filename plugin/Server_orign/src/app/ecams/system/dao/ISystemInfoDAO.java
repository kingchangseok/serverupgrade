package app.ecams.system.dao;


import java.util.HashMap;
import java.util.List;

import app.ecams.system.model.SystemInfo;

public interface ISystemInfoDAO {
	List<SystemInfo> getSysInfo();
	List<SystemInfo> getSysInfo_user(HashMap params);
	HashMap getSysInfo_detail(String syscd) ;
	HashMap timecheck(String syscd) ;
	HashMap sysdirinfo(String pathcd) ;
}
