/*-----------------------------------------------------------------
 ┌──────┬───────────────────────┐
 │ 프로그램명 │ ecams_ih_cs.c                                │
 ├──────┼───────────────────────┤
 │ 기      능 │ 형상관리(eCAMS) 자원이행 MAIN                │
 ├──────┼───────────────────────┤
 │ 작  성  일 │ 2007. 08. 16                                 │
 ├──────┼───────────────────────┤
 │ 작  성  자 │ 최   병   남                                 │
 └──────┴───────────────────────┘
-----------------------------------------------------------------*/

#define		dfMain		1

/*---------------------------------------------------------------*/
/*     Header files                                              */
/*---------------------------------------------------------------*/
#include	<ecamsapi.h>
#include 	<ecams_util.h>


#define WIN  0
#define UNIX 1

/*****************************************************************/
/*                                                               */
/*      자원이행 처리  M A I N                                   */
/*                                                               */
/*****************************************************************/
int 	main(int argc, char *argv[])
{
char 	szSysCD                [dfSysCD];
char 	szServerIP             [dfSvrIP];
char 	szPath                   [dfDir];
char 	szFileList          [dfFullPath];
char 	szFileList1         [dfFullPath];
short 	bFlag = UNIX                    ;
int 	nRst                            ;
char 	szTempFile          [dfFullPath];
char 	szCommand           [dfFullPath];
char 	szAgentDir               [dfDir];

CMD_INFO	  CmdInfo;


	if (argc != 9) {
		printf("USAGE : %s <시스템> <서버주소> <Socket Port> <Buffer Size> <이행 Directory> <OutFile> <GBNCD> <AgentDir> \n",argv[0]);
		exit(1);
	}

	sprintf(szSysCD   , "%s", argv[1]);
	sprintf(szServerIP, "%s", argv[2]);
	sprintf(szPath    , "%s", argv[5]);
	sprintf(szFileList, "%s", argv[6]);
	sprintf(szAgentDir, "%s", argv[8]);

	if (Char_Check(szPath,":") > 0) {
		sprintf(szPath, "%s", rep_char(szPath,"/","\\"));
		fprintf(stdout, "WINDOWS 이행입니다........... \n");
		bFlag = WIN;
	}
	else
		fprintf(stdout, "UNIX 이행입니다.. [%s] \n", szPath);

	InitCmdInfo(&CmdInfo);

	CmdInfo.nPort = atoi(argv[3]);
	dfMaxFileSize = atoi(argv[4]);
		
	fprintf(stdout, "파일이름...[%s]\n",szFileList);
    sprintf(szFileList1, "tmp/%s.%s.ih.cs", szFileList, szSysCD);
	sprintf(szFileList , "%s.%s.ih"   , szFileList, szSysCD);

	if (bFlag == WIN) {
		sprintf(szAgentDir, "%s", rep_char(szAgentDir, "/", "\\"));
	}

	/*-----------------------------------------------------------*/
	/*  파일 리스트 생성                                         */
	/*-----------------------------------------------------------*/
	fprintf(stdout, "파일리스트 생성중...[%s]\n",szFileList);
	strcpy(CmdInfo.szServerIP, szServerIP);
	strcpy(CmdInfo.szJobGub,"S");
	if (bFlag == WIN)
	   if (strcmp(argv[7],"9") == 0)
		   sprintf(CmdInfo.szCommand,"DIR /S \"%s\" > %s\\%s", szPath, szAgentDir, szFileList);
	   else
		   sprintf(CmdInfo.szCommand,"DIR    \"%s\" > %s\\%s", szPath, szAgentDir, szFileList);
	else
	   if (strcmp(argv[7],"9") == 0)
		  sprintf(CmdInfo.szCommand,"ls -lLR \"%s\" > %s/%s", szPath, szAgentDir, szFileList);
	   else
		  sprintf(CmdInfo.szCommand,"ls -l   \"%s\" > %s/%s", szPath, szAgentDir, szFileList);

	fprintf(stdout, "명령문 : [%s]\n", CmdInfo.szCommand);

	Server_Cmd_JOB(&CmdInfo);

	/*-----------------------------------------------------------*/
	/* 	생성 파일 GET                                            */
	/*-----------------------------------------------------------*/
	fprintf(stdout, "파일리스트 GET...[%s/%s]\n",szAgentDir,szFileList);
	strcpy(CmdInfo.szJobGub,"G");
	sprintf(CmdInfo.szLocal,"%s",szFileList1);
	sprintf(CmdInfo.szCommand, "%s", "");
	if (bFlag == WIN) {
		sprintf(CmdInfo.szRemote,"%s\\%s", szAgentDir, szFileList);
		sprintf(CmdInfo.szRemote, "%s", rep_char(CmdInfo.szRemote, "\\\\", "\\"));
	}
	else {
		sprintf(CmdInfo.szRemote,"%s/%s", szAgentDir, szFileList);
	}
	Server_Cmd_JOB(&CmdInfo);
	LookCMD_INFO(&CmdInfo);
	if (strcmp(CmdInfo.szRstCond,"0000") != 0) {
		fprintf(stdout, "파일 리스트를 가져오지 못했습니다.[%s] \n",CmdInfo.szRstCond);
		exit(1);

	}
    
	/*-----------------------------------------------------------*/
	/* 	서버에 생성 파일 삭제                                    */
	/*-----------------------------------------------------------*/
	fprintf(stdout, "서버에 생성 파일 삭제...\n");
	strcpy(CmdInfo.szJobGub,"S");
	if (bFlag == WIN)
		sprintf(CmdInfo.szCommand,"DEL /F %s\\%s",szAgentDir, szFileList);
	else
		sprintf(CmdInfo.szCommand,"rm -rf %s/%s",szAgentDir, szFileList);
	Server_Cmd_JOB(&CmdInfo);
	LookCMD_INFO(&CmdInfo);
	
	/*-----------------------------------------------------------*/
	/*	ls -lR 실행시 지정 디렉토리는 출력이 않되므로 추가 시켜  */
	/*	줘야 함                                                  */
	/*-----------------------------------------------------------*/
	sprintf(szTempFile,"%s.tmp",szFileList1);
	if (bFlag != WIN) {
		FILE * fp;
		if ((fp = fopen(szTempFile,"w")) == NULL) {
			printf("임시파일 생성 실패!\n");
			exit(1);
		}

		fprintf(fp,"%s:\n",szPath);
		fclose(fp);

		sprintf(szCommand,"cat %s >> %s",szFileList1, szTempFile);
		printf("%s\n",szCommand);
		nRst = system(szCommand);

		fprintf(stdout, "patch Rst = %d\n", nRst);
		if (nRst != 0) {
			fprintf(stderr, "파일리스트에 경로 추가 실패!\n");
			exit(1);
		}
		else {
		    sprintf(szCommand,"mv %s %s", szTempFile,szFileList1);
		    printf("%s\n",szCommand);
		    nRst = system(szCommand);
	    }
	    remove(szTempFile);
	}


	exit (0);
}

/*---------------------------------------------------------------*/
/*             E N D    O F    P R O G R A M                     */
/*---------------------------------------------------------------*/
