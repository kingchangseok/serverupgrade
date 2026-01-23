/*-----------------------------------------------------------------
 ┌──────┬───────────────────────┐
 │ 프로그램명 │zen.c                                         │
 ├──────┼───────────────────────┤
 │ 기      능 │ Socket Test 프로그램                         │
 ├──────┼───────────────────────┤
 │ 작  성  일 │ 2011. 07. 20                                 │
 ├──────┼───────────────────────┤
 │ 작  성  자 │ 최   병   남                                 │
 └──────┴───────────────────────┘
-----------------------------------------------------------------*/

/*---------------------------------------------------------------*/
/*     Header files                                              */
/*---------------------------------------------------------------*/
#define		dfMain 1

#include	<ecamsapi.h>
#include 	<ecams_util.h>

#define WIN  0
#define UNIX 1



/*****************************************************************/
/*                                                               */
/*             Socket Test 프로그램  M A I N                     */
/*                                                               */
/*****************************************************************/
int 	main(int argc, char *argv[])
{
char	szSvrIP                [dfSvrIP];
int		szPortNo                        ;
char	szRemote            [dfFullPath];
char	szCommand           [dfFullPath];
char	szTypeofCommand              [2];
CMD_INFO	  CmdInfo                   ;


	/*-----------------------------------------------------------*/
	/*	파라미터 체크                                            */
	/*-----------------------------------------------------------*/
	if (argc < 6) {
		if (argc < 5 || strcmp(argv[4], "U") != 0) {
			fprintf(stderr, "USAGE: %s <IP> <Port> <Buffer Size> <S> <COMMAND>                  \n", argv[0]);
			fprintf(stderr, "       %s <IP> <Port> <Buffer Size> <G> <Local File> <Remote File> \n", argv[0]);
			fprintf(stderr, "       %s <IP> <Port> <Buffer Size> <F> <Local File> <Remote File> \n", argv[0]);
			fprintf(stderr, "       %s <IP> <Port> <Buffer Size> <I> <Remote File>              \n", argv[0]);
			exit(1);
		}
	}

	if ((strcmp(argv[4], "G") == 0 ||
	     strcmp(argv[4], "F") == 0 ) &&
	    argc < 7                      ) {
		fprintf(stderr, "USAGE: %s <IP> <Port> <BUFFER SIZE> <G/F> <Local File> <Remote File> \n", argv[0]);
		exit(1);
	}

	/*-----------------------------------------------------------*/
	/*	파라미터 초기화                                          */
	/*-----------------------------------------------------------*/
	strcpy(szSvrIP, argv[1]);
	szPortNo      = atoi(argv[2]);
	dfMaxFileSize = atoi(argv[3]);
	strcpy(szTypeofCommand, argv[4]);

	NotUseDataTruncate (szSvrIP, 0x20);
	NotUseDataTruncate (szTypeofCommand, 0x20);

	/*-----------------------------------------------------------*/
	/*	Socket Structure 초기화                                  */
	/*-----------------------------------------------------------*/
	InitCmdInfo(&CmdInfo);

	/*-----------------------------------------------------------*/
	/*	서버정보 Socket Set                                      */
	/*-----------------------------------------------------------*/
	strcpy(CmdInfo.szServerIP, szSvrIP);
	CmdInfo.nPort = szPortNo;
	strcpy(CmdInfo.szJobGub, szTypeofCommand);

	if (strcmp(szTypeofCommand,"G") == 0 ||
		strcmp(szTypeofCommand,"F") == 0 ||
		strcmp(szTypeofCommand,"X") == 0 ||
		strcmp(szTypeofCommand,"Z") == 0 ) {
		strcpy(CmdInfo.szLocal, argv[5]);
		strcpy(szRemote       , argv[6]);

		if (Char_Check(szRemote, ":") > 0) {
			while( Char_Check(szRemote,"\\") >= 0) {
				sprintf(szRemote,"%s",rep_char(szRemote,"\\","/"));
			}
			while( Char_Check(szRemote,"/") >= 0) {
				sprintf(szRemote,"%s",rep_char(szRemote,"/","\\"));
			}
			while( Char_Check(szRemote,"\\\\") >= 0) {
				sprintf(szRemote,"%s",rep_char(szRemote,"\\\\","\\"));
			}
		}
		strcpy(CmdInfo.szRemote, szRemote);
	}
	else {
		strcpy(szCommand, argv[5]);
		if (Char_Check(szCommand, ":") > 0) {
			while( Char_Check(szCommand,"\\") >= 0) {
				sprintf(szCommand,"%s",rep_char(szCommand,"\\","/"));
			}
			while( Char_Check(szCommand,"/") >= 0) {
				sprintf(szCommand,"%s",rep_char(szCommand,"/","\\"));
			}
			while( Char_Check(szCommand,"\\\\") >= 0) {
				sprintf(szCommand,"%s",rep_char(szCommand,"\\\\","\\"));
			}
		}
		strcpy(CmdInfo.szCommand,szCommand);
	}
	Server_Cmd_JOB(&CmdInfo);
	LookCMD_INFO (&CmdInfo);

	if (strcmp(CmdInfo.szRstCond, "EROR") == 0) {
		exit(9);
	}

	else
	if (strcmp(CmdInfo.szRstCond, "SVER") == 0) {
		exit(8);
	}

	else {
		exit (atoi(CmdInfo.szRstCond));
	}

}

/*---------------------------------------------------------------*/
/*             E N D    O F    P R O G R A M                     */
/*---------------------------------------------------------------*/
