package app.core.socket;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.WriteCompletionEvent;
import org.springframework.beans.factory.annotation.Autowired;

import app.core.proto.ProtoEcams.EcamsMessage;
import app.core.proto.ProtoEcams.ReturnMsg;
import app.ecams.anal.service.IAnalService;
import app.ecams.befjob.service.IBefJobService;
import app.ecams.commoncode.service.ICommonCodeService;
import app.ecams.file.service.IFileService;
import app.ecams.history.service.IHistoryService;
import app.ecams.job.service.IJobService;
import app.ecams.lang.service.ILangService;
import app.ecams.request.checkin.service.ICheckInRequestService;
import app.ecams.request.checkincnl.service.ICheckInCnlRequestService;
import app.ecams.request.checkinreal.service.ICheckInRealRequestService;
import app.ecams.request.checkout.service.ICheckOutRequestService;
import app.ecams.request.checkoutcnl.service.ICheckOutCnlRequestService;
import app.ecams.request.lastcheckout.service.ILastCheckOutRequestService;
import app.ecams.request.registfileall.dao.IRegistFileAllDAO;
import app.ecams.request.registfileall.service.IRegistFileAllService;
import app.ecams.request.registfilenew.service.IRegistFileNewService;
import app.ecams.request.service.IRequestService;
import app.ecams.resourcetype.service.IResourceTypeService;
import app.ecams.srjob.service.ISRJobService;
import app.ecams.syncwith.service.ISyncWithService;
import app.ecams.system.service.ISystemService;
import app.ecams.user.service.IUserService;
import app.util.file.FileMake;

@ChannelPipelineCoverage("all")
public class EcamsServerHandler extends SimpleChannelHandler {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Autowired private IUserService userService;
	@Autowired private ISystemService systemService;
	@Autowired private IResourceTypeService resourceTypeService;
	@Autowired private IFileService fileService;
	@Autowired private ICheckOutRequestService checkOutRequestService;
	@Autowired private ICheckOutCnlRequestService checkOutCnlRequestService;
	@Autowired private ICheckInCnlRequestService checkInCnlRequestService;
	@Autowired private ICheckInRequestService checkInRequestService;
	@Autowired private ICheckInRealRequestService checkInRealRequestService;
	@Autowired private IHistoryService historyService;
	@Autowired private IAnalService analService;
	@Autowired private IBefJobService befjobService;
	@Autowired private ICommonCodeService commonCodeService;
	@Autowired private IRequestService requestService;
	@Autowired private ILangService langService;
	@Autowired private ILastCheckOutRequestService lastCheckOutRequestService;
	@Autowired private FileMake fileMake;
	@Autowired private IJobService jobService;
	@Autowired private ISRJobService serviceRequestService;
	@Autowired private IRegistFileAllService registFileAllService;
	@Autowired private IRegistFileNewService registFileNewService;
	@Autowired private ISyncWithService serviceSyncWithService;
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		// TODO Auto-generated method stub
		logger.log(Level.DEBUG,"exceptionCaught",e.getCause());
		e.getChannel().close();

	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		// TODO Auto-generated method stub
			
		String msgType="";
		//String qrycd="";
		EcamsMessage ecamsmsg = (EcamsMessage) e.getMessage();
		
		ReturnMsg returnmsg = null;
		
		msgType = ecamsmsg.getMsgtype();
		
		/*
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("mm분 ss초 SSS");
		Date strDate = calendar.getTime();
		*/
		
