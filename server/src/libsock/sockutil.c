/*-----------------------------------------------------------------
 ┌──────┬───────────────────────┐
 │ 프로그램명 │ sockutil.c                                   │
 ├──────┼───────────────────────┤
 │ 기      능 │ eCAMS Socket 통신관련 공통 프로그램          │
 ├──────┼───────────────────────┤
 │ 작  성  일 │ 2007. 10. 25                                 │
 ├──────┼───────────────────────┤
 │ 작  성  자 │ 최   병   남                                 │
 └──────┴───────────────────────┘
-----------------------------------------------------------------*/

/*---------------------------------------------------------------*/
/*     Header files                                              */
/*---------------------------------------------------------------*/
#include    <stdio.h>
#include    <sys/stat.h>
#include    <ecamsapi.h>
#include    <ecams_util.h>
#include	<AES.h>

/*---------------------------------------------------------------*/
/*       Socket 관련  PROCEDURE  DEFINE                          */
/*---------------------------------------------------------------*/
#if defined(__STDC__) || defined(__cplusplus) || defined(__sun) || defined(_AIX)
void     ExitProc              (int);
void     DefineSignal          (void);
int      ProcRecvData          (int, uchar *, int);
int      ProcSystemCmd         (int, uchar *, int);
int      ProcReadData          (int, uchar *, int);
int      HandleMainProc        (int);
void     NotUseDataTruncate    (char *, int );
int   	 CheckProcMsgQ 		   (int );
int		FileDataSend		   (int );
#else
void     ExitProc              ();
void     DefineSignal          ();
int      ProcRecvData          ();
int      ProcSystemCmd         ();
int      ProcReadData          ();
int      HandleMainProc        ();
void     NotUseDataTruncate    ();
int   	 CheckProcMsgQ         ();
int		FileDataSend		   ();
#endif

#ifdef _OSF_SOURCE
static    void sig_cld (void);
#endif


/*---------------------------------------------------------------*/
/*       USER  PROCEDURE  DEFINE                                 */
/*---------------------------------------------------------------*/
int     FileSizeInf           	(char *);
int   	Rsrc_GetPut_FTP 		(char *, char *, char *, char *, char *, char *, char *);
void    eCAMS_Logging         	(char *);
void	Local_Dir_Make			(char *);


/*---------------------------------------------------------------*/
/*    User Work 변수                                             */
/*---------------------------------------------------------------*/
int      ngCSockID                 ;  /* Connect Socket ID       */
int  	 ngRcvFd                   ;  /* 수신용 File Pointer     */
int  	 ngSndFd                   ;  /* 송신용 File Pointer     */
int  	 ngProcStep                ;  /* 송신/수신 상태          */
int  	 ngProcWork                ;  /* 송신/수신 여부          */
int  	 ngFileOffset              ;  /* 파일 송신 OffSet        */
char     szgJobGub              [2];  /* 처리구분                */
char	 *szgCommand;				  /* 처리명령문              */
char	 *szgLocal;					  /* Local File Name         */
char	 *szgRemote;				  /* Remote File Name        */
int      ngFileSize                ;  /* File Size               */
char     szgFileDate           [20];  /* File Date               */
char     szgRstIPAddr          [20];  /* 서버주소                */
char     szgSystime            [16];  /* 시스템시간              */
char     szgSysdate            [16];  /* 시스템일자              */
char     szgFileName          [200];  /* File Name               */
char     szgSV_RstCond          [5];  /* 처리 결과               */
char     szgLogMsg           [1024];  /* Logging Message Work    */
char     szgLogAcptNo          [20];  /* eCAMS Log 접수번호      */
char     szgLogFile           [256];  /* eCAMS Logging File      */
int      ngErrNo                   ;  /* Error No                */
char	 szFileInfo		  	  [256];
int		 indexrcvchk;
char	 szTardir			  [512];
char	 SMS_RecvBuf          [256];  /* SMS 수신내용            */

extern	char SMS_Server;

/*---------------------------------------------------------------*/
/*   eCAMS  LOGGING 관련  CONSTANT                               */
/*---------------------------------------------------------------*/
struct stat  gFileState;              /* Logging File Struct     */
struct tm   *gLoc;                    /* File 정보 Read Pointer  */


/*---------------------------------------------------------------*/
/*   SOCKET 통신관련  CONSTANT                                   */
/*---------------------------------------------------------------*/
int      sockid     ;
ulong  lgServip;                      /* Server IP의 Convert IP  */
int    nRetval;                       /* 처리결과                */
char   **ppgEnvp;                     /* Parameter죵료용 Pointer */
extern    int    errno;               /* 시스템 오류코드         */


void ExitProc (int nSig_num)
{
    DisConnectSocket (ngCSockID);
    exit (1);
}

#ifdef _OSF_SOURCE
static void sig_cld ()
{
    int nPid, nStatus;
    while ((nPid = wait3 (&nStatus, WNOHANG, (struct rusage *)0)) > 0)
        ;
}
#endif


void DefineSignal ()
{
    int    nSig_num;

    for (nSig_num = 0; nSig_num < 32; nSig_num++)
         signal (nSig_num, SIG_IGN);

#ifdef _OSF_SOURCE
    signal (SIGCLD, sig_cld);
#else
    signal (SIGCHLD, SIG_IGN);
#endif

    signal (SIGTERM, ExitProc);
    signal (SIGBUS,  ExitProc);
    signal (SIGSEGV, ExitProc);
    signal (SIGSYS,  ExitProc);
    signal (SIGKILL, ExitProc);
    signal (SIGINT,  ExitProc);
}


/*---------------------------------------------------------------*/
/*	Function  :  ProcRecvData                                    */
/*	Action    : 송신 데이터를 수신하는 Procedure                 */
/*	Parameter : nSock   : Socket 번호                            */
/*	            szRdBuf : Read Buffer                            */
/*	            nRdLng  : Read Length                            */
/*	Return    : 양수:정상, 음수:오류                             */
/*---------------------------------------------------------------*/
int     ProcRecvData 	( int   nSock
						, uchar *szRdBuf
						, int   nRdLng
						)
{
	int nRet, nHdlng, nPulng, nMakelng, nWrtlng;
	char szFileName [200];
	char szTmpbuf    [80];
	char strDir    [1024];
	char strTmp    [1024];
	char szCommand[512];
	int	 nRet2;


	BpComHead *uhead = (BpComHead *)szRdBuf;

    nHdlng = sizeof (BpComHead);
    nPulng = nRdLng - nHdlng;

    /*-----------------------------------------------------------*/
    /* 거래에 대한 응답 Check.                                   */
    /*-----------------------------------------------------------*/
	switch (uhead->CF) {
	case  dfFirstChain   :
		/*-------------------------------------------------------*/
		/* 	File 송신의 시작을 알리는 정보인 경우 먼저 저장할    */
		/*	File명을 얻어와서 생성                               */
		/*-------------------------------------------------------*/
		memset (szgFileName, 0x00, sizeof (szgFileName));
		sprintf (szgFileName, "%s", &szRdBuf [nHdlng+dfRemoteOffset]);

		/*-------------------------------------------------------*/
		/* 2. 디렉토리 존재 여부 검사                            */
		/*-------------------------------------------------------*/
		memset(strDir, 0x00, sizeof(strDir));
		strcpy(strTmp, szgFileName);
		nRet = 0;
		nRet = Right_Char_Check(strTmp, '/');

		if (nRet >= 0) {
			sprintf(strDir, "%s", left_char(strTmp, nRet));
		}

		if (strlen(strDir) != 0 && access(strDir, F_OK) != 0) {
			Local_Dir_Make(strDir);
		}

		memset(szTardir,0x00,sizeof(szTardir));
		strcpy(szTardir,strDir);

		ngRcvFd = open (szgFileName, O_RDWR | O_CREAT | O_TRUNC, 0755);
		if (ngRcvFd < 0) {
			/*---------------------------------------------------*/
			/* Open Err시 오류 전송. Process 종료                */
			/*---------------------------------------------------*/
			ngProcWork = dfCmd_EROR;
			ngErrNo = errno;
			memset (szTmpbuf, 0x00, sizeof (szTmpbuf));
			nMakelng = MakeCommHeader (CmdBpExit, 0, 0, szTmpbuf, szTmpbuf, 0);
			nWrtlng = SendDataToSock (nSock, szTmpbuf, nMakelng);
			return (-1);
		}
		break;

	case  dfMiddleChain  :
		/*-------------------------------------------------------*/
		/* 	File의 실제 내용을 보내오는 것으로 File의 마지막     */
		/*	위치에 Write.                                        */
		/*-------------------------------------------------------*/
		lseek (ngRcvFd, 0L, SEEK_END);
		write (ngRcvFd, &szRdBuf[nHdlng], nPulng);
		break;

	case  dfLastChain    :
		/*-------------------------------------------------------*/
		/* 	File의 마지막 내용을 보내오는 것으로 실제 저장할     */
		/*	내용이 있는 경우를 Check하여 Write하고 그렇지 않은   */
		/*	경우는 FD를 Close만 한다.                            */
		/*-------------------------------------------------------*/
		lseek (ngRcvFd, 0L, SEEK_END);
		write (ngRcvFd, &szRdBuf[nHdlng], nPulng);
		close (ngRcvFd);

		if (szgJobGub [0] == 'X'){
			if (tarFileMake_ext_X(szgLocal) != 0)
				return (-1);
		}

		/*-------------------------------------------------------*/
		/* 	HandleMainProc에서 return을 위해                     */
		/*-------------------------------------------------------*/
		ngProcWork = dfCmd_DAOK;
		ngProcStep = dfLastChain;

		memset(szgRstIPAddr, 0x00, sizeof(szgRstIPAddr));
		memcpy(szgRstIPAddr, uhead->IPAddr, 10);
		break;
	}
    return (1);
}


