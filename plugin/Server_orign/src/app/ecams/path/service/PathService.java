package app.ecams.path.service;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.ecams.path.dao.IPathDAO;

@Service
public class PathService implements IPathService {
	private Logger logger = Logger.getLogger(this.getClass());
	@Autowired private IPathDAO pathDAO;

	public String getPath(HashMap param) {
		// TODO Auto-generated method stub
		return pathDAO.getPath(param);
	}
	
	public String getPathCD(HashMap param) {
		HashMap nparam = null;
		String dsncd=null;


		nparam = new HashMap();
		nparam.put("cm_syscd",param.get("cm_syscd"));
		nparam.put("cm_rsrccd",param.get("cm_rsrccd"));
		nparam.put("cm_jobcd",param.get("cm_jobcd"));
		
		String basepath =  pathDAO.getBasePath(nparam);

		System.out.println(">>>PathService.getPathCD: basepath: "+basepath);
		if (null == basepath || "".equals(basepath)) {
			System.out.println(">>>체크아웃서버 프로그램종류 연결정보 등록확인 rsrccd["+param.get("cm_rsrccd")+"]");
			return null;
		}
		String dirpath="";
		
		nparam = new HashMap();
		
		if ( ((String)param.get("dirpath")).equals("/")){
			dirpath = basepath;
		}
		else{
			if( ((String)param.get("dirpath")).indexOf(basepath+"/") == 0 ) {
				dirpath = (String)param.get("dirpath");
			} else {
				if ( ((String)param.get("dirpath")).equals(basepath)){
					dirpath = (String)param.get("dirpath");
				} else {
					dirpath = basepath+"/"+(String)param.get("dirpath");
				}
			}
		}
		
		nparam.put("cm_syscd",param.get("cm_syscd"));
		nparam.put("cm_dirpath",dirpath.replaceAll("//", "/"));

		System.out.println(">>>PathService.getPathCD: "+nparam.get("cm_dirpath"));
		
		dsncd = pathDAO.getPathCD(nparam);
		
		if (dsncd == null){
			String strDsnCD = pathDAO.getMaxDsnCD((String)param.get("cm_syscd"));
			if (strDsnCD == null){
				strDsnCD = "0000001";
			}
			
			nparam = new HashMap();
			
			nparam.put("cm_syscd",param.get("cm_syscd"));
			nparam.put("cm_dsncd",strDsnCD);
			nparam.put("cm_userid",param.get("cm_userid"));
			nparam.put("cm_dirpath",dirpath.replaceAll("//", "/"));
			
			if (pathDAO.insert_cmm0070(nparam) <= 0){
				return null;
			}
			
			nparam = new HashMap();
			
			nparam.put("cm_syscd",param.get("cm_syscd"));
			nparam.put("cm_dsncd",strDsnCD);
			nparam.put("cm_rsrccd",param.get("cm_rsrccd"));
			
			if (pathDAO.insert_cmm0072(nparam) <= 0){
				return null;
			}
			
			nparam = new HashMap();
			
			nparam.put("cm_syscd",param.get("cm_syscd"));
			nparam.put("cm_dsncd",strDsnCD);
			nparam.put("cm_jobcd",param.get("cm_jobcd"));
			
			if (pathDAO.insert_cmm0073(nparam) <= 0){
				return null;
			}			
			
			dsncd = strDsnCD;
		}
			
		
		
		// TODO Auto-generated method stub
		return dsncd;
	}

}
