/*-----------------------------------------------------------------
 ┌──────┬───────────────────────┐
 │ 프로그램명 │ ecams_gwsvr.c                                │
 ├──────┼───────────────────────┤
 │ 기      능 │ eCAMS 클라이언트와 통신하기위한 프로그램     │
 ├──────┼───────────────────────┤
 │ 작  성  일 │ 2011. 03. 03                                 │
 ├──────┼───────────────────────┤
 │ 작  성  자 │ 최   병   남                                 │
 └──────┴───────────────────────┘
-----------------------------------------------------------------*/

#include 	<ecamsapi.h>
#include 	<stdio.h>
#include 	<stdlib.h>
#include 	<errno.h>
#include 	<netdb.h>
#include 	<sys/types.h>
#include 	<netinet/in.h>
#include	<AES.h>

#ifdef _OSF_SOURCE
static  void sig_cld         (void);
#endif

/*---------------------------------------------------------------*/
/*       Socket Common  PROCEDURE  DEFINE                        */
/*---------------------------------------------------------------*/
#if defined(__STDC__) || defined(__cplusplus) || defined(__sun) || defined(_AIX)
void     ExitParentProc         (int );
void     DefineParentSignal     (void);
void     ExitChildProc          (int);
void     DefineChildSignal      (void);
void     HandleMainProc         (int);
int      GetFileInf             (char *);
#else
void     ExitParentProc         ();
void     DefineParentSignal     ();
void     ExitChildProc          ();
void     DefineChildSignal      ();
void     HandleMainProc         ();
int      GetFileInf             ();
#endif
int      WorkResultSendData 	(int , char *, int );
char     LogDir [256];


int     ProcFileRcv 			();
int     ProcFileInf 			();
int     ProcEnvGet  			();
int     ProcDECrypt 			();
int     ServerAcceptClient      ();
void    DumpData            	();


extern int      DisConnectSocket        ();
extern int      MakeCommHeader          ();
extern int      SendDataToSock          ();
extern int      Rcv_Data_Sock           ();
extern int      ReadDataFromSock		();
extern void		Get_Sys_Time10          ();
extern int      FileDataSend            ();
extern ulong	CvtIPchr2ulong          ();
extern int		ConnectClient2Server    ();
extern void		Get_Sys_Date            ();
extern void		Get_Sys_Time            ();
extern int  	Char_Check              ();
extern char 	*left_char              ();
extern char 	*right_char             ();
extern void     ExitProc                ();
extern void     DefineSignal            ();
extern int      ProcRecvData            ();
extern int      ProcSystemCmd           ();
extern int      ProcReadData            ();
extern void     NotUseDataTruncate      ();
extern int   	CheckProcMsgQ           ();
extern int      ServerSocketInit        ();
extern int		Right_Char_Check        ();


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
/*       Local Directory Make                                    */
/*---------------------------------------------------------------*/
void Local_Dir_Make(char *szMakeDir)
{
char szCommand[200];

    if (access(szMakeDir, F_OK) < 0 && strlen(szMakeDir) > 0) {
        sprintf(szCommand, "\\mkdir -p \"%s\"", szMakeDir);
		system (szCommand);
    }

}


/*---------------------------------------------------------------*/
/*   SOCKET 통신관련  CONSTANT                                   */
/*---------------------------------------------------------------*/
int      PSock            ;        /* Connect ID                 */
int      CSock            ;        /* Connect Detail ID          */
int  	 gRcvFd           ;        /* 수신파일작성용 Pointer     */
int  	 gSndFd           ;        /* 전송파일용 Pointer         */
int  	 gProcStep        ;        /* 파일송수신 단계 Work       */
int  	 gFileOffset      ;        /* 파일 송신 OffSet           */
int  	 gProcWork        ;        /* 파일 송신/수신 여부        */
char     statbuf      [80];        /* 처리상태 Work              */
int      gFileSize        ;        /* File Size                  */
char     gFileDate    [20];        /* File Date                  */
char     gFileOwner   [20];        /* File Owner                 */
char     gFileGroup   [20];        /* File Owner Group           */
char     gFileMode     [5];        /* File Access Mode           */
char     gSvrIP       [20];        /* Server IP                  */
struct   stat  f_st       ;        /* File 상태 LayOut           */
struct   tm   *loc        ;        /* File 시간                  */
char	 tarFilename	[512];
char	 idxFilename	[512];

int		 indexrcvchk;
int		 gFileSendflag;
int    	 myerrno;                  /* 시스템 오류코드            */
const char *String;