/*---------------------------------------------------------------*/
/*	Function  : FileSizeInf                                      */
/*	Action    : FILE SIZE GET Procedure                          */
/*	Parameter : szFileName : 파일명                              */
/*	Return    : 파일Size                                         */
/*---------------------------------------------------------------*/
int     FileSizeInf		( char *szFileName
						)
{
	if (stat(szFileName, &gFileState) < 0)
		return (-1);

	gLoc = localtime(&gFileState.st_mtime);
	return gFileState.st_size;
}


/*---------------------------------------------------------------*/
/*   FILE  Information  GET   Procedure                          */
/*---------------------------------------------------------------*/
int     GetFileInf	( char 	*FileName
					, int	 gFileSize
					, char	*gFileDate
					)
{
struct   group   *gr_p;
struct   passwd  *pw_p;
char  amode        [10];
char  rmode         [4];
int   j, i, k          ;
char  tmpchar       [2];
char  xtbl  [10] = "421421421";
struct   stat  f_st       ;        /* File 상태 LayOut           */
struct   tm   *loc        ;        /* File 시간                  */


	if (stat(FileName, &f_st) < 0)
		return (1);

	gFileSize = f_st.st_size;

	loc = localtime(&f_st.st_mtime);
	sprintf(gFileDate, "%d%002d%002d%002d%002d%002d"
                     , loc->tm_year + 1900, loc->tm_mon + 1, loc->tm_mday
                     , loc->tm_hour, loc->tm_min, loc->tm_sec);

   return (0);

}


/*---------------------------------------------------------------*/
/*	Function  : ProcSystemCmd                                    */
/*	Action    : 시스템명령 요청시 처리                           */
/*	Parameter : nSock   : Socket 번호                            */
/*	            szRdBuf : Read Buffer                            */
/*	            nRdLng  : Read Length                            */
/*	Return    : 정상 : '0', 오류 : '1'                           */
/*---------------------------------------------------------------*/
int     ProcSystemCmd 	( int   nSock
						, uchar *szRdBuf
						, int nRdLng
						)
{
int 	        nRetval, nHdlng, nPulng;
char 	        szTmpbuf [256];

	BpComHead *uhead = (BpComHead *) szRdBuf;

    nHdlng = sizeof (BpComHead);
    nPulng = nRdLng - nHdlng;

	/*-----------------------------------------------------------*/
	/* 	수행할 System 명령을 Get.                                */
	/*-----------------------------------------------------------*/
	memset (szTmpbuf, 0x00, sizeof (szTmpbuf));
	memcpy (szTmpbuf, &szRdBuf[nHdlng], nPulng);

	/*-----------------------------------------------------------*/
	/* 	ret가 0 이면 성공                                        */
	/*-----------------------------------------------------------*/
	nRetval = system (szTmpbuf);
	if (!nRetval) /* 성공한 경우 */
		return (0);
	else
		return (1);
}


/*---------------------------------------------------------------*/
/*	Function  : ProcReadData                                     */
/*	Action    : Socket Data Receive                              */
/*	Parameter : nSock   : Socket 번호                            */
/*	            szRdBuf : Read Buffer                            */
/*	            nRdLng  : Read Length                            */
/*	Return    : 정상 : '0', 오류 : '1'                           */
/*---------------------------------------------------------------*/
int    ProcReadData  	( int   nSock
						, uchar *szRdBuf
						, int nRdLng
						)
{
	BpComHead *uhead = (BpComHead *) szRdBuf;
	ngErrNo = uhead->FepSts;

	switch (uhead->Cmd) {
	case CmdSendTrx    :
		return (ProcRecvData (nSock, szRdBuf, nRdLng));

	case CmdSystem    :
	case 'K'		  :
		return (ProcSystemCmd (nSock, szRdBuf, nRdLng));

	case CmdBidMsg    :
		ngProcWork = uhead->EC;
		memset(szgRstIPAddr, 0x00, sizeof(szgRstIPAddr));
		memcpy(szgRstIPAddr, uhead->IPAddr, 10);

		if (!strcmp(szgJobGub,"I")) {
		   ngFileSize = atoi((char *)uhead->FileSize);
		   memcpy(szgFileDate, uhead->FileDate, 14);
		   sprintf(szFileInfo,"%s",&szRdBuf[sizeof(BpComHead)]);
		}

		if (!strcmp(szgJobGub,"U")) {
		   sprintf(szFileInfo,"%s",&szRdBuf[sizeof(BpComHead)]);
		}
		break;

	case CmdBpExit  :
		ExitProc (0);
		break;
    }
    return (0);
}


