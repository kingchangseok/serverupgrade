/*-----------------------------------------------------------------
 ┌──────┬───────────────────────┐
 │ 프로그램명 │ ecams_SubFile_GetList.c                               │
 ├──────┼───────────────────────┤
 ├──────┼───────────────────────┤
 │ 작  성  일 │ 2008. 01. 19                                 │
 ├──────┼───────────────────────┤
 │ 작  성  자 │ 최   병   남                                 │
 └──────┴───────────────────────┘
-----------------------------------------------------------------*/

/*---------------------------------------------------------------*/
/*     Header files                                              */
/*---------------------------------------------------------------*/
#define		dfMain	1
#include	<ecamsapi.h>
#include 	<ecams_util.h>


#define WIN  0
#define UNIX 1


char gszLogFile[50];	/*Log File Name*/
char gszLogPath[100];	/*Log File Path*/
char gszLogMsg[512];	/*Log Message  */

/*---------------------------------------------------------------*/
/*       INTERNAL  FUNCTION  DEFINE                              */
/*---------------------------------------------------------------*/

int setErrCode_unix(char *pResult,char *pResultTmp,char *errMsg,int resultCode, char *volPath);
int setErrCode_win(char *pResult,char *pResultTmp,char *errMsg,int resultCode,char *pVolPath,char *pSubGbn);
/*---------------------------------------------------------------*/
/*		USER WORK DEFINE                                         */
/*---------------------------------------------------------------*/
/*---------------------------------------------------------------*/
/*		전역변수 			                                     */
/*---------------------------------------------------------------*/
char   **gEnvp;			/*환경 변수    */
char gszTempPath[512];	/*Temp Direcotry*/



int		main (int argc, char **argv, char **envp)
{
char	szSubGbn	   [512];
char	szVolPath	   [512];
char	szOutFileName	[512];
char	szOutFileName_tmp	[512];
char	szSvrIP			[21];
int		nPort			;
int		szExitCode;
char	szSysOS		[3];
char	szAgentDir		[512];
char	szRemote		[512];
char	szLocal			[512];
struct stat f_st;
CMD_INFO	  CmdInfo;
char	szErrMsg		[2048];
char	szErrMsg2		[2048];

char ExeName[512];
int	 nret;

	    

	if (argc < 9) {
        printf ("Usage : %s <IP> <PORT> <SubGbn> <VolPath> <SYSOS> <TMPPATH> <AGENTDIR> <RESULTFILE>\n", argv[0]);
        exit (0);
    }
	    
    
    memset(szSvrIP , 0x00, sizeof(szSvrIP ));
    memset(szSubGbn , 0x00, sizeof(szSubGbn ));
	memset(szVolPath , 0x00, sizeof(szVolPath ));
	memset(szOutFileName , 0x00, sizeof(szOutFileName ));
	memset(szSysOS , 0x00, sizeof(szSysOS ));
	memset(gszTempPath , 0x00, sizeof(gszTempPath ));
	memset(szAgentDir , 0x00, sizeof(szAgentDir ));

	sprintf(szSvrIP , "%s", argv[1]);
	nPort = atoi(argv[2]);
	sprintf(szSubGbn , "%s", argv[3]);
	sprintf(szVolPath , "%s", argv[4]);
	sprintf(szSysOS , "%s", argv[5]);
	sprintf(gszTempPath , "%s", argv[6]);
	sprintf(szAgentDir , "%s", argv[7]);	
	sprintf(szOutFileName , "%s", argv[8]);
	
	sprintf(szOutFileName_tmp,"%s_tmp",szOutFileName);
	
	
	sprintf(CmdInfo.szServerIP,szSvrIP);
	CmdInfo.nPort = nPort;	

	sprintf(CmdInfo.szJobGub,"S");
	
	if(strcmp(szSysOS,"03") == 0){
		if (strcmp(szSubGbn,"1") == 0){
			sprintf(CmdInfo.szCommand,"DIR /S /B /A:-D \"%s\" > %s\\ecamsagent\\%s",szVolPath,szAgentDir,szOutFileName_tmp);
		}
		else{
			sprintf(CmdInfo.szCommand,"DIR /B /A:-D \"%s\" > %s\\ecamsagent\\%s",szVolPath,szAgentDir,szOutFileName_tmp);
		}
	}
	else{
		if (strcmp(szSubGbn,"1") == 0){
			sprintf(CmdInfo.szCommand,"ls -lRL \"%s\" > %s/%s",szVolPath,szAgentDir,szOutFileName_tmp);
		}
		else{
			sprintf(CmdInfo.szCommand,"ls -lL \"%s\" > %s/%s",szVolPath,szAgentDir,szOutFileName_tmp);
		}
	}
	
	

	Server_Cmd_JOB(&CmdInfo);
	
	
	if (strcmp(CmdInfo.szRstCond,"0000") != 0) {
		sprintf(szErrMsg2," Shellscript run fail szCommand=[%s] szRstCond=[%s] \n",CmdInfo.szCommand,CmdInfo.szRstCond);
		szExitCode = 1;
	}
	else{
		szExitCode = 0;
	}
	
	printf("Shellscript run szCommand=[%s]\n",CmdInfo.szCommand);
	
	if(strcmp(szSysOS,"03") == 0){
		sprintf(szRemote,"%s\\ecamsagent\\%s",szAgentDir,szOutFileName_tmp);
	}
	else{
		sprintf(szRemote,"%s/%s",szAgentDir,szOutFileName_tmp);
	}
	
	sprintf(szLocal,"%s/%s",gszTempPath,szOutFileName);
	
	
	sprintf(CmdInfo.szJobGub,"G");
	
	sprintf(CmdInfo.szRemote,"%s",szRemote);
	sprintf(CmdInfo.szLocal,"%s_tmp",szLocal);
	
	Server_Cmd_JOB(&CmdInfo);
	
	if (strcmp(CmdInfo.szRstCond,"0000") != 0){
		if (szExitCode == 1){
			szExitCode = 3;
		}
		else{
			szExitCode = 2;
		}
		if (szExitCode == 3){
			sprintf(szErrMsg,"%s 파일 수신 실패- szLocal=[%s] szRemote=[%s] szRstCond=[%s] \n",szErrMsg2,CmdInfo.szLocal,CmdInfo.szRemote,CmdInfo.szRstCond);
		}
		else{
			sprintf(szErrMsg,"파일 수신 실패- szLocal=[%s] szRemote=[%s] szRstCond=[%s] \n",CmdInfo.szLocal,CmdInfo.szRemote,CmdInfo.szRstCond);
		}
	}
	else{
		if(stat(CmdInfo.szLocal,&f_st) < 0) {
			sprintf(szErrMsg,"파일 수신 실패- szLocal=[%s] szRemote=[%s] szRstCond=[%s] \n",CmdInfo.szLocal,CmdInfo.szRemote,CmdInfo.szRstCond);
			
			if (szExitCode == 1){
				szExitCode = 3;
			}
			else{
				szExitCode = 2;
			}
		}
		else{
			if(f_st.st_size == 0) {	
				if (szExitCode == 1){
					szExitCode = 3;
				}
				else{
					szExitCode = 2;
				}
				if (szExitCode == 3){
					sprintf(szErrMsg,"%s 파일 수신 실패- szLocal=[%s] szRemote=[%s] szRstCond=[%s] \n",szErrMsg2,CmdInfo.szLocal,CmdInfo.szRemote,CmdInfo.szRstCond);
				}
				else{
					sprintf(szErrMsg,"파일 수신 실패- szLocal=[%s] szRemote=[%s] szRstCond=[%s] \n",CmdInfo.szLocal,CmdInfo.szRemote,CmdInfo.szRstCond);
				}
				
			}
			else{
				if (szExitCode == 1){
					szExitCode = 1;
				}
				else{
					printf("파일 수신 완료 - szLocal=[%s] szRemote=[%s]  szRstCond=[%s] \n",CmdInfo.szLocal,CmdInfo.szRemote,CmdInfo.szRstCond);
					szExitCode = 0;
				}
			}
		}
	}	
	
	if(strcmp(szSysOS,"03") == 0){
		nret = setErrCode_win(szLocal,CmdInfo.szLocal,szErrMsg,szExitCode,szVolPath,szSubGbn);
		sprintf(CmdInfo.szJobGub,"S");
		
		sprintf(CmdInfo.szCommand,"del /f /q %s\\ecamsagent\\%s",szAgentDir,szOutFileName_tmp);
		Server_Cmd_JOB(&CmdInfo);		
	}
	else{
		nret = setErrCode_unix(szLocal,CmdInfo.szLocal,szErrMsg,szExitCode,szVolPath);
		sprintf(CmdInfo.szJobGub,"S");
		
		sprintf(CmdInfo.szCommand,"rm -rf %s/%s",szAgentDir,szOutFileName_tmp);
		Server_Cmd_JOB(&CmdInfo);		
	}	
	
	if (nret != 0) {
		exit (nret);
	}
		
	exit(0);
		
}

int setErrCode_win(char *pResult,char *pResultTmp,char *errMsg,int resultCode,char *pVolPath,char *pSubGbn)
{
	FILE	*tmpPtr;
	FILE	*resultPtr;
	char	indat[5000];
	char	indattmp[5000];
	int     nRet            ;                 /* 문자열찿기 WORK     */
	
	char RsrcName[512];

	remove(pResult);
	
	
	if ( (resultPtr = fopen(pResult,"a+")) == (FILE *) NULL){
		return 1;	
	}
	
	if (resultCode > 1){
		fprintf(resultPtr,"%d\n",resultCode);
		fprintf(resultPtr,"%s\n",errMsg);
	}
	else{
		fprintf(resultPtr,"%d\n",resultCode);
		
		if ( (tmpPtr = fopen(pResultTmp,"r")) == (FILE *) NULL){
			fprintf(resultPtr,"%d\n",1);
			fprintf(resultPtr,"file open Error=[%s] \n",pResultTmp);
			fclose(resultPtr);
			return 1;
		}
			
		while (fgets(indat, 5000, tmpPtr) != (char *) NULL) {
			sprintf(indat,"%s",rep_char(indat,"\r\n",""));
			sprintf(indat,"%s",rep_char(indat,"\n",""));
			sprintf(indat,"%s",trunc_char(indat));
			
	  		nRet = 0;
	  		if (strcmp(pSubGbn,"1") == 0){
				strcpy(RsrcName,indat);
	  		}
	  		else{
	  			sprintf(RsrcName,"%s\\%s",pVolPath,indat);
	  		}
	  		
			if ((nRet = Char_Check(RsrcName, ":")) >=0){
				sprintf(RsrcName, "%s",right_char(RsrcName, strlen(RsrcName) - nRet - 1));
			}

			while( Char_Check(RsrcName,"\\") >= 0){
				sprintf(RsrcName,"%s",rep_char(RsrcName,"\\","/"));
			}	  		
	  		
	        
			fprintf(resultPtr,"%s\n",RsrcName);
	        
		}
		
		fclose (tmpPtr);
	}
	
	fclose(resultPtr);
	remove(pResultTmp);
	
	return 0;
	
	
}

/*---------------------------------------------------------------*/
/*        이행 Script 작성                                       */
/*---------------------------------------------------------------*/
int setErrCode_unix(char *pResult,char *pResultTmp,char *errMsg,int resultCode, char *volPath)
{
	FILE	*tmpPtr;
	FILE	*resultPtr;
	char	indat[5000];
	int cnt;
	char DirName[512];
	char RsrcName[512];
	int nRet1;
	
	remove(pResult);
	
	
	if ( (resultPtr = fopen(pResult,"a+")) == (FILE *) NULL){
		return 1;	
	}
	
	if (resultCode > 1){
		fprintf(resultPtr,"%d\n",resultCode);
		fprintf(resultPtr,"%s\n",errMsg);
	}
	else{
		fprintf(resultPtr,"%d\n",resultCode);
		
		if ( (tmpPtr = fopen(pResultTmp,"r")) == (FILE *) NULL){
			fprintf(resultPtr,"%d\n",1);
			fprintf(resultPtr,"file open Error=[%s]\n",pResultTmp);
			fclose(resultPtr);
			return 1;
		}
		
		cnt = 0;
		
		while (fgets(indat, 5000, tmpPtr) != (char *) NULL) {
			sprintf(indat,"%s",rep_char(indat,"\r\n",""));
			sprintf(indat,"%s",rep_char(indat,"\n",""));
	    sprintf(indat,"%s",trunc_char(indat));
	        
	        cnt++;
	        
	        if (cnt==1){
	        	sprintf(DirName,"%s",volPath);
	        }
	        else{
		        if (indat[0] == '/') {
		        	memset(DirName, 0x00, sizeof(DirName));
		        	sprintf(DirName, "%s", left_char(indat, strlen(indat)-1));
		        	
		        	continue;
		        }
		    }
	
	        if (memcmp(indat, "-", 1) != 0)
			{
				continue;
			}
	
	        if (strlen(DirName) == 0) 
			{
				continue;
			}
			
			while (1) {
				sprintf(indat,"%s",trunc_char(indat));
				nRet1 = Char_Check(indat, " ");
	             
				if (nRet1 >= 0) {
	  			sprintf(indat, "%s",right_char(indat, strlen(indat) - nRet1 - 1));
				}
				else
				    break;
			}
			
	        sprintf(indat,"%s",rep_char(indat,"*",""));
	        sprintf(indat,"%s",trunc_char(indat));
	        
	        memset(RsrcName, 0x00, sizeof(RsrcName));
	        strcpy(RsrcName, indat);
	        
			
			fprintf(resultPtr,"%s/%s\n",DirName,RsrcName);
		}
		
		fclose (tmpPtr);
	}
	
	fclose(resultPtr);
	
	
	remove(pResultTmp);	
	return 0;
}




/*---------------------------------------------------------------*/
/*             E N D    O F    P R O G R A M                     */
/*---------------------------------------------------------------*/
