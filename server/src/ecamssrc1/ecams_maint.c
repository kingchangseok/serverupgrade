/*-----------------------------------------------------------------
 ┌──────┬───────────────────────┐
 │ 프로그램명 │ ecams_maint.c                                │
 ├──────┼───────────────────────┤
 │ 기      능 │ eCAMS UNIX FILE System Maint Module          │
 ├──────┼───────────────────────┤
 │ 작  성  일 │ 2005. 12. 15                                 │
 ├──────┼───────────────────────┤
 │ 작  성  자 │ 최   병   남                                 │
 └──────┴───────────────────────┘
-----------------------------------------------------------------*/
#define		dfMain	1

#include <ecamsapi.h>

/*---------------------------------------------------------------*/
/*       USER  PROCEDURE  DEFINE                                 */
/*---------------------------------------------------------------*/



/*---------------------------------------------------------------*/
/*   SOCKET 통신관련  CONSTANT                                   */
/*---------------------------------------------------------------*/
char     indat              [dfFullPath];      /* Process Name Work */

time_t time_clock;
struct stat  f_st;
struct tm   *loc;


/*****************************************************************/
/*                                                               */
/*       eCAMS UNIX FILE System Maint  M A I N                   */
/*                                                               */
/*****************************************************************/
int		main(int argc, char **argv, char **envp)
{
char 	SysCmd              [dfFullPath]; /* 시스템 명령문       */
char 	FileName            [dfFullPath]; /* File Name           */
char 	MaintFile           [dfFullPath]; /* File Name           */
char 	FileBuf                 [dfFile]; /* File Read Buffer    */
char 	FileDate                    [10]; /* File 변경일자       */
int  	ret                             ; /* 처리결과            */
FILE 	*FCHK                           ; /* File Open Pointer   */

	if (argc != 4) {
		fprintf(stderr, "Usage : %s <대상 Directory> <삭제기준일자> <Local File Name>\n\n", argv[0]);
		exit (1);
	}

	sprintf(FileName, "%s", argv[3]);
	sprintf(SysCmd, "ls -rt %s > %s", argv[1], FileName);
	system (SysCmd);

	NotUseDataTruncate(FileName, 0x20);
	if ((FCHK = fopen(FileName, "r")) == (FILE *) NULL)
		return 1;

	while ((fgets(FileBuf, dfFullPath, FCHK)) != (char *) NULL ) {
		sprintf(FileBuf, "%s", rep_char(FileBuf, "\r\n", ""));
		sprintf(FileBuf, "%s", rep_char(FileBuf, "\n", ""));
		NotUseDataTruncate(FileBuf, 0x20);

		sprintf(MaintFile, "%s/%s", argv[1], FileBuf);

		/*-------------------------------------------------------*/
		/* 파일의 변경일자와 삭제기준일과 비교하여 파일의 일자가 */
		/* 삭제기준일 이전인경우 삭제처리                        */
		/*-------------------------------------------------------*/
		if (stat(MaintFile, &f_st) >= 0) {
			loc = localtime(&f_st.st_mtime);
		    sprintf(FileDate, "%d%002d%002d", loc->tm_year + 1900, loc->tm_mon + 1, loc->tm_mday);

		    if (memcmp(FileDate, argv[2], 8) < 0)  {
		    	sprintf(SysCmd, "rm -rf %s", MaintFile);
		    	ret = system(SysCmd)/256;
		    	fprintf(stdout, "DELETE FILE : [%d] [%s]\n", ret, MaintFile);
		    }

		    if (memcmp(FileDate, argv[2], 8) >= 0) break;
		}
	}
	fclose (FCHK);
	ret = remove(FileName);

	return 0;
}

/*---------------------------------------------------------------*/
/*                E N D   O F   F I L E                          */
/*---------------------------------------------------------------*/