/*---------------------------------------------------------------*/
/*	Function  : HandleMainProc                                   */
/*	Action    : Socket 처리결과 수신                             */
/*	Parameter : nSock   : Socket 번호                            */
/*	Return    : 정상 : '0', 오류 : '0'이외                       */
/*---------------------------------------------------------------*/
int   HandleMainProc 	( int   nSock
						)
{
int     nReadlng;
char    szgSystime[12];
uchar	*szReadbuf;


	szReadbuf = (uchar *)malloc(dfMaxBufSize);

    ngProcStep = dfLastChain;

	/*-----------------------------------------------------------*/
    /* 작업 결과를 받아야 하므로 해당 Flag Reset                 */
	/*-----------------------------------------------------------*/
    ngProcWork = 0;
    if (CheckProcMsgQ (nSock) == 0){
    	free(szReadbuf);
        return (1);
    }


    while (1) {
        memset (szReadbuf, 0x00, dfMaxBufSize);

        nReadlng = ReadDataFromSock (nSock, szReadbuf);

        if (nReadlng > 0) {
            if (ProcReadData  (nSock, szReadbuf, nReadlng) < 0)  {
            	free(szReadbuf);
                return (-1);
            }

			if (SMS_Server == ON) {
				strcpy(SMS_RecvBuf, (char *)&szReadbuf[8]);
				ngProcStep = dfLastChain;
		        ngProcWork = dfCmd_DAOK;
		        break;
			}
        }
        else if (nReadlng < 0)   {
            Get_Sys_Time10 (szgSystime);
            free(szReadbuf);
            return (-1);
        }

	    /*-------------------------------------------------------*/
        /*  Remote  File Open Err                                */
	    /*-------------------------------------------------------*/
        if (ngProcWork == dfCmd_EROR){
        	free(szReadbuf);
            return (1);
        }

	    /*-------------------------------------------------------*/
        /* 현재 처리하고 있는 작업이 없는 경우 MsgQue Check      */
        /* 그리고 File 송신하려고 하는 경우 처리.                */
	    /*-------------------------------------------------------*/
        if (szgJobGub [0] == 'F' || szgJobGub[0] == 'Z' ) {
        	if (ngProcStep != dfLastChain) {
            	FileDataSend (nSock);
            }
        }

        if (szgJobGub [0] == 'X' && indexrcvchk == 0) {
        	if (ngProcStep != dfLastChain) {
            	FileDataSend (nSock);
            }
            else if (ngProcStep == dfLastChain){
            	indexrcvchk = 1;
			    if (Change_Rcv_Mode(nSock) == 0){
			    	free(szReadbuf);
			        return (1);
			    }
            }
        }


	    /*-------------------------------------------------------*/
        /* 작업완료이고 처리결과 수신 여부 Check                 */
	    /*-------------------------------------------------------*/
        if (ngProcStep == dfLastChain) {
            if (ngProcWork == dfCmd_DAOK){
            	free(szReadbuf);
                return (0);
            }

            else if (ngProcWork == dfCmd_EROR) {
            	free(szReadbuf);
                return (1);
            }
            else
                continue;
        }
        else if (ngProcWork == dfCmd_EROR) {
        	free(szReadbuf);
            return (1);
            break;
        }
    }
    free(szReadbuf);
    return 0;
}


/*---------------------------------------------------------------*/
/*	Function  : Server_Cmd_JOB                                   */
/*	Action    : SERVER 명령 전송                                 */
/*	Parameter : CmdInfo : Socket Structure                       */
/*	Return    : 정상 : '0', 오류 : '0'이외                       */
/*---------------------------------------------------------------*/
int 	Server_Cmd_JOB		( CMD_INFO *CmdInfo
							)
{
char SysCmd                         [50];
char szServerIP                     [20];

	if (dfMaxFileSize == 0) {
		dfMaxFileSize  = dfeCAMSBufSize;
	}

	dfMaxMsqSize  = dfMaxFileSize + BaseCommHeadLng;
	dfMaxBufSize  = dfMaxMsqSize;

	szgCommand = (char *)malloc(dfFullPath);
	szgLocal   = (char *)malloc(dfFullPath);
	szgRemote  = (char *)malloc(dfFullPath);

	memset(szgCommand, 0x00, dfFullPath);
	memset(szgLocal  , 0x00, dfFullPath);
	memset(szgRemote , 0x00, dfFullPath);

    strcpy(szgLogFile  , CmdInfo->szLogFile );
    strcpy(szgLogAcptNo, CmdInfo->szAcptNo  );
    strcpy(szServerIP  , CmdInfo->szServerIP);
    dfSendPort = CmdInfo->nPort;
    strcpy(szgJobGub   , CmdInfo->szJobGub  );
    strcpy(szgLocal    , CmdInfo->szLocal   );
    strcpy(szgRemote   , CmdInfo->szRemote  );
    strcpy(szgCommand  , CmdInfo->szCommand );

    NotUseDataTruncate (szgJobGub, 0x20);
    lgServip = CvtIPchr2ulong (szServerIP);

	if (dfSendPort == 0)
    	ngCSockID = ConnectClient2Server (dfeCAMSFepPort, lgServip);
	else
		ngCSockID = ConnectClient2Server (dfSendPort    , lgServip);

    memset (szgSysdate, 0x00, sizeof (szgSysdate));
    memset (szgSystime, 0x00, sizeof (szgSystime));
    Get_Sys_Date (szgSysdate);
    Get_Sys_Time (szgSystime);
    memset(szgRstIPAddr, 0x00, sizeof(szgRstIPAddr));

    if (ngCSockID > 0) {
        nRetval = HandleMainProc (ngCSockID);
		if (nRetval == 0) {
           sprintf(szgSV_RstCond, "0000");
        }
        else {
			if (memcmp(szgJobGub, "K", 1) == 0 &&
				ngErrNo == 0                   ) {
				sprintf(szgSV_RstCond, "%04d", ngErrNo);		   	
			}
			else {
				if (ngErrNo == 0)
					sprintf(szgSV_RstCond, "%s", "EROR");
				else
					sprintf(szgSV_RstCond, "%04d", ngErrNo);
			}
        }
        NotUseDataTruncate (szgSV_RstCond, 0x20);
        DisConnectSocket (ngCSockID);
    }
    else {
        /*-------------------------------------------------------*/
        /* Server Connect 실패                                   */
        /*  (Server에 eCAMS_svr process가 없다)                  */
        /*-------------------------------------------------------*/
        sprintf(szgSV_RstCond, "SVER");
    }

    memset(szgLogMsg, 0x00, sizeof(szgLogMsg));
    if (!strcmp(szgJobGub,"S")) {
		sprintf (szgLogMsg, "[S] %s ServerIP=%s %d, COND=[%s], Command = %s",
                            szgSystime, szServerIP, dfSendPort, szgSV_RstCond, szgCommand);
    }
    else if (!strcmp(szgJobGub,"I")) {
		CmdInfo->lFileSize = ngFileSize;
		strcpy(CmdInfo->szFileDate, szgFileDate);
		strcpy(CmdInfo->szFileInfo,szFileInfo);
        sprintf (szgLogMsg, "[I]  %s ServerIP=%s %d, COND=[%s], FileName=%s",
                            szgSystime, szServerIP, dfSendPort, szgSV_RstCond, szgCommand);
    }

    else if (!strcmp(szgJobGub,"U")) {
		strcpy(CmdInfo->szFileInfo, szFileInfo);
        sprintf (szgLogMsg, "[U]  %s ServerIP=%s %d, COND=[%s], FileName=%s",
                            szgSystime, szServerIP, dfSendPort, szgSV_RstCond, szFileInfo);
    }

    else {
        sprintf (szgLogMsg, "[%s] %s ServerIP=%s %d, COND=[%s], Local=%s, Remote=%s",
                          szgJobGub, szgSystime, szServerIP, dfSendPort, szgSV_RstCond, szgLocal, szgRemote);
    }

    eCAMS_Logging (szgLogMsg);
	strcpy(CmdInfo->szRstCond,szgSV_RstCond);

	free(szgCommand);
	free(szgLocal);
	free(szgRemote);


    if (memcmp(szgSV_RstCond, "0000", 4) == 0)
       return 0;
    else
    if (memcmp(szgSV_RstCond, "SVER", 4) == 0 &&
    	memcmp(szgJobGub, "K", 1) != 0)
	{
		sprintf(SysCmd, "ecams_svrck 1 &");
		system (SysCmd);
		return -8;
	}
    else if (memcmp(szgSV_RstCond, "EROR", 4) == 0) {
    	if (memcmp(szgJobGub, "K", 1) == 0)
    		return 0;
    	else
			return -7;
	}
    else
		return atoi(szgSV_RstCond);

}

