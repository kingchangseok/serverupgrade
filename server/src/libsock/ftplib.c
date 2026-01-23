
/*-----------------------------------------------------------------
 ┌──────┬───────────────────────┐
 │ 프로그램명 │ ftplib.c                                     │
 ├──────┼───────────────────────┤
 │ 기      능 │ FTP 처리 Module                              │
 ├──────┼───────────────────────┤
 │ 작  성  일 │ 2007. 10. 25                                 │
 ├──────┼───────────────────────┤
 │ 작  성  자 │ 최   병   남                                 │
 └──────┴───────────────────────┘
-----------------------------------------------------------------*/

/*---------------------------------------------------------------*/
/*     Header files                                              */
/*---------------------------------------------------------------*/
#include 	<ecamsapi.h>

/*---------------------------------------------------------------*/
/*       Socket 관련  PROCEDURE  DEFINE                          */
/*---------------------------------------------------------------*/
#if defined(__STDC__) || defined(__cplusplus) || defined(__sun) || defined(_AIX)
static int  Ftp_Cliconn    (char *, char *);
#ifdef MULTIARG
static int  Ftp_Command    (int , const char *, ...);
#else
static int  Ftp_Command    (int , char *);
#endif
static int  Ftp_Serconn    (int );
static FILE *Conn_Data     (int , char *);
static int  Get_Data       (int , int , int , char *, char *);
static int  Get_File_Size  (char *);
static int  Put_Data       (int , int , int , char *, char *);
static int  Del_Data       (int , int , int , char *);
static int  Dir_Get        (int , int , char *, char *);
int  JES_Conn              (int );
int  JES_DisConn           (int );
int  JES_Kor               (int , int );
int  OIC_FtpConnect        (char *, char *, char *);
int  OIC_FtpGetFile        (int , int , char *, char *, char *);
int  OIC_FtpGetJES         (int , int , char *, char *, char *);
int  OIC_FtpPutFile        (int , int , char *, char *, char *);
int  OIC_FtpPutJES         (int , int , char *, char *, char *);
int  OIC_FtpDelFile        (int , int , char *, char *, char *);
int  OIC_FtpDir            (int , char *, char *);
void OIC_FtpDisConnect     (int );
void OIC_FTPServerSet      (int );
void OIC_TNSServerSet      (int );
int  SocketFlag_Set        (int , int);
int	 FTPTRACE_Logging      (char *);
void OIC_SMSServerSet      (int );
#else
static int  Ftp_Cliconn    ();
static int  Ftp_Command    ();
static int  Ftp_Serconn    ();
static FILE *Conn_Data     ();
static int  Get_Data       ();
static int  Get_File_Size  ();
static int  Put_Data       ();
static int  Del_Data       ();
static int  Dir_Get        ();
int  JES_Conn       ();
int  JES_DisConn    ();
int  JES_Kor        ();
int  OIC_FtpConnect        ();
int  OIC_FtpGetFile        ();
int  OIC_FtpGetJES         ();
int  OIC_FtpPutFile        ();
int  OIC_FtpPutJES         ();
int  OIC_FtpDelFile        ();
int  OIC_FtpDir            ();
void OIC_FtpDisConnect     ();
void OIC_FTPServerSet      ();
void OIC_TNSServerSet      ();
int  SocketFlag_Set        ();
int	 FTPTRACE_Logging      ();
void OIC_SMSServerSet      ();
#endif


/*---------------------------------------------------------------*/
/*    User Work 변수                                             */
/*---------------------------------------------------------------*/
char     IBM_Server = OFF;
char     TNS_Server = OFF;
char     JES_Start  = OFF;
char     AIX_Server = OFF;
char     ErrBuf    [1024];
static
char	 LogData   [4096];
extern int errno;
static struct sockaddr_in Cli_addr;  /* Client 정보 */
static char CommandBuf[1024];        /* 명령에 대한응답을 가지는 buffer */

char     SMS_Server = OFF;


/*************************************************************
  name    :  OIC_SMSServerSet ()
  action  : 작업할 Server가 SMS Host인지
*************************************************************/
void    OIC_SMSServerSet (int  Flag)
{
    SMS_Server = Flag;

    return;
}


/*---------------------------------------------------------------*/
/*	Function  : OIC_FTPServerSet ()                              */
/*	Action    : 작업할 FTP Server가 IBM Host인지 일반 Unix인지   */
/*	            Set.                                             */
/*	Parameter : Flag : On/Off Flag                               */
/*	Return    : 없슴                                             */
/*---------------------------------------------------------------*/
void    OIC_FTPServerSet (int  Flag)
{
    IBM_Server = Flag;

    FTPTRACE_Logging ("OIC_FTPServerSet");

    if (Flag == ON)
       TNS_Server = OFF;

    return;
}

/*---------------------------------------------------------------*/
/*	Function  : OIC_FTPServerSet ()                              */
/*	Action    : 작업할 FTP Server가 TANDEM Host인지 일반 Unix    */
/*	            인지 Set.                                        */
/*	Parameter : Flag : On/Off Flag                               */
/*	Return    : 없슴                                             */
/*---------------------------------------------------------------*/
void    OIC_TNSServerSet (int  Flag)
{
    TNS_Server = Flag;

    FTPTRACE_Logging ("OIC_TNSServerSet");

    if (Flag == ON)
       IBM_Server = OFF;

    return;
}


/*---------------------------------------------------------------*/
/*	Function  : OIC_AIXServerSet ()                              */
/*	Action    : 작업할 FTP Server가 AIX Server인지 일반 Unix     */
/*	            인지 Set.                                        */
/*	Parameter : Flag : On/Off Flag                               */
/*	Return    : 없슴                                             */
/*---------------------------------------------------------------*/
void    OIC_AIXServerSet (int  Flag)
{
    AIX_Server = Flag;

    FTPTRACE_Logging ("OIC_AIXServerSet");

    if (Flag == ON)
       IBM_Server = OFF;

    return;
}


