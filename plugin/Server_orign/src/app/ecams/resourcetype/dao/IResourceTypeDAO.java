package app.ecams.resourcetype.dao;


import java.util.HashMap;
import java.util.List;

import app.ecams.resourcetype.model.ResourceType;

public interface IResourceTypeDAO {
	List<ResourceType> getRsrcInfo(String cmSyscd) ;
	HashMap getRsrcInfo_detail(HashMap param);
}