/*---------------------------------------------------------------*/
/*	Function  : CheckProcMsgQ                                    */
/*	Action    : MsgQue를 Check하여 Data가 존재하는경우           */
/*	            이를 중개 서버로 송신                            */
/*	Parameter : nSockFd : Socket 번호                            */
/*	Return    : 전송된메시지길이                                 */
/*---------------------------------------------------------------*/
int   CheckProcMsgQ 	( int   nSockFd
						)
{
	int        nLng = 0, nMakelng, nWrtlng;
	char       szgSystime [16];
	char	   *szMsgbuf;
	uchar      *szTmpbuf ;
	int  nRet            ;
	char strDir    [1024];
	char strTmp    [1024];
	char	*encBuf;
	int		ncount;
	int 	nCmdSize;
	int		i;

	u8	szKey[16] = { 0x06, 0xa9, 0x21, 0x40, 0x36, 0xb8, 0xa1, 0x5b,
					  0x51, 0x2e, 0x03, 0xd5, 0x34, 0x12, 0x00, 0x06 };
	u8	szIV[16] =  { 0x3d, 0xaf, 0xba, 0x42, 0x9d, 0x9e, 0xb4, 0x30,
					  0xb4, 0x22, 0xda, 0x80, 0x2c, 0x9f, 0xac, 0x41 };



    szTmpbuf = (uchar *) malloc (dfMaxBufSize);
    szMsgbuf = (char *) malloc (dfMaxMsqSize);

    /*-----------------------------------------------------------*/
    /* 중개 서버가 수신할 수 있도록  통신 Header 조립            */
    /*-----------------------------------------------------------*/
    memset (szMsgbuf, 0x00, dfMaxMsqSize);
    memset (szTmpbuf, 0x00, dfMaxBufSize);

    if (!strcmp(szgJobGub,"S")) {
        /*-------------------------------------------------------*/
        /* System Command 수행                                   */
        /*-------------------------------------------------------*/

        encBuf = (char *)malloc (dfMaxBufSize);
        memset(encBuf,0x00,dfMaxBufSize);


		strcpy(encBuf,szgCommand);

		ncount = 0;

	    if (strlen(szgCommand)%16 == 0) {
			nCmdSize = strlen(szgCommand);
		}
		else {
			nCmdSize = (strlen(szgCommand)/16+1) * 16;
		}

		for (i = 0; i< nCmdSize;i++){
			if (&szgCommand[i] == 0x00){
				break;
			}
			else{
				ncount++;
			}
		}

		aes_128_cbc_encrypt(szKey, szIV, (u8 *)encBuf, ncount);

        memset(szMsgbuf,0x00,dfMaxMsqSize);

		ncount = 0;
		for (i = 0; i< nCmdSize;i++){
			if (&encBuf[i] == 0x00){
				break;
			}
			else{
				ncount++;
			}
		}

        memcpy(szMsgbuf,encBuf,ncount);


        free(encBuf);


        nLng = strlen (szMsgbuf);

        ngProcStep = dfLastChain;
        nMakelng = MakeCommHeader (CmdSystem, 0, ngProcStep, szTmpbuf, szMsgbuf, ncount);
    }

    else if (!strcmp(szgJobGub,"M")) {
        /*-------------------------------------------------------*/
        /* System Command 수행                                   */
        /*-------------------------------------------------------*/
        sprintf (szMsgbuf, "%s", szgCommand);
        nLng = strlen (szMsgbuf);
        ngProcStep = dfLastChain;
        nMakelng = MakeCommHeader (CmdSMSMsg, 0, ngProcStep, szTmpbuf, szMsgbuf, nLng);
    }

    else if (!strcmp(szgJobGub,"K")) {
        /*-------------------------------------------------------*/
        /* System Command 수행                                   */
        /*-------------------------------------------------------*/
        sprintf (szMsgbuf, "%s", szgCommand);
        nLng = strlen (szMsgbuf);
        ngProcStep = dfLastChain;
        nMakelng = MakeCommHeader (CmdSystem, 0, ngProcStep, szTmpbuf, szMsgbuf, nLng);
    }

    else if (!strcmp(szgJobGub,"U")) {
        /*-------------------------------------------------------*/
        /* System Command 수행                                   */
        /*-------------------------------------------------------*/
        sprintf (szMsgbuf, "%s", szgCommand);
        nLng = strlen (szMsgbuf);
        ngProcStep = dfLastChain;
        nMakelng = MakeCommHeader (CmdEnvGet, 0, ngProcStep, szTmpbuf, szMsgbuf, nLng);
    }

    else if (!strcmp(szgJobGub,"I")) {
        /*-------------------------------------------------------*/
        /* System Command 수행                                   */
        /*-------------------------------------------------------*/
        sprintf (szMsgbuf, "%s", szgCommand);
        nLng = strlen (szMsgbuf);
        ngProcStep = dfLastChain;
        nMakelng = MakeCommHeader (CmdFileInf, 0, ngProcStep, szTmpbuf, szMsgbuf, nLng);
    }

    else {
        /*-------------------------------------------------------*/
        /* Local File Name Set                                   */
        /*-------------------------------------------------------*/
		if (szgJobGub [0] == 'F') {
             errno = 0;
	         ngSndFd = open (szgLocal, O_RDONLY);
	         /*--------------------------------------------------*/
             /* 송신할 File을 Open하는데 이상이 생기면 리턴      */
             /*--------------------------------------------------*/
             if (ngSndFd < 0) {
             	 ngErrNo = errno;
             	 free (szTmpbuf);
             	 free (szMsgbuf);
                 return (0);
             }
             sprintf (szMsgbuf, "F%s", szgLocal);
        } else if (szgJobGub [0] == 'G'){
             errno = 0;

			/*---------------------------------------------------*/
			/* 2. 디렉토리 존재 여부 검사                        */
			/*---------------------------------------------------*/
			memset(strDir, 0x00, sizeof(strDir));
			strcpy(strTmp, szgLocal);

			nRet = 0;
			nRet = Right_Char_Check(strTmp, '/');

			if(nRet >= 0) {
				sprintf(strDir, "%s", left_char(strTmp,nRet));
			}

			if (strlen(strDir) != 0 && access(strDir, F_OK) != 0) {
				Local_Dir_Make(strDir);
			}

             ngRcvFd = open (szgLocal, O_RDWR | O_CREAT | O_TRUNC, 0755);

             /*--------------------------------------------------*/
             /* 수신할 File을 Open하는데 이상이 생기면 리턴      */
             /*--------------------------------------------------*/
             if (ngRcvFd < 0) {
             	 ngErrNo = errno;
             	 free (szTmpbuf);
             	 free (szMsgbuf);
                 return (0);
             }
             sprintf (szMsgbuf, "G%s", szgLocal);
        }
        else if (szgJobGub [0] == 'Z'){
             errno = 0;

             if (tarFileMake_comp_Z(szgLocal) != 0){
             	fprintf(stderr,"tarFileMake_s return !=0\n");
             	return (0);
            }

	         ngSndFd = open (szgLocal, O_RDONLY);
	         /*--------------------------------------------------*/
             /* 송신할 File을 Open하는데 이상이 생기면 리턴      */
             /*--------------------------------------------------*/
             if (ngSndFd < 0) {
				 fprintf(stderr,"file open error [%s][%d]\n",szgLocal,ngSndFd);
             	 ngErrNo = errno;
             	 free (szTmpbuf);
             	 free (szMsgbuf);
                 return (0);
             }
             sprintf (szMsgbuf, "Z%s", szgLocal);
        }
        else if (szgJobGub [0] == 'X'){
             errno = 0;
             indexrcvchk = 0;

			 szg_chg(indexrcvchk);

	         ngSndFd = open (szgLocal, O_RDONLY);
	         /*--------------------------------------------------*/
             /* 송신할 File을 Open하는데 이상이 생기면 리턴      */
             /*--------------------------------------------------*/
             if (ngSndFd < 0) {
				 fprintf(stderr,"file open error [%s][%d]\n",szgLocal,ngSndFd);
             	 ngErrNo = errno;
             	 free (szTmpbuf);
             	 free (szMsgbuf);
                 return (0);
             }
             sprintf (szMsgbuf, "X%s", szgLocal);

        }

        /*-------------------------------------------------------*/
        /* Remote File Name Set                                  */
        /*-------------------------------------------------------*/
        sprintf (&szMsgbuf [dfRemoteOffset], "%s", szgRemote);

		nLng = dfFullPath;
        ngFileOffset = 0L;
        ngProcStep = dfFirstChain;
        nMakelng = MakeCommHeader (CmdSendTrx, 0, ngProcStep, szTmpbuf, szMsgbuf, nLng);
    }

    /*-----------------------------------------------------------*/
    /* 서버로 Data의 송신                                        */
    /*-----------------------------------------------------------*/
    nWrtlng = SendDataToSock (nSockFd, szTmpbuf, nMakelng);
    free (szTmpbuf);
    free (szMsgbuf);
    return (nWrtlng);
}


