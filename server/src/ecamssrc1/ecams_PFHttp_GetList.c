/*-----------------------------------------------------------------
 ┌──────┬───────────────────────┐
 │ 프로그램명 │ ecams_PFHttp_GetList.c                                 │
 ├──────┼───────────────────────┤
 │ 기      능 │ 형상관리(eCAMS) 자원이행 MAIN                │
 ├──────┼───────────────────────┤
 │ 작  성  일 │ 2005. 12. 20                                 │
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
/*---------------------------------------------------------------*/
/*       INTERNAL  FUNCTION  DEFINE                              */
/*---------------------------------------------------------------*/
void ErrResult_Make ();
int  Make_FileList  ();
int	 ProFrame_Call	();


char gszLogFile[50];	/*Log File Name*/
char gszLogPath[100];	/*Log File Path*/
char gszLogMsg[512];	/*Log Message  */

char 	szAcptNo [13];
char 	szServerIP     [21];
int		szPfmPort;
int		szPortNo;
char	szPfmUsr[256];
char	szPfmPass[256];
char	szRsrcName[512];
char	szTmpPath[512];
char	szReqPath[512];
char	szResult[512];
char	szAgentDir[512];
char 	szCommand     [1000];
CMD_INFO	  CmdInfo;

void ErrResult_Make(char *pErrMsgFile,char *errmsg,int errcode,char *pMsgFile);
int Make_FileList(char *pListFileName,char *preturnFileName,char *tarPath);

/*****************************************************************/
/*                                                               */
/*      자원이행 처리  M A I N                                   */
/*                                                               */
/*****************************************************************/
int 	main(int argc, char *argv[])
{
	int		szCallType;	
	char	szReserve[512];
	int     ret = 0;

	if(argc < 14)
	{
		printf("USAGE : %s <CallType> <ACPTNO> <서버IP> <PFHPORT> <SVRPORT> <PFMUSR> <PFMPASS> <RSRCNAME> <Reserve> <TMPPATH> <RESULTFILE> <AGENTDIR> <REQPATH>\n",argv[0]);
		exit(1);
	}
	

	szCallType = atoi(argv[1]);
	strcpy(szAcptNo   , argv[2]);
	strcpy(szServerIP, argv[3]);
	szPfmPort = atoi(argv[4]);
	szPortNo = atoi(argv[5]);
	strcpy(szPfmUsr    , argv[6]);
	strcpy(szPfmPass    , argv[7]);
	strcpy(szRsrcName   , argv[8]);
	strcpy(szReserve   , argv[9]);
	strcpy(szTmpPath, argv[10]);
	strcpy(szResult    , argv[11]);
	strcpy(szAgentDir    , argv[12]);
	strcpy(szReqPath    , argv[13]);
	
	
    NotUseDataTruncate (szAcptNo, 0x20);
    NotUseDataTruncate (szServerIP, 0x20);
    NotUseDataTruncate (szPfmUsr, 0x20);
	NotUseDataTruncate (szPfmPass, 0x20);
    NotUseDataTruncate (szRsrcName, 0x20);
    NotUseDataTruncate (szReserve, 0x20);
    NotUseDataTruncate (szTmpPath, 0x20);
    NotUseDataTruncate (szResult, 0x20);
	NotUseDataTruncate (szAgentDir, 0x20);
	NotUseDataTruncate (szReqPath, 0x20);

	Get_Sys_Date(gszLogFile);
	strcat(gszLogFile,".log");
	strcpy(gszLogPath,"LogMessage/");
	
	InitCmdInfo(&CmdInfo);
	strcpy(CmdInfo.szAcptNo,szAcptNo);
	sprintf(CmdInfo.szLogFile,"%sTransList.%s",gszLogPath,gszLogFile);		
	
	sprintf(szCommand,"rm -rf %s/%s*",szTmpPath,szResult);
	ret = system(szCommand)/256;
		
	ret = ProFrame_Call("CHECKIN");
	if (ret != 0) {
		/*LOG*/sprintf(gszLogMsg,"[%s] [%s] ProFrame Call 실패 API=[CHECKIN] return=[%d]",
										 szAcptNo,szRsrcName,ret);		
		eCAMS_Log(gszLogPath,gszLogFile,gszLogMsg);
		exit(1);
	}	
	ret = ProFrame_Call("REQUEST_PUBLISH");
	if (ret != 0) {
		/*LOG*/sprintf(gszLogMsg,"[%s] [%s] ProFrame Call 실패 API=[REQUEST_PUBLISH] return=[%d]",
										 szAcptNo,szRsrcName,ret);		
		eCAMS_Log(gszLogPath,gszLogFile,gszLogMsg);	
		exit(1);
	}
	ret = ProFrame_Call("PUBLISH_APPROVAL");
	if (ret != 0) {
		/*LOG*/sprintf(gszLogMsg,"[%s] [%s] ProFrame Call 실패 API=[PUBLISH_APPROVAL] return=[%d]",
										 szAcptNo,szRsrcName,ret);		
		eCAMS_Log(gszLogPath,gszLogFile,gszLogMsg);	
		exit(1);
	}
	exit(0);
}


