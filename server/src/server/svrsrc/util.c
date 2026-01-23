
/*-----------------------------------------------------------------
 ¦£¦¡¦¡¦¡¦¡¦¡¦¡¦¨¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¤
 ¦¢ ÇÁ·α׷¥¸í ¦¢ util.c                                       ¦¢
 ¦§¦¡¦¡¦¡¦¡¦¡¦¡¦«¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦©
 ¦¢ ±â      ´É ¦¢ ÇÁ·α׷¥ »󿡼­ Çʿä·ÎÇϴÂ ¿©·¯ ÇԼöµéÀÌ     ¦¢
 ¦¢            ¦¢ µé¾î ÀִÂ ÇÁ·α׷¥                           ¦¢
 ¦§¦¡¦¡¦¡¦¡¦¡¦¡¦«¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦©
 ¦¢ ÀÛ  ¼º  ÀÏ ¦¢ 2004.  3. 26                                 ¦¢
 ¦§¦¡¦¡¦¡¦¡¦¡¦¡¦«¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦©
 ¦¢ ÀÛ  ¼º  ÀÚ ¦¢ ÃÖ   º´   ³²                                 ¦¢
 ¦¦¦¡¦¡¦¡¦¡¦¡¦¡¦ª¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¥
-----------------------------------------------------------------*/

#include 	<ecamsapi.h>
#include 	<ctype.h>


/*---------------------------------------------------------------*/
/*       Socket Common  PROCEDURE  DEFINE                        */
/*---------------------------------------------------------------*/
#if defined(__STDC__) || defined(__cplusplus) || defined(__sun)
char        *Get_String_Time     (void);
struct tm   *Get_Struct_Time     (void);
void         Get_Sys_Date        (char *);
void         Get_File_Date       (char *, char *);
void         Get_Sys_Slash_Date  (char *);
void         Get_Sys_Time        (char *);
void         Get_Sys_Time10      (char *);
void         DumpData            (FILE *, u_char *, int , int );
int          ExistReadData       (int , int , int );
int          ExistSendData       (int , int , int );
void         Usleep              (int );
uchar        BccCompute          (uchar *, int );
void         UL2Ch4              (ulong, uchar *);
void         Pc2UnixPath         (char *);
void         ConvData2Unpack     (char *, char *, int );
#else
char        *Get_String_Time     ();
struct tm   *Get_Struct_Time     ();
void         Get_Sys_Date        ();
void         Get_File_Date       ();
void         Get_Sys_Slash_Date  ();
void         Get_Sys_Time        ();
void         Get_Sys_Time10      ();
void         Get_Sys_Time20      ();
void         DumpData            ();
int          ExistReadData       ();
int          ExistSendData       ();
void         Usleep              ();
uchar        BccCompute          ();
void         UL2Ch4              ();
void         Pc2UnixPath         ();
void         ConvData2Unpack     ();
#endif