/*---------------------------------------------------------------*/
/*	Function  : FileDataSend                                     */
/*	Action    : 파일수신요구시  해당 파일의 DATA 전송            */
/*	Parameter : nSockFd : Socket 번호                            */
/*	Return    : 전송된메시지길이                                 */
/*---------------------------------------------------------------*/
int		FileDataSend	( int   nSockFd
						)
{
	int        nLng = 0, nMakelng, nWrtlng;
	uchar      *szTmpbuf;
	uchar      *szMsgbuf;


    szTmpbuf = (uchar *) malloc (dfMaxBufSize);
    szMsgbuf = (uchar *) malloc (dfMaxMsqSize);

    lseek (ngSndFd, ngFileOffset, SEEK_SET);

    memset (szMsgbuf, 0x00, dfMaxMsqSize);
    memset (szTmpbuf, 0x00, dfMaxBufSize);

    nLng = read (ngSndFd, szMsgbuf, dfMaxFileSize);

    ngFileOffset += nLng;
    if (nLng < dfMaxFileSize)
        ngProcStep = dfLastChain;
    else
        ngProcStep = dfMiddleChain;

    nMakelng = MakeCommHeader (CmdSendTrx, 0, ngProcStep, szTmpbuf, szMsgbuf, nLng);
    nWrtlng = SendDataToSock (nSockFd, szTmpbuf, nMakelng);

    if (ngProcStep == dfLastChain)
		close(ngSndFd);

	free(szTmpbuf);
	free(szMsgbuf);

	return (nWrtlng);
}


/*---------------------------------------------------------------*/
/*	Function  : InitCmdInfo                                      */
/*	Action    : Socket Parameter Initial                         */
/*	Parameter : pCmdInfo : Socket Structure                      */
/*	Return    : 없슴                                             */
/*---------------------------------------------------------------*/
void InitCmdInfo(CMD_INFO * pCmdInfo)
{

	memset(pCmdInfo->szLogFile   , 0x00, sizeof(pCmdInfo->szLogFile   ));
	memset(pCmdInfo->szAcptNo    , 0x00, sizeof(pCmdInfo->szAcptNo    ));
	memset(pCmdInfo->szServerIP  , 0x00, sizeof(pCmdInfo->szServerIP  ));
	memset(pCmdInfo->szGWServerIP, 0x00, sizeof(pCmdInfo->szGWServerIP));
	memset(pCmdInfo->szJobGub    , 0x00, sizeof(pCmdInfo->szJobGub    ));
	memset(pCmdInfo->szLocal     , 0x00, sizeof(pCmdInfo->szLocal     ));
	memset(pCmdInfo->szRemote    , 0x00, sizeof(pCmdInfo->szRemote    ));
	memset(pCmdInfo->szCommand   , 0x00, sizeof(pCmdInfo->szCommand   ));
	memset(pCmdInfo->szRstCond   , 0x00, sizeof(pCmdInfo->szRstCond   ));
	memset(pCmdInfo->szFileDate  , 0x00, sizeof(pCmdInfo->szFileDate  ));
	memset(pCmdInfo->szFileInfo  , 0x00, sizeof(pCmdInfo->szFileInfo  ));

	pCmdInfo->nPort     = 0;
	pCmdInfo->nGWPort   = 0;
	pCmdInfo->lFileSize = 0;

}

/*---------------------------------------------------------------*/
/*	Function  : LookCMD_INFO                                     */
/*	Action    : Socket Status View                               */
/*	Parameter : CmdInfo : Socket Structure                       */
/*	Return    : 없슴                                             */
/*---------------------------------------------------------------*/
void LookCMD_INFO(CMD_INFO *CmdInfo)
{
	fprintf(stdout, "CmdInfo : JobGub[%s] ServerIP[%s], Port[%d], Local[%s], Remote[%s], Command[%s] \n",
					CmdInfo->szJobGub, CmdInfo->szServerIP, CmdInfo->nPort, CmdInfo->szLocal,
					CmdInfo->szRemote, CmdInfo->szCommand);
	fprintf(stdout, "          FileSize[%d], FileDate[%s], RstCond[%s]\n",
					(int)CmdInfo->lFileSize, CmdInfo->szFileDate, CmdInfo->szRstCond);
	fprintf(stdout,"FileInfo = [%s]\n",CmdInfo->szFileInfo);
}


/*---------------------------------------------------------------*/
/*	Function  : Server_Dir_Make                                  */
/*	Action    : SERVER DIRECTORY  Make (mkdir xxx)               */
/*	Parameter : CmdInfo : Socket Structure                       */
/*	            MakeDir : Make 대상 Directory                    */
/*	Return    : 없슴                                             */
/*---------------------------------------------------------------*/
void  	Server_Dir_Make	( CMD_INFO *CmdInfo
						, char *MakeDir
						)
{
	int nret;
	char    TestPath    [200];  /* Directory Check Work          */
	char    MakePath    [200];  /* Check & Create 대상 Directory */
	char	szTempLog	[256];
	char 	szDir         [3];


	strcpy(szTempLog,CmdInfo->szLogFile);

	if(Char_Check(MakeDir, ":") > 0)
		strcpy(szDir,"\\");
	else
		strcpy(szDir,"/");

    memset(MakePath, 0x00, sizeof(MakePath));
    sprintf(TestPath, "%s", MakeDir);
    while (1) {
		nret = Char_Check(TestPath,szDir);

       if (nret >= 0) {
          sprintf(MakePath, "%s%s", MakePath, left_char(TestPath, nret+1));
          sprintf(TestPath, "%s"  , right_char(TestPath,strlen(TestPath)-nret-1));
       }
       else {
          sprintf(MakePath, "%s%s", MakePath, TestPath);
          memset(TestPath, 0x00, sizeof(TestPath));
       }

       if (strlen(MakePath) > 1) {
		  sprintf(CmdInfo->szLogFile,"");
       	  sprintf(CmdInfo->szJobGub,"I");
		  if(strcmp(szDir,"/") == 0 || cmp_right_char(MakePath,2,":\\") == 0)
			sprintf(CmdInfo->szCommand, "%s", MakePath);
		  else
			sprintf(CmdInfo->szCommand,"%s", left_char(MakePath,strlen(MakePath) - 1));

          Server_Cmd_JOB(CmdInfo);
		  sprintf(CmdInfo->szLogFile,szTempLog);
          if (memcmp(CmdInfo->szRstCond, "0000", 4) != 0) {
	          sprintf(CmdInfo->szJobGub,"S");
			  sprintf(CmdInfo->szCommand, "mkdir %s", MakePath);
			  Server_Cmd_JOB(CmdInfo);
	      }
       }
       if (strlen(TestPath) == 0) break;
    }
}

