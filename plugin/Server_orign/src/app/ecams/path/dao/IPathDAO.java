package app.ecams.path.dao;

import java.util.HashMap;

public interface IPathDAO {
	public String getPath(HashMap param);
	public String getPathCD(HashMap param);
	public String getBasePath(HashMap param);
	public String getMaxDsnCD(String syscd);
	public int insert_cmm0070(HashMap param);
	public int insert_cmm0072(HashMap param);
	public int insert_cmm0073(HashMap param);
	public String getHomePath(String syscd);
	public String getTempPath(String syscd);
	public String getProjectPath(HashMap param);
}
