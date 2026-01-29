package app.ecams.anal.dao;

import java.util.HashMap;
import java.util.List;

import app.ecams.anal.model.AnalInfo;

public interface IAnalDAO {
	List<AnalInfo> getAnalList_detail(HashMap param);
	List<AnalInfo> getAnalList_Method(String itemid) ;
}