/*---------------------------------------------------------------*/
/*	Function  : OIC_FtpConnect                                   */
/*	Action    : Ftp host와 command용으로 socket을 연결한다.      */
/*	Parameter : hostnm = 연결하고자하는 host의 Ip주소            */
/*	            user   = Hosts의 계정이름                        */
/*	            pass   = 계정에 대한 비밀번호                    */
/*	Return    : 0이상이면 연결된 socket번호                      */
/*	            -1 = Data recv error                             */
/*	            -2 = User command error                          */
/*	            -3 = Password command error                      */
/*---------------------------------------------------------------*/
int OIC_FtpConnect(char *hostnm, char *user, char *pass)
{
char  tmpbuf [80];
int sockid, ret;


    FTPTRACE_Logging ("OIC_FtpConnect START");
    FTPTRACE_Logging ("Ftp_Cliconn START");
    sockid = Ftp_Cliconn(hostnm, "ftp");
    if (sockid < 0)
        return sockid;

    FTPTRACE_Logging ("Ftp_Cliconn START");

    if (recv(sockid, &CommandBuf[0], 1024, 0) < 0)
        return -1;

    FTPTRACE_Logging (CommandBuf);

    /*--------------------------------------------*/
    /* IBM Host인 경우 한번더 Recv                */
    /* 두번째 데이터 Recv전에 0.3초 쉬었다가.     */
    /*--------------------------------------------*/
    if (IBM_Server == ON) {
        usleep (300000);
        if (recv(sockid, &CommandBuf[0], 1024, 0) < 0)
            return -1;

        FTPTRACE_Logging ("IBM은 한번더 메시지 수신");
        FTPTRACE_Logging (CommandBuf);
    }

#ifdef MULTIARG
    ret = Ftp_Command(sockid, "USER %s", user);
#else
    memset (tmpbuf, 0x00, sizeof (tmpbuf));
    sprintf (tmpbuf, "USER %s", user);
    ret = Ftp_Command(sockid, tmpbuf);
#endif

    sprintf(LogData, "USER 결과 [%d]", ret);
    FTPTRACE_Logging (LogData);

    if (ret != 331)
        return -2;

#ifdef MULTIARG
    ret = Ftp_Command(sockid, "PASS %s", pass);
#else
    memset (tmpbuf, 0x00, sizeof (tmpbuf));
    sprintf (tmpbuf, "PASS %s", pass);
    ret = Ftp_Command(sockid, tmpbuf);
#endif

    sprintf(LogData, "PASS 결과 [%d]", ret);
    FTPTRACE_Logging (LogData);

    if (ret != 230)
        return -3;

	if (AIX_Server == ON) {
		if (recv(sockid, &CommandBuf[0], 1024, 0) < 0)
			return (-5);

	    FTPTRACE_Logging ("AIX SERVER 메시지 수신1");
	    FTPTRACE_Logging (CommandBuf);

		if (memcmp(CommandBuf, "4", 1) >= 0)
			return (-6);

		if (memcmp(CommandBuf, "230 User", 8) != 0) {
			if (recv(sockid, &CommandBuf[0], 1024, 0) < 0)
				return (-5);

		    FTPTRACE_Logging ("AIX SERVER 메시지 수신2");
		    FTPTRACE_Logging (CommandBuf);
		}
	}

	FTPTRACE_Logging ("OIC_FtpConnect END");

    return sockid;
}

/*---------------------------------------------------------------*/
/*	Function  : OIC_FtpGetFile                                   */
/*	Action    : Ftp host에 연결해서 file을 수신받는다.           */
/*	Parameter : sockid = 연결된 socket번호(command 용)           */
/*	            type   = binary(TYPE_I) or accii(TYPE_A) file구분*/
/*	            remote_filename = remote filename                */
/*	            local_filename  = local  filename                */
/*	Return    :  1 = 정상                                        */
/*	            -1 = Command(TYPE) error                         */
/*	            -2 = Data용 대표 socket open error               */
/*	            -3 = File 전송 error                             */
/*	            -3 = Command(RETR) recv error                    */
/*---------------------------------------------------------------*/
int OIC_FtpGetFile(int sockid, int type, char *remote_filename, char *local_filename, char *ErrMsg)
{
int ret, s_sockid;

	FTPTRACE_Logging ("OIC_FtpGetFile START");

    if (IBM_Server != ON) {
        if (type == TYPE_I)
            ret = Ftp_Command(sockid, "TYPE I");
        else
            ret = Ftp_Command(sockid, "TYPE A");

		sprintf(LogData, "TYPE 변경결과 [%d]", ret);
		FTPTRACE_Logging (LogData);

       	if (ret != 200)
            return ret * -1;
    }

    if ((s_sockid = Ftp_Serconn(sockid)) < 0)
        return -2;

	FTPTRACE_Logging ("Ftp_Serconn");

    if (*local_filename == 0x00)
        strcpy(local_filename, remote_filename);

	FTPTRACE_Logging ("Get_Data CALL");
    ret = Get_Data(s_sockid, sockid, type, remote_filename, local_filename);
	FTPTRACE_Logging ("Get_Data CALL END");

    if (ret < 0) {
    	sprintf(ErrMsg, "%s", ErrBuf);
		FTPTRACE_Logging (ErrMsg);
        return ret;
    }

	FTPTRACE_Logging ("OIC_FtpGetFile END");

    return 1;
}