/*---------------------------------------------------------------*/
/* 로그 작성												     */
/*---------------------------------------------------------------*/
char gszLogMsg[1024];
void Agent_log(char *pMessage)
{
	FILE * fp;
	char szDate[20];
	char szTime[20];
	char filename[80];
	char szCmd[1024];

	return;

	Get_Sys_Date(szDate);
    memset (filename, 0x00, sizeof (filename));

	if (strcmp(LogDir, "") == 0 ) {
		if(access("./AgentLog",F_OK) != 0) {
			system("mkdir ./AgentLog");
			system("chmod 777 ./AgentLog");
		}
		sprintf (filename, "./AgentLog/%s.%d.ecams_svr.log",szDate,dfSendPort);
	}
	else {
		if (access(LogDir,F_OK) != 0) {
			sprintf(szCmd, "mkdir %s", LogDir);
			system (szCmd);
			sprintf(szCmd, "chmod 777 %s", LogDir);
			system (szCmd);
		}
		sprintf (filename, "%s/%s.%d.ecams_svr.log",LogDir, szDate,dfSendPort);
	}

	fp = fopen (filename, "a+");
	if(fp == NULL) {
		return;
	}

	Get_Sys_Time(szTime);
	fprintf(fp, "[%s %s]	%s",szDate,szTime,pMessage);
	fclose(fp);
	sprintf(szCmd,"chmod 666 %s",filename);
	system(szCmd);

}

/*---------------------------------------------------------------*/
/*     Signal Define                                             */
/*---------------------------------------------------------------*/
void ExitParentProc (sig_num)
int sig_num;
{
    DisConnectSocket (PSock);

    if (TRACE) {
       fprintf (TRACE, "중계수신 서버 Exit Sig=%d\n", sig_num);
       fflush (TRACE);
    }

    exit (0);
}

#ifdef _OSF_SOURCE
static void sig_cld ()
{
    int pid, status;

    while ((pid = wait3 (&status, WNOHANG, (struct rusage *)0)) > 0)   ;
}
#endif


void DefineParentSignal ()
{

	int sig_num;

    for (sig_num = 1; sig_num < 32; sig_num++)
         signal (sig_num,  SIG_IGN);


#ifdef _OSF_SOURCE
    signal (SIGCLD, sig_cld);
#else
 	signal (SIGCHLD,SIG_IGN);
#endif


    signal (SIGBUS,  ExitParentProc);
    signal (SIGSEGV, ExitParentProc);
    signal (SIGSYS,  ExitParentProc);
    signal (SIGKILL, ExitParentProc);
    signal (SIGINT,  ExitParentProc);
    signal (SIGTERM, ExitParentProc);


    return;
}

void ExitChildProc (sig_num)
int sig_num;
{
    DisConnectSocket (CSock);

    if (TRACE) {
       fprintf (TRACE, "ExitProc : signal = %d\n", sig_num);
       fflush (TRACE);
       fclose (TRACE);
    }

    exit (0);
}

void DefineChildSignal ()
{
    int    sig_num;

    for (sig_num = 0; sig_num < 32; sig_num++)
         signal (sig_num,  SIG_IGN);

    signal (SIGBUS,  ExitChildProc);
    signal (SIGSEGV, ExitChildProc);
    signal (SIGSYS,  ExitChildProc);
    signal (SIGKILL, ExitChildProc);
    signal (SIGINT,  ExitChildProc);
    signal (SIGTERM, ExitChildProc);
    signal (SIGPIPE, ExitChildProc);

    return;
}


/*---------------------------------------------------------------*/
/*      Socket 처리결과 수신                                     */
/*---------------------------------------------------------------*/
void HandleMainProc (Sock)
int Sock;
{
int      readlng;                          /* Read 길이          */
char     systime [16];                     /* 시스템 시간        */
u_char	 *readbuf;							/* Read Buffer        */


	readbuf = (uchar *)malloc(dfMaxBufSize);

    gProcStep = dfLastChain;

	/*-----------------------------------------------------------*/
    /* 작업 결과를 받아야 하므로 해당 Flag Reset                 */
	/*-----------------------------------------------------------*/
    gProcWork = 0;
	indexrcvchk = 0;
	gFileSendflag = 0;
    while (1) {
        memset (readbuf, 0x00, dfMaxBufSize);

		myerrno = 0;
        readlng = ReadDataFromSock (Sock, readbuf);
        if (readlng > 0) {
			if (ProcReadData  (Sock, readbuf, readlng) < 0)  {
				free(readbuf);
				return ;
            }
        }
        else if (readlng < 0)   {
            Get_Sys_Time10 (systime);
            free(readbuf);
            return;
        }

        if (gProcStep != dfLastChain && gProcWork == 'G') {
            FileDataSend (Sock);
        }
        if (gProcStep != dfLastChain && gProcWork == 'X' && indexrcvchk == 1 && gFileSendflag == 1) {
        	FileDataSend (Sock);
        }
    }

    free(readbuf);
}