/*---------------------------------------------------------------*/
/* 		Table for translating EBCDIC to ASCII                    */
/*---------------------------------------------------------------*/
static char ebcprint[256] = {
/* 00 */   '.',  '.',  '.',  '.',  '.',  '.',  '.',  '.',
/* 08 */   '.',  '.',  '.',  '.',  '.',  '.',  '.',  '.',
/* 10 */   '.',  '.',  '.',  '.',  '.',  '.',  '.',  '.',
/* 18 */   '.',  '.',  '.',  '.',  '*',  '.',  ';',  '.',
/* 20 */   '.',  '.',  '.',  '.',  '.',  '.',  '.',  '.',
/* 28 */   '.',  '.',  '.',  '.',  '.',  '.',  '.',  '.',
/* 30 */   '.',  '.',  '.',  '.',  '.',  '.',  '.',  '.',
/* 38 */   '.',  '.',  '.',  '.',  '.',  '.',  '.',  '.',
/* 40 */   ' ',  '.',  '.',  '.',  '.',  '.',  '.',  '.',
/* 48 */   '.',  '.',  '\\',  '.',  '<',  '(',  '+',  '|',
/* 50 */   '&',  '.',  '.',  '.',  '.',  '.',  '.',  '.',
/* 58 */   '.',  '.',  '!',  '$',  '*',  ')',  ';',  '^',
/* 60 */   '-',  '/',  '.',  '.',  '.',  '.',  '.',  '.',
/* 68 */   '.',  '.',  '.',  ',',  '%',  '_',  '>',  '?',
/* 70 */   '.',  '^',  '.',  '.',  '.',  '.',  '.',  '.',
/* 78 */   '.',  '`',  ':',  '#',  '@', '\'',  '=',  '"',
/* 80 */   '.',  'a',  'b',  'c',  'd',  'e',  'f',  'g',
/* 88 */   'h',  'i',  '.',  '.',  '.',  '.',  '.',  '.',
/* 90 */   '.',  'j',  'k',  'l',  'm',  'n',  'o',  'p',
/* 98 */   'q',  'r',  '.',  '.',  '.',  '.',  '.',  '.',
/* A0 */   '.',  '~',  's',  't',  'u',  'v',  'w',  'x',
/* A8 */   'y',  'z',  '.',  '.',  '.',  '[',  '.',  '.',
/* B0 */   '.',  '.',  '.',  '.',  '.',  '.',  '.',  '.',
/* B8 */   '.',  '.',  '.',  '.',  '.',  ']',  '.',  '.',
/* C0 */   '{',  'A',  'B',  'C',  'D',  'E',  'F',  'G',
/* C8 */   'H',  'I',  '.',  '.',  '.',  '.',  '.',  '.',
/* D0 */   '}',  'J',  'K',  'L',  'M',  'N',  'O',  'P',
/* D8 */   'Q',  'R',  '.',  '.',  '.',  '.',  '.',  '.',
/* E0 */  '\\',  '.',  'S',  'T',  'U',  'V',  'W',  'X',
/* E8 */   'Y',  'Z',  '.',  '.',  '.',  '.',  '.',  '.',
/* F0 */   '0',  '1',  '2',  '3',  '4',  '5',  '6',  '7',
/* F8 */   '8',  '9',  '.',  '.',  '.',  '.',  '.',  '.',
};


/*---------------------------------------------------------------*/
/* Function Name: Get_String_Time                                */
/* Action       : Get System String Time                         */
/* Arguments    : None                                           */
/* Returns      : String Time                                    */
/*---------------------------------------------------------------*/
char *Get_String_Time ()
{
    char      *time_string;
    time_t    time_clock;

    time_clock = time(NULL);

    time_string = ctime(&time_clock);
    *(strchr(time_string, '\n')) = '\0';
    return (time_string);
}


/*---------------------------------------------------------------*/
/* Function Name: Get_Struct_Time                                */
/* Action       : Get System Struct Time                         */
/* Arguments    : None                                           */
/* Returns      : Struct Time                                    */
/*---------------------------------------------------------------*/
struct tm *Get_Struct_Time ()
{
    struct tm *local_time;
    time_t    time_clock;

    time_clock = time(NULL);
    local_time = localtime(&time_clock);
    return (local_time);
}


/*---------------------------------------------------------------*/
/* Function Name: Get_Sys_Date                                   */
/* Action       : Get System Date                                */
/* Arguments    : get_date = Buffer for move the Date            */
/* Returns      : get_date = Get System Date                     */
/*---------------------------------------------------------------*/
void Get_Sys_Date(get_date)
char    *get_date;
{
    struct tm    *loc;
    char         sys_date[9];

    loc = Get_Struct_Time();

    memset(sys_date, 0x00, sizeof(sys_date));
    sprintf(sys_date,"%d%002d%002d",
            loc->tm_year + 1900, loc->tm_mon + 1, loc->tm_mday);
    memcpy(get_date, sys_date, sizeof(sys_date));
}


/*---------------------------------------------------------------*/
/* Function Name: Get_File_Date                                  */
/* Action       : Get File Date                                  */
/* Arguments    : file_date = Buffer for move the Date           */
/*                file_name = File Name for Get Date             */
/* Returns      : file_date = Get File Date                      */
/*---------------------------------------------------------------*/
void Get_File_Date(file_date, file_name)
char    *file_date, *file_name;
{
time_t        time_clock;
struct stat   f_st;
struct tm    *loc;

    if (stat (file_name, &f_st) < 0) {
        strcpy (file_date, "00000000");
        return;
    }

    loc = localtime(&f_st.st_mtime);

    memset(file_date, 0x00, sizeof(file_date));
    sprintf(file_date,"%d%002d%002d",
              loc->tm_year + 1900, loc->tm_mon + 1, loc->tm_mday);

    return;
}


