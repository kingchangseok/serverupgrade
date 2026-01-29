package app.ecams.commoncode.dao;

import java.util.HashMap;
import java.util.List;

import app.ecams.commoncode.model.CommonCode;
import app.ecams.file.model.FileInfo;

public interface ICommonCodeDAO {
	public List<CommonCode> getCode(HashMap param);
}
