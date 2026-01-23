
/*-----------------------------------------------------------------
 ┌──────┬───────────────────────┐
 │ 프로그램명 │ lanapi.c                                     │
 ├──────┼───────────────────────┤
 │ 기      능 │ Socket 관련하여 필요한 함수들이              │
 │            │ 존재하는  프로그램                           │
 ├──────┼───────────────────────┤
 │ 작  성  일 │ 2006. 10. 26                                 │
 ├──────┼───────────────────────┤
 │ 작  성  자 │ 최   병   남                                 │
 └──────┴───────────────────────┘
-----------------------------------------------------------------*/

/*---------------------------------------------------------------*/
/*     Header files                                              */
/*---------------------------------------------------------------*/
#include 	<ecamsapi.h>


/*---------------------------------------------------------------*/
/*       Socket Common  PROCEDURE  DEFINE                        */
/*---------------------------------------------------------------*/
#if defined(__STDC__) || defined(__cplusplus) || defined(__sun) || defined(_AIX)
int      DisConnectSocket        (int);
int      Rcv_Data_Sock           (int, uchar *, int);
int      SendDataToSock          (int, uchar *, int);
int      ReadDataFromSock        (int, uchar *);
int      ReadSizeDataFromSock    (int, uchar *, int);
ulong    CvtIPchr2ulong          (char *);
char    *CvtIPulong2chr          (ulong );
int      MakeCommHeader          (int, int, int, uchar *, uchar *, int);
int      ServerSocketInit        (int);
int      ServerAcceptClient      (int );
int      GetPeerName             (int, char *);
int      ConnectClient2Server    (int, ulong);
void     SetSocketOpt            (int  );
int 	 ConnectWait			 (int, struct sockaddr *, int, int);
#else
int      DisConnectSocket        ();
int      Rcv_Data_Sock           ();
int      SendDataToSock          ();
int      ReadDataFromSock        ();
int      ReadSizeDataFromSock    ();
ulong    CvtIPchr2ulong          ();
char    *CvtIPulong2chr          ();
int      MakeCommHeader          ();
int      ServerSocketInit        ();
int      ServerAcceptClient      ();
int      GetPeerName             ();
int      ConnectClient2Server    ();
void     SetSocketOpt            ();
int 	 ConnectWait			 ();
#endif

extern   FILE *TRACE;


/*---------------------------------------------------------------*/
/* Function Name: DisConnectSocket                               */
/* Action       : 기 연결된 Socket의 연결을 단절한다.            */
/* Arguments    : fdSock = 연결된 SocketID                       */
/* Returns      : TRUE                                           */
/*---------------------------------------------------------------*/
int  DisConnectSocket (fdSock)
int  fdSock;
{
    shutdown (fdSock, 2);
    close (fdSock);

    return (TRUE);
}

/*---------------------------------------------------------------*/
/* Function Name: Rcv_Data_Sock                                  */
/* Action       : 연결된 소켓에서 Data Recv.                     */
/* Arguments    : Sock     => 소켓 ID                            */
/*                RcvBuf   => 수신한 Data를 저장할 버퍼 포인터.  */
/*                LimitLng => 수신해야 되는 Data의 길이          */
/* Returns      : lngptr   => 수신한 Data의 길이                 */
/*---------------------------------------------------------------*/
int    Rcv_Data_Sock (Sock, RcvBuf, LimitLng)
int    Sock;
uchar  *RcvBuf;
int    LimitLng;
{
int     flag = OFF, rcvlng, maxlng, lngptr = 0;
char   *tmpbuf;
int     loop = 0;

    maxlng = LimitLng;

    tmpbuf = (char *) malloc (LimitLng + dfMaxBufSize);

    while (1) {
        usleep (10000);

        rcvlng = recv (Sock, (char *) tmpbuf, maxlng, (int) 0);

         
        if (rcvlng < 0) {
            if (errno == EWOULDBLOCK) {
                usleep (10000);
                continue;
            }

            free (tmpbuf);
            return (rcvlng);
        }

        memcpy ((char *) &RcvBuf [lngptr], tmpbuf, rcvlng);
        lngptr += rcvlng;


        if (lngptr < LimitLng) {
            if ((flag == OFF) && (rcvlng == OFF)) {
                 free (tmpbuf);
                 return (-1);
            }

            maxlng = LimitLng - lngptr;
            flag   = ON;

/*
            if (loop ++ > 100)
            {
            	free (tmpbuf);
                return (lngptr);
            }
*/
            continue;
        }
        else
            break;
    }

    if (TRACE) {
        fprintf (TRACE, "<---- 원격지로부터 Data 수신 : Lng = %d\n", lngptr);
        fflush  (TRACE);
        if (lngptr > 256)
        	DumpData (TRACE,  RcvBuf, 256, 1);
        else
        	DumpData (TRACE,  RcvBuf, lngptr, 1);
    }

    free (tmpbuf);
    return (lngptr);
}

/*---------------------------------------------------------------*/
/* Function Name: SendDataToSock                                 */
/* Action       : 연결된 해당 소켓에 Data를 송신.                */
/* Arguments    : Wsock   = Socket ID                            */
/*                SendBuf = 송신 버퍼 포인터                     */
/*                SendLng = 송신 버퍼 길이                       */
/* Returns      : 송신한 총 길이.                                */
/*---------------------------------------------------------------*/
int    SendDataToSock (Wsock, SndBuf, SndLng)
int    Wsock;
uchar *SndBuf;
int    SndLng;
{
int        totlng, sendcnt, wrtlng, ret;

    totlng  = 0;
	sendcnt = 0;
    wrtlng  = SndLng;

    while (1) {
         sendcnt = send (Wsock, (char*)&SndBuf[totlng], wrtlng, (int)0);
         if (sendcnt <= 0) {
             if (errno == EWOULDBLOCK) {
                 usleep (10000);
                 continue;
             }

             return (-1);
         }

         totlng += sendcnt;
         wrtlng -= sendcnt;

         if (totlng == SndLng)
             break;

    }

    if (TRACE) {
        fprintf (TRACE, "----> 원격지로 Data 송신 : Lng = %d\n", totlng);
        fflush  (TRACE);
        if (totlng > 256)
        	DumpData (TRACE,  SndBuf, 256, 1);
        else
        	DumpData (TRACE,  SndBuf, totlng, 1);
    }

    return (totlng);
}

/*---------------------------------------------------------------*/
/* Function Name: ReadDataFromSock                               */
/* Action       : 연결된 해당 소켓에서 Data를 수신.              */
/* Arguments    : fdSock = Socket ID                             */
/*                RcvBuf = 수신 버퍼 포인터                      */
/* Returns      : 수신한 총 길이.                                */
/*---------------------------------------------------------------*/
int     ReadDataFromSock (Rsock, RcvBuf)
int     Rsock;
uchar   *RcvBuf;
{
int         hdlng , readcnt, userlng;
uchar      *tmpbuf;
BpComHead   *chead;

    hdlng  = sizeof (BpComHead);
    tmpbuf = (uchar *) malloc (dfMaxBufSize);
    chead  = (BpComHead *) RcvBuf;

    /*-----------------------------------------------------------*/
    /* 	Check the Specific Socket if Data Rcv or Not             */
    /*-----------------------------------------------------------*/
    readcnt = ExistReadData (Rsock, 0, 100000);

    if (readcnt <= 0) {
        free (tmpbuf);
        return (readcnt);
    }

	/*-----------------------------------------------------------*/
    /* 	Read Only BpComHead Data                                 */
	/*-----------------------------------------------------------*/
    memset (tmpbuf, 0x00, sizeof (tmpbuf));
    readcnt = Rcv_Data_Sock (Rsock, tmpbuf, hdlng);
    if (readcnt < hdlng)  {
        free (tmpbuf);
        return (readcnt);
    }

	/*-----------------------------------------------------------*/
    /* 	Copy Read Comm Header Data to Recv Buffer                */
	/*-----------------------------------------------------------*/
    memcpy ((char *) RcvBuf, (char *) tmpbuf, readcnt);

	/*-----------------------------------------------------------*/
    /* 	datalng means that Total Size of User Data except        */
    /*  BpComHead                                                */
	/*-----------------------------------------------------------*/
    userlng = UINT(chead->Lng [0],chead->Lng [1],chead->Lng [2],chead->Lng [3]);
    if (!userlng)  {
        free (tmpbuf);
        return (hdlng);
    }

	/*-----------------------------------------------------------*/
    /* 	Read Only User Data except BpComHead                     */
	/*-----------------------------------------------------------*/
    memset (tmpbuf, 0x00, sizeof (tmpbuf));

    readcnt = Rcv_Data_Sock (Rsock, tmpbuf, userlng);
    if (readcnt != userlng) {
        free (tmpbuf);
        return (readcnt + hdlng);
    }
    else
    if (readcnt < 0)  {
        free (tmpbuf);
        return (readcnt);
    }

    memcpy ((char *) &RcvBuf [hdlng], (char *) tmpbuf, readcnt);
    userlng = hdlng + readcnt;

    free (tmpbuf);
    return (userlng);
}


/*---------------------------------------------------------------*/
/* Function Name: ReadSizeDataFromSock                           */
/* Action       : 협회와 접속된 소켓에서 Data 수신.              */
/* Arguments    : fdSock = Socket ID                             */
/*                RcvBuf = 수신 버퍼 포인터                      */
/* Returns      : 수신한 총 길이.                                */
/*---------------------------------------------------------------*/
int     ReadSizeDataFromSock (Rsock, RcvBuf, RcvLng)
int     Rsock;
uchar   *RcvBuf;
int     RcvLng;
{
int         readcnt, userlng;
uchar      *tmpbuf;

    tmpbuf = (uchar *) malloc (dfMaxBufSize);

	/*-----------------------------------------------------------*/
    /* Check the Specific Socket if Data Rcv or Not              */
	/*-----------------------------------------------------------*/
    readcnt = ExistReadData (Rsock, 0, 100000);
    if (readcnt <= 0) {
        free (tmpbuf);
        return (readcnt);
    }

	/*-----------------------------------------------------------*/
    /* datalng means that Total Size of UserData except BpComHead*/
	/*-----------------------------------------------------------*/
    userlng = RcvLng;

	/*-----------------------------------------------------------*/
    /* Read Only User Data except BpComHead                      */
	/*-----------------------------------------------------------*/
    memset (tmpbuf, 0x00, sizeof (tmpbuf));

    readcnt = Rcv_Data_Sock (Rsock, tmpbuf, userlng);
    if (readcnt < 0)  {
        free (tmpbuf);
        return (readcnt);
    }

    memcpy ((char *) RcvBuf, (char *) tmpbuf, readcnt);
    userlng = readcnt;

    free (tmpbuf);
    return (userlng);
}

/*---------------------------------------------------------------*/
/* Function Name: CvtIPchr2ulong                                 */
/* Action       : String형태의 IP Addr를 Unsigned Long형태로 변환*/
/* Arguments    : IpBuf    => String형태의 IP Addr               */
/* Returns      : serverip => Unsigned Long 형태의 IP Addr       */
/*---------------------------------------------------------------*/
ulong  CvtIPchr2ulong (IpBuf)
char  *IpBuf;
{
ulong   serverip;

    serverip = (ulong) inet_addr (IpBuf);
    return (serverip);
}

/*---------------------------------------------------------------*/
/* Function Name: CvtIPulong2chr                                 */
/* Action       : Unsigned Long형태의 IP Addr을 String형태로 변환*/
/* Arguments    : IpVal  => Unsigned Long 형태의 IP Addr         */
/* Returns      : ipaddr => String형태의 IP Addr                 */
/*---------------------------------------------------------------*/
char  *CvtIPulong2chr (IpVal)
ulong  IpVal;
{
char   *ipaddr;
struct sockaddr_in    sockaddr;
struct in_addr        ipget;

    ipget.s_addr = IpVal;
    ipaddr       = (char *) inet_ntoa (ipget);

    return (ipaddr);
}

/*---------------------------------------------------------------*/
/* Function Name: MakeCommHeader                                 */
/* Action       : 이체 서버와 업무계 서버간의 통신 헤더 조립     */
/* Arguments    : Command => 전송 명령                           */
/*                TrxSeq  => 송신용 거래 순번                    */
/*                MakBuf  => Header를 추가한 반환용 버퍼 포인터. */
/*                OrgBuf  => Header를 포함한 전송하려는 버퍼의   */
/*                           포인터.                             */
/*                TotLng  => 조립을 하려고 하는 버퍼의 총 길이   */
/* Returns      : sendlng => Header 포함한 버퍼의  길이.         */
/*---------------------------------------------------------------*/
int    MakeCommHeader (Command, TrxSeq, Chain, MakBuf, OrgBuf, TotLng)
int    Command;
int    TrxSeq;
int    Chain;
uchar *MakBuf;
uchar *OrgBuf;
int    TotLng;
{
int         hdlng, sendlng;
int         datalng;
BpComHead  *chead;

    chead = (BpComHead *) MakBuf;
    hdlng = sizeof (BpComHead);

    memset (MakBuf, 0x00, hdlng);

	/*-----------------------------------------------------------*/
    /* Command Set                                               */
	/*-----------------------------------------------------------*/
    chead->Cmd     = Command;
    chead->Lng [0] = TotLng >> 24;
    chead->Lng [1] = TotLng >> 16;
    chead->Lng [2] = TotLng >> 8;
    chead->Lng [3] = TotLng % 256;

    chead->Seq [0] = TrxSeq / 256;
    chead->Seq [1] = TrxSeq % 256;

    chead->CF      = Chain;

    memcpy (&MakBuf[hdlng], OrgBuf, TotLng);
    sendlng = TotLng + hdlng;

    return (sendlng);
}


/*---------------------------------------------------------------*/
/* Function Name: ServerSocketInit                               */
/* Action       : 서버용으로 사용되는 소켓 초기화 함수.          */
/* Arguments    : PortNumber => Client가 접속할 서버의 Port 번호 */
/* Returns      : sockid     => Socket ID                        */
/*---------------------------------------------------------------*/
int    ServerSocketInit (PortNumber)
int    PortNumber;
{
int    sockid;
struct sockaddr       serv_addr;
struct sockaddr_in   *addr_in;
struct linger        li;

    if ((sockid = socket (AF_INET, SOCK_STREAM, 0)) < 0)  {
        fprintf (stderr,"tcpapi: can't open stream socket.\n" );
        fflush (stderr);
        exit (1);
    }

    memset (&serv_addr, 0x00, sizeof (struct sockaddr));

    addr_in = (struct sockaddr_in *) &serv_addr;
    addr_in->sin_family      = AF_INET ;
    addr_in->sin_addr.s_addr = htonl (INADDR_ANY);
    addr_in->sin_port        = htons (PortNumber);

    SetSocketOpt (sockid);

    if (bind (sockid, &serv_addr, sizeof(struct sockaddr)) < 0 ) {
        fprintf (stderr, "tcpapi: can't bind local addr = %d\n" , PortNumber);
        fflush (stderr);

        close (sockid);
        exit (1);
    }
    listen (sockid, 100);

    return (sockid);
}

/*---------------------------------------------------------------*/
/* Function Name: ServerAcceptClient                             */
/* Action       : Client의 접속을 기다리는 함수                  */
/* Arguments    : Sock   => Parent의 Socket ID                   */
/* Returns      : sockid => Client와 통신을 위한 Socket ID       */
/*---------------------------------------------------------------*/
int   ServerAcceptClient (Sock)
int   Sock;
{
int    ssock, clilen, sndlng, retval;
struct sockaddr      cli_addr;

    memset (&cli_addr, 0x00, sizeof (struct sockaddr));

    clilen = sizeof (cli_addr);

    ssock  = accept (Sock, &cli_addr, (int *)&clilen);

    return  (ssock);
}

/*---------------------------------------------------------------*/
/* Function Name: GetPeerName                                    */
/* Action       : 해당 Client의 접속 상태 Check.                 */
/* Arguments    : Sock   => Client와 접속된 Socket ID            */
/*                IPAddr => 성공시 해당 Client의 IPAddr 저장버퍼 */
/* Returns      : port   => Client와 접속된 Port 번호            */
/*---------------------------------------------------------------*/
int    GetPeerName (Sock, IPAddr)
int    Sock;
char  *IPAddr;
{
int    sockalng;
char   *ipaddr;
struct sockaddr_in    sockaddr;
struct in_addr        ipget;

    sockalng = sizeof (struct sockaddr_in);

    if(getpeername(Sock,(struct sockaddr*)&sockaddr, (int *)&sockalng)<0)
        return (0);
    else {
        ipget.s_addr = sockaddr.sin_addr.s_addr;
        ipaddr       = (char *) inet_ntoa (ipget);

        sprintf (IPAddr, "%s", ipaddr);
        return (sockaddr.sin_port);
    }
}