/*****************************************************************/
/*                                                               */
/*       Socket  통신  Server  Main                              */
/*                                                               */
/*****************************************************************/
int	main (int argc,char ** argv)
{
	char        filename[80];  /* Logging File Name */
	int         forkpid ;      /* Fork Process ID   */
	int ret = 0;
	char tmpLogDir [100];

   	sprintf (filename, "%s.log",argv[0]);

	#if 0
    TRACE = fopen (filename, "w+");
    if (TRACE == NULL)  {
        fprintf (stderr, "%s :  file Open Err\n", filename);
        fflush  (stderr);
        exit (0);
    }
	#endif


    DefineParentSignal ();

	if (argc == 1)	{
		dfSendPort = dfeCAMSFepPort;
		strcpy(LogDir , "./AgentLog");
    }
    else
    if (argc == 2 ) {
		dfSendPort = atoi(argv[1]);
		strcpy(LogDir , "./AgentLog");
    }
    else
    if (argc == 3 ) {
		dfSendPort = atoi(argv[1]);
		strcpy(LogDir , argv[2]);
	}

    PSock = ServerSocketInit (dfSendPort);
    if (TRACE) {
       fprintf (TRACE, "중개 Recv 서버 Portnum = %d \n", dfSendPort);
       fflush (TRACE);
    }

	memset(gSvrIP, 0x00, sizeof(gSvrIP));
    while (1) {
        CSock = ServerAcceptClient (PSock);

        if (TRACE) {
           fprintf (TRACE, "Client Connect Server :: SockID = %d\n", CSock);
           fflush (TRACE);
        }

        if (CSock < 0) {
            continue;
        }

        if ((forkpid = fork()) < 0) {
            continue;
        }
        else if (!forkpid) {
            close (PSock);
            HandleMainProc (CSock);
            close (CSock);

            ExitChildProc (0);

            return (1);
        }
        else
            close (CSock);
    }

}


/*---------------------------------------------------------------*/
/*      Read 요구 파일 전송                                      */
/*---------------------------------------------------------------*/
int   ProcFileSnd (SockFd, RdBuf, RdLng)
int    SockFd;
char  *RdBuf;
int    RdLng;
{
int    lng = 0, makelng, wrtlng, fname_offset; /* File Name 위치 */
char   systime  [16];                          /* 시스템 시간    */
char   filename [512];                         /* 송신 File Name */

    if (TRACE) {
       fprintf (TRACE, "Client에서 File Get요청\n");
       fflush  (TRACE);
    }

    fname_offset = sizeof (BpComHead) + dfRemoteOffset;

    /*-----------------------------------------------------------*/
    /*   송신할 File을 Open하는데 이상이 생기면 리턴 .           */
    /*-----------------------------------------------------------*/
    memset (filename, 0x00, sizeof (filename));
    sprintf (filename, "%s", &RdBuf [fname_offset]);

    if (TRACE) {
       fprintf (TRACE, "PUT File = |%s|\n", filename);
       fflush  (TRACE);
    }

	/*LOG*/sprintf(gszLogMsg,"START	SEND FILE[%s][%d] \n",filename,gSndFd);
	Agent_log(gszLogMsg);

    gSndFd = open (filename, O_RDONLY);
    if (gSndFd < 0) {
        memset (statbuf, 0x00, sizeof(statbuf));
        WorkResultSendData (SockFd, statbuf, dfCmd_EROR);
        return (-1);
    }

    gFileOffset = 0L;
    gProcStep = dfFirstChain;
    gFileSendflag = 1;

    return (1);
}