/*---------------------------------------------------------------*/
/*	Function  : Local_Dir_Make                                   */
/*	Action    : LOCAL DIRECTORY  Make (mkdir xxx)                */
/*	Parameter : szMakeDir : Make 대상 Directory                  */
/*	Return    : 없슴                                             */
/*---------------------------------------------------------------*/
void 	Local_Dir_Make2	( char *szMakeDir,char *pUser,char *pGroup, char *pMod
						)
{
int 	nret1;
char 	szTestPath   [512];     /* Directory Check Work          */
char 	szMakePath   [512];     /* Check & Create 대상 Directory */
char 	szCommand    [1024];

	memset(szTestPath , 0x00, sizeof(szTestPath));
	memset(szMakePath , 0x00, sizeof(szMakePath));
    sprintf(szTestPath, "%s", szMakeDir);

    if (cmp_right_char(szTestPath, 1, "/") == 0)
        sprintf(szTestPath, "%s", left_char(szTestPath, strlen(szTestPath)-1));

    while (1) {
        nret1 = Char_Check(szTestPath,"/");
        if (nret1 >= 0) {
           sprintf(szMakePath, "%s%s", szMakePath, left_char(szTestPath, nret1));
           sprintf(szTestPath, "%s"  , right_char(szTestPath,strlen(szTestPath)-nret1-1));
        }
        else {
           sprintf(szMakePath, "%s%s", szMakePath,  szTestPath);
           memset(szTestPath, 0x00, sizeof(szTestPath));
        }

        if (access(szMakePath, F_OK) < 0 && strlen(szMakePath) > 0) {
        	memset(szCommand,0x00,sizeof(szCommand));
            sprintf(szCommand, "\\mkdir \"%s\"", szMakePath);
            system (szCommand);
            if (strlen(pUser)>0 && strcmp(pUser,"@") != 0){
	        	memset(szCommand,0x00,sizeof(szCommand));
	            sprintf(szCommand, "\\chown %s \"%s\"", pUser,szMakePath);
	            system (szCommand);
            }
            if (strlen(pGroup)>0 && strcmp(pGroup,"@") != 0){
	        	memset(szCommand,0x00,sizeof(szCommand));
	            sprintf(szCommand, "\\chgrp %s \"%s\"", pGroup,szMakePath);
	            system (szCommand);

			}
            if (strlen(pMod)>0 && strcmp(pMod,"@") != 0){
	        	memset(szCommand,0x00,sizeof(szCommand));
	            sprintf(szCommand, "\\chmod %s \"%s\"", pMod,szMakePath);
	            system (szCommand);
            }
        }

        sprintf(szMakePath, "%s/", szMakePath);
        if (strlen(szTestPath) == 0) break;
    }
}

/*---------------------------------------------------------------*/
/*	Function  : Local_Dir_Make                                   */
/*	Action    : LOCAL DIRECTORY  Make (mkdir xxx)                */
/*	Parameter : szMakeDir : Make 대상 Directory                  */
/*	Return    : 없슴                                             */
/*---------------------------------------------------------------*/
void 	Local_Dir_Make	( char *szMakeDir
						)
{
int 	nret1;
char 	szTestPath   [512];     /* Directory Check Work          */
char 	szMakePath   [512];     /* Check & Create 대상 Directory */
char 	szTempPath   [512];     /* Directory Check Work          */
char 	szCommand    [1024];

	memset(szTestPath , 0x00, sizeof(szTestPath));
	memset(szMakePath , 0x00, sizeof(szMakePath));
    sprintf(szTestPath, "%s", szMakeDir);
   
    if (cmp_right_char(szTestPath, 1, "/") == 0)
        sprintf(szTestPath, "%s", left_char(szTestPath, strlen(szTestPath)-1));
     
    while (1) {
        nret1 = Char_Check(szTestPath,"/");
        if (nret1 >= 0) {
           sprintf(szMakePath, "%s%s", szMakePath, left_char(szTestPath, nret1));
           sprintf(szTestPath, "%s"  , right_char(szTestPath,strlen(szTestPath)-nret1-1));
        }
        else {
           sprintf(szMakePath, "%s%s", szMakePath,  szTestPath);
           memset(szTestPath, 0x00, sizeof(szTestPath));
        }
        if (access(szMakePath, F_OK) < 0 && strlen(szMakePath) > 0) {
        	memset(szCommand,0x00,sizeof(szCommand));
            sprintf(szCommand, "\\mkdir \"%s\"", szMakePath);
            system (szCommand);
        }
        sprintf(szMakePath, "%s/", szMakePath);
        if (strlen(szTestPath) == 0) break;
    }
}


/*---------------------------------------------------------------*/
/*	Function  : eCAMS_Log                                        */
/*	Action    : eCAMS LOGGING                                    */
/*	Parameter : pLogDir  : Logging Directory                     */
/*	            pLogFile : Logging File                          */
/*	            pLogMsg  : Logging 내용                          */
/*	Return    : 없슴                                             */
/*---------------------------------------------------------------*/
void 	eCAMS_Log	( char *pLogDir
					, char *pLogFile
					, char *pLogMsg
					)
{
char 	szFile        [256];
char 	szSysTime      [16];
FILE 	*LogPtr;

	if (access(pLogDir, F_OK) != 0)
		Local_Dir_Make(pLogDir);

	Get_Sys_Time20(szSysTime);

	sprintf(szFile, "%s%s", pLogDir, pLogFile);
    LogPtr = fopen (szFile, "a+");
    if (LogPtr == (FILE *)NULL) {
    	printf("LOG FILE OPEN ERROR = [%s]\n", szFile);
    	return;
    }

    fprintf (LogPtr, "%s %s\n",szSysTime, pLogMsg);
    fflush  (LogPtr);
    fclose  (LogPtr);
}


/*---------------------------------------------------------------*/
/*	Function  : eCAMS_Logging                                    */
/*	Action    : eCAMS LOGGING                                    */
/*	Parameter : LogData : Logging Data                           */
/*	Return    : 없슴                                             */
/*---------------------------------------------------------------*/
void     eCAMS_Logging	( char *LogData
						)
{
FILE   *LogPtr;

	if (strlen(szgLogFile) == 0) {
		return;
	}

    LogPtr = fopen (szgLogFile, "a+");
    fprintf (LogPtr, "%s %s\n", szgLogAcptNo, LogData);
    fflush  (LogPtr);
    fclose  (LogPtr);
}


/*---------------------------------------------------------------*/
/*	Function  : ServerCmd                                        */
/*	Action    : Socket 명령 수행                                 */
/*	Parameter : *pRemoteIP : 서버주소                            */
/*	            nPort      : 서버 Socket Port                    */
/*	            *pJob      : 처리구분(단계)                      */
/*	            *pLocal    : Local  File                         */
/*	            *pRemote   : Remote File                         */
/*	            *pCommand  : 처리 명령문                         */
/*	            log       : 처리명령문 View 여부                */
/*	Return    : 처리결과 값                                      */
/*---------------------------------------------------------------*/
int		ServerCmd	( char *pRemoteIP
					, int  nPort
					, char *pJob
					, char *pLocal
					, char *pRemote
					, char *pCommand
					, int  log
					)
{

	CMD_INFO CmdInfo;
	InitCmdInfo(&CmdInfo);

	CmdInfo.nPort = nPort;
	strcpy(CmdInfo.szServerIP, pRemoteIP);
	strcpy(CmdInfo.szJobGub  , pJob     );
	strcpy(CmdInfo.szRemote  , pRemote  );
	strcpy(CmdInfo.szLocal   , pLocal   );
	strcpy(CmdInfo.szCommand , pCommand );
	Server_Cmd_JOB(&CmdInfo);

	if (log)
		LookCMD_INFO(&CmdInfo);

	if(strcmp(CmdInfo.szRstCond,"EROR") == 0)
		return 999;
	else
		return atoi(CmdInfo.szRstCond);
}

/*---------------------------------------------------------------*/
/*	Function  : NotUseDataTruncate                               */
/*	Action    : Character Truncate (Not Use Charater)            */
/*	Parameter : *StrBuf : 대상버퍼주소                           */
/*	            NotUse  : 대상문자열                             */
/*	Return    : 없슴                                             */
/*---------------------------------------------------------------*/
void 	NotUseDataTruncate	( char *StrBuf
							, int  NotUse
							)
{
char    *ptr1, *ptr2;

    /*
    ptr1 = StrBuf;
    ptr2 = strchr (StrBuf, NotUse);

    if (ptr2 == NULL) return;

    if (strlen (StrBuf) <= (int)(ptr2 - ptr1))
        StrBuf [strlen (StrBuf)] = 0x00;
    else
        StrBuf [(int)ptr2 - (int)ptr1] = 0x00;
	*/
	
	sprintf(StrBuf, "%s", trunc_char(StrBuf));
	
    return;
}