/*---------------------------------------------------------------*/
/*	Function  : OIC_FtpGetJES                                    */
/*	Action    : Ftp host에 연결해서 file을 수신받는다.           */
/*	Parameter : sockid = 연결된 socket번호(command 용)           */
/*	            type   = binary(TYPE_I) or accii(TYPE_A) file구분*/
/*	            remote_filename = remote filename                */
/*	            local_filename  = local  filename                */
/*	Return    :  1 = 정상                                        */
/*	            -1 = Command(TYPE) error                         */
/*	            -2 = Data용 대표 socket open error               */
/*	            -3 = File 전송 error                             */
/*	            -3 = Command(RETR) recv error                    */
/*---------------------------------------------------------------*/
int OIC_FtpGetJES (int Sockid, int type, char *remote_filename, char *local_filename, char *ErrMsg)
{
int ret, s_sockid;

	FTPTRACE_Logging ("OIC_FtpGetJES END");

	FTPTRACE_Logging ("OIC_FtpGetJES::Ftp_Serconn");
    if ((s_sockid = Ftp_Serconn(Sockid)) < 0)
        return -2;

    if (*local_filename == 0x00)
        strcpy(local_filename, remote_filename);

	FTPTRACE_Logging ("Get_Data CALL");
    ret = Get_Data(s_sockid, Sockid, type, remote_filename, local_filename);
	FTPTRACE_Logging ("Get_Data CALL END");
    if (ret < 0) {
    	sprintf(ErrMsg, "%s", ErrBuf);
		FTPTRACE_Logging (ErrMsg);
        return ret;
    }

	FTPTRACE_Logging ("OIC_FtpGetJES END");

    return 1;
}

/*---------------------------------------------------------------*/
/*	Function  : OIC_FtpPutFile                                   */
/*	Action    : Ftp host에 연결해서 file을 송신한다.             */
/*	Parameter : sockid = 연결된 socket번호(command 용)           */
/*	            type   = binary(TYPE_I) or accii(TYPE_A) file구분*/
/*	            local_filename  = local  filename                */
/*	            remote_filename = remote filename                */
/*	Return    :  1 = 정상                                        */
/*	            -1 = Command(TYPE) error                         */
/*	            -2 = Data용 대표 socket open error               */
/*	            -3 = File 수신 error                             */
/*	            -3 = Command(STOR) recv error                    */
/*---------------------------------------------------------------*/
int OIC_FtpPutFile(int Sockid, int type, char *local_filename, char *remote_filename, char *ErrMsg)
{
int ret, s_sockid;


	FTPTRACE_Logging ("OIC_FtpPutFile START");

    if (IBM_Server != ON) {
    	FTPTRACE_Logging ("TYPE 변경");
        if (type == TYPE_I)
            ret = Ftp_Command(Sockid, "TYPE I");
        else
            ret = Ftp_Command(Sockid, "TYPE A");

        if (ret != 200)
            return -200;
    }


	FTPTRACE_Logging ("Ftp_Serconn");

    if ((s_sockid = Ftp_Serconn(Sockid)) < 0)
        return -2;

    if (*remote_filename == 0x00)
        strcpy(remote_filename, local_filename);


	FTPTRACE_Logging ("Put_Data Call START");

    ret = Put_Data(s_sockid, Sockid, type, local_filename, remote_filename);
    if (ret < 0) {
    	sprintf(ErrMsg, "%s", ErrBuf);
    	FTPTRACE_Logging (ErrMsg);
        return ret;
    }

	FTPTRACE_Logging ("Put_Data Call END");
	FTPTRACE_Logging (CommandBuf);

    if (recv(Sockid, &CommandBuf[0], 1024, 0) < 0)
        return -4;

	FTPTRACE_Logging (CommandBuf);

	if (memcmp(CommandBuf, "4", 1) >= 0)
		return (-5);

	if (memcmp(CommandBuf, "226", 3) != 0 &&
		memcmp(CommandBuf, "250", 3) != 0 ) {
		recv(Sockid, &CommandBuf[0], 1024, 0);
	}

	FTPTRACE_Logging ("OIC_FtpPutFile END");

    return 1;
}

/*---------------------------------------------------------------*/
/*	Function  : OIC_FtpPutJES                                    */
/*	Action    : Ftp host에 연결해서 file을 송신한다.             */
/*	Parameter : sockid = 연결된 socket번호(command 용)           */
/*	            type   = binary(TYPE_I) or accii(TYPE_A) file구분*/
/*	            local_filename  = local  filename                */
/*	            remote_filename = remote filename                */
/*	Return    :  1 = 정상                                        */
/*	            -1 = Command(TYPE) error                         */
/*	            -2 = Data용 대표 socket open error               */
/*	            -3 = File 수신 error                             */
/*	            -3 = Command(STOR) recv error                    */
/*---------------------------------------------------------------*/
int OIC_FtpPutJES(int Sockid, int type, char *local_filename, char *remote_filename, char *ErrMsg)
{
int ret, s_sockid;

	FTPTRACE_Logging ("OIC_FtpPutJES START");
	FTPTRACE_Logging ("Ftp_Serconn");

    if ((s_sockid = Ftp_Serconn(Sockid)) < 0)
        return -2;

    if (*remote_filename == 0x00)
        strcpy(remote_filename, local_filename);


	FTPTRACE_Logging ("Put_Data Call START");

    ret = Put_Data(s_sockid, Sockid, type, local_filename, remote_filename);
    if (ret < 0) {
    	sprintf(ErrMsg, "%s", ErrBuf);
    	FTPTRACE_Logging (ErrMsg);
        return ret;
    }

	FTPTRACE_Logging ("Put_Data Call END");
	FTPTRACE_Logging (CommandBuf);

    if (recv(Sockid, &CommandBuf[0], 1024, 0) < 0)
        return -4;

	FTPTRACE_Logging ("OIC_FtpPutJES END");

    return 1;
}

/*---------------------------------------------------------------*/
/*	Function  : OIC_FtpDelFile                                   */
/*	Action    : Ftp host에 연결해서 file을 송신한다.             */
/*	Parameter : sockid = 연결된 socket번호(command 용)           */
/*	            type   = binary(TYPE_I) or accii(TYPE_A) file구분*/
/*	            local_filename  = local  filename                */
/*	            remote_filename = remote filename                */
/*	Return    :  1 = 정상                                        */
/*	            -1 = Command(TYPE) error                         */
/*	            -2 = Data용 대표 socket open error               */
/*	            -3 = File 수신 error                             */
/*	            -3 = Command(STOR) recv error                    */
/*---------------------------------------------------------------*/
int OIC_FtpDelFile(int Sockid, int type, char *local_filename, char *remote_filename, char *ErrMsg)
{
int ret, s_sockid;

	FTPTRACE_Logging ("OIC_FtpDelFile START");

    if (IBM_Server != ON) {
       FTPTRACE_Logging ("TYPE 변경");
       if (type == TYPE_I)
           ret = Ftp_Command(Sockid, "TYPE I");
       else
           ret = Ftp_Command(Sockid, "TYPE A");

       if (ret != 200)
           return -1;
    }

	FTPTRACE_Logging ("Ftp_Serconn");

    if ((s_sockid = Ftp_Serconn(Sockid)) < 0)
        return -2;

    if (*remote_filename == 0x00)
        strcpy(remote_filename, local_filename);

	FTPTRACE_Logging ("Del_Data CALL");

    ret = Del_Data(s_sockid, Sockid, type, remote_filename);
    if (ret<0) {
    	sprintf(ErrMsg, "%s", ErrBuf);
    	FTPTRACE_Logging (ErrMsg);
        return ret;
    }

	FTPTRACE_Logging ("OIC_FtpDelFile END");

    return 1;
}