/*---------------------------------------------------------------*/
/*       Socket Data Receive                                     */
/*---------------------------------------------------------------*/
int    ProcReadData  (Sock, RdBuf, RdLng)
int    Sock;
uchar *RdBuf;
int    RdLng;
{
char   SysCmd [100];
char   tmpBuf [256];
ulong  ul_servip;                     /* Server IP의 Convert IP  */
FILE   *IPPtr      ;

BpComHead *uhead = (BpComHead *) RdBuf;



	myerrno = 0;
    switch (uhead->Cmd) {
        case CmdSendTrx    :
            if ((gProcWork == 'F') ||
                (RdBuf [sizeof (BpComHead)] == 'F' && uhead->CF == dfFirstChain))  {
                /*-----------------------------------------------*/
                /* Client to Server File Put                     */
                /* Client에서 Server로 File Data 송신시          */
                /* MiddleChain부터는 첫번째버퍼가 'F'가 아니므로 */
                /* 이부분 보완                                   */
                /*-----------------------------------------------*/
                gProcWork = 'F';
                return(ProcFileRcv (Sock, RdBuf, RdLng));
            }
            else if ((gProcWork == 'G') ||
                     (RdBuf [sizeof (BpComHead)] == 'G' && uhead->CF == dfFirstChain)) {
                gProcWork = 'G';
                return (ProcFileSnd (Sock, RdBuf, RdLng));
            }
            else if ((gProcWork == 'Z') ||
                (RdBuf [sizeof (BpComHead)] == 'Z' && uhead->CF == dfFirstChain))  {
                /*-----------------------------------------------*/
                /* Client to Server File Put                     */
                /* Client에서 Server로 File Data 송신시          */
                /* MiddleChain부터는 첫번째버퍼가 'F'가 아니므로 */
                /* 이부분 보완                                   */
                /*-----------------------------------------------*/
                gProcWork = 'Z';
                return(ProcFileRcv (Sock, RdBuf, RdLng));
            }
            else if ((gProcWork == 'X') ||
                     (RdBuf [sizeof (BpComHead)] == 'X' && uhead->CF == dfFirstChain)) {

                gProcWork = 'X';

                if (indexrcvchk == 0){
                	return(ProcFileRcv (Sock, RdBuf, RdLng));
                }
                else{
                	return (ProcFileSnd (Sock, RdBuf, RdLng));
                }
            }

            break;

        case CmdSystem    :
            gProcWork = 'S';
            return (ProcSystemCmd (Sock, RdBuf, RdLng));

		case CmdDECrypt   :
            gProcWork = 'D';
            return (ProcDECrypt (Sock, RdBuf, RdLng));

        case CmdFileInf   :
            gProcWork = 'I';
            return (ProcFileInf (Sock, RdBuf, RdLng));

		case CmdEnvGet    :
            gProcWork = 'U';
            return (ProcEnvGet (Sock, RdBuf, RdLng));

        case CmdBpExit  :
            ExitParentProc (0);
            break;
    }

    return (0);

}

/*---------------------------------------------------------------*/
/*       시스템 명령 처리요구                                    */
/*---------------------------------------------------------------*/
int     ProcSystemCmd (Sock, RdBuf, RdLng)
int     Sock;
uchar  *RdBuf;
int     RdLng;
{

int 	retval, hdlng, pulng;  /* 처리결과, Header길이, Data길이 */
char 	*tmpbuf;                         /* 명령처리 Buffer */
char	*decbuf;
u8	szKey[16] = { 0x06, 0xa9, 0x21, 0x40, 0x36, 0xb8, 0xa1, 0x5b,
					  0x51, 0x2e, 0x03, 0xd5, 0x34, 0x12, 0x00, 0x06 };
u8	szIV[16] =  { 0x3d, 0xaf, 0xba, 0x42, 0x9d, 0x9e, 0xb4, 0x30,
					  0xb4, 0x22, 0xda, 0x80, 0x2c, 0x9f, 0xac, 0x41 };
int		ncount;
int		i;

	BpComHead *uhead = (BpComHead *) RdBuf;

    hdlng = sizeof (BpComHead);
    pulng = RdLng - hdlng;

    /*-----------------------------------------------------------*/
    /* 	수행할 System 명령을 Get.                                */
    /*-----------------------------------------------------------*/
    tmpbuf = (char *) malloc (dfeCAMSBufSize);

    memset (tmpbuf, 0x00, dfeCAMSBufSize);

    memcpy (tmpbuf, &RdBuf[hdlng], pulng);


	decbuf = (char *) malloc (dfeCAMSBufSize);
	memset (decbuf, 0x00, dfeCAMSBufSize);


	ncount = 0;

	for (i = 0; i< dfeCAMSBufSize;i++){
		if (&tmpbuf[i] == 0x00){
			break;
		}
		else{
			ncount++;
		}
	}


	memcpy(decbuf,tmpbuf,ncount);


	aes_128_cbc_decrypt(szKey, szIV, decbuf, ncount);

    myerrno = 0;

    /*-----------------------------------------------------------*/
    /*   ret가 0 이면 성공                                       */
    /*-----------------------------------------------------------*/
    retval = system (decbuf) / 256;
	myerrno = retval;

	free(decbuf);

    memset (tmpbuf, 0x00, dfeCAMSBufSize);
    if (!retval) {                                /* 성공한 경우 */
        WorkResultSendData (Sock, tmpbuf, dfCmd_DAOK);
        free(tmpbuf);
        return (0);
    }
    else
	{
        WorkResultSendData (Sock, tmpbuf, dfCmd_EROR);
        free(tmpbuf);
        return (1);
    }
}