/*---------------------------------------------------------------*/
/*	Function  : NotUseDataTrunc                                  */
/*	Action    : Character Truncate (Not Use Charater)            */
/*	Parameter : *StrBuf : 대상버퍼주소                           */
/*	            NotUse  : 대상문자열                             */
/*	Return    : 없슴                                             */
/*---------------------------------------------------------------*/
void	NotUseDataTrunc	( char *StrBuf
						, int  NotUse
						)
{
char    *ptr1, *ptr2;

	#if 0
    ptr1 = StrBuf;
    ptr2 = strchr (StrBuf, NotUse);

    if (ptr2 == NULL) return;

    if (strlen (StrBuf) <= (int)(ptr2 - ptr1))
        StrBuf [strlen (StrBuf)] = 0x00;
    else
        StrBuf [(int)ptr2 - (int)ptr1] = 0x00;
	#endif

	sprintf(StrBuf, "%s", trunc_char(StrBuf));

    return;
}


/*---------------------------------------------------------------*/
/*	Function  : Trunc_NewLine                                    */
/*	Action    : Character Truncate (Not Use Charater)            */
/*	Parameter : *strValue : 대상버퍼주소                         */
/*	Return    : 없슴                                             */
/*---------------------------------------------------------------*/
void	Trunc_NewLine	( char *strValue
						)
{
	sprintf(strValue, "%s", rep_char(strValue, "\r\n", " "));
	sprintf(strValue, "%s", rep_char(strValue, "\r"  , " "));
	sprintf(strValue, "%s", rep_char(strValue, "\n"  , " "));
	sprintf(strValue, "%s", rep_char(strValue, "\t"  , " "));
	return;
}


/*---------------------------------------------------------------*/
/*	Function  : File_Merge_2                                     */
/*	Action    : FILE Merge (ADD) 처리                            */
/*	Parameter : OLD_File : 변경전파일                            */
/*	            NEW_File : 변경후파일                            */
/*	Return    : 1 : 정상                                         */
/*---------------------------------------------------------------*/
int		File_Merge_2	( char *OLD_File
						, char *NEW_File
						)
{

	FILE   *OLDPtr    ;                   /* OLD File Pointer    */
	FILE   *NEWPtr    ;                   /* NEW File Pointer    */
	int    LineCnt    ;                   /* OLD File Line Count */
	char   indat [256];                   /* Read Buffer         */

    LineCnt = 0;
    if ((NEWPtr = fopen(NEW_File,"r")) == (FILE *) NULL) {
    	printf(">>> NEW_File Read Open Error : [%s]\n", NEW_File);
        return (-1);
	}

	if ((OLDPtr = fopen(OLD_File,"a+")) == (FILE *) NULL) {
    	printf(">>> OLD_File Write Open Error : [%s]\n", OLD_File);
		return (-1);
	}

	while (fgets(indat, 256, NEWPtr) != (char *) NULL) {
		sprintf(indat, "%s", rep_char(indat, "\r\n", ""));
		sprintf(indat, "%s", rep_char(indat, "\n"  , ""));
		LineCnt++;
		fprintf(OLDPtr, "%s\n", indat);
	}

	if (LineCnt > 0) {
		fprintf(OLDPtr, "\n\n\n");
	}

	fclose (OLDPtr);
	fclose (NEWPtr);

	return (1);
}

/*---------------------------------------------------------------*/
/*	Function  : Msg_Merge                                        */
/*	Action    : 파일에 처리메세지 추가하기                       */
/*	Parameter : OLD_File : 파일명                                */
/*	            MergeMsg : 추가메시지 내용                       */
/*	Return    : 1 : 정상                                         */
/*---------------------------------------------------------------*/
int		Msg_Merge	( char *OLD_File
					, char *MergeMsg
					)
{
FILE   	*OLDPtr          ;                /* OLD File Pointer    */
int    	LineCnt          ;                /* OLD File Line Count */
char	indat       [256];                /* Read Buffer         */

    LineCnt = 0;
	if ((OLDPtr = fopen(OLD_File,"a+")) == (FILE *) NULL) {
   		printf(">>> Msg_Merge Read Open Error : [%s]\n", OLD_File);
		return (-1);
	}
	fprintf(OLDPtr, "%s\n", MergeMsg);
	fclose (OLDPtr);

	return (1);
}



/*---------------------------------------------------------------*/
/*	Function  : Convert_Path                                     */
/*	Action    : Directory명 변경                                 */
/*	Parameter : DirPath  : 변경전 디렉토리명                     */
/*	            HomePath : 치환기준명                            */
/*				NewHome  : 치환대상명                            */
/*				RstPath  : 변경후 디렉토리명                     */
/*	Return    : 없슴                                             */
/*---------------------------------------------------------------*/
void	Convert_Path	( char *DirPath
						, char *HomePath
						, char *NewHome
						, char *RstPath
						)
{
char	szHomePath		[256];
char	szNewHome		[256];

	memset(szHomePath, 0x00, sizeof(szHomePath));
	memset(szNewHome , 0x00, sizeof(szNewHome ));
	sprintf(szHomePath, "%s", HomePath);
	sprintf(szNewHome , "%s", NewHome );

	if (cmp_right_char(szHomePath, 1, "/") != 0)
		sprintf(szHomePath, "%s/", szHomePath);

	if (cmp_right_char(szNewHome, 1, "/") != 0)
		sprintf(szNewHome, "%s/", szNewHome);

	/*-----------------------------------------------------------*/
	/*	변경전 디렉토리가 없는경우 변경후 디렉토리명도 없다      */
	/*-----------------------------------------------------------*/
	if (strlen(DirPath) == 0) {
		memset(RstPath, 0x00, sizeof(RstPath));
		return;
	}

	/*-----------------------------------------------------------*/
	/*	치환기준명이 변경전디렉토리에 없거나 치환대상 디렉토리명 */
	/*	이 없으면 변경전을 그래로 변경후 디렉토리명으로 반환     */
	/*-----------------------------------------------------------*/
	if (memcmp(DirPath, szHomePath, strlen(szHomePath)) != 0 ||
		strlen(NewHome) == 0 ) {
		sprintf(RstPath, "%s", DirPath);
		return;
	}

	/*-----------------------------------------------------------*/
	/*	디렉토리명 변경                                          */
	/*-----------------------------------------------------------*/
	sprintf(RstPath, "%s%s", szNewHome, right_char(DirPath, strlen(DirPath) - strlen(szHomePath)));
	return;
}