/*---------------------------------------------------------------*/
/*	Function  : OIC_FtpDir                                       */
/*	Action    : Ftp host에 연결해서 file목록을 얻어온다.         */
/*	Parameter : sockid = 연결된 socket번호(command 용)           */
/*	            path   = 해당 목록을 보고자 하는 디렉토리 명     */
/*	            dirbuf = ls에대한 결과를 저장할 buffer pointer   */
/*	Return    :  1 = 정상                                        */
/*	            -1 = Data용 대표 socket open error               */
/*	            -2 = File목록 recv error                         */
/*	            -3 = Command(NLST) recv error                    */
/*---------------------------------------------------------------*/
int OIC_FtpDir(int Sockid, char *path, char *dirbuf)
{
int ret, s_sockid;

	FTPTRACE_Logging ("OIC_FtpDir START");
	FTPTRACE_Logging ("Ftp_Serconn");

    if ((s_sockid = Ftp_Serconn(Sockid)) < 0) {
        return -1;
    }

	FTPTRACE_Logging ("Dir_Get CALL START");

    if (Dir_Get(s_sockid, Sockid, path, dirbuf) < 0) {
        return -2;
    }
	FTPTRACE_Logging ("Dir_Get CALL END");

    close(s_sockid);

	FTPTRACE_Logging (CommandBuf);

    if (recv(Sockid, &CommandBuf[0], 1024, 0) < 0) {
        return -3;
    }

	FTPTRACE_Logging ("OIC_FtpDir END");

    return 1;
}


/*---------------------------------------------------------------*/
/*	Function  : OIC_FtpDisConnect                                */
/*	Action    : Ftp host와 연결된 socket을 종료한다.             */
/*	Parameter : sockid = 연결된 socket번호(command용)            */
/*	Return    : 없음                                             */
/*---------------------------------------------------------------*/
void OIC_FtpDisConnect(int Sockid)
{
	FTPTRACE_Logging ("OIC_FtpDisConnect START");
    Ftp_Command(Sockid, "QUIT");

    shutdown(Sockid, 2);
    FTPTRACE_Logging ("OIC_FtpDisConnect END");
    close(Sockid);
}

/*---------------------------------------------------------------*/
/*	Function  : Ftp_Cliconn                                      */
/*	Action    : Ftp host와 처음하는 작업으로 command용으로       */
/*	            socket을 연결한다.                               */
/*	Parameter : hostnm = host ip address or host name            */
/*	            portnm = port name or port number                */
/*	Return    :  0 이상이면 연결된 socket description(command용) */
/*	            -1 = socket system call fail                     */
/*	            -2 = connect system call fail                    */
/*	            -3 = getsockname system call fail                */
/*---------------------------------------------------------------*/
int Ftp_Cliconn(char *hostnm, char *portnm)
{
int sockfd, keepalive, len;
struct hostent *hostent;
struct servent *servent;
struct sockaddr_in cli_addr;

	FTPTRACE_Logging ("Ftp_Cliconn START");

    hostent = (struct hostent *)gethostbyname(hostnm);

    servent = (struct servent *)getservbyname(portnm, "tcp");

    if ((sockfd = socket(AF_INET, SOCK_STREAM, 0)) < 0)
        return -1;

    /*
     *  socket이 연결되었으면 지속적으로 유지 시킴
     */
    keepalive = 1;
    setsockopt(sockfd, SOL_SOCKET, SO_KEEPALIVE, (char *)&keepalive, sizeof(keepalive));

    cli_addr.sin_family= AF_INET;
    if (hostent == NULL)
        cli_addr.sin_addr.s_addr = inet_addr(hostnm);
    else
        cli_addr.sin_addr.s_addr = *((unsigned long *)*hostent->h_addr_list);

    if (servent == NULL)
        cli_addr.sin_port= atoi(portnm);
    else
        cli_addr.sin_port= htons(ntohs((unsigned short)servent->s_port));

    /*
     *  server와 연결한다
     */
    FTPTRACE_Logging ("Ftp_Cliconn::connect");
    if (connect (sockfd,(struct sockaddr*)&cli_addr,sizeof(cli_addr)) < 0){
        close (sockfd);
        return -2;
    }

    len = sizeof(Cli_addr);
    FTPTRACE_Logging ("Ftp_Cliconn::getsockname");
#ifdef __sun
    if (getsockname(sockfd, (struct sockaddr *)&Cli_addr, (unsigned int *)&len) < 0)
#else
    if (getsockname(sockfd, (struct sockaddr *)&Cli_addr, (int *)(size_t *)&len) < 0)
#endif
        return -3;

	FTPTRACE_Logging ("Ftp_Cliconn END");

    return (sockfd);
}