/*---------------------------------------------------------------*/
/*       시스템 명령 처리요구                                    */
/*---------------------------------------------------------------*/
int     ProcDECrypt (Sock, RdBuf, RdLng)
int     Sock;
uchar  *RdBuf;
int     RdLng;
{
int 	retval, hdlng, pulng;  /* 처리결과, Header길이, Data길이 */
char 	tmpbuf [256];                         /* 명령처리 Buffer */
char	sPID    [50];

FILE	*SrcPtr     ;

BpComHead *uhead = (BpComHead *) RdBuf;

    hdlng = sizeof (BpComHead);
    pulng = RdLng - hdlng;

    /*-----------------------------------------------------------*/
    /* 	수행할 System 명령을 Get.                                */
    /*-----------------------------------------------------------*/
    memset (tmpbuf, 0x00, sizeof (tmpbuf));
    memcpy (tmpbuf, &RdBuf[hdlng], pulng);
    myerrno = 0;
    sprintf(sPID, "%d", getpid());

	sprintf(tmpbuf, "%s %s", tmpbuf, sPID);

    /*-----------------------------------------------------------*/
    /*   ret가 0 이면 성공                                       */
    /*-----------------------------------------------------------*/
    retval = system (tmpbuf);
    memset (tmpbuf, 0x00, sizeof (tmpbuf));


    if (!retval) {                                /* 성공한 경우 */
    	SrcPtr = fopen(sPID, "r");
    	if (SrcPtr == NULL)
    	{
    		WorkResultSendData (Sock, tmpbuf, dfCmd_EROR);
    		return (1);
    	}

    	if (fgets(tmpbuf, 256, SrcPtr) == (char *) NULL)
    	{
    		memset(tmpbuf, 0x00, sizeof(tmpbuf));
    		WorkResultSendData (Sock, tmpbuf, dfCmd_EROR);
    		return (1);
    	}
    	fclose (SrcPtr);
    	remove (sPID);

        WorkResultSendData (Sock, tmpbuf, dfCmd_DAOK);
        return (0);
    }
    else {
        WorkResultSendData (Sock, tmpbuf, dfCmd_EROR);
        return (1);
    }
}



/*---------------------------------------------------------------*/
/*    FILE Information Read                                      */
/*---------------------------------------------------------------*/
int     ProcFileInf (Sock, RdBuf, RdLng)
int     Sock;
uchar  *RdBuf;
int     RdLng;
{
int 	retval, hdlng, pulng; /* 명령처리결과, Header길이, Data길이 */
char 	tmpbuf [256];                            /* 명령처리 Buffer */
BpComHead *uhead = (BpComHead *) RdBuf;

    hdlng = sizeof (BpComHead);
    pulng = RdLng - hdlng;

    memset (tmpbuf, 0x00, sizeof (tmpbuf));
    memcpy (tmpbuf, &RdBuf[hdlng], pulng);

    /*-----------------------------------------------------------*/
    /*    File Information Read                                  */
    /*-----------------------------------------------------------*/
    retval = GetFileInf(tmpbuf);

    memset (tmpbuf, 0x00, sizeof (tmpbuf));
    if (!retval) {                                /* 성공한 경우 */
        WorkResultSendData (Sock, tmpbuf, dfCmd_DAOK);
        return (0);
    }
    else {
        WorkResultSendData (Sock, tmpbuf, dfCmd_EROR);
        return (1);
    }
}




/*---------------------------------------------------------------*/
/*       변경관리 DB Server UserID/PassWord INFORMATION READ     */
/*---------------------------------------------------------------*/
int     ProcEnvGet (Sock, RdBuf, RdLng)
int     Sock;
uchar  *RdBuf;
int     RdLng;
{
int 	retval, hdlng, pulng; /* 명령처리결과,Header길이,Data길이*/
char 	tmpbuf [256];                         /* 명령처리 Buffer */
char    userid [50] ;
char    *ret        ;
FILE    *infPtr     ;
char 	tmpFile[256];
char    tmpSize [20];

BpComHead *uhead = (BpComHead *) RdBuf;

    hdlng = sizeof (BpComHead);
    pulng = RdLng - hdlng;

    memset (tmpbuf, 0x00, sizeof (tmpbuf));
    memcpy (tmpbuf, &RdBuf[hdlng], pulng);

	sprintf(tmpbuf, "DBINF=%s/%s", getenv("DBUSER"), getenv("DBPASS"));
    WorkResultSendData (Sock, tmpbuf, dfCmd_DAOK);
    return (0);
}


/*---------------------------------------------------------------*/
/*   FILE  Information  GET   Procedure                          */
/*---------------------------------------------------------------*/
int     GetFileInf(char *FileName)
{
struct   group   *gr_p;
struct   passwd  *pw_p;

char  amode        [10];
char  rmode         [4];
int   j, i, k          ;
char  tmpchar       [2];
char  xtbl  [10] = "421421421";


   if (stat(FileName, &f_st) < 0)
      return (1);

   gFileSize = f_st.st_size;

   loc = localtime(&f_st.st_mtime);
   sprintf(gFileDate, "%d%002d%002d%002d%002d%002d",
                     loc->tm_year + 1900, loc->tm_mon + 1, loc->tm_mday,
                     loc->tm_hour, loc->tm_min, loc->tm_sec);

   pw_p = getpwuid(f_st.st_uid);
   gr_p = getgrgid(f_st.st_gid);
   myerrno = 0;

   sprintf(gFileOwner, "%s", pw_p->pw_name);
   sprintf(gFileGroup, "%s", gr_p->gr_name);
   memset(tmpchar, 0x00, sizeof(tmpchar));
   memset(rmode, 0x00, sizeof(rmode));

   for (i = 0, j = (1 << 8); i < 9; i++, j >>= 1)
       amode[i] = (f_st.st_mode & j) ? xtbl[i]: '0';

   for (i = 0; i < 3; i++) {
   	   k = 0;
   	   for (j = 0; j < 3; j++) {
   	   	   strncpy(tmpchar, &amode[i * 3 + j], 1)	;
   	   	   k += atoi(tmpchar)	;
   	   }
   	   sprintf(rmode, "%s%d", rmode, k);
   }

   memset(gFileMode, 0x00, sizeof(gFileMode));
   sprintf(gFileMode, "%s", rmode);

   return (0);

}


/*---------------------------------------------------------------*/
/*      파일 송신 처리요구시 파일 수신 처리                      */
/*---------------------------------------------------------------*/
int     ProcFileRcv (Sock, RdBuf, RdLng)
int     Sock;
uchar  *RdBuf;
int     RdLng;
{
	int     ret, hdlng, pulng; /* 처리결과,Header 길이,Data 길이 */
	char    filename [512];                /* 송수신 File Name   */
	char 	tmpbuf    [80];                /* 결과 전송용 Buffer */
	int     nRet,nStrLen ;
	char    *strDir;
	char    *strTmp;

	BpComHead *uhead = (BpComHead *) RdBuf;

    hdlng = sizeof (BpComHead);
    pulng = RdLng - hdlng;

	switch (uhead->CF)
	{
	case  dfFirstChain   :
		/*-------------------------------------------------------*/
		/* File 송신의 시작을 알리는 정보인 경우 먼저 저장할     */
		/* File명을 얻어와서 생성 RemoteFile Name으로 저장.      */
		/*-------------------------------------------------------*/
		memset (filename, 0x00, sizeof (filename));
		sprintf (filename, "%s", &RdBuf [hdlng+dfRemoteOffset]);

		if (gProcWork == 'Z'){
			memset(tarFilename,0x00,sizeof(tarFilename));
			strcpy(tarFilename,filename);
		}
		if (gProcWork == 'X'){
			memset(idxFilename,0x00,sizeof(idxFilename));
			strcpy(idxFilename,filename);
		}
		/*-------------------------------------------------------*/
		/* 디렉토리가 없을 경우 디렉토리 생성후 파일 생성        */
		/* 1. 디렉토리와 파일명 분리                             */
		/*-------------------------------------------------------*/

		nRet = 0;

		nStrLen = strlen(filename);
		strDir = (char *)malloc(nStrLen);
		strTmp = (char *)malloc(nStrLen);

		memset(strDir,0x00,sizeof(char)*nStrLen);
		memset(strTmp,0x00,sizeof(char)*nStrLen);
		strcpy(strTmp,filename);

		nRet = -1;
		nRet = Right_Char_Check(strTmp,(char)'/');

		if (nRet != -1)
			strncpy(strDir,strTmp,nRet);


		/*-------------------------------------------------------*/
		/* 2. 디렉토리 존재 여부 검사                            */
		/*-------------------------------------------------------*/
		if(strlen(strDir) != 0 && access(strDir,F_OK) != 0)
		{
			sprintf(gszLogMsg,"No Such Directory [%s][%d] \n",filename, nRet);
			Agent_log(gszLogMsg);
			/*return(-1);*/
			sprintf(gszLogMsg,"Make Directory!!!! [%s][%d] \n",strDir, nRet);
			Agent_log(gszLogMsg);
			Local_Dir_Make(strDir);
		}


		gRcvFd = open (filename, O_RDWR | O_CREAT | O_TRUNC,0666);
		if (gRcvFd < 0) {
		  memset (statbuf, 0x00, sizeof(statbuf));
		  WorkResultSendData (Sock, statbuf, dfCmd_EROR);
		}

		/*LOG*/sprintf(gszLogMsg,"START	RECEIVE FILE [%s][%d] \n",filename, gRcvFd);
		Agent_log(gszLogMsg);
		free(strDir);
		free(strTmp);
		if (gRcvFd < 0)
			return (-1);

		break;

	case  dfMiddleChain  :
		/*-------------------------------------------------------*/
		/* File의 실제 내용을 보내오는 것으로 File의 마지막      */
		/* 위치에 Write.                                         */
		/*-------------------------------------------------------*/
		lseek (gRcvFd, 0L, SEEK_END);
		write (gRcvFd, &RdBuf[hdlng], pulng);
		break;

	case  dfLastChain    :
		/*-------------------------------------------------------*/
		/* File의 마지막 내용을 보내오는 것으로 실제 저장할      */
		/* 내용이 있는 경우를 Check하여 Write하고 그렇지 않은    */
		/* 경우는 FD를 Close만 한다.                             */
		/*-------------------------------------------------------*/
		if (pulng > 0) {
		  lseek (gRcvFd, 0L, SEEK_END);
		  write (gRcvFd, &RdBuf[hdlng], pulng);
		}

		close (gRcvFd);

		if (gProcWork == 'Z'){
			tarFileMake_ext_Z(tarFilename);
		}

		if (gProcWork == 'X'){
			tarFileMake_comp_X(idxFilename);
			indexrcvchk = 1;
		}

		memset (tmpbuf, 0x00, sizeof (tmpbuf));
		WorkResultSendData (Sock, tmpbuf, dfCmd_DAOK);
		break;
	}

    return (1);
}