void ErrResult_Make(char *pErrMsgFile,char *errmsg,int errcode,char *pMsgFile){
	FILE *RstPtr1;
	FILE *RstPtr2;
	char	szSysDate		[9];
	char 	szSysTime      [16];
	char	szNSysDate	[11];
	char	indat[1024];
	
	
	Get_Sys_Date(szSysDate);
	
	sprintf(szNSysDate,"%s",mid_char(szSysDate,1,4));
	strcat(szNSysDate,"/");
	sprintf(szNSysDate,"%s",mid_char(szSysDate,5,2));
	strcat(szNSysDate,"/");
	sprintf(szNSysDate,"%s",mid_char(szSysDate,7,2));
		
	Get_Sys_Time20(szSysTime);	
	
	RstPtr1 = fopen (pErrMsgFile, "a+");
	if (RstPtr1 == NULL)		return;
	fprintf (RstPtr1, ">>> SERVER 작업처리내용 [%s %s]\n\n", szNSysDate,szSysTime );
	
	if (strlen(pMsgFile) >= 0){
		RstPtr2 = fopen(pMsgFile,"r");
		if (RstPtr2 != NULL){
			while (fgets(indat, 1024, RstPtr2) != (char *) NULL) {
				sprintf(indat,"%s",rep_char(indat,"\r\n",""));
				sprintf(indat,"%s",rep_char(indat,"\n",""));
				sprintf(indat,"%s",trunc_char(indat));
				fprintf (RstPtr1, "%s\n",indat);
			}
			fclose(RstPtr2);
		}
	}
	fprintf (RstPtr1, "errcode=[%d]\n",errcode);
	if (strlen(errmsg) >= 0){
		fprintf (RstPtr1, "errmsg=[%s]\n", errmsg);
	}
	fclose  (RstPtr1);
}