/*---------------------------------------------------------------*/
/*	Function  : Ftp_Command                                      */
/*	Action    : Ftp host에 command를 보낸다.                     */
/*	Parameter : sockfd = 연결된 socket번호(command용)            */
/*	            fmt    = format된 string                         */
/*	Return    :  0 이상이면 command recv code                    */
/*	            -1 = send system call fail                       */
/*	            -2 = recv system call fail                       */
/*---------------------------------------------------------------*/
#ifdef MULTIARG
int Ftp_Command(int sockfd, const char *fmt, ...)
#else
int Ftp_Command(int sockfd, char *fmt)
#endif
{
#ifdef MULTIARG
va_list ap;
#else
char    tmpbuf [80];
#endif

int r, lng;
static char buf[1024];

    memset (buf, 0x00, sizeof (buf));

#ifdef MULTIARG
    va_start(ap, fmt);
    vsprintf(&buf[0], fmt, ap);
    va_end(ap);
    strcat(&buf[0], "\r\n");
#else
    sprintf(&buf[0], "%s\r\n", fmt);
#endif

    lng = strlen(&buf[0]);

	FTPTRACE_Logging ("Ftp_Command START");

	FTPTRACE_Logging (buf);
    if (send(sockfd, &buf[0], lng, 0) != lng) {
        return -1;
    }

    memset(&CommandBuf[0], 0x00, sizeof(CommandBuf));
    if (recv(sockfd, &CommandBuf[0], 1024, 0) <= 0) {
        return -2;
    }

	FTPTRACE_Logging (CommandBuf);

    sprintf(ErrBuf, "%s\n", CommandBuf);
    CommandBuf[3] = 0x00;
    r = atoi(&CommandBuf[0]);

	FTPTRACE_Logging ("Ftp_Command END");

    return(r);
}

/*---------------------------------------------------------------*/
/*	Function  : Ftp_Serconn                                      */
/*	Action    : FtpData용으로 연결하기위한 작업을 한다.          */
/*	Parameter : Cli_sd = client로 연결된 socket번호(command용)   */
/*	Return    :  0 이상이면 연결된 대표socket description        */
/*	            -1 = socket system call fail                     */
/*	            -2 = bind system call fail                       */
/*	            -3 = getsockname system call fail                */
/*	            -4 = listen system call fail                     */
/*	            -5 = port command call fail                      */
/*---------------------------------------------------------------*/
int Ftp_Serconn(int Cli_sd)
{
char     tmpbuf [80];
register char *p, *a;
int sockfd, on, len, ret;
struct hostent *hostent;
struct servent *servent;
struct sockaddr_in serv_addr;

	FTPTRACE_Logging ("Ftp_Serconn START");

    serv_addr = Cli_addr;
    serv_addr.sin_port = 0;

    if ((sockfd = socket(AF_INET, SOCK_STREAM, 0)) < 0)
        return -1;

    on = 1;
    setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR, (char *)&on, sizeof(on));

    if (bind(sockfd, (struct sockaddr *)&serv_addr, sizeof(serv_addr)) < 0)
        return -2;

    len = sizeof(serv_addr);

#ifdef __sun
    if (getsockname(sockfd, (struct sockaddr *)&serv_addr, (unsigned int *)&len) < 0)
#else
    if (getsockname(sockfd, (struct sockaddr *)&serv_addr, (int *)(size_t *)&len) < 0)
#endif
        return -3;

    if (listen(sockfd, 1) < 0)
        return -4;

    a = (char *)&serv_addr.sin_addr;
    p = (char *)&serv_addr.sin_port;

#define UC(b) (((int)b)&0xff)

#ifdef MULTIARG
    ret = Ftp_Command(Cli_sd, "PORT %d,%d,%d,%d,%d,%d", UC(a[0]), UC(a[1]), UC(a[2]), UC(a[3]), UC(p[0]), UC(p[1]));
#else
    memset (tmpbuf, 0x00, sizeof (tmpbuf));

    sprintf (tmpbuf, "PORT %d,%d,%d,%d,%d,%d", UC(a[0]), UC(a[1]), UC(a[2]), UC(a[3]), UC(p[0]), UC(p[1]));
    ret = Ftp_Command(Cli_sd, tmpbuf);
#endif

	FTPTRACE_Logging ("Ftp_Serconn END");

    if (ret != 200)
        return -5;

    return (sockfd);
}

/*---------------------------------------------------------------*/
/*	Function  : Conn_Data                                        */
/*	Action    : 대표socket번호에서 client가 connect하기를        */
/*	            기다리고 연결이되면 연결번호를 return한다.       */
/*	Parameter : ser_sd = 연결된 대표socket번호(data 용)          */
/*	            lmode  = fdopen file mode                        */
/*	Return    : NULL이 아니면 정상                               */
/*	            NULL = accept error                              */
/*---------------------------------------------------------------*/
FILE *Conn_Data(int ser_sd, char *lmode)
{
int sockfd, len;
struct sockaddr_in from_addr;

    len = sizeof(from_addr);

    FTPTRACE_Logging ("Conn_Data START");

#ifdef __sun
    sockfd = accept(ser_sd, (struct sockaddr *)&from_addr, (unsigned int *)&len);
#else
    sockfd = accept(ser_sd, (struct sockaddr *)&from_addr, (int *)(size_t *)&len);
#endif
    close(ser_sd);

	FTPTRACE_Logging ("Conn_Data END");

    return (fdopen(sockfd, lmode));
}

