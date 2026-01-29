package app.ecams.request.service;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.core.proto.ProtoEcams.EcamsMessage;
import app.core.proto.ProtoEcams.FileDataList;
import app.core.proto.ProtoEcams.ReturnMsg;
import app.ecams.file.service.IFileService;
import app.ecams.request.checkincnl.dao.ICheckInCnlDAO;
import app.ecams.request.checkoutcnl.dao.ICheckOutCnlDAO;
import app.ecams.request.confirm.service.IConfirmService;
import app.ecams.request.dao.IRequestDAO;
import app.ecams.srjob.dao.ISRJobDAO;
import app.ecams.srjob.model.SRJobInfo;

@Service
public class RequestService implements IRequestService{
	private Logger logger = Logger.getLogger(this.getClass());
	@Autowired protected IFileService fileService;	
	@Autowired private IRequestDAO requestDAO;
	@Autowired private IConfirmService confirmService;
	@Autowired private ICheckOutCnlDAO checkOutCnlDAO;
	@Autowired private ISRJobDAO serviceRequestDAO;
	
	public String checkFilesStatus(FileDataList fileDataList,HashMap systeminfo,String qrycd,String editor, String srid){
		int i;
		String errStr = null;
		
		try {

			if ( (null != srid) && !"".equals(srid) ){
				HashMap param = new HashMap();
				param.put("srid",srid);
				param.put("editor", editor);
				
				HashMap srtatus = serviceRequestDAO.getCmc0110(param);
				
				if(null == srtatus || "".equals(srtatus)){
					errStr = srid+"에 해당하는 담당개발자가 아닙니다.";
				}else if(!"2".equals(srtatus.get("CC_STATUS")) && !"A".equals(srtatus.get("CC_STATUS")) && !"C".equals(srtatus.get("CC_STATUS")) ){
					if (!"11".equals(qrycd)) {
						errStr = srid+"는 개발가능한 상태가 아닙니다. ("+srtatus.get("CM_CODENAME")+")";
					}
				}
			} else {
				if (systeminfo.get("cm_sysinfo").toString().substring(9,10).equals("0")) {
					errStr = "SR을 사용하는 시스템은 SR을 사용하여 신청하시기 바랍니다.";
				}
				
			}
			
			if(null == errStr){
				for (i=0;i<fileDataList.getFiledatasCount();i++){
					
					if (fileDataList.getFiledatas(i).getItemid() == null || fileDataList.getFiledatas(i).getItemid().equals("")) {
						continue;
					}else {
						HashMap filestatus = fileService.getFileStatus(fileDataList.getFiledatas(i).getItemid(), qrycd);
						
						if (filestatus == null || filestatus.equals("")){
							errStr = "["+ fileDataList.getFiledatas(i).getFilename() + "] 폐기된파일입니다.";
						}else {
							if(((String)filestatus.get("cr_status")).equals("9")){
								errStr = "["+ fileDataList.getFiledatas(i).getFilename() + "] 폐기된파일입니다.";
							}else{
								if (qrycd.equals("01") || qrycd.equals("02")){//체크아웃
									if (((String)filestatus.get("cr_status")).equals("5")){
										errStr = "["+ fileDataList.getFiledatas(i).getFilename() + "]" + "\n" + ((String)filestatus.get("cm_username")) +"님이 Check-Out 하셨습니다.";
									} else if (!((String)filestatus.get("cr_status")).equals("0")){
										errStr =  "["+ fileDataList.getFiledatas(i).getFilename() + "]" + "\n" + "이미 요청가능한 상태가 아닙니다 . " + "\n" + "[" + ((String)filestatus.get("cm_codename")) +"]";
									}
								}else if (qrycd.equals("07")){//체크인
									if( fileDataList.getFiledatas(i).getItemid().equals(fileDataList.getFiledatas(i).getBaseitem())) {
										if(!editor.equals(filestatus.get("cr_editor"))){
											errStr = "[" + fileDataList.getFiledatas(i).getFilename() + "] 다른사용자가 사용 중인 파일입니다. ]";
										}else{
											if (!((String)filestatus.get("cr_status")).equals("3") && !((String)filestatus.get("cr_status")).equals("5") 
													&& !((String)filestatus.get("cr_status")).equals("B") && !((String)filestatus.get("cr_status")).equals("G") && !((String)filestatus.get("cr_status")).equals("E")){
												errStr =  "["+ fileDataList.getFiledatas(i).getFilename() + "] 이미 요청가능한 상태가 아닙니다 . " + "\n" + "[" + ((String)filestatus.get("cm_codename")) +"]";
											}else if (null != srid){
												if( (null != filestatus.get("cr_isrid")) && !srid.equals(filestatus.get("cr_isrid").toString())){
													errStr =  "["+ fileDataList.getFiledatas(i).getFilename()+"]\n"
										             + "체크아웃한 SR로 체크인 하시기 바랍니다."+"\n" 
										             + "Check-Out SR-ID["+filestatus.get("cr_isrid").toString()+"]\n"
										             + "선택한 SR-ID["+srid +"]";
												}
											}
										}
									}
								}else if (qrycd.equals("11")){//체크아웃취소
									if (!editor.equals(filestatus.get("cr_editor"))) {
										errStr = "[" + fileDataList.getFiledatas(i).getFilename() + "] 다른사용자가 사용 중인 파일입니다. ]";
									} else if (!((String)filestatus.get("cr_status")).equals("5") && !((String)filestatus.get("cr_status")).equals("B") && !((String)filestatus.get("cr_status")).equals("G") && !((String)filestatus.get("cr_status")).equals("E")){
										errStr =  "["+ fileDataList.getFiledatas(i).getFilename() + "] 이미 요청가능한 상태가 아닙니다 . " + "\n" + "[" + ((String)filestatus.get("cm_codename")) +"]";
									}else if (null != srid){
										if( (null != filestatus.get("cr_isrid")) && !srid.equals(filestatus.get("cr_isrid").toString())){
											errStr =  "["+ fileDataList.getFiledatas(i).getFilename()+"]\n"
								             + "체크아웃한 SR로 체크아웃취소 하시기 바랍니다."+"\n" 
								             + "Check-Out SR["+filestatus.get("cr_isrid").toString()+"]\n"
								             + "선택한 SR-ID["+srid +"]";
										}
									}
								}
							}
						}
					}
					if (errStr != null && !errStr.equals("")){
						break;
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return errStr;
	}
	
	public boolean acptnoCheck(String acptno){
		if (acptno == null){
			return false;
		}
		if (requestDAO.dupleCheck(acptno)>0){
			return false;
		}
		
		return true;
	}
	
	public int insertCmr1000(HashMap param){
		try{
			return requestDAO.insertCmr1000(param);
		}catch(Exception exception){
			return -1;
		}
	}
	
	public int updateCmr1010_confno(HashMap param){
		return requestDAO.updateCmr1010_confno(param);
	}
	
	public int insertCmr1010(HashMap param){
		return requestDAO.insertCmr1010_default(param);
	}
	
	public List updateConfCmr1010(HashMap param){
		return requestDAO.updateConfCmr1010(param);
	}
	
	public ReturnMsg request_complete(EcamsMessage ecamsmsg){
		int i;
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
		
		HashMap param = new HashMap();
		
		List acptlist = new ArrayList();
		Collections.addAll(acptlist, ecamsmsg.getRequestinfo().getAcptno().split(":"));
		
		String team= null;
		
		if (ecamsmsg.getRequestinfo().getQrycd().equals("01") || ecamsmsg.getRequestinfo().getQrycd().equals("02")){
			team = "SYSEDN";
		}
		else if (ecamsmsg.getRequestinfo().getQrycd().equals("11")){
			team = "SYSENC";
		}
		else if(ecamsmsg.getRequestinfo().getQrycd().equals("04") || ecamsmsg.getRequestinfo().getQrycd().equals("07")){
			team = "SYSEUP";
		}

		//returnmsg_builder.setReturnStr("START COMPLETE");	
		//logger.error(">>>>>>team start: "+team);
		
		boolean retflg = false;
		
		for(i=0;i<acptlist.size();i++){			
		    param = new HashMap();
			param.put("CR_ACPTNO", acptlist.get(i));
			param.put("CR_USERID", team);
			param.put("CR_SGNMSG", "eCAMS 자동처리");		
			param.put("CR_SGNCD", "9");
			param.put("CR_QRYCD", "");	
			param.put("CR_CNCLSW", "1");
			
			if(!confirmService.cmr9900_str(param)){
				retflg = true;
				break;
			} else {
				//System.out.println("call CMR9900_STR('"+acptlist.get(i)+"','"+team+"','eCAMS 자동처리','9','','1')");	
				if(ecamsmsg.getRequestinfo().getQrycd().equals("11")){
					checkOutCnlDAO.updateCmr1010_confno(acptlist.get(i).toString());
				}
			}
			param = null;
		}
		
		if(retflg){
			returnmsg_builder.setReturnval(1);
			returnmsg_builder.setReturnStr("call CMR9900_STR 완료처리실패");	
		}else{
			returnmsg_builder.setReturnval(0);

			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			if(ecamsmsg.getRequestinfo().getQrycd().equals("01") || ecamsmsg.getRequestinfo().getQrycd().equals("02")){
					
				if(ecamsmsg.getRequestinfo().getQrycd().equals("01")){
					System.out.println(formatter.format(new Date())+" CHECK-OUT COMPLETE UserID:"+ecamsmsg.getUserinfo().getId()+", AcptNo:"+acptlist.get(0));
					returnmsg_builder.setReturnStr("체크아웃 완료.");	
				} else {
					System.out.println(formatter.format(new Date())+" PREVIOUS CHECK-OUT COMPLETE UserID:"+ecamsmsg.getUserinfo().getId()+", AcptNo:"+acptlist.get(0));
					returnmsg_builder.setReturnStr("이전버전 체크아웃 완료.");	
				}
				
			}else if(ecamsmsg.getRequestinfo().getQrycd().equals("04") || ecamsmsg.getRequestinfo().getQrycd().equals("07")){
				System.out.println(formatter.format(new Date())+" CHECK-IN COMPLETE UserID:"+ecamsmsg.getUserinfo().getId()+", AcptNo:"+acptlist.get(0));
				returnmsg_builder.setReturnStr("체크인 완료.");	
			}
			else if(ecamsmsg.getRequestinfo().getQrycd().equals("11")){
				System.out.println(formatter.format(new Date())+" CHECK-OUT CNCL COMPLETE UserID:"+ecamsmsg.getUserinfo().getId()+", AcptNo:"+acptlist.get(0));
				returnmsg_builder.setReturnStr("체크아웃취소완료.");	
			}
		}
		
		return returnmsg_builder.build();
	}

	public boolean request_complete(String acptno){
		return false;	
	}
	
	public ReturnMsg request_allcncl(EcamsMessage ecamsmsg){
		int i;
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
		
		HashMap param = new HashMap();
		
		List acptlist = new ArrayList();
		Collections.addAll(acptlist, ecamsmsg.getRequestinfo().getAcptno().split(":"));
		
		String team= null;
		if (ecamsmsg.getRequestinfo().getQrycd().equals("01") || ecamsmsg.getRequestinfo().getQrycd().equals("02")){
			team = "SYSEDN";
		}
		else if (ecamsmsg.getRequestinfo().getQrycd().equals("11")){
			team = "SYSENC";
		}
		else if(ecamsmsg.getRequestinfo().getQrycd().equals("04") || ecamsmsg.getRequestinfo().getQrycd().equals("07")){
			team = "SYSEUP";
		}
		
		boolean retflg = false;
		for(i=0;i<acptlist.size();i++){
			//logger.error("request_allcncl acptno : "+acptlist.get(i));
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//yyyy/MM/dd HH:mm:ss
			System.out.println(formatter.format(new Date())+" REQUEST ALL CNCL ACPTNO:"+acptlist.get(i)+", UserID:"+ecamsmsg.getUserinfo().getId());
			
		    param = new HashMap();
			param.put("CR_ACPTNO", acptlist.get(i));
			param.put("CR_USERID", team);
			param.put("CR_SGNMSG", "eCAMS 자동처리");		
			param.put("CR_SGNCD", "9");
			param.put("CR_QRYCD", "");		
			param.put("CR_CNCLSW", "9");
			if(!confirmService.cmr9900_str(param)){
				retflg = true;
				break;
			}
			param = null;
		}
		if(retflg) {
			returnmsg_builder.setReturnval(1);
			returnmsg_builder.setReturnStr("call CMR9900_STR 반려 실패");	
		} else {
			returnmsg_builder.setReturnval(0);
			if(ecamsmsg.getRequestinfo().getQrycd().equals("01") || ecamsmsg.getRequestinfo().getQrycd().equals("02")){
				returnmsg_builder.setReturnStr("체크아웃자동반려");	
			}else if (ecamsmsg.getRequestinfo().getQrycd().equals("11")){
				returnmsg_builder.setReturnStr("체크아웃취소자동반려");	
			}else if(ecamsmsg.getRequestinfo().getQrycd().equals("04") || ecamsmsg.getRequestinfo().getQrycd().equals("07")){
				returnmsg_builder.setReturnStr("체크인자동반려");	
			}
		}
		return returnmsg_builder.build();
	}
	
	public ReturnMsg request_setcncl(EcamsMessage ecamsmsg){
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
		
		HashMap param = new HashMap();
		
		List acptlist = new ArrayList();
		Collections.addAll(acptlist, ecamsmsg.getRequestinfo().getAcptno().split(":"));
		
		param.put("CR_ITEMID", ecamsmsg.getFiledata().getItemid());
		param.put("acptlist", acptlist);
		
		
		String acptno = requestDAO.getAcptno(param);
		
		if (acptno == null || acptno.equals("")){
			returnmsg_builder.setReturnval(1);
			returnmsg_builder.setReturnStr("에러파일에 해당하는 신청번호를 찾지 못했습니다.");
			return returnmsg_builder.build();
		}
		
		if (acptno.length() < 12){
			returnmsg_builder.setReturnval(1);
			returnmsg_builder.setReturnStr("에러파일에 해당하는 신청번호를 찾지 못했습니다.");
			return returnmsg_builder.build();
		}
		
		
		param = new HashMap();
		param.put("SET_CR_STATUS", "3");
		param.put("PARAM_CR_ACPTNO", acptno);
		param.put("PARAM_CR_ITEMID", ecamsmsg.getFiledata().getItemid());
		
		if (requestDAO.updateCmr1010_setcncl(param) < 1){
			returnmsg_builder.setReturnval(1);
			returnmsg_builder.setReturnStr("에러파일 개별반려 실패.");
			param = null;
			return returnmsg_builder.build();
		}
		param = null;
		
		returnmsg_builder.setReturnval(0);
		returnmsg_builder.setReturnStr("에러파일 개별반려 완료.");
		
		return returnmsg_builder.build();
	}
	
	public ReturnMsg request(EcamsMessage ecamsmsg) {
		// TODO Auto-generated method stub
		return null;
	}

}