/*-----------------------------------------------------------------
 ┌──────┬───────────────────────┐
 │ 프로그램명 │ ecams_encrypt.pc                             │
 ├──────┼───────────────────────┤
 │ 기      능 │ COLUMN DATA ENCRYPT & DECRYPT PROGRAM        │
 ├──────┼───────────────────────┤
 │ 작  성  일 │ 2005. 12. 16                                 │
 ├──────┼───────────────────────┤
 │ 작  성  자 │ 최   병   남                                 │
 └──────┴───────────────────────┘
-----------------------------------------------------------------*/


#include <ecamsapi.h>
/*#include "strcvt.h"                 String Trunc               */
                                   /* Left, Right, Mid           */
#define FALSE 0
#define TRUE 1



/*---------------------------------------------------------------*/
/*    User Work 변수                                             */
/*---------------------------------------------------------------*/
void     ExitProc              (int);
void     DefineSignal          (void);

#ifdef _OSF_SOURCE
static  void sig_cld         (void);
#endif

char **gEnvp;


void DefineSignal ()
{
    int    sig_num;

    for (sig_num = 0; sig_num < 32; sig_num++)
         signal (sig_num, SIG_IGN);

#ifdef _OSF_SOURCE
    signal (SIGCLD, sig_cld);
#else
    signal (SIGCHLD, SIG_IGN);
#endif

    signal (SIGTERM, ExitProc);
    signal (SIGBUS,  ExitProc);
    signal (SIGSEGV, ExitProc);
    signal (SIGSYS,  ExitProc);
    signal (SIGKILL, ExitProc);
    signal (SIGINT,  ExitProc);
}

#ifdef _OSF_SOURCE
static void sig_cld ()
{
    int pid, status;

    while ((pid = wait3 (&status, WNOHANG, (struct rusage *)0)) > 0)
        ;
}
#endif


void ExitProc (sig_num)
int  sig_num;
{
    exit (1);
}



/*****************************************************************/
/*                                                               */
/*			COLUMN DATA ENCRYPT & DECRYPT PROGRAM                */
/*                                                               */
/*****************************************************************/
int  	main(int argc, char **argv, char **envp)
{
char	 ENCCode			 [5];
char	*ENCValue1			    ;
char	*ENCValue2			    ;
int		 ENCLen					;
int		 DECLen					;
char	*DSTValue1			    ;
char	*DSTValue2			    ;



    gEnvp = envp;


    DefineSignal ();


char	encStr[2048];
char	decStr[2048];

    /*-----------------------------------------------------------*/
    /*		입력 PARAMETER SETTING & CHECK                       */
    /*-----------------------------------------------------------*/
    memset(encStr,0x00,sizeof(encStr));

	strcpy(encStr,argv[1]);


	memset(ENCCode, 0x00, sizeof(ENCCode));

	sprintf(ENCCode, "%s", "E");

	ENCLen = strlen(encStr);
	ENCValue1 = (char *) malloc (ENCLen+2);
	DSTValue1 = (char *) malloc (ENCLen+2);

    memset(ENCValue1, 0x00, sizeof(ENCValue1));
	memset(DSTValue1, 0x00, sizeof(DSTValue1));

	strcpy(ENCValue1, encStr);

	EnCryption ("V", ENCCode, "CFB", ENCValue1, DSTValue1);

	printf("encStr=[%s] encval=[%s] dstVal=[%s] strlen=[%d] strlen=[%d] strlen=[%d]\n\n\n\n",encStr,ENCValue1,DSTValue1,strlen(encStr),strlen(ENCValue1),strlen(DSTValue1));


	memset(ENCCode, 0x00, sizeof(ENCCode));
	sprintf(ENCCode, "%s", "D");

	memset(decStr,0x00,sizeof(decStr));
	strcpy(decStr,DSTValue1);

	DECLen = strlen(decStr);


	ENCValue2 = (char *) malloc (DECLen+2);
	DSTValue2 = (char *) malloc (DECLen+2);

	strcpy(ENCValue2,decStr);

 	EnCryption ("V", ENCCode, "CFB", ENCValue2, DSTValue2);

	printf("decStr=[%s] encval=[%s] dstVal=[%s] strlen=[%d] strlen=[%d] strlen=[%d]\n\n\n\n",decStr,ENCValue2,DSTValue2,strlen(decStr),strlen(ENCValue2),strlen(DSTValue2));


	/*
    memset(ENCValue1, 0x00, sizeof(ENCValue1));
	memset(DSTValue1, 0x00, sizeof(DSTValue1));

	strcpy(ENCValue1, DSTValue2);

	sprintf(ENCCode, "%s", "E");

	EnCryption ("V", ENCCode, "CFB", ENCValue1, DSTValue1);

	printf("encStr=[%s] encval=[%s] dstVal=[%s] strlen=[%d] strlen=[%d] strlen=[%d]\n\n\n\n",DSTValue2,ENCValue1,DSTValue1,strlen(DSTValue2),strlen(ENCValue1),strlen(DSTValue1));


    memset(ENCValue2, 0x00, sizeof(ENCValue2));
	memset(DSTValue2, 0x00, sizeof(DSTValue2));

	strcpy(ENCValue2,DSTValue1);

	sprintf(ENCCode, "%s", "D");

 	EnCryption ("V", ENCCode, "CFB", ENCValue2, DSTValue2);

	printf("decStr=[%s] encval=[%s] dstVal=[%s] strlen=[%d] strlen=[%d] strlen=[%d]\n\n\n\n",DSTValue1,ENCValue2,DSTValue2,strlen(DSTValue1),strlen(ENCValue2),strlen(DSTValue2));
	*/
}

/*---------------------------------------------------------------*/
/*             E N D    O F    P R O G R A M                     */
/*---------------------------------------------------------------*/
