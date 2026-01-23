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

	char    gConnStr       [256];
	char    confFile       [256];
	char	gCommand	   [256];           /* 시스템명령 WORK       */
	int		retval               ;          /* 쿼리결과              */
	char	*dummy               ;          /* Read Buffer           */
	FILE    * SrcPtr;
	long	nFileSize            ;
	char    makeModule     [512];
	char    temppath        [50];
	char    ecamsBin       [256];
	char    ecamsTemp      [256];


    if (argc < 2) {
        printf ("\nUSAGE : %s <Compile Module> \n\n", argv[0]);
        exit (1);
    }
    
	sprintf(ecamsBin,"%s",getenv("ECAMSBIN"));
	sprintf(ecamsTemp,"%s",getenv("ECAMSTMP"));
	
    strcpy(confFile,"ecams.conf");
	sprintf(temppath,"%s/clntmp",ecamsTemp);		
	strcat (temppath, "XXXXXXXXXX");
	mkstemp (temppath);
	sprintf(makeModule, "%s", argv[1]);
	sprintf(gCommand, "cd %s;java eCAMSConf D %s \"%s\"",ecamsBin,confFile,temppath);
	retval = system (gCommand) / 256;
	if (retval != 0) {		
		fprintf(stderr,"DB Connection Decryption CALL ERROR [%s][%d]\n", gCommand,retval);
		return FALSE;
	}
	if (access(temppath, F_OK) < 0) {		
		fprintf(stderr,"DB Connection Decryption CALL ERROR [%s][%s]\n", gCommand,temppath);
		return FALSE;
	}
	if ((SrcPtr = fopen(temppath, "r")) == (FILE *) NULL) {
		fprintf(stderr,"DB Connection Decryption Result File Open Error [%s]\n", temppath);
		return FALSE;
	}
	
	/*nFileSize = FileSizeInf(temppath) + 1;*/
	nFileSize = 128;
	dummy = (char *)malloc(nFileSize);
	memset(dummy, 0x00, nFileSize);
	
	while (fgets(dummy, nFileSize, SrcPtr) != (char *) NULL) {
		sprintf(dummy,"%s", rep_char(dummy,"\n",""));
		sprintf(dummy,"%s", rep_char(dummy,"\r\n",""));
		sprintf(dummy,"%s", trunc_char(dummy));
		
		if (strlen(dummy) < 2) {
			fprintf(stderr,"DB Connection Decryption Result File Error [%s]\n", temppath);
			free(dummy);
			fclose (SrcPtr);
			unlink(temppath);
			return (3);
		}
		if (cmp_left_char(dummy,2,"OK") != 0) {
			fprintf(stderr,"DB Connection Decryption Result Error [%s][%s]\n", temppath,dummy);
			free(dummy);
			fclose (SrcPtr);
			unlink(temppath);
			return (3);
		}	
		sprintf(gConnStr,"%s",mid_char(dummy,3,strlen(dummy) - 2));
		break;
		
	}
	free(dummy);
	fclose (SrcPtr);
	unlink(temppath);
	/*printf("gConnStr [%s]",gConnStr);*/
	setenv("DBUSER",gConnStr,1);
	sprintf(gCommand, "make %s", makeModule);
	retval = system (gCommand)/256;

	exit (retval);

}


/*---------------------------------------------------------------*/
/*             E N D    O F    P R O G R A M                     */
/*---------------------------------------------------------------*/
