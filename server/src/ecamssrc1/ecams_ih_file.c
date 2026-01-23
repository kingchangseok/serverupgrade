/*-----------------------------------------------------------------
 ┌──────┬───────────────────────┐
 │ 프로그램명 │ ecams_ih_1.c                                 │
 ├──────┼───────────────────────┤
 │ 기      능 │ 형상관리(eCAMS) 자원이행 대상 목록 작성      │
 ├──────┼───────────────────────┤
 │ 작  성  일 │ 2011. 07. 21                                 │
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

int Make_Script_unix();
char 	szFileList2         [dfFullPath];
char 	szCommand           [dfFullPath];
/*****************************************************************/
/*                                                               */
/*          자원이행 대상 목록 작성 처리  M A I N                */
/*                                                               */
/*****************************************************************/
int 	main(int argc, char *argv[])
{
char 	szSysCD                [dfSysCD];
char 	szSysOs                [dfSysOS];
char 	szServerIP             [dfSvrIP];
char 	szPath                   [dfDir];
char 	szFileList          [dfFullPath];
char 	szFileList1         [dfFullPath];
short 	bFlag = UNIX                    ;
int 	nRst                            ;
char 	szTempFile          [dfFullPath];
CMD_INFO	  CmdInfo                   ;


	if (argc != 9) {
		printf("USAGE : %s <시스템> <서버주소> <Socket Port> <Buffer Size> <OS> <이행 Directory> <FILENAME> <EXENAME>\n",argv[0]);
		exit(1);
	}

	sprintf(szSysCD    , "%s", argv[1]);
	sprintf(szServerIP , "%s", argv[2]);
	sprintf(szSysOs    , "%s", argv[5]);
	sprintf(szPath     , "%s", argv[6]);
	sprintf(szFileList , "%s", argv[7]);



    NotUseDataTruncate (szSysCD   , 0x20);
    NotUseDataTruncate (szServerIP, 0x20);
    NotUseDataTruncate (szPath    , 0x20);
	NotUseDataTruncate (szSysOs   , 0x20);
	NotUseDataTruncate (szFileList, 0x20);

	if (strcmp(szSysOs, dfWINDOWS) == 0) {
		sprintf(szPath, "%s", rep_char(szPath,"/","\\"));
		fprintf(stdout, "WINDOWS 이행입니다........... \n");
		bFlag = WIN;
	}
	else {
		fprintf(stdout, "UNIX 이행입니다.. [%s] \n", szPath);
	}

	/*-----------------------------------------------------------*/
	/*	소켓 명령 변수 초기화                                    */
	/*-----------------------------------------------------------*/
	InitCmdInfo(&CmdInfo);

	CmdInfo.nPort = atoi(argv[3]);
	dfMaxFileSize = atoi(argv[4]);

   sprintf(szFileList1,"%s.%s.%s.ih.1",szFileList, szServerIP, szSysCD);
   sprintf(szFileList2,"%s.%s.%s.ih.utfconv",szFileList, szServerIP, szSysCD);   
	sprintf(szFileList,"%s.%s.%s.ih",szFileList, szServerIP, szSysCD);

	/*-----------------------------------------------------------*/
	/*  파일 리스트 생성                                         */
	/*-----------------------------------------------------------*/
	fprintf(stdout, "파일리스트 생성중...\n");
	strcpy(CmdInfo.szServerIP, szServerIP);
	strcpy(CmdInfo.szJobGub,"S");
	if(bFlag == WIN)
		sprintf(CmdInfo.szCommand,"DIR /S %s > %s",szPath,szFileList);
	else
		sprintf(CmdInfo.szCommand,"ls -alLR %s > %s",szPath,szFileList);

	fprintf(stdout, "명령문 : [%s]\n", CmdInfo.szCommand);

	Server_Cmd_JOB(&CmdInfo);

	/*-----------------------------------------------------------*/
	/* 	생성 파일 GET                                            */
	/*-----------------------------------------------------------*/
	fprintf(stdout, "파일리스트 GET...[%s]\n",szFileList);
	strcpy(CmdInfo.szJobGub,"G");
	sprintf(CmdInfo.szLocal,"%s",szFileList1);
	sprintf(CmdInfo.szRemote,"%s",szFileList);
	fprintf(stdout, "명령문 2 : [%s][%s]\n", CmdInfo.szLocal,CmdInfo.szRemote);
	Server_Cmd_JOB(&CmdInfo);

	if(strcmp(CmdInfo.szRstCond,"0000") != 0) {
		fprintf(stdout, "파일 리스트를 가져오지 못했습니다.[%s] \n",CmdInfo.szRstCond);
		exit(1);
	}

	/*-----------------------------------------------------------*/
	/* 	서버에 생성 파일 삭제                                    */
	/*-----------------------------------------------------------*/
	fprintf(stdout, "서버에 생성 파일 삭제...\n");
	strcpy(CmdInfo.szJobGub,"S");
	if(bFlag == WIN)
		sprintf(CmdInfo.szCommand,"DEL /F %s",szFileList);
	else
		sprintf(CmdInfo.szCommand,"rm -rf %s",szFileList);
	Server_Cmd_JOB(&CmdInfo);

	/*-----------------------------------------------------------*/
	/*	ls -lR 실행시 지정 디렉토리는 출력이 않되므로 추가 시켜  */
	/*	줘야 함                                                  */
	/*-----------------------------------------------------------*/
	sprintf(szTempFile,"%s.tmp",szFileList1);
	if(bFlag == UNIX) {
		FILE * fp;
		if((fp = fopen(szTempFile,"w")) == NULL) {
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
	    
	}

	/*-----------------------------------------------------------*/
	/*  makeihsh 실행                                            */
	/*-----------------------------------------------------------*/
	printf("이행 스크립트 파일 생성 ... \n");

	nRst = Make_Script_unix(szSysCD, szServerIP,szFileList1, argv[8]);
	    
	printf("이행 스크립트 파일 생성 [%d/%s] \n", nRst,szCommand);
	
	remove (szFileList1);

	return 0;
	
}


/*---------------------------------------------------------------*/
/*	Function  : Make_Script_unix                                 */
/*	Action    : 이행 Script 작성                                 */
/*	Parameter :	pAcptNo   : 신청번호                             */
/*	            pPrcSys   : 처리단계                             */
/*	            pRstCond  : 처리결과                             */
/*	Return    : 오류건수                                         */
/*---------------------------------------------------------------*/
int 	Make_Script_unix	( char *SysCD
							, char *ServerIP
							, char *FileName
							, char *gExe
							)
{
int 	nCnt                            ;
int 	cnt                             ;
int 	nCnt2                           ;
int 	nCnt3                           ;
char	szFile              [dfRsrcName];
char	szFileName          [dfRsrcName];
int 	nRet                            ;
char	szExeName           [dfRsrcName];
char	szDot                        [2];
char	szScriptFile        [dfRsrcName];
FILE	*ScrPtr                         ;
FILE	*ScrPtrtmp                      ;
FILE	*fptr                           ;
char	DirName                  [dfDir];
char	dummy                     [1024];
char	RsrcName            [dfRsrcName];
char	ExtName             [dfRsrcName];
char	ExeName             [dfRsrcName];
int		nRet1                           ;
int     sFg                             ;
    /*-----------------------------------------------------------*/
    /*   Make File Name Find                                     */
    /*-----------------------------------------------------------*/
    if ((fptr = fopen(FileName,"r")) == (FILE *) NULL) {
	    printf("FILE OPEN ERROR!! \n");
        return (0);
    }
    memset(DirName, 0x00, sizeof(DirName));
    memset(dummy, 0x00, sizeof(dummy));
    sprintf(szScriptFile , "%s.ecams", FileName);
    printf("Create File  = [%s]\n",szScriptFile);

    ScrPtr = fopen(szScriptFile,"w+");
    if (ScrPtr == NULL)  {
       printf("makeihsh 파일생성 error %s\n",szScriptFile);
       return (1);
    }

    while (fgets(dummy, 256, fptr) != (char *) NULL) {
        /*-------------------------------------------------------*/
    	/* Return Valure Truncate                                */
        /*-------------------------------------------------------*/
        sprintf(dummy, "%s", rep_char(dummy,"\n",""));
        sprintf(dummy, "%s", rep_char(dummy,"\r\n",""));
        sprintf(dummy, "%s", trunc_char(dummy));

        if (dummy[0] == '/') {
        	memset(DirName, 0x00, sizeof(DirName));
        	sprintf(DirName, "%s", left_char(dummy, strlen(dummy)-1));
        	cnt = 0;
        	continue;
        }

        if (memcmp(dummy, "-", 1) != 0)		continue;
        if (strlen(DirName) == 0) 			continue;

		sFg = 0;
		while (1) {
            sFg ++;
			sprintf(dummy, "%s", trunc_char(dummy));
			nRet1 = Char_Check(dummy, " ");

			/*if (nRet1 < 0)	break; */
			if (sFg == 9) break;

  			sprintf(dummy, "%s", right_char(dummy, strlen(dummy) - nRet1 - 1));
		}
        sprintf(dummy, "%s", rep_char(dummy,"*",""));
        sprintf(dummy, "%s", trunc_char(dummy));
        memset(RsrcName, 0x00, sizeof(RsrcName));
        memset(ExtName , 0x00, sizeof(ExtName ));
        memset(ExeName , 0x00, sizeof(ExeName ));
        strcpy(RsrcName, dummy);

        nRet1 = Char_Check(RsrcName, ".");
		/*if (nRet1 == 0) continue;*/

		if (nRet1 >= 0) {
	        strcpy(ExtName , dummy);
			while (1) {
				sprintf(ExtName, "%s", trunc_char(ExtName));
				nRet1 = Char_Check(ExtName, ".");
				if (nRet1 >= 0) {
					sprintf(ExeName, "%s", right_char(ExtName, strlen(ExtName) - nRet1));
			 	    sprintf(ExtName, "%s", right_char(ExtName, strlen(ExtName) - nRet1 - 1));
				}
				else {
				   strcpy(ExeName,ExtName);
				   break;
	            }
			}
	        sprintf(ExtName, "%s", trunc_char(ExeName));
		}
		else {
			strcpy(ExtName,"");
		}

	    /*-------------------------------------------------------*/
		/*	확장자가 관리 대상이 아니면. Pass                    */
		/*-------------------------------------------------------*/
		nCnt = 0;
		if (strlen(ExtName) > 0 && strcmp(gExe,"ALL") != 0) {
		   	 nRet1 = Char_Check(gExe,ExtName);
		   	 if (nRet1 >= 0)
		   	    continue;
		}

        NotUseDataTruncate (DirName, 0x20);

	    fprintf(ScrPtr, "%s\t%s\r\n",DirName,RsrcName);
    }
    fclose (fptr);
    fclose (ScrPtr);
    

	sprintf(szCommand,"iconv -f UTF-8 -t eucKR %s > %s ",szScriptFile,szFileList2);	    
  system(szCommand);
      
    return (0);
}

/*---------------------------------------------------------------*/
/*             E N D    O F    P R O G R A M                     */
/*---------------------------------------------------------------*/
