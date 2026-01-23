/*-----------------------------------------------------------------
 ┌──────┬───────────────────────┐
 │ 프로그램명 │ ecams_dir.c                                  │
 ├──────┼───────────────────────┤
 │ 기      능 │ Server Directory/File List Get               │
 ├──────┼───────────────────────┤
 │ 작  성  일 │ 2011. 07. 20                                 │
 ├──────┼───────────────────────┤
 │ 작  성  자 │ 최   병   남                                 │
 └──────┴───────────────────────┘
-----------------------------------------------------------------*/

#define		dfMain	1


/*---------------------------------------------------------------*/
/*     Header files                                              */
/*---------------------------------------------------------------*/
#include    <ecamsapi.h>
#include 	<ecams_util.h>



/*---------------------------------------------------------------*/
/*       INTERNAL  FUNCTION  DEFINE                              */
/*---------------------------------------------------------------*/


/*---------------------------------------------------------------*/
/*    User Work 변수                                             */
/*---------------------------------------------------------------*/
char	**gEnvp                         ; /* Parameter 죵료용    */
extern int	errno                       ; /* 시스템 오류코드     */



/*****************************************************************/
/*                                                               */
/*		Server Directory/File List Get 처리   M A I N            */
/*                                                               */
/*****************************************************************/
int		main (int argc, char **argv, char **envp)
{
char	gServerIP              [dfSvrIP];
char	filename            [dfFullPath];
char	szTempFile          [dfFullPath];
char	szCommand           [dfFullPath];
int 	nRst                            ;
FILE 	*fp                             ;
CMD_INFO	CmdInfo                     ;


    if (argc != 6) {
        printf ("\nUSAGE : %s <SERVER IP> <PORT> <Buffer Size> <DIRECTORY> <사용자번호> \n\n", argv[0]);
        exit (1);
    }

    memset (filename, 0x00, sizeof (filename));
    sprintf(filename, "%s.log", argv[0]);

	#if 0
    TRACE = fopen (filename, "w+");
    if (TRACE == NULL)  {
        fprintf (stderr, "Log file Open Err = %s\n", filename);
        fflush  (stderr);
        exit (0);
    }
	#endif


	/*-----------------------------------------------------------*/
	/*	소켓 명령 변수 초기화                                    */
	/*-----------------------------------------------------------*/
	InitCmdInfo(&CmdInfo);
	strcpy(CmdInfo.szServerIP, argv[1]);
	CmdInfo.nPort = atoi(argv[2]);
	dfMaxFileSize = atoi(argv[3]);
	strcpy(CmdInfo.szJobGub, "S");

	if(Char_Check(argv[4],":") > 0) {
		sprintf(argv[4], "%s", rep_char(argv[4],"/","\\"));
		sprintf(CmdInfo.szCommand , "DIR /S /AD \"%s\" > \"%s\"", argv[4], argv[5]);
	}
	else {
		/*sprintf(CmdInfo.szCommand , "ls -AFR \"%s\" | grep / > \"%s\"", argv[4], argv[5]);*/
		sprintf(CmdInfo.szCommand , "find \"%s\"/* -type d -prune | grep / > \"%s\"", argv[4], argv[5]);
	}
	Server_Cmd_JOB(&CmdInfo);
	LookCMD_INFO(&CmdInfo);

	if (memcmp(CmdInfo.szRstCond, "0000", 4) != 0) {
		strcpy(CmdInfo.szJobGub,"I");
		sprintf(CmdInfo.szCommand , "%s", argv[4]);
		Server_Cmd_JOB(&CmdInfo);
		LookCMD_INFO(&CmdInfo);

		if (memcmp(CmdInfo.szRstCond, "0000", 4) != 0) {
			exit (1);
		}
	}

	sprintf(CmdInfo.szJobGub,"G");
	sprintf(CmdInfo.szLocal , "tmp/%s", argv[5]);
	sprintf(CmdInfo.szRemote, "%s"    , argv[5]);
	Server_Cmd_JOB(&CmdInfo);
	LookCMD_INFO(&CmdInfo);

	/*-----------------------------------------------------------*/
	/*	ls -lR 실행시 지정 디렉토리는 출력이 않되므로 추가 시켜  */
	/*	줘야 함                                                  */
	/*-----------------------------------------------------------*/
	sprintf(szTempFile,"%s.tmp",CmdInfo.szLocal);
	if (Char_Check(argv[4],":") <= 0) {
		if ((fp = fopen(szTempFile,"w")) == NULL) {
			printf("임시파일 생성 실패!\n");
			exit(1);
		}

		fprintf(fp,"%s:\n",argv[4]);
		fclose(fp);

		sprintf(szCommand,"cat \"%s\" >> \"%s\"",CmdInfo.szLocal, szTempFile);
		printf("%s\n",szCommand);
		nRst = system(szCommand);

		fprintf(stdout, "patch Rst = %d\n", nRst);
		if (nRst != 0) {
			fprintf(stderr, "파일리스트에 경로 추가 실패!\n");
			exit(1);
		}
		else {
			sprintf(szCommand,"mv \"%s\" \"%s\"", szTempFile,CmdInfo.szLocal);
			printf("%s\n",szCommand);
			nRst = system(szCommand);
	    }
	}

	sprintf(CmdInfo.szJobGub,"S");
	if (Char_Check(argv[4],":") > 0)
		sprintf(CmdInfo.szCommand , "DEL \"%s\"", argv[5]);
	else
		sprintf(CmdInfo.szCommand , "rm \"./%s\"", argv[5]);

	Server_Cmd_JOB(&CmdInfo);

	if (memcmp(CmdInfo.szRstCond, "0000", 4) != 0)
		exit (1);

	exit (0);

}


/*---------------------------------------------------------------*/
/*             E N D    O F    P R O G R A M                     */
/*---------------------------------------------------------------*/
