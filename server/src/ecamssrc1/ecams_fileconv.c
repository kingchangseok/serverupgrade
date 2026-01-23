/*-----------------------------------------------------------------
 ¦£¦¡¦¡¦¡¦¡¦¡¦¡¦¨¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¤
 ¦¢ ÇÁ·α׷¥¸í ¦¢ ecams_fileconv.c                             ¦¢
 ¦§¦¡¦¡¦¡¦¡¦¡¦¡¦«¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦©
 ¦¢ ±â      ´É ¦¢ ÆÄÀÏÀÇ Äڵå º¯ȯ (DOS <-> UNIX)              ¦¢
 ¦§¦¡¦¡¦¡¦¡¦¡¦¡¦«¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦©
 ¦¢ ÀÛ  ¼º  ÀÏ ¦¢ 2011. 08. 12                                 ¦¢
 ¦§¦¡¦¡¦¡¦¡¦¡¦¡¦«¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦©
 ¦¢ ÀÛ  ¼º  ÀÚ ¦¢ ÃÖ   º´   ³²                                 ¦¢
 ¦¦¦¡¦¡¦¡¦¡¦¡¦¡¦ª¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¡¦¥
-----------------------------------------------------------------*/

#define		dfMain		1

#include    <ecamsapi.h>
#include 	<ecams_util.h>

/*---------------------------------------------------------------*/
/*       USER  PROCEDURE  DEFINE                                 */
/*---------------------------------------------------------------*/

/*---------------------------------------------------------------*/
/*    User Work º¯¼ö                                             */
/*---------------------------------------------------------------*/
char   **gEnvp;                     /* Parameter Áշá¿ë Pointer  */
extern  int     errno;              /* ½ýºÅÛ ¿À·ùÄڵå           */


/*****************************************************************/
/*                                                               */
/*       ÆÄÀÏÀÇ Äڵå º¯ȯ (DOS <-> UNIX) ó¸® MAIN  LOGIC        */
/*                                                               */
/*****************************************************************/
int		main (int argc, char **argv, char **envp)
{
char    szCMD               [dfFullPath];
char    OLDSRC              [dfFullPath];
char    RSTSRC              [dfFullPath];
char	*dummy		                    ;
char	*dummy1		                    ;
int		retval                          ;
int 	lng                             ;
int 	file_lng = 0                    ;
struct stat filestat                    ;
int		UNICODE	= FALSE                 ;
FILE	*SrcPtr                         ;
FILE	*RstPtr                         ;
char	filename            [dfFullPath];
int		nFileSize                       ;


	if (argc != 3) {
		fprintf (stderr, "\nUSAGE : %s <FileName> <±¸ºÐ> \n", argv[0]);
		fprintf (stderr, "  ±¸ºÐ -> 1 : TO UNIX, 2 : TO WINDOWS \n\n");
		exit (1);
	}

   	sprintf(filename, "%s.log", argv[0]);
	#if 0
	TRACE = fopen (filename, "w+");
	if (TRACE == NULL)  {
		fprintf (stderr, "%s : file Open Err\n", filename);
		fflush  (stderr);
		exit (0);
	}
	#endif

	/*-----------------------------------------------------------*/
	/*   File COPY (OLD File)                                    */
	/*-----------------------------------------------------------*/
	sprintf(OLDSRC, "%s"     , argv[1]);
	sprintf(RSTSRC, "%s.conv", argv[1]);


	/*-----------------------------------------------------------*/
	/*   File Open (OLD/NEW File)                                */
	/*-----------------------------------------------------------*/
	if ((SrcPtr = fopen(OLDSRC,"r")) == (FILE *) NULL) {
		printf("FILE OPEN ERROR %s\n", OLDSRC);
		exit (1);
	}

	/*-----------------------------------------------------------*/
	/*   File Open (Result File)                                 */
	/*-----------------------------------------------------------*/
	if ((RstPtr = fopen(RSTSRC,"w")) == (FILE *) NULL) {
		printf("FILE OPEN ERROR %s\n", RSTSRC);
		fclose (SrcPtr);
		exit (2);
	}

	nFileSize = FileSizeInf(OLDSRC) + 1;
	dummy = (char *)malloc(nFileSize);
	memset(dummy, 0x00, nFileSize);

	nFileSize = FileSizeInf(OLDSRC) + 1;
	dummy1 = (char *)malloc(nFileSize * 1.5);
	memset(dummy1, 0x00, nFileSize);
		
	while (1) {
		memset(dummy, 0x00, sizeof(dummy));
		if (fgets(dummy, 2048, SrcPtr) == (char *)NULL) {
			break;
		}

		if (memcmp(argv[2], "1", 1) == 0) {
			sprintf(dummy1, "%s", rep_char(dummy , "\r\n", "\n"));
			sprintf(dummy1, "%s", rep_char(dummy1, "\r"  , "\n"));
			fprintf(RstPtr, "%s"  , dummy1);
		}
		else {
			sprintf(dummy1, "%s", rep_char(dummy , "\r\n", "\n"  ));
			sprintf(dummy1, "%s", rep_char(dummy1, "\r"  , "\n"  ));
			sprintf(dummy1, "%s", rep_char(dummy1, "\n"  , ""));
			fprintf(RstPtr, "%s"  , dummy1);
		}
	}
	fclose (SrcPtr);
	fclose (RstPtr);

	sprintf(szCMD, "mv %s %s", RSTSRC, OLDSRC);
	system (szCMD);

	exit (0);
}

/*---------------------------------------------------------------*/
/*             E N D    O F    P R O G R A M                     */
/*---------------------------------------------------------------*/