/*---------------------------------------------------------------*/
/*	Function  : szg_chg                                          */
/*	Action    : tar 파일 변경                                    */
/*	Parameter : ntype  : 구분                                    */
/*	Return    : 0 : 정상, 1 : 오류                               */
/*---------------------------------------------------------------*/
int 	szg_chg	( int   ntype
				)
{
char 	newLocal                   [512];
char 	newRemote                  [512];
int 	nRet                            ;


	memset(newLocal , 0x00, sizeof(newLocal ));
	memset(newRemote, 0x00, sizeof(newRemote));

	strcpy(newLocal , szgLocal);
	strcpy(newRemote, szgRemote);

	if (ntype == 0) {
		nRet = Char_Check(newLocal,".tar");
		if (nRet < 0){
			fprintf(stderr," 1 szg_chg() .tar not found  =[%s][%d]\n",newLocal,nRet);
			return 1;
		}
		memset(szgLocal,0x00,dfFullPath);

		strncpy(szgLocal,newLocal,nRet);
		strcat(szgLocal,".idx");

		nRet = Char_Check(newRemote,".tar");

		if (nRet < 0){
			fprintf(stderr," 2 szg_chg() .tar not found  =[%s][%d]\n",newRemote,nRet);
			return 1;
		}
		memset(szgRemote,0x00,dfFullPath);

		strncpy(szgRemote,newRemote,nRet);
		strcat(szgRemote,".idx");
	}
	else{
		nRet = Char_Check(newLocal,".idx");
		if (nRet < 0){
			fprintf(stderr," 1 szg_chg() .idx not found  =[%s][%d]\n",newLocal,nRet);
			return 1;
		}
		memset(szgLocal,0x00,dfFullPath);

		strncpy(szgLocal,newLocal,nRet);
		strcat(szgLocal,".tar");

		nRet = Char_Check(newRemote,".idx");

		if (nRet < 0){
			fprintf(stderr," 2 szg_chg() .idx not found  =[%s][%d]\n",newRemote,nRet);
			return 1;
		}
		memset(szgRemote,0x00,dfFullPath);

		strncpy(szgRemote,newRemote,nRet);
		strcat(szgRemote,".tar");
	}

	return 0;
}


/*---------------------------------------------------------------*/
/*	Function  : Change_Rcv_Mode                                  */
/*	Action    : tar 파일 권한 변경                               */
/*	Parameter : ntype  : 구분                                    */
/*	Return    : 0 : 정상, 1 : 오류                               */
/*---------------------------------------------------------------*/
int		Change_Rcv_Mode	(int   nSockFd
						)
{
int  	nLng = 0                        ;
int 	nMakelng                        ;
int 	nWrtlng                         ;
char 	szgSystime                  [16];
char	*szMsgbuf                       ;
uchar	*szTmpbuf                       ;
int  	nRet                            ;
char 	strDir                   [dfDir];
char 	strTmp                    [1024];


    szTmpbuf = (uchar *) malloc (dfMaxBufSize);
    szMsgbuf = (char *) malloc (dfMaxMsqSize);

    /*-----------------------------------------------------------*/
    /* 중개 서버가 수신할 수 있도록  통신 Header 조립            */
    /*-----------------------------------------------------------*/
    memset (szMsgbuf, 0x00, dfMaxMsqSize);
    memset (szTmpbuf, 0x00, dfMaxBufSize);

	szg_chg(indexrcvchk);
	errno = 0;

	/*-----------------------------------------------------------*/
	/*	디렉토리 존재 여부 검사                                  */
	/*-----------------------------------------------------------*/
	memset(strDir, 0x00, sizeof(strDir));
	strcpy(strTmp, szgLocal);

	nRet = 0;
	nRet = Right_Char_Check(strTmp, '/');

	if (nRet >= 0) {
		sprintf(strDir, "%s", left_char(strTmp,nRet));
	}

	if (strlen(strDir) != 0 && access(strDir, F_OK) != 0) {
		Local_Dir_Make(strDir);
	}

	ngRcvFd = open (szgLocal, O_RDWR | O_CREAT | O_TRUNC, 0755);

	/*-----------------------------------------------------------*/
	/*	수신할 File을 Open하는데 이상이 생기면 리턴              */
	/*-----------------------------------------------------------*/
	if (ngRcvFd < 0) {
		ngErrNo = errno;
		free (szTmpbuf);
		free (szMsgbuf);
		return (0);
	}
	sprintf (szMsgbuf, "X%s", szgLocal);
	sprintf (&szMsgbuf [dfRemoteOffset], "%s", szgRemote);

	nLng = dfFullPath;
	ngFileOffset = 0L;
	ngProcStep = dfFirstChain;
	nMakelng = MakeCommHeader (CmdSendTrx, 0, ngProcStep, szTmpbuf, szMsgbuf, nLng);

    nWrtlng = SendDataToSock (nSockFd, szTmpbuf, nMakelng);
    free (szTmpbuf);
    free (szMsgbuf);
    return (nWrtlng);
}


/*---------------------------------------------------------------*/
/*	Function  : MD5SUM                                           */
/*	Action    : 파일의 MD5SUM 값 작성                            */
/*	Parameter : filename : 파일명                                */
/*	            md5val   : MD5SUM 값                             */
/*	Return    : 0 : 정상, 1 : 오류                               */
/*---------------------------------------------------------------*/
int 	MD5SUM	( char *filename
				, char *md5val
				)
{
char    szSysCmd                  [1024];
char	indat                     [1024];
int     nRet                            ;
int     gPid                            ;
char    szPid                      [256];
FILE    *SRCPTR                         ;

	gPid = getpid();
	sprintf(szPid, "%d", gPid);

	sprintf(szSysCmd, "ecams_md5sum '%s' > %s", filename, szPid);
    nRet = system(szSysCmd) / 256;
    if (nRet != 0) {
    	fprintf(stderr, "ecams_md5sum ERROR [%d] [%s]\n", nRet, szSysCmd);
		remove(szPid);
    	return 1;
    }
	
	if ((SRCPTR = fopen(szPid, "r")) == (FILE *) NULL) {
		fprintf(stderr, "ecams_md5sum Result File Open ERROR [%s]\n", szPid);
		remove(szPid);
		return 1;
	}

	if (fgets(indat, 1024, SRCPTR) == (char *) NULL) {
		fprintf(stderr, "ecams_md5sum Result File Read ERROR [%s]\n", szPid);
		remove (szPid);
		return 1;
	}

	sprintf(indat, "%s", rep_char(indat, "\r\n", ""));
	sprintf(indat, "%s", rep_char(indat, "\n", ""));
	NotUseDataTruncate(indat, 0x20);
	
	fclose (SRCPTR);
	remove(szPid);

    strcpy(md5val, indat);

    return 0;

}





/*---------------------------------------------------------------*/
/*	Function  : Conver_Date                                      */
/*	Action    : 일자의 변환 (formatted)                          */
/*	Parameter : SrcDate : 원시일자                               */
/*	            RstDate : 변환일자                               */
/*	Return    : NONE                                             */
/*---------------------------------------------------------------*/
void	Conver_Date	( char *SrcDate
					, char *RstDate
					)
{

	sprintf(RstDate, "%s"   ,          mid_char(SrcDate, 1, 4));
	sprintf(RstDate, "%s/%s", RstDate, mid_char(SrcDate, 5, 2));
	sprintf(RstDate, "%s/%s", RstDate, mid_char(SrcDate, 7, 2));

	return;

}


/*---------------------------------------------------------------*/
/*	Function  : Convert_WinDirPath                               */
/*	Action    : OS가 WINDOWS면 디렉토리명 변환 ('/' -> '\')      */
/*	Parameter : pSysOS  : 서버 OS 종류                           */
/*	            pSrcDir : 변환전 디렉토리명                      */
/*	            pDstDir : 변환후 디렉토리명                      */
/*	Return    : NONE                                             */
/*---------------------------------------------------------------*/
void	Convert_WinDirPath	( char *pSysOS
							, char *pSrcDir
							, char *pDstDir
							)
{
	/*-----------------------------------------------------------*/
	/*	OS종류별 디렉토리명 변환                                 */
	/*-----------------------------------------------------------*/
	strcpy(pDstDir, pSrcDir);

	if (strcmp(pSysOS, dfWINDOWS) == 0 ) {
		while (Char_Check(pDstDir, "\\") >= 0) {
			sprintf(pDstDir, "%s", rep_char(pDstDir,"\\","/"));
		}

		while (Char_Check(pDstDir,"/") >= 0) {
			sprintf(pDstDir, "%s", rep_char(pDstDir,"/","\\"));
		}

		while (Char_Check(pDstDir,"\\\\") >= 0) {
			sprintf(pDstDir, "%s", rep_char(pDstDir,"\\\\","\\"));
		}
	}

	return;

}


/*---------------------------------------------------------------*/
/*                E N D   O F   F I L E                          */
/*---------------------------------------------------------------*/