/*---------------------------------------------------------------*/
/*	Function  : Get_Data                                         */
/*	Action    : Ftp host에 연결해서 file을 수신한다.             */
/*	Parameter : s_sockid = 연결된 socket번호(data    용)         */
/*	            Cli_sd   = 연결된 socket번호(command 용)         */
/*	            type     = binary(TYPE_I) or ascii(TYPE_A)       */
/*	                       file 구분                             */
/*	            remote_filename = remote filename                */
/*	            local_filename  = local  filename                */
/*	Return    :  1 = 정상                                        */
/*	            -1 = get command error                           */
/*	            -2 = accept system call error                    */
/*	            -3 = local file open error                       */
/*	            -4 = remote file recv error                      */
/*	            -5 = local file write error                      */
/*	            -6 = 수신받은 filesize가 받아야될 filesize보다   */
/*	                 작을경우                                    */
/*---------------------------------------------------------------*/
int Get_Data(int s_sockid, int Cli_sd, int type, char *remote_filename, char *local_filename)
{
char        tmpbuf[80];
static char buf[BUFSIZ+1];
char byte, r = '\r';
int  lng, ret, cnt, filesize, file_lng = 0;
FILE *din, *fout;

	FTPTRACE_Logging ("Get_Data START");

#ifdef MULTIARG
    ret = Ftp_Command(Cli_sd, "RETR %s", remote_filename);
#else
    memset (tmpbuf, 0x00, sizeof (tmpbuf));
    sprintf (tmpbuf, "RETR %s", remote_filename);
    ret = Ftp_Command(Cli_sd, tmpbuf);
#endif
    /*
     * 접속 Server가 IBM인 경우 JES Start 여부에 따라 반환 값 차이.
     */

  if (ret >= 400) {
     return -550;
  }

  if (JES_Start == ON) {
    if (IBM_Server == ON) {
        if (recv(Cli_sd, &CommandBuf[0], 1024, 0) < 0)
            return -1;

		FTPTRACE_Logging ("Get_Data 전문수신 1");
		FTPTRACE_Logging (CommandBuf);

        if (strstr(CommandBuf,"550") != NULL) {
              sprintf(ErrBuf, "%s\n", CommandBuf);
              return -550;
        }


        /* ret가 250으로 온다 */
        if (JES_Start == ON) {
            if (memcmp (CommandBuf, "125", 3)) {
                sprintf(ErrBuf, "%s\n", CommandBuf);
                return -125;
            }
        }
        else {
            if (memcmp (CommandBuf, "250", 3)) {
                sprintf(ErrBuf, "%s\n", CommandBuf);
                return -250;
            }
        }

    }
  }

    filesize = Get_File_Size(&CommandBuf[0]);

    if ((din = Conn_Data(s_sockid, "r")) == NULL)
        return -2;

    fout = fopen(local_filename, "w");
    if (fout == NULL)
    {
        printf("GET LOCAL FILE OPEN ERROR : [%s]\n", local_filename);
        return -3;
    }

    switch (type) {
        case TYPE_I :
            while (1) {
                memset(&buf[0], 0x00, sizeof(buf));
                if ((lng = recv(fileno(din), &buf[0], BUFSIZ, 0)) < 0)
                    return -4;

                if (!lng) {
                    fclose(fout);
                    fclose(din);
                    if (file_lng >= filesize)
                        return 1;
                    else
                        return -6;
                }

                if (write(fileno(fout), &buf[0], lng) != lng) {
                    fclose(fout);
                    fclose(din);
                    return -5;
                }
                file_lng += lng;
            }
            break;

        case TYPE_A :
            if (IBM_Server == ON) {
                while (1) {
                   memset(&buf[0], 0x00, sizeof(buf));
                   if ((lng = recv(fileno(din), &buf[0], BUFSIZ, 0)) < 0)
                       return -4;

                   if (!lng) {
                       fclose(fout);
                       fclose(din);
                       if (file_lng >= filesize)
                          break;
                       else
                           return -6;
                   }

                   if (write(fileno(fout), &buf[0], strlen(&buf[0])) != strlen(&buf[0])) {
                       fclose(fout);
                       fclose(din);
                       return -5;
                   }
                   file_lng += lng;
               }
               break;
            }
            else {
               while ((byte = getc(din)) != EOF) {
                   while (byte == '\r') {
                       if ((byte = getc(din)) != '\n') {
                           putc('\r', fout);
                           file_lng ++;
                           if (byte == EOF)
                               break;
                       }
                   }
                   if (byte == EOF)  break;
                   else {
                       putc(byte, fout);
                   }
                   file_lng ++;
               }
               fclose(fout);
               fclose(din);
               if (file_lng >= filesize)
                   break;
              else
                   return -6;
            }
    }

    if (IBM_Server == ON) {
        if (recv(Cli_sd, &CommandBuf[0], 1024, 0) <= 0) {
            return -1;
        }

        FTPTRACE_Logging ("Get_Data IBM 전문수신 2");
        FTPTRACE_Logging (CommandBuf);

        if (JES_Start == ON) {
            if (memcmp (CommandBuf, "250", 3)) {
            	sprintf(ErrBuf, "None 250 : %s\n", CommandBuf);
                ret = -250;
            }
            else
                ret = 250;
        }
        else {
            if (memcmp (CommandBuf, "250", 3)) {
            	sprintf(ErrBuf, "None 250 : %s\n", CommandBuf);
                return -1;
            }
            else
                ret = 250;

        }
    }

    if (TNS_Server == ON) {
        if (recv(Cli_sd, &CommandBuf[0], 1024, 0) <= 0) {
            return -1;
        }

		FTPTRACE_Logging ("Get_Data TNS_Server 전문수신 3");
		FTPTRACE_Logging (CommandBuf);

        if (memcmp (CommandBuf, "226", 3)) {
           sprintf(ErrBuf, "None 250 : %s\n", CommandBuf);
           return -1;
        }
        else
           ret = 250;
    }


	/*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*/
    if (AIX_Server == ON) {
        if (recv(Cli_sd, &CommandBuf[0], 1024, 0) <= 0) {
            return -1;
        }

		FTPTRACE_Logging ("Get_Data AIX_Server 전문수신 4");
		FTPTRACE_Logging (CommandBuf);

        if (memcmp (CommandBuf, "226", 3)) {
           sprintf(ErrBuf, "None 250 : %s\n", CommandBuf);
           return -1;
        }
        else
           ret = 250;
    }

    FTPTRACE_Logging ("Get_Data END");

    if (ret != 250)
        return -250;

    return 1;

}

/*---------------------------------------------------------------*/
/*	Function  : Get_File_Size                                    */
/*	Action    : 수신받은 file의 size를 구한다.                   */
/*	Parameter : buf = filesize가 들어있는 buffer pointer         */
/*	Return    : 0 = error                                        */
/*	            0 이외의 다른값이면 filesize                     */
/*---------------------------------------------------------------*/
int Get_File_Size(char *buf)
{
char tmp[20];
char *cp;
int  idx = 0;

	FTPTRACE_Logging ("Get_File_Size START");

    cp = strchr(buf, (int)'(');
    if (cp == NULL)
        return 0;

    while (1) {
        cp++;
        if (*cp == '\n') {
        	FTPTRACE_Logging ("Get_File_Size END");
            return 0;
        }
        else if ((*cp >= '0') && (*cp <= '9')) {
            tmp[idx] = *cp;
            idx ++;
        }
        else if (!memcmp(cp, " byte", 5)) {
            tmp[idx] = 0;
		    FTPTRACE_Logging ("Get_File_Size END");
            return (atoi(tmp));
        }
        else
            idx = 0;
    }
}

