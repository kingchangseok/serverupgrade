package app.ecams.lang.dao;


import java.util.HashMap;
import java.util.List;

import app.ecams.lang.model.Lang;

public interface ILangDAO {
	List<Lang> getLangInfo(HashMap params);
	String getRsrcCD(String syscd);
}