/*---------------------------------------------------------------*/
/*      송신 데이터를 수신하는 Procedure                         */
/*---------------------------------------------------------------*/
int     ProcRecvData (Sock, RdBuf, RdLng)
int     Sock;
uchar  *RdBuf;
int     RdLng;
{
int 	ret, hdlng, pulng;     /* 처리결과, Header길이, Data길이 */
char 	filename     [512];                /* 송수신 File Name   */
char 	tmpbuf        [80];                /* 결과 전송용 Buffer */
BpComHead *uhead = (BpComHead *) RdBuf;

    hdlng = sizeof (BpComHead);
    pulng = RdLng - hdlng;

	switch (uhead->CF) {
	    case  dfFirstChain   :
              /*-------------------------------------------------*/
              /* File 송신의 시작을 알리는 정보인경우 먼저저장할 */
              /* File명을 얻어와서 생성 RemoteFile Name으로 저장.*/
              /*-------------------------------------------------*/
              memset (filename, 0x00, sizeof (filename));
              sprintf (filename, "%s", &RdBuf [hdlng+dfRemoteOffset]);
              gRcvFd = open (filename, O_RDWR | O_CREAT | O_TRUNC, 0777);

              /*-------------------------------------------------*/
              /*    Open Error시 오류전송                        */
              /*-------------------------------------------------*/
			  if (gRcvFd < 0) {
                  memset (tmpbuf, 0x00, sizeof (tmpbuf));
                  WorkResultSendData (Sock, tmpbuf, dfCmd_EROR);
                  return (-1);
              }

              break;

	    case  dfMiddleChain  :
              /*-------------------------------------------------*/
              /* File의 실제 내용을 보내오는 것으로 File의 마지막*/
              /* 위치에 Write.                                   */
              /*-------------------------------------------------*/
              lseek (gRcvFd, 0L, SEEK_END);
              write (gRcvFd, &RdBuf[hdlng], pulng);
              break;

	    case  dfLastChain    :
              /*-------------------------------------------------*/
              /* File의 마지막 내용을 보내오는 것으로 실제저장할 */
              /* 내용이 있는 경우를 Check하여 Write하고 그렇지   */
              /* 않은 경우는 FD를 Close만 한다.                  */
              /*-------------------------------------------------*/
              if (pulng > 0) {
                  lseek (gRcvFd, 0L, SEEK_END);
                  write (gRcvFd, &RdBuf[hdlng], pulng);
              }
              close (gRcvFd);

              memset (tmpbuf, 0x00, sizeof (tmpbuf));
              uhead = (BpComHead *) tmpbuf;
              strncpy((char *)uhead->IPAddr, gSvrIP, 10);
              WorkResultSendData (Sock, tmpbuf, dfCmd_DAOK);

              break;
	}

    return (1);
}