/*---------------------------------------------------------------*/
/*	Function  : Put_Data                                         */
/*	Action    : Ftp host에 연결해서 file을 송신한다.             */
/*	Parameter : s_sockid = 연결된 socket번호(data    용)         */
/*	            Cli_sd   = 연결된 socket번호(command 용)         */
/*	            type     = binary(TYPE_I) or accii(TYPE_A)       */
/*	                       file 구분                             */
/*	            local_filename  = local  filename                */
/*	            remote_filename = remote filename                */
/*	Return    :  1 = 정상                                        */
/*	            -1 = put command error                           */
/*	            -2 = accept system call error                    */
/*	            -3 = local file open error                       */
/*	            -4 = local file read error                       */
/*	            -5 = remote file send error                      */
/*---------------------------------------------------------------*/
int Put_Data(int s_sockid, int Cli_sd, int type, char *local_filename, char *remote_filename)
{
char        tmpbuf [80];
static char buf[BUFSIZ+1];
struct stat filestat;
char byte, *bufp;
int  d, lng, ret, file_lng = 0;
FILE *dout, *fin;

	FTPTRACE_Logging ("Put_Data START");

#ifdef MULTIARG
    ret = Ftp_Command(Cli_sd, "STOR %s", remote_filename);
#else
    memset (tmpbuf, 0x00, sizeof (tmpbuf));
    sprintf (tmpbuf, "STOR %s", remote_filename);
    ret = Ftp_Command(Cli_sd, tmpbuf);
#endif

	FTPTRACE_Logging (CommandBuf);

    /*
     * 접속 Server가 IBM인 경우 JES Start 여부에 따라 반환 값 차이.
     */
    if (IBM_Server == ON) {
        if (JES_Start == ON) {
            if (ret != 226) {
                return -226;
            }
        }
        else {
            if (memcmp (CommandBuf, "125", 3)) {
                return -125;
            }
        }
    }

	if (memcmp(CommandBuf, "4", 1) >= 0)
		return (-5);

    if ((dout = Conn_Data(s_sockid, "w")) == NULL)
        return -2;

    fin = fopen(local_filename, "r");
    if (fin == NULL)
    {
        printf("PUT LOCAL FILE OPEN ERROR = [%s]\n", local_filename);
        return -3;
    }

    stat(local_filename, &filestat);

    switch (type) {
        case TYPE_I :
            d = 0;
            while (1) {
                if ((lng = read(fileno(fin), &buf[0], BUFSIZ)) < 0)
                    return -4;

                file_lng += lng;

                for (bufp = buf; lng > 0; lng -= d, bufp += d)
                    if ((d = send(fileno(dout), bufp, lng, 0)) < 0)
                        return -5;

                if (file_lng == filestat.st_size) {
                    fclose(fin);
                    fclose(dout);
                    return 1;
                }
            }
            break;

        case TYPE_A :
            while ((byte = getc(fin)) != EOF) {
                if (byte == '\n') {
                    if (ferror(dout))
                        break;
                    putc('\r', dout);
                    /*  file_lng++;  */
                }
                putc(byte, dout);
                file_lng++;

                if (file_lng >= filestat.st_size) break;

                if (byte == EOF)  break;

            }
            fclose(dout);
            fclose(fin);
            if (file_lng >= filestat.st_size)
                return 1;
            else
                return -4;
    }

    FTPTRACE_Logging ("Put_Data END");
	return 1;
}

/*---------------------------------------------------------------*/
/*	Function  : Del_Data                                         */
/*	Action    : Ftp host에 연결해서 file을 송신한다.             */
/*	Parameter : s_sockid = 연결된 socket번호(data    용)         */
/*	            Cli_sd   = 연결된 socket번호(command 용)         */
/*	            type     = binary(TYPE_I) or accii(TYPE_A)       */
/*	                       file 구분                             */
/*	            local_filename  = local  filename                */
/*	            remote_filename = remote filename                */
/*	Return    :  1 = 정상                                        */
/*	            -1 = put command error                           */
/*	            -2 = accept system call error                    */
/*	            -3 = local file open error                       */
/*	            -4 = local file read error                       */
/*	            -5 = remote file send error                      */
/*---------------------------------------------------------------*/
int Del_Data(int s_sockid, int Cli_sd, int type, char *remote_filename)
{
static char buf[BUFSIZ+1];
char byte, *bufp;
int  d, lng, ret, file_lng = 0;
FILE *dout, *fin;

	FTPTRACE_Logging ("Del_Data START");

    memset (buf, 0x00, sizeof (buf));
    sprintf (buf, "DELE %s", remote_filename);

    ret = Ftp_Command(Cli_sd, buf);
    if (ret != 250) {
        return (-250);
    }

    FTPTRACE_Logging ("Del_Data END");

    return 0;
}

/*---------------------------------------------------------------*/
/*	Function  : JES_Conn                                         */
/*	Action    : JES에 연결 설정.                                 */
/*	Parameter : s_sockid = 연결된 socket번호(data    용)         */
/*	            Cli_sd   = 연결된 socket번호(command 용)         */
/*	Return    : 1 = 정상                                         */
/*---------------------------------------------------------------*/
int JES_Conn (int Cli_sd)
{
static char buf[BUFSIZ+1];
int    ret;

	FTPTRACE_Logging ("JES_Conn START");

    memset (buf, 0x00, sizeof (buf));
    sprintf (buf, "SITE FILETYPE=JES");

    ret = Ftp_Command(Cli_sd, buf);
    if (ret != 200)
        return -1;

    JES_Start = ON;

    FTPTRACE_Logging ("JES_Conn END");

    return 0;
}

