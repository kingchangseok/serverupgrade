/*-----------------------------------------------------------------
 ┌──────┬───────────────────────┐
 │ 프로그램명 │ ecams_GetInnerList.c                         │
 ├──────┼───────────────────────┤
 │ 기      능 │ 형상관리(eCAMS) InnerClass 추출              │
 ├──────┼───────────────────────┤
 │ 수  정  일 │ 2006. 08. 07  by AHJ                         │
 ├──────┼───────────────────────┤
 │ 작  성  자 │ 최   병   남                                 │
 └──────┴───────────────────────┘
-----------------------------------------------------------------*/

/*---------------------------------------------------------------*/
/*     Header files                                              */
/*---------------------------------------------------------------*/
#define		dfMain	1
#include	<ecamsapi.h>
#include 	<ecams_util.h>


#define WIN  0
#define UNIX 1


char gszLogFile[50];	/*Log File Name*/
char gszLogPath[100];	/*Log File Path*/
char gszLogMsg[512];	/*Log Message  */


/*****************************************************************/
/*                                                               */
/*      자원이행 처리  M A I N                                   */
/*                                                               */
/*****************************************************************/
int 	main(int argc, char *argv[])
{

	char 	szAcptNo [13];
	char	szSvrIP[21];
	int		szPortNo;
	char	szRsrcName[512];
	char	szChangDirPath[512];
	char	szTmpPath[512];
	char	szInnerResult[512];
	char	szDir[512];
	
	
	char	szExeName[512];
	
	char	szCommand[1024];
	char  szSysOS[2+1];
	
	
	
	char	szInnerResultTmp[512];
	


	int	nRet;
	
	
	CMD_INFO	  CmdInfo;

	if(argc < 9)
	{
		printf("USAGE : %s <ACPTNO> <SVRIP> <PORT> <RSRCNAME> <szChangDirPath> <TMPPATH> <INNERESULT> <AGENTDIR> <SYSOS>\n",argv[0]);
		exit(1);
	}
	

	
	strcpy(szAcptNo   , argv[1]);
	strcpy(szSvrIP   , argv[2]);
	szPortNo = atoi(argv[3]);
	strcpy(szRsrcName   , argv[4]);
	strcpy(szChangDirPath   , argv[5]);
	strcpy(szTmpPath   , argv[6]);
	strcpy(szInnerResult   , argv[7]);
	strcpy(szDir   , argv[8]);
	strcpy(szSysOS , argv[9]);


	Get_Sys_Date(gszLogFile);
	strcat(gszLogFile,".log");
	sprintf(gszLogPath,"%s/",getenv("LOGDIR"));	


	InitCmdInfo(&CmdInfo);
	strcpy(CmdInfo.szAcptNo,szAcptNo);
	sprintf(CmdInfo.szLogFile,"%sTransList.%s",gszLogPath,gszLogFile);		

	
	
	
	nRet = Right_Char_Check(szRsrcName,'.');
	
	if (nRet >=0){
		sprintf(szExeName,"%s",left_char(szRsrcName,nRet - 1));
	}
	else{
		strcpy(szExeName,szRsrcName);
	}
	
	
	while( Char_Check(szChangDirPath,"//") >= 0){
		sprintf(szChangDirPath,"%s",rep_char(szChangDirPath,"//","/"));
	}	
/*	
	InitCmdInfo(&CmdInfo);
*/		

		
	strcpy(CmdInfo.szServerIP, szSvrIP);
	CmdInfo.nPort = szPortNo;
	strcpy(CmdInfo.szJobGub,"S");

	if ( strcmp(szSysOS, "03") == 0 ) {
		sprintf(CmdInfo.szCommand,"dir %s\\%s*.class > %s\\%s",szChangDirPath,szExeName,szDir,szInnerResult);
	} else {	
		sprintf(CmdInfo.szCommand,"find %s/* -prune -name '%s$*.class' > %s/%s ",szChangDirPath,szExeName,szDir,szInnerResult);
	}
	Server_Cmd_JOB(&CmdInfo);
	LookCMD_INFO  (&CmdInfo);
	
	if(strcmp(CmdInfo.szRstCond,"0000") != 0){
		sprintf(gszLogMsg, "[%s] [%s]InnerClass File Find Not Found [%s][%s]", szAcptNo,szSvrIP,
															CmdInfo.szRstCond,CmdInfo.szCommand);
		eCAMS_Log(gszLogPath,gszLogFile,gszLogMsg);
		exit(1);
	}
	
	sprintf(gszLogMsg, "[%s] [%s]InnerClass File Find Success [%s][%s]", szAcptNo,szSvrIP,
														CmdInfo.szRstCond,CmdInfo.szCommand);
	eCAMS_Log(gszLogPath,gszLogFile,gszLogMsg);

	strcpy(CmdInfo.szJobGub,"G");
	sprintf(CmdInfo.szLocal,"%s/%s_tmp",szTmpPath,szInnerResult);
	if ( strcmp(szSysOS, "03") == 0 ) {
	   sprintf(CmdInfo.szRemote,"%s\\%s",szDir,szInnerResult);
	}
	else 
	   sprintf(CmdInfo.szRemote,"%s/%s",szDir,szInnerResult);

	Server_Cmd_JOB(&CmdInfo);
	LookCMD_INFO  (&CmdInfo);
	
	if(strcmp(CmdInfo.szRstCond,"0000") != 0)
	{
		sprintf(gszLogMsg, "[%s]  [%s] InnerClass File Get Fail [%s][%s][%s]", szAcptNo,szSvrIP,
															CmdInfo.szRstCond,CmdInfo.szLocal,CmdInfo.szRemote);
		eCAMS_Log(gszLogPath,gszLogFile,gszLogMsg);

	  strcpy(CmdInfo.szJobGub,"S");
	
	  if ( strcmp(szSysOS, "03") == 0 ) {
/* 2017.04.12 추가 파일 사이즈 0 인 파일 삭제 처리*/
		   sprintf(CmdInfo.szCommand,"DEL /S /Q \"%s\\%s\"",szDir,szInnerResult);
    } else { 
		   sprintf(CmdInfo.szCommand,"\\rm -rf \"%s/%s\"",szDir,szInnerResult);
	  }
	
	  Server_Cmd_JOB(&CmdInfo);	

		exit (1);

	}	

	sprintf(gszLogMsg, "[%s]  [%s] InnerClass File Get Success [%s][%s][%s]", szAcptNo,szSvrIP,
														CmdInfo.szRstCond,CmdInfo.szLocal,CmdInfo.szRemote);
	eCAMS_Log(gszLogPath,gszLogFile,gszLogMsg);
	
	/*-----------------------------------------------------------*/
	/* 	서버에 생성 파일 삭제                                    */
	/*-----------------------------------------------------------*/
	strcpy(CmdInfo.szJobGub,"S");
	
	if ( strcmp(szSysOS, "03") == 0 ) {
/* 2017.02.24 RD 명령에서 DEL 로 변경 처리 */
		sprintf(CmdInfo.szCommand,"DEL /S /Q \"%s\\%s\"",szDir,szInnerResult);
    } else { 
		sprintf(CmdInfo.szCommand,"\\rm -rf \"%s/%s\"",szDir,szInnerResult);
	}
	
	Server_Cmd_JOB(&CmdInfo);	


	sprintf(szCommand,"mv \"%s/%s_tmp\" \"%s/%s\" ",szTmpPath,szInnerResult,szTmpPath,szInnerResult);
	system(szCommand);

	sprintf(gszLogMsg, "[%s]  [%s] InnerClass File END [%s][%s][%s]", szAcptNo,szSvrIP,
														CmdInfo.szRstCond,CmdInfo.szLocal,CmdInfo.szRemote);
	eCAMS_Log(gszLogPath,gszLogFile,gszLogMsg);
		
	exit (0);
}

/*---------------------------------------------------------------*/
/*             E N D    O F    P R O G R A M                     */
/*---------------------------------------------------------------*/