/*---------------------------------------------------------------*/
/*      파일수신요구시  해당 파일의 DATA 전송                    */
/*---------------------------------------------------------------*/
int   FileDataSend (SockFd)
int   SockFd;
{
int     lng = 0, makelng, wrtlng; /* 처리결과, Header길이, Data길이 */
uchar   *tmpbuf;
uchar	*msgbuf;
char	szCommand[1024];
char	szDelPath[512];
int		nnret;



BpComHead *chead;


    tmpbuf = (uchar *) malloc (dfMaxBufSize);
	msgbuf = (uchar *) malloc (dfMaxMsqSize);

    lseek (gSndFd, gFileOffset, SEEK_SET);

    memset (tmpbuf, 0x00, dfMaxBufSize);
     memset (msgbuf, 0x00, dfMaxMsqSize);

    lng = read (gSndFd, msgbuf, dfMaxFileSize);

    gFileOffset += lng;
    if (lng < dfMaxFileSize)
	{
        gProcStep = dfLastChain;
	}
    else
        gProcStep = dfMiddleChain;

    makelng = MakeCommHeader (CmdSendTrx, 0, gProcStep, tmpbuf, msgbuf, lng);

    chead = (BpComHead *) tmpbuf;
    strncpy((char *)chead->IPAddr, gSvrIP, 10);

    wrtlng = SendDataToSock (SockFd, tmpbuf, makelng);

    if (gProcStep == dfLastChain){
#if 0
    	if (gProcWork == 'X'){
    		memset(szDelPath,0x00,sizeof(szDelPath));
    		memset(szCommand,0x00,sizeof(szCommand));
    		nnret = Right_Char_Check(idxFilename,'/');
    		if (nnret > 5){
    			strncpy(szDelPath,idxFilename,nnret);
    			sprintf(szCommand,"\\rm -rf \"%s\"",szDelPath);
    			system(szCommand);
			}
		}
#endif

		close(gSndFd);
	}

	free(tmpbuf);
	free(msgbuf);


    return (wrtlng);
}



/*---------------------------------------------------------------*/
/*      처리결과 전송                                            */
/*---------------------------------------------------------------*/
int  WorkResultSendData (int SockFd, char *RdBuf, int WorkRst)
{
int        wrtlng;
int        sndlng;
char       SndMsg  [256];

BpComHead *chead;

    memset(SndMsg, 0x00, sizeof(SndMsg));
    if (gProcWork == 'I')
       sprintf(SndMsg, "%s/%s/%s", gFileOwner, gFileGroup, gFileMode);

    else if (gProcWork == 'N')
       sprintf(SndMsg, "%s", RdBuf);

    else if (gProcWork == 'U')
       sprintf(SndMsg, "%s", RdBuf);

    else if (gProcWork == 'D')
       sprintf(SndMsg, "%s", RdBuf);

    gProcStep = dfMiddleChain;

    sndlng = MakeCommHeader (CmdBidMsg, 0, gProcStep, RdBuf, SndMsg, strlen(SndMsg));

    chead = (BpComHead *) RdBuf;

    if (gProcWork == 'I' ||
        gProcWork == 'N'  ) {
       memset (chead->FileSize, 0x00, sizeof(chead->FileSize));
       memset (chead->FileDate, 0x00, sizeof(chead->FileDate));

       sprintf((char *)chead->FileSize, "%011d", gFileSize);
       strcpy ((char *)chead->FileDate, gFileDate);
    }
    strncpy((char *)chead->IPAddr, gSvrIP, 10);

    /* Command Set */
    chead->Cmd    = CmdBidMsg;
    chead->EC     = WorkRst;
    chead->FepSts = myerrno;

	if (gProcWork == 'G') {
		/*LOG*/sprintf(gszLogMsg,"END   SEND FILE       FILE-HANDLE[%d] RST[%c] ERR[%d]\n", gSndFd, WorkRst, myerrno);
		Agent_log(gszLogMsg);
	}
	else
	if (gProcWork == 'F') {
		/*LOG*/sprintf(gszLogMsg,"END   RECEIVE FILE    FILE-HANDLE[%d] RST[%c] ERR[%d]\n", gRcvFd, WorkRst, myerrno);
		Agent_log(gszLogMsg);

	}
	else
	if (gProcWork == 'S') {
		/*LOG*/sprintf(gszLogMsg,"END	SYSTEMCALL     RST[%c] ERR[%d]\n",WorkRst, myerrno);
		Agent_log(gszLogMsg);
	}
	else
	if (gProcWork == 'Z') {
		/*LOG*/sprintf(gszLogMsg,"END   RECEIVE FILE    FILE-HANDLE[%d] RST[%c] ERR[%d]\n", gRcvFd, WorkRst, myerrno);
		Agent_log(gszLogMsg);

	}
	else
	if (gProcWork == 'X' && indexrcvchk == 0) {
		/*LOG*/sprintf(gszLogMsg,"END   RECEIVE FILE    FILE-HANDLE[%d] RST[%c] ERR[%d]\n", gRcvFd, WorkRst, myerrno);
		Agent_log(gszLogMsg);
	}
	else
	if (gProcWork == 'X' && indexrcvchk == 1) {
		/*LOG*/sprintf(gszLogMsg,"END   SEND FILE       FILE-HANDLE[%d] RST[%c] ERR[%d]\n", gSndFd, WorkRst, myerrno);
		Agent_log(gszLogMsg);
	}

    strcpy(&RdBuf[sizeof(BpComHead)], SndMsg);
    wrtlng = SendDataToSock (SockFd, RdBuf, sndlng);
    return (wrtlng);
}


/*---------------------------------------------------------------*/
/*                E N D   O F   F I L E                          */
/*---------------------------------------------------------------*/