/*---------------------------------------------------------------*/
/*	Function  : JES_DisConn                                      */
/*	Action    : JES에 연결 해제.                                 */
/*	Parameter : s_sockid = 연결된 socket번호(data    용)         */
/*	            Cli_sd   = 연결된 socket번호(command 용)         */
/*	Return    : 1 = 정상                                         */
/*---------------------------------------------------------------*/
int JES_DisConn (int Cli_sd)
{
static char buf[BUFSIZ+1];
int    ret;

	FTPTRACE_Logging ("JES_DisConn START");

    memset (buf, 0x00, sizeof (buf));
    sprintf (buf, "SITE FILETYPE=SEQ");

    ret = Ftp_Command(Cli_sd, buf);
    if (ret != 200)
        return -1;

    JES_Start = OFF;

    FTPTRACE_Logging ("JES_DisConn END");

    return 0;
}

/*---------------------------------------------------------------*/
/*	Function  : JES_Kor                                          */
/*	Action    : JES 한글 변환                                    */
/*	Parameter : s_sockid = 연결된 socket번호(data    용)         */
/*	            Cli_sd   = 연결된 socket번호(command 용)         */
/*	Return    : 1 = 정상                                         */
/*---------------------------------------------------------------*/
int JES_Kor (int Cli_sd, int KorCD)
{
static char buf[BUFSIZ+1];
int    ret;

	FTPTRACE_Logging ("JES_Kor START");

    memset (buf, 0x00, sizeof (buf));
    if (KorCD == 0)
       sprintf (buf, "TYPE B 6 S A");
    else
       sprintf (buf, "TYPE B 6");

	FTPTRACE_Logging ("한글변환 시작");
	FTPTRACE_Logging (buf);

    ret = Ftp_Command(Cli_sd, buf);
    if (ret != 200) {
    	FTPTRACE_Logging ("한글변환 명령처리 오류");
        return -1;
    }

    /* 한글 변환시 2개의 응답이 오니까 0.3초 간격으로 1번더 Recv */
    memset (buf, 0x00, sizeof (buf));
    if (recv(Cli_sd, buf, 1024, 0) <= 0) {
    	FTPTRACE_Logging ("한글변환 오류");
    	FTPTRACE_Logging (buf);

        return -2;
    }

	FTPTRACE_Logging (buf);
    FTPTRACE_Logging ("JES_Kor END");

    return 0;
}

/*---------------------------------------------------------------*/
/*	Function  : Dir_Get                                          */
/*	Action    : Ftp host에 연결해서 file목록을 가지고온다.       */
/*	Parameter : s_sockid = 연결된 socket번호(data    용)         */
/*	            Cli_sd   = 연결된 socket번호(command 용)         */
/*	            dirbuf   = ls에대한 결과를 저장할 buffer pointer */
/*	Return    :  1 = 정상                                        */
/*	            -1 = ls command error                            */
/*	            -2 = accept system call error                    */
/*	            -3 = data recv error                             */
/*---------------------------------------------------------------*/
int Dir_Get(int s_sockid, int Cli_sd, char *path, char *dirbuf)
{
char tmpbuf [80];
int  lng, ret, dir_lng = 0;
FILE *din;

	FTPTRACE_Logging ("Dir_Get START");

    memset (tmpbuf, 0x00, sizeof (tmpbuf));
    if (*path)
        sprintf (tmpbuf, "LIST %s", path);
    else
        sprintf (tmpbuf, "LIST");

    ret = Ftp_Command(Cli_sd, tmpbuf);

    /*-------------------------------------------*/
    /* 접속 Server가 IBM인 경우 JES Start 여부에 */
    /* 따라 반환 값 차이.                        */
    /*-------------------------------------------*/
    if (IBM_Server == ON) {
        if (ret != 125)
           return -1;
    }
    else {
        if (ret != 150)
            return -1;
    }

    if ((din = Conn_Data(s_sockid, "r")) == NULL)
        return -2;

    memset(&dirbuf[0], 0x00, sizeof(dirbuf));

    while (1) {
        if ((lng = recv(fileno(din), &dirbuf[dir_lng], BUFSIZ, 0)) < 0) {
        	FTPTRACE_Logging ("Dir_Get END");
            return -3;
        }

        if (!lng) {
            fclose(din);
		    FTPTRACE_Logging ("Dir_Get END");
            return 1;
        }
        dir_lng += lng;
    }
}


/*---------------------------------------------------------------*/
/*	Function  : SocketFlag_Set                                   */
/*	Action    : 해당 소켓의 Flag를 Update.                       */
/*	            사용예 : 소켓을 NONBLOCK 모드로 Set              */
/*	            SocketFlag_Set (sockfd, O_NONBLOCK)              */
/*	Parameter : fd    : 연결된 socket번호(data    용)            */
/*	            flags : Socket Falg                              */
/*	Return    : 1 = 정상                                         */
/*---------------------------------------------------------------*/
int  SocketFlag_Set (int fd, int flags)
{
int  val;

	FTPTRACE_Logging ("SocketFlag_Set START");

    if ((val = fcntl(fd, F_GETFL, 0)) < 0)
        return -1;

    val |= flags;           /* turn on flags */

    FTPTRACE_Logging ("SocketFlag_Set END");

    if (fcntl(fd, F_SETFL, val) < 0)
        return -1;
    else
        return 1;
}

/*---------------------------------------------------------------*/
/*	Function  : FTPTRACE_Logging                                 */
/*	Action    : FTP 관련 로깅작성                                */
/*	Parameter : LOGDATA : 로깅 데이터                            */
/*	Return    : 0 = 정상                                         */
/*---------------------------------------------------------------*/
int  FTPTRACE_Logging (char *LOGDATA)
{

	if (strlen(LOGDATA) == 0) return (0);

	if (FTPTRACE) {
		fprintf (FTPTRACE, "%s\n", LOGDATA);
		fflush  (FTPTRACE);
	}

	return (0);
}


/*---------------------------------------------------------------*/
/*                E N D   O F   P R O G R A M                    */
/*---------------------------------------------------------------*/