int Make_FileList(char *pListFileName,char *preturnFileName,char *tarPath){
	FILE *fptr1;
	FILE *fptr2;
	
	int ncnt;
	int	nRet1;
	char DirName[512];
	char dummy[16384];
	char RsrcName[512];
	
	

    /*-----------------------------------------------------------*/
    /*   Make File Name Find                                     */
    /*-----------------------------------------------------------*/    
    if ((fptr1 = fopen(pListFileName,"r")) == (FILE *) NULL) {
	    printf("FILE OPEN ERROR!! \n");
        return (1);
    }
    
    /*-----------------------------------------------------------*/
    /*   Make File Name Find                                     */
    /*-----------------------------------------------------------*/    
    if ((fptr2 = fopen(preturnFileName,"w")) == (FILE *) NULL) {
	    printf("FILE OPEN ERROR!! \n");
        return (1);
    }    
    
    
    memset(DirName, 0x00, sizeof(DirName));
    memset(dummy, 0x00, sizeof(dummy));

	ncnt = 0;

    while (fgets(dummy, 16384, fptr1) != (char *) NULL) 
	{
        /*-------------------------------------------------------*/
    	/* Return Valure Truncate                                */
        /*-------------------------------------------------------*/
		sprintf(dummy,"%s",rep_char(dummy,"\r\n",""));        
        sprintf(dummy,"%s",rep_char(dummy,"\n",""));
        sprintf(dummy,"%s",trunc_char(dummy));

        if (dummy[0] == '/') {
        	memset(DirName, 0x00, sizeof(DirName));
        	sprintf(DirName, "%s", left_char(dummy, strlen(dummy)-1));
        	memset(dummy, 0x00, sizeof(dummy));
        	continue;
        }

        if (memcmp(dummy, "-", 1) != 0)
		{
			memset(dummy, 0x00, sizeof(dummy));
			continue;
		}

        if (strlen(DirName) == 0) 
		{
			memset(dummy, 0x00, sizeof(dummy));
			continue;
		}
		
		sprintf(dummy,"%s",trunc_char(dummy));
		nRet1 = Char_Check(dummy, " ");


		if (nRet1 >= 0) {
  			sprintf(dummy, "%s",right_char(dummy, strlen(dummy) - nRet1 - 1));
		}
		else{
			memset(dummy, 0x00, sizeof(dummy));
			continue;
		}

        sprintf(dummy,"%s",rep_char(dummy,"*",""));
        sprintf(dummy,"%s",trunc_char(dummy));
        memset(RsrcName, 0x00, sizeof(RsrcName));
        strcpy(RsrcName, dummy);
        
        sprintf(DirName,"%s",rep_char(DirName,tarPath,""));
        fprintf(fptr2,"%s/%s\n",DirName,RsrcName);
        fflush(fptr2);
        
        ncnt++;
        memset(dummy, 0x00, sizeof(dummy));
        
    }
    fclose (fptr1);
    fclose (fptr2);

	if (ncnt == 0)
		return (1);
	else
    	return (0);
}
/*---------------------------------------------------------------*/
/*	Function  : ProFrame_Call                                    */
/*	Action    : PFHttpCom Class Call                             */
/*	Parameter :	pCODE       : 구분                               */
/*	            pSysCD      : 시스템코드                         */
/*	            szRsrcType  : 리소스타입	                     */
/*	            szPhyName   : 물리명                             */
/*	            szOutName   : 리스트파일                         */
/*	Return    : 처리결과                                         */
/*---------------------------------------------------------------*/
int		ProFrame_Call	( char *pCODE
						)
{
	char 	szCODE            [256];
	int		retval                 ;
	int     readcnt = 0;
	char	szPfmResult[512];
	char	szPfmResultTmp[512];
	char	szPfmResultTmp2[512];
	char	szPfmErrFile[512];
	char	szListName       [2048];
	char	szListFile       [2048];
	char	szTarPath        [2048];
	char 	returnmessage    [2048];
	char	*szKeys[5]             ;
	int		Index_key=0            ;
	char	*toktmp                ;
	char	*Readbuf               ;
	int		returnval=0            ;
	CMD_INFO	  CmdInfo          ;
	FILE    *SrcPtr                ;
	FILE    *DstPtr                ;
	char	*dummy                 ;      /* Read Buffer             */
	long	nFileSize              ;


	/*-----------------------------------------------------------*/
	/*		파리미터 SET                                         */
	/*-----------------------------------------------------------*/
	sprintf(szCODE       , "%s", pCODE     );
	
	memset(szPfmResultTmp,0x00,sizeof(szPfmResultTmp));
	memset(szPfmResult,0x00,sizeof(szPfmResult));
	
	sprintf(szPfmResultTmp,"%s/%s.tmp",szTmpPath,szResult);
	sprintf(szPfmResultTmp2,"%s/%s.tmp2",szTmpPath,szResult);
	sprintf(szPfmResult,"%s/%s",szTmpPath,szResult);
	sprintf(szPfmErrFile,"%s/%s.err",szTmpPath,szResult);
	/*-----------------------------------------------------------*/
	/*		구분 체크                                            */
	/*-----------------------------------------------------------*/
	if (strcmp(szCODE, "REQUEST_PUBLISH"              ) != 0 &&
		strcmp(szCODE, "PUBLISH_APPROVAL"             ) != 0 &&
		strcmp(szCODE, "CHECKIN"                      ) != 0 &&
		strcmp(szCODE, "LOAD_XML"                     ) != 0 &&
		strcmp(szCODE, "CRUD_LIST"                    ) != 0 &&
		strcmp(szCODE, "INCLUDE_LIST"                 ) != 0 &&
		strcmp(szCODE, "REFERRED_LIST"                ) != 0 &&
		strcmp(szCODE, "RESOURCE_LIST"                ) != 0 &&
		strcmp(szCODE, "PFM_TABLE_DATA_LIST"          ) != 0 &&
		strcmp(szCODE, "KFCC_OUT_LINK_TABLE_KEY_LIST" ) != 0 &&
		strcmp(szCODE, "KFCC_OUT_LINK_TABLE_DATA_LIST") != 0 ) {
		fprintf(stderr, "구분오류 : [%s]\n", szCODE);
		return (9);
	}

	/*-----------------------------------------------------------*/
	/*		구분별 처리                                          */
	/*-----------------------------------------------------------*/
	sprintf(szCommand, "java PFHttpCom \"%s\" \"%s\" %d \"%s\" \"%s\" \"%s\" \"%s\" "
							,szCODE
							,szServerIP
							,szPfmPort
		               		,szPfmUsr
		               		,szPfmPass
		               		,szRsrcName
		               		,szPfmResultTmp);	
		           
	/*LOG*/sprintf(gszLogMsg,"[%s] [%s] PROFRAME API CALL START!![%s] ",
									 szAcptNo,szRsrcName,szCommand);			
	eCAMS_Log(gszLogPath,gszLogFile,gszLogMsg);
		   
	retval = system(szCommand) / 256;
    
    /*LOG*/sprintf(gszLogMsg,"[%s] [%s] PROFRAME API CALL END!!!! RET=[%d]",
									 szAcptNo,szRsrcName,retval);			
	eCAMS_Log(gszLogPath,gszLogFile,gszLogMsg);
	
	if (retval != 0){
		ErrResult_Make(szPfmErrFile,"",retval,szPfmResultTmp);
		/*LOG*/sprintf(gszLogMsg,"[%s] [%s] PROFRAME API CALL ERR!!!! RET=[%d] command=[%s]",
										 szAcptNo,szRsrcName,retval,szCommand);		
		eCAMS_Log(gszLogPath,gszLogFile,gszLogMsg);
		return (1);
	}

	/*-----------------------------------------------------------*/
	/*		처리구분별 처리결과 체크                             */
	/*-----------------------------------------------------------*/
	nFileSize = FileSizeInf(szPfmResultTmp) + 1;
	dummy = (char *)malloc(nFileSize);
	memset(dummy, 0x00, nFileSize);

    if ((SrcPtr = fopen(szPfmResultTmp, "r")) == (FILE *) NULL) {
		ErrResult_Make(szPfmErrFile,"처리결과파일 READ FAIL",-1,"");
		/*LOG*/sprintf(gszLogMsg,"[%s] [%s] 처리결과파일 OPEN 오류 [%s]",
										 szAcptNo,szRsrcName,szPfmResultTmp);		
		eCAMS_Log(gszLogPath,gszLogFile,gszLogMsg);
		return (8);
	}

	while (fgets(dummy, nFileSize, SrcPtr) != (char *) NULL) {
		strcpy(dummy, (char *)rep_char(dummy,"\n"  , ""));
		strcpy(dummy, (char *)rep_char(dummy,"\r\n", ""));
		strcpy(dummy, (char *)trunc_char(dummy));
		Readbuf = (char *)malloc(nFileSize);

		memset(Readbuf, 0x00, nFileSize);
		strcpy(Readbuf, dummy);

		Index_key = 0;
		while(1) {
			if (Index_key == 0) {
				szKeys[Index_key] = strtok_r(Readbuf, ":", &toktmp);
				returnval= atoi(szKeys[0]);

				if (returnval != 0)	break;

				if (strcmp(szCODE, "CRUD_LIST"                    ) == 0 ||
					strcmp(szCODE, "INCLUDE_LIST"                 ) == 0 ||
					strcmp(szCODE, "REFERRED_LIST"                ) == 0 ||
					strcmp(szCODE, "PFM_TABLE_DATA_LIST"          ) == 0 ||
					strcmp(szCODE, "KFCC_OUT_LINK_TABLE_DATA_LIST") == 0 ||
					strcmp(szCODE, "LOAD_XML"                     ) == 0 ) break;
			}
			else
				szKeys[Index_key] = strtok_r(NULL, (char *)":", &toktmp);

			if (szKeys[Index_key] == NULL)
				break;

			Index_key++;
		}

		if (strcmp(szCODE, "CRUD_LIST"                    ) == 0 ||
			strcmp(szCODE, "INCLUDE_LIST"                 ) == 0 ||
			strcmp(szCODE, "REFERRED_LIST"                ) == 0 ||
			strcmp(szCODE, "PFM_TABLE_DATA_LIST"          ) == 0 ||
			strcmp(szCODE, "KFCC_OUT_LINK_TABLE_DATA_LIST") == 0 ) {
		    /*---------------------------------------------------*/
		    /*	CRUD 영향도 조회 결과파일 작성                   */
		    /*---------------------------------------------------*/
			break;
		}

		if (strcmp(szCODE, "LOAD_XML") != 0 && Index_key == 0) {
			ErrResult_Make(szPfmErrFile,"처리결과파일 분석 실패",-1,"");
			/*LOG*/sprintf(gszLogMsg,"[%s] [%s] 처리결과파일 분석 실패 dummy=[%s] [%d]",
											 szAcptNo,szRsrcName,dummy, Index_key);		
			eCAMS_Log(gszLogPath,gszLogFile,gszLogMsg);
			
			returnval = -1;
			free(Readbuf);
			break;
		}	
		++readcnt;
			
		returnval = -1;
		memset(returnmessage, 0x00, sizeof(returnmessage));
		returnval = atoi(szKeys[0]);
		if (returnval != 0){
			strcpy(returnmessage,szKeys[1]);
			ErrResult_Make(szPfmErrFile,returnmessage,returnval,"");
			fclose (SrcPtr);
			free(Readbuf);
			return returnval;
		}
		else{
			free(Readbuf);
			break;
		}
	}
	fclose (SrcPtr);
	free(dummy);
    
	if (readcnt < 1){
		ErrResult_Make(szPfmErrFile,"",-1,szPfmResultTmp);
		/*LOG*/sprintf(gszLogMsg,"[%s] [%s] 처리결과파일 분석 실패 dummy=[%s]",
										 szAcptNo,szRsrcName,szPfmResultTmp);		
		eCAMS_Log(gszLogPath,gszLogFile,gszLogMsg);
		return -1;
	}
		
	if (access(szPfmResultTmp, F_OK) >= 0)
		remove(szPfmResultTmp);

	if (returnval != 0 ||
	    strcmp(szCODE, "CHECKIN"                      ) == 0 ||
	    strcmp(szCODE, "REQUEST_PUBLISH"              ) == 0 ||
	    strcmp(szCODE, "LIST"                         ) == 0 ||
		strcmp(szCODE, "LOAD_XML"                     ) == 0 ||
	    strcmp(szCODE, "CRUD_LIST"                    ) == 0 ||
	    strcmp(szCODE, "INCLUDE_LIST"                 ) == 0 ||
	    strcmp(szCODE, "REFERRED_LIST"                ) == 0 ||
		strcmp(szCODE, "PFM_TABLE_DATA_LIST"          ) == 0 ||
		strcmp(szCODE, "KFCC_OUT_LINK_TABLE_DATA_LIST") == 0 ) {
		return	returnval;
	}

	sprintf(returnmessage,"%s", (char *)trunc_char(returnmessage));

	InitCmdInfo(&CmdInfo);

	CmdInfo.nPort = szPortNo;
	strcpy(CmdInfo.szServerIP, szServerIP);


	if (strcmp(szCODE, "PUBLISH_APPROVAL" ) == 0) {
		sprintf(szTarPath ,"%s/%s/%s", szReqPath,szAcptNo,szRsrcName);		
		sprintf(szPfmResultTmp,"%s/%s/%s.list",szReqPath,szAcptNo,szResult);
		sprintf(szPfmResultTmp2,"%s/%s/%s.tmp",szReqPath,szAcptNo,szResult);
		sprintf(szPfmResult,"%s/%s/%s",szReqPath,szAcptNo,szResult);
		
		Local_Dir_Make(szTarPath);
			
		sprintf(szCommand,"rm -rf %s/*",szTarPath);
		retval = system(szCommand)/256;	
		
		sprintf(szCommand,"rm -rf %s/%s/%s*",szReqPath,szAcptNo,szResult);
		retval = system(szCommand)/256;
		
		/*-------------------------------------------------------*/
		/* 	Tar파일 GET                                          */
		/* 	1. Remote : /appl/cfw/publish                        */
		/* 	2. Local  : ReqPath + AcptNo + physicalname          */
		/*-------------------------------------------------------*/
		sprintf(szListFile ,"%s/%s.tar", szTarPath,szRsrcName);
		sprintf(szListName ,"/appl/cfw/publish/%s.tar", szRsrcName);
		strcpy(CmdInfo.szJobGub,"G");
		sprintf(CmdInfo.szLocal ,"%s", szListFile);
		sprintf(CmdInfo.szRemote,"%s", szListName);
		Server_Cmd_JOB(&CmdInfo);
		LookCMD_INFO(&CmdInfo);

		if (strcmp(CmdInfo.szRstCond,"0000") != 0) {
			ErrResult_Make(szPfmErrFile,"Tar파일 수신 실패",-1,"");
			/*LOG*/sprintf(gszLogMsg,"[%s] [%s] Tar파일 수신 실패 remote=[%s] return=[%s]",
											 szAcptNo,szRsrcName,szListName,CmdInfo.szRstCond);		
			eCAMS_Log(gszLogPath,gszLogFile,gszLogMsg);
			return (1);
		}
		if (FileSizeInf(szListFile) == 0) {
			ErrResult_Make(szPfmErrFile,"Tar파일 Size zero",-1,"");
			/*LOG*/sprintf(gszLogMsg,"[%s] [%s] Tar파일 Size zero local=[%s]",
											 szAcptNo,szRsrcName,szListFile);		
			eCAMS_Log(gszLogPath,gszLogFile,gszLogMsg);
			return (1);
		}
		
		/*-------------------------------------------------------*/
		/* 	서버에 tar 파일 삭제                                 */
		/*-------------------------------------------------------*/
		strcpy(CmdInfo.szJobGub,"S");
		sprintf(CmdInfo.szCommand,"rm -rf \"%s\"", szListName);
		Server_Cmd_JOB(&CmdInfo);
		LookCMD_INFO(&CmdInfo);
		
		/*-------------------------------------------------------*/
		/* 	Tar파일 해제                                         */
		/* 	위치 : ReqPath + AcptNo + physicalname               */
		/*-------------------------------------------------------*/
		sprintf(szCommand,"cd %s;tar xvf %s",szTarPath,szListFile);
		retval = system(szCommand) / 256;
		if (retval != 0){
			ErrResult_Make(szPfmErrFile,"Tar파일 해제 실패",retval,"");
			/*LOG*/sprintf(gszLogMsg,"[%s] [%s] Tar파일 해제 실패!!!!  command=[%s] RET=[%d]",
											 szAcptNo,szRsrcName,szCommand,szCommand, retval);		
			eCAMS_Log(gszLogPath,gszLogFile,gszLogMsg);
			return (1);
		}
		/*-------------------------------------------------------*/
		/* 	Tar파일 해제 목록 추출                               */
		/* 	위치 : ReqPath + AcptNo + physicalname               */
		/*-------------------------------------------------------*/
		
		sprintf(szCommand,"ls -lLR \"%s\" | awk '{print $1,\" \",$9}' > %s", szTarPath, szPfmResultTmp);
		retval = system(szCommand) / 256;
		if (retval != 0){
			ErrResult_Make(szPfmErrFile,"Tar파일 해제목록 추출 실패",retval,"");
			/*LOG*/sprintf(gszLogMsg,"[%s] [%s] Tar파일 해제목록 추출 실패!!!!  command=[%s] RET=[%d]",
											 szAcptNo,szRsrcName,szCommand,szCommand, retval);		
			eCAMS_Log(gszLogPath,gszLogFile,gszLogMsg);
			return (1);
		}
		if (FileSizeInf(szPfmResultTmp) == 0) {
			ErrResult_Make(szPfmErrFile,"Tar파일 해제목록 Size zero",-1,"");
			/*LOG*/sprintf(gszLogMsg,"[%s] [%s] Tar파일 해제목록 Size zero filename=[%s]",
											 szAcptNo,szRsrcName,szPfmResultTmp);		
			eCAMS_Log(gszLogPath,gszLogFile,gszLogMsg);
			return (1);
		}
		
		if ((SrcPtr = fopen(szPfmResultTmp2,"w")) == NULL) {
			ErrResult_Make(szPfmErrFile,"임시파일 생성실패",-1,"");
			/*LOG*/sprintf(gszLogMsg,"[%s] [%s] 임시파일 생성실패 filename=[%s]",
											 szAcptNo,szRsrcName,szPfmResultTmp);		
			eCAMS_Log(gszLogPath,gszLogFile,gszLogMsg);
			return (1);
		}

		fprintf(SrcPtr,"%s:\n",szTarPath);
		fclose(SrcPtr);

		sprintf(szCommand,"cat %s >> %s",szPfmResultTmp, szPfmResultTmp2);
		retval = system(szCommand);
		if (retval != 0) {
			ErrResult_Make(szPfmErrFile,"파일리스트에 경로 추가 실패",-1,"");
			/*LOG*/sprintf(gszLogMsg,"[%s] [%s] 파일리스트에 경로 추가 실패 command=[%s]",
											 szAcptNo,szRsrcName,szCommand);		
			eCAMS_Log(gszLogPath,gszLogFile,gszLogMsg);
			return (1);
		}
		else {
		    sprintf(szCommand,"mv %s %s", szPfmResultTmp2,szPfmResultTmp);
		    retval = system(szCommand);
		}

		if (Make_FileList(szPfmResultTmp,szPfmResult,szTarPath) != 0) {
			return 1;
		}
		remove(szPfmResultTmp);
		return 0;
	}

	if (strcmp(szCODE, "RESOURCE_LIST"                ) == 0 ||
		strcmp(szCODE, "KFCC_OUT_LINK_TABLE_KEY_LIST" ) == 0 ) {
	}

	return 0;

}



/*---------------------------------------------------------------*/
/*             E N D    O F    P R O G R A M                     */
/*---------------------------------------------------------------*/