/*---------------------------------------------------------------*/
/* Function Name: Get_Sys_Slash_Date                             */
/* Action       : Get System Date & insert Delimiter             */
/* Arguments    : get_date = File Name for Get Date              */
/* Returns      : get_date = Get System Slash Date               */
/*---------------------------------------------------------------*/
void Get_Sys_Slash_Date(get_date)
char    *get_date;
{
    struct tm    *loc;
    char         sys_date[11];

    loc = Get_Struct_Time();

    memset(sys_date, 0x00, sizeof(sys_date));
    sprintf(sys_date,"%d/%002d/%002d",
            loc->tm_year + 1900, loc->tm_mon + 1, loc->tm_mday);
    memcpy(get_date, sys_date, sizeof(sys_date));
}

/*---------------------------------------------------------------*/
/* Function Name: Get_Sys_Time                                   */
/* Action       : Get System Time                                */
/* Arguments    : get_time = Buffer for Get Date                 */
/* Returns      : get_time = Get System Time  Buffer             */
/*---------------------------------------------------------------*/
void Get_Sys_Time(get_time)
char    *get_time;
{
    struct tm    *loc;
    char         sys_time[7];

    loc = Get_Struct_Time();

    memset(sys_time, 0x00, sizeof(sys_time));
    sprintf(sys_time,"%002d%002d%002d",
            loc->tm_hour, loc->tm_min, loc->tm_sec);
    memcpy(get_time, sys_time, sizeof(sys_time));
}


/*---------------------------------------------------------------*/
/* Function Name: Get_Sys_Time10                                 */
/* Action       : Get System Time (char 8 Bytes)                 */
/* Arguments    : get_time = Buffer for Get Date                 */
/* Returns      : get_time = Get System Time  Buffer             */
/*---------------------------------------------------------------*/
void  Get_Sys_Time10 (get_time)
char *get_time;
{
char   sys_time [12];
struct tm      *loc;
struct timeval  time_val;

    loc = Get_Struct_Time();
    gettimeofday(&time_val, NULL);

    memset(sys_time, 0x00, sizeof(sys_time));

    sprintf(sys_time,"%002d%002d%002d%004d",
      loc->tm_hour, loc->tm_min, loc->tm_sec, (int )time_val.tv_usec/100);

    memcpy(get_time, sys_time, sizeof(sys_time));

    return;
}

void  Get_Sys_Time20 (get_time)
char *get_time;
{
char   sys_time [16];
struct tm      *loc;
struct timeval  time_val;

    loc = Get_Struct_Time();
    gettimeofday(&time_val, NULL);

    memset(sys_time, 0x00, sizeof(sys_time));

    sprintf(sys_time,"%02d:%02d:%02d.%06d", loc->tm_hour, loc->tm_min, loc->tm_sec, (int)time_val.tv_usec);
    memcpy(get_time, sys_time, sizeof(sys_time));

    return;
}


/*---------------------------------------------------------------*/
/* Function Name: DumpData ()                                    */
/* Action       : Dump a buffer in both hexidecimal and EBCDIC   */
/*                (translate)  EBCDIC to ASCII for display       */
/* Arguments    : file            output file for dumping        */
/*                length          length of data to display      */
/*                buffer          pointer to data to display     */
/*                ascii_data      flag indicating whether EBCDIC */
/*				                  or ASCII Display.              */
/* Returns      : No value                                       */
/*---------------------------------------------------------------*/
void   DumpData (file, buffer, length, ascii_data)
FILE   *file;
u_char *buffer;
int     length, ascii_data;
{
    int        i;
    int        c;
    char       hex[80], *time;
    char       ascii[32], time10 [16];

    if (length == 0 || buffer == NULL)  return;

    hex[0] = '\0';
    ascii[0] = '\0';

    /*-----------------------------------------------------------*/
    /* Dump hex and EBCDIC/ASCII data.  Accumlate hex and ascii  */
    /* buffers  until an entire line can be written.             */
    /*-----------------------------------------------------------*/
    memset (time10, 0x00, sizeof (time10));
    Get_Sys_Time10(time10);
    fprintf (file, "TIME : %s length = %d\n", time10, length);

    for (i = 0; i < length; i++) {
        if (length > dfMaxBufSize) break;
        if ((i%24) == 0 && i != 0) {
            sprintf(hex,"%s     %s", hex, ascii);
            ascii[0] = '\0';

            fprintf(file, "%s\n", hex);
            hex[0] = '\0';
        }
        else if ((i%8) == 0 && i != 0)
            sprintf(hex, "%s ", hex);

        sprintf(hex, "%s%02x", hex, buffer[i]);

        if (ascii_data == ON) {
            c = buffer[i];
            if (!isascii(c) ||  !isprint(c))  c = '.';
        }
        else
            c = ebcprint[buffer[i]&0xff];
        sprintf(ascii, "%s%c", ascii, c);
    }

    /*-----------------------------------------------------------*/
    /* Dump the remaining portion of incomplete lines            */
    /*-----------------------------------------------------------*/
    if (i = i%24) {
        if (i < 9)           i--;
        else if (i < 17)     strcat(hex, " " );
        for ( ; i < 24; i++) strcat(hex, "  ");
    }
    sprintf(hex, "%s     %s", hex, ascii);
    fprintf(file, "%s\n", hex);
    fflush(file);
}


/*---------------------------------------------------------------*/
/* Function Name: ExistSendData                                  */
/* Action       : Check Exist Send On the Socket                 */
/* Arguments    : fd   = Connected Socket ID                     */
/*                sec  = Check for Time Sec until Status is Up   */
/*                usec = Check for Time Micro Sec until Status   */
/*                       is Up                                   */
/* Returns      : ret_val = Return Value for Status              */
/*---------------------------------------------------------------*/
int ExistSendData (fd, sec, usec)
int fd, sec, usec;
{
    int		       retval;
    fd_set	       readfds;
    struct timeval timeout;

    FD_ZERO (&readfds);
    FD_SET (fd, &readfds);

    timeout.tv_sec  = sec;
    timeout.tv_usec = usec;

    retval = select (FD_SETSIZE, NULL, &readfds, NULL, &timeout);
    if (retval < 0) return (retval);

    retval = FD_ISSET (fd, &readfds);

    return (retval);
}

/*---------------------------------------------------------------*/
/* Function Name: ExistReadData                                  */
/* Action       : Check Exist Send On the Socket                 */
/* Arguments    : fd   = Connected Socket ID                     */
/*                sec  = Check for Time Sec until Status is Up   */
/*                usec = Check for Time Micro Sec until Status   */
/*                       is Up                                   */
/* Returns      : ret_val = Return Value for Status              */
/*---------------------------------------------------------------*/
int ExistReadData (fd, sec, usec)
int fd, sec, usec;
{
    int		       retval;
    fd_set	       readfds;
    struct timeval timeout;

    FD_ZERO (&readfds);
    FD_SET (fd, &readfds);

    timeout.tv_sec  = sec;
    timeout.tv_usec = usec;

    retval = select (FD_SETSIZE, &readfds, NULL, NULL, &timeout);
    if (retval < 0) return (retval);

    retval = FD_ISSET (fd, &readfds);

    return (retval);
}

/*---------------------------------------------------------------*/
/* Function Name: Usleep                                         */
/* Action       : Sleep until the Micro Second                   */
/* Arguments    : usec = want to sleep micro second              */
/* Returns      : None                                           */
/*---------------------------------------------------------------*/
void Usleep (usec)
int usec;
{
    struct timeval timeout;

    timeout.tv_sec  = 0;
    timeout.tv_usec = usec;

    select (0, NULL, NULL, NULL, &timeout);

    return;
}

/*---------------------------------------------------------------*/
/* Function Name: BccCompute                                     */
/* Action       : Calcuate the Exclusive OR                      */
/* Arguments    : Buf = Buffer for Calculating                   */
/*                Lng = Buffer Size                              */
/* Returns      : Val = BCC Value                                */
/*---------------------------------------------------------------*/
uchar BccCompute (Buf, Lng)
uchar *Buf;
int    Lng;
{
    int   i;
    uchar Val = 0;

    Val = Buf[0];
    for (i = 1; i < Lng; i++)
         Val ^= Buf[i];

    return (Val);
}

/*---------------------------------------------------------------*/
/* Function Name: UL2Ch4                                         */
/* Action       : Convert Unsigned Long to Char 4 Bytes          */
/* Arguments    : Ulnum = Unsigned Long Value                    */
/*                CalBuf = Converted Data Move Buffer            */
/* Returns      : None                                           */
/*---------------------------------------------------------------*/
void   UL2Ch4  (Ulnum, CalBuf)
ulong     Ulnum;
uchar    *CalBuf;
{
     CalBuf [0] = Ulnum / 256 / 256 / 256 % 256;
     CalBuf [1] = Ulnum / 256 / 256 % 256;
     CalBuf [2] = Ulnum / 256 % 256;
     CalBuf [3] = Ulnum % 256;

     return ;
}

/*---------------------------------------------------------------*/
/* Function Name: Pc2UnixPath                                    */
/* Action       : Convert PC Directory Path to Unix              */
/* Arguments    : Fname = Full Path File Name                    */
/* Returns      : None                                           */
/*---------------------------------------------------------------*/
void  Pc2UnixPath  (Fname)
char *Fname;
{
int  cnt, nsz;

    nsz = strlen ((char *) Fname);
    for (cnt = 0; cnt < nsz; cnt++) {
         if (Fname[cnt] == '\\')
             Fname[cnt]  = '/';
    }

    return;
}


/*---------------------------------------------------------------*/
/* Function Name: ConvData2Unpack                                */
/* Action       : Data UnPack                                    */
/* Arguments    : SrcData  = Source Data                         */
/*                DestData = UnPack Data                         */
/*                Lng      = UnPack Data Length                  */
/* Returns      : None                                           */
/*---------------------------------------------------------------*/
void    ConvData2Unpack (char *SrcData, char *DestData, int Lng)
{
int    ff, cnt;

    for (ff = 0, cnt = 0; ff < Lng; ff ++) {
         DestData [cnt++] = ((SrcData [ff] & 0xf0) >> 4) + 0x30;
         DestData [cnt++] = (SrcData [ff] & 0x0f) + 0x30;
    }

    return;
}


/*---------------------------------------------------------------*/
/* Function Name: ConvData2Pack                                  */
/* Action       : Data Pack                                      */
/* Arguments    : SrcData  = Source Data                         */
/*                DestData = Pack Data                           */
/*                Lng      = Pack Data Length                    */
/* Returns      : None                                           */
/*---------------------------------------------------------------*/
void    ConvData2Pack (char *SrcData, char *DestData, int Lng)
{
int    ff, cnt;

    for (ff = 0, cnt = 0; ff < Lng; ){
         DestData [cnt]    = ((SrcData [ff++] & 0x0f) << 4);
         DestData [cnt++] |=  (SrcData [ff++] & 0x0f);
    }

    return;
}

/*
#undef system
int	callsystem(char *szCommand){
	char cmdbuf[10000];

	int	 cmdidx;
	int  bufidx;

	int	 chkawk;


	memset(cmdbuf, 0x00,sizeof(cmdbuf));


	cmdidx = 0;
	bufidx = 0;

	if (Char_Check(szCommand,"awk ") >= 0){
		chkawk = 1;
	}
	else{
		chkawk = 0;
	}


	while( szCommand[cmdidx] != 0x00 ){
		if (szCommand[cmdidx] == 0x24 && chkawk==0){
			cmdbuf[bufidx++] = 0x5c; 
			cmdbuf[bufidx++] = szCommand[cmdidx];
		}
		else{
			cmdbuf[bufidx++] = szCommand[cmdidx];
		}

		cmdidx++;
	}

	return system(cmdbuf);
}
#define system(t) callsystem(t)
*/

#undef system
int	callsystem(char *szCommand){
	char cmdbuf[10000];
  char szPid [100];
  char szCmdFile[100];
  char szRstFile[100];
  FILE *SRCPtr;
  
  system("mkdir tmp 1>/dev/null 2>&1");
  
  sprintf (szPid, "%d", getpid());
  sprintf (szCmdFile, "tmp/cmd_%s", szPid);
  sprintf (szRstFile, "tmp/rst_%s", szPid);
  
  if((SRCPtr = fopen(szCmdFile, "w+")) == (FILE *)NULL) {
  	printf ( ">>> Read Open Error:[%s]\n", szCmdFile);
  	return(-1);
  }
  
  fprintf(SRCPtr, "%s\n", szCommand);
  fprintf(SRCPtr, "echo $? > %s\n", szRstFile);
  fclose(SRCPtr);
  
  sprintf (cmdbuf, "ksh %s", szCmdFile);
  system(cmdbuf);
  remove(szCmdFile);
  
  if((SRCPtr = fopen(szRstFile, "r")) == (FILE *)NULL) {
  	printf(">>> Read Open Error :[%s]\n", szRstFile);
  	return(-1);
  }
  
  while (fgets(cmdbuf, 1024, SRCPtr) != (char *)NULL) {
  	strcpy(cmdbuf, (char*)rep_char(cmdbuf, "\r\n", ""));
  	strcpy(cmdbuf, (char*)rep_char(cmdbuf, "\r", ""));
  	strcpy(cmdbuf, (char*)rep_char(cmdbuf, "\n", ""));
  	break;
  }
  fclose(SRCPtr);
  remove(szRstFile);
  
  return(atoi(cmdbuf)*256);
  
 }
#define system(t) callsystem(t)

/*---------------------------------------------------------------*/
/*                E N D   O F   F I L E                          */
/*---------------------------------------------------------------*/