		//logger.error("++++ msgType +++++"+msgType);
		if (msgType.equals("CONNECT_TEST") || msgType.equals("LOGIN_CHECK")|| msgType.equals("GET_USERNAME")||msgType.equals("PASSWD_CHECK")||msgType.equals("GETFILE")){
			if (msgType.equals("CONNECT_TEST")){
				ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
				returnmsg_builder.setReturnStr("접속성공");
				returnmsg_builder.setReturnval(0);
				returnmsg = returnmsg_builder.build();
			}else if (msgType.equals("LOGIN_CHECK")){
				returnmsg = userService.login_check((EcamsMessage) e.getMessage());
			}else if (msgType.equals("GET_USERNAME")){
				returnmsg = userService.get_username((EcamsMessage) e.getMessage());
			}else if (msgType.equals("PASSWD_CHECK")){
				returnmsg = userService.set_passwd((EcamsMessage) e.getMessage());
			}else if (msgType.equals("GETFILE")){
				returnmsg = fileService.getFileData((EcamsMessage) e.getMessage());
			}
		}
		else{
			returnmsg = userService.login_check((EcamsMessage) e.getMessage());
			
			if (returnmsg.getReturnval() == 0){
				if (msgType.equals("SYSINFOLIST_GET")){
					returnmsg = systemService.getSysInfo((EcamsMessage) e.getMessage());		
				}
				else if (msgType.equals("SYSINFOLIST_USER_GET")){
					returnmsg = systemService.getSysInfo_user((EcamsMessage) e.getMessage());		
				}
				else if (msgType.equals("JOBLIST_GET")){
					returnmsg = resourceTypeService.getRsrcInfo((EcamsMessage) e.getMessage());			
				}
				else if (msgType.equals("SYNC_PROJECT_GETCNT")){
					returnmsg = fileService.getFileListCount((EcamsMessage) e.getMessage());
				}
				else if (msgType.equals("SYNC_FILEDATA")){
					returnmsg = fileService.getFileList_Data((EcamsMessage) e.getMessage());
				}
				else if (msgType.equals("SYNC_PROJECT_GETLIST")){
					returnmsg = fileService.getFileList((EcamsMessage) e.getMessage());
					//System.out.println("SYNC_PROJECT_GETLIST: "+ecamsmsg.getSysinfo().getSyscd()+" "+ecamsmsg.getUserinfo().getId());
					//logger.log(Level.ERROR,"\nSYNC_PROJECT_GETLIST:"+ecamsmsg.getUserinfo().getId());
				}
				else if (msgType.equals("SYNC_PROJECT_NEW_GETLIST")){
					returnmsg = fileService.getNewFileList((EcamsMessage) e.getMessage());
				}
				else if (msgType.equals("GETFILETST")){
					returnmsg = fileService.getFileTstData((EcamsMessage) e.getMessage());
				}
				else if (msgType.equals("GETLASTFILE")){
					returnmsg = fileService.getLastFileData((EcamsMessage) e.getMessage());
				}
				else if (msgType.equals("CHECKOUT")){
					returnmsg = checkOutRequestService.request((EcamsMessage) e.getMessage());
				}
				else if (msgType.equals("CHECKIN")){
					returnmsg = checkInRequestService.request((EcamsMessage) e.getMessage());
				}
				else if (msgType.equals("ANALLIST_GET")){
					returnmsg = analService.getMethod((EcamsMessage) e.getMessage());
				}
				else if (msgType.equals("ANALLIST_DETAILGET")){
					returnmsg = analService.getAnalList_detail((EcamsMessage) e.getMessage());
				}
				else if (msgType.equals("CHECKINREAL")){
					returnmsg = checkInRealRequestService.request((EcamsMessage) e.getMessage());
				}
				else if (msgType.equals("REQUEST_SETCNCL")){
					returnmsg = requestService.request_setcncl((EcamsMessage) e.getMessage());
				}
				else if (msgType.equals("REQUEST_ALLCNCL")){
					returnmsg = requestService.request_allcncl((EcamsMessage) e.getMessage());
				}
				else if (msgType.equals("REQUEST_COMPLETE")){
					returnmsg = requestService.request_complete((EcamsMessage) e.getMessage());
				}	
				else if (msgType.equals("GETCODEINFO")){
					returnmsg = commonCodeService.getCodes((EcamsMessage) e.getMessage());
				}
				else if(msgType.equals("CHECKOUTCNL")){
					returnmsg = checkOutCnlRequestService.request((EcamsMessage) e.getMessage());
				}
				else if(msgType.equals("CHECKINCNL")){
					returnmsg = checkInCnlRequestService.request((EcamsMessage) e.getMessage());
				}
				else if(msgType.equals("FILETRANS")){
					returnmsg = checkInRequestService.transfile((EcamsMessage) e.getMessage());
				}
				else if(msgType.equals("HISTORYLIST_GET")){
					returnmsg = historyService.getHistoryList_detail((EcamsMessage) e.getMessage());
				}
				else if(msgType.equals("BEFJOBLIST_GET")){
					returnmsg = befjobService.getBefJobList_detail((EcamsMessage) e.getMessage());
				}
				else if(msgType.equals("GETLANG")){
					returnmsg = langService.getLang((EcamsMessage) e.getMessage());
				}
				else if(msgType.equals("REGISTFILE")){
					returnmsg = fileService.registFile((EcamsMessage) e.getMessage());
				}
				else if(msgType.equals("ADMIN")){
					returnmsg = userService.isAdmin((EcamsMessage) e.getMessage());
				}
				else if(msgType.equals("GET_LASTVERSION")){
					returnmsg = lastCheckOutRequestService.select_lastver((EcamsMessage) e.getMessage());
				}
				else if(msgType.equals("FILETRANS_MAKE")){
					returnmsg = fileMake.fileMake((EcamsMessage) e.getMessage());
				}
				else if(msgType.equals("ANALMETHODLIST_GET")){
					returnmsg = analService.getAnalList_detail((EcamsMessage) e.getMessage());
				}
				else if(msgType.equals("GETANALLIST")){
					returnmsg = analService.getAnalList_detail((EcamsMessage) e.getMessage());
				}
				else if(msgType.equals("MYJOB_CHECK")){
					returnmsg = jobService.getMyJobCheck((EcamsMessage) e.getMessage());
				}
				else if(msgType.equals("DELETE")){
					returnmsg = fileService.setDeleteStatus((EcamsMessage) e.getMessage());
				}
				else if(msgType.equals("SRLIST_GET")){
					returnmsg = serviceRequestService.getSRInfo((EcamsMessage) e.getMessage());
				}
				else if(msgType.equals("GETRESOURCES")){
					returnmsg = serviceRequestService.getSResource((EcamsMessage) e.getMessage());
				}
				else if(msgType.equals("ISMYSR")){
					returnmsg = serviceRequestService.getSRAcess((EcamsMessage) e.getMessage());
				}
				else if(msgType.equals("REGISTFILEALL")){
					returnmsg = registFileAllService.request((EcamsMessage) e.getMessage());
				}
				else if(msgType.equals("REGISTFILENEW")){
					returnmsg = registFileNewService.request((EcamsMessage) e.getMessage());
				}				
				else if(msgType.equals("GETDOWNFILELIST")){
					returnmsg = checkInRequestService.getDownFileList((EcamsMessage) e.getMessage());
				}else if (msgType.equals("GETMERGEFILE")){
					returnmsg = fileService.getMergeFileData((EcamsMessage) e.getMessage());
				}
				else if(msgType.equals("LOCAL_VS_SERVER")){
					returnmsg = serviceSyncWithService.diffSvr((EcamsMessage) e.getMessage());
				}
				else if(msgType.equals("NEW_FILETRANS")){
					returnmsg = checkInRequestService.splitFileSend((EcamsMessage) e.getMessage());
				}
				else if(msgType.equals("GETRESOURCES2")){
					returnmsg = serviceRequestService.getSResource2((EcamsMessage) e.getMessage());
				}
			}
		}
		
		e.getChannel().write(returnmsg);
		
	}

	@Override
	public void writeComplete(ChannelHandlerContext ctx, WriteCompletionEvent e)
			throws Exception {
		
		// TODO Auto-generated method stub
		logger.log(Level.DEBUG,"writeComplete"+ctx.getName());
		super.writeComplete(ctx, e);
	}

}