/*---------------------------------------------------------------*/
/* Function Name: SetSocketOpt                                   */
/* Action       : 소켓과 연관된 옵션을 조작.                     */
/* Arguments    : SockId    => 조작 하려는 소켓.                 */
/* Returns      : None                                           */
/*---------------------------------------------------------------*/
void   SetSocketOpt (SockId)
int    SockId;
{
int    optval = 1;

    setsockopt(SockId,SOL_SOCKET,SO_REUSEADDR,(char *)&optval,sizeof(optval));
    optval = 1;
    setsockopt(SockId,SOL_SOCKET,SO_KEEPALIVE,(char *)&optval,sizeof(optval));

    return;
}

/*---------------------------------------------------------------*/
/* Function Name: ConnectClient2Server                           */
/* Action       : Client에서 서버에 접속 요구.                   */
/* Arguments    : Port    => 서버에 접속할 Port Number           */
/*                IpAddrL => 접속할 서버의 IP Addr               */
/* Returns      : sockid  => Accept Socket ID                    */
/*---------------------------------------------------------------*/
int    ConnectClient2Server (Port, IpAddrL)
int    Port;
ulong  IpAddrL;
{
int     sockfd, ret;
int     optval = 1;
struct sockaddr_in  serv_addr, SockAddr;

    if ((sockfd = socket (AF_INET, SOCK_STREAM, 0)) < 0)
         return (-1);

    setsockopt(sockfd,SOL_SOCKET,SO_REUSEADDR,(char *)&optval,sizeof(optval));
    optval = 1;
    setsockopt(sockfd,SOL_SOCKET,SO_KEEPALIVE,(char *)&optval,sizeof(optval));



    bzero ((char*) &serv_addr, sizeof (serv_addr));
    

    serv_addr.sin_family      = AF_INET;
    serv_addr.sin_addr.s_addr = IpAddrL;
    serv_addr.sin_port        = htons (Port);

	/*
    ret = connect (sockfd,(struct sockaddr*)&serv_addr,sizeof(serv_addr));
    if (ret < 0){
        close (sockfd);
        return (-2);
    }
    return (sockfd);
    */
    if (ConnectWait(sockfd,(struct sockaddr*)&serv_addr,sizeof(serv_addr), 5) < 0) {
    	close (sockfd);
        return (-1);
    }
    else{
        return (sockfd);
    }
}


/*---------------------------------------------------------------*/
/* Function Name: ConnectWait                                    */
/* Action       : Client에서 서버에 접속 요구.                   */
/* Arguments    : Port    => 서버에 접속할 Port Number           */
/*                IpAddrL => 접속할 서버의 IP Addr               */
/* Returns      : sockid  => Accept Socket ID                    */
/*---------------------------------------------------------------*/
int 	ConnectWait	(int sockfd, struct sockaddr *saddr, int addrsize,int sec){

    int newSockStat;
    int orgSockStat;
    int res,n;
    fd_set rset,wset;
    struct timeval tval;

    int error = 0;
    int esize;

    if ((newSockStat = fcntl(sockfd,F_GETFL,NULL)) <0){
        return -1;
    }

    orgSockStat = newSockStat;

    newSockStat |= O_NONBLOCK;

    if (fcntl(sockfd,F_SETFL,newSockStat) < 0){
        return -1;
    }

    if((res = connect(sockfd,saddr,addrsize)) < 0){
        if (errno != EINPROGRESS){
            return -1;
        }
    }


    if (res == 0){
        fcntl(sockfd,F_SETFL,orgSockStat);
        return 1;
    }

    FD_ZERO(&rset);
    FD_SET(sockfd,&rset);
    wset = rset;

    tval.tv_sec = sec;
    tval.tv_usec = 0;

    if ((n = select(sockfd+1, &rset, &wset, NULL, &tval)) == 0){
        errno = ETIMEDOUT;
        return -1;
    }


    if (FD_ISSET(sockfd,&rset) || FD_ISSET(sockfd,&wset) ){
        esize = sizeof(int);
        if((n=getsockopt(sockfd,SOL_SOCKET,SO_ERROR,&error,(int *)(socklen_t *)&esize)) < 0)
            return -1;
    }
    else{
        return -1;
    }

    fcntl(sockfd,F_SETFL,orgSockStat);
    if(error){
        errno = error;
        return -1;
    }

    return 1;
}

/*---------------------------------------------------------------*/
/*                E N D   O F   F I L E                          */
/*---------------------------------------------------------------*/
