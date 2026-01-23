/*-----------------------------------------------------------------
 ┌──────┬───────────────────────┐
 │ 프로그램명 │ strcvt.c                                     │
 ├──────┼───────────────────────┤
 │ 기      능 │ 문자열 계산 및 작성                          │
 ├──────┼───────────────────────┤
 │ 작  성  일 │ 2011. 07. 28                                 │
 ├──────┼───────────────────────┤
 │ 작  성  자 │ 최   병   남                                 │
 └──────┴───────────────────────┘
-----------------------------------------------------------------*/

/*---------------------------------------------------------------*/
/*     Header files                                              */
/*---------------------------------------------------------------*/
#include <stdio.h>
#include <fcntl.h>
#include <string.h>
#include <stdlib.h>
#include <ctype.h>


/*---------------------------------------------------------------*/
/*		USER  PROCEDURE  DEFINE                                  */
/*---------------------------------------------------------------*/
int     Char_Check		(char *, char *);                   		/* Char POS Find                */
char    *trunc_char		(char *);                           		/* Left & Right Space Truncate  */
char    *rtrunc_char	(char *);                           		/* Left & Right Space Truncate  */
char    *left_char 		(char *, int  lcnt  );              		/* Left 문자열                  */
char    *right_char		(char *, int  rcnt  );              		/* Right 문자열                 */
char    *mid_char  		(char *, int  start , int  slen  ); 		/* 중간 문자열                  */
char    *rep_char  		(char *, char find[], char repl[]); 		/* 문자열 치환                  */
char    *upper_char		(char *);                           		/* 문자열 치환 (소문자->대문자) */
char    *lower_char		(char *);                           		/* 문자열 치환 (대문자->소문자) */
int 	cmp_trunc_char	(char *, char *);                           /* Left & Right Space Truncate  */
int 	cmp_rtrunc_char (char *, char *);                           /* Left & Right Space Truncate  */
int 	cmp_left_char 	(char *, int  lcnt  , char *);              /* Left 문자열                  */
int 	cmp_right_char	(char *, int  rcnt  , char *);              /* Right 문자열                 */
int 	cmp_mid_char  	(char *, int  start , int  slen  , char *); /* 중간 문자열                  */
int 	cmp_upper_char	(char *, char *);                           /* 문자열 치환 (소문자->대문자) */
int 	cmp_lower_char	(char *, char *);                           /* 문자열 치환 (대문자->소문자) */

/*---------------------------------------------------------------*/
/*    User Work 변수                                             */
/*---------------------------------------------------------------*/
char    str_make                 [8192];
char    str_makeck               [8192];


/*---------------------------------------------------------------*/
/*	Function  : Char_Check                                       */
/*	Action    : Special Character Check (INSTR)                  */
/*	Parameter : sorc : 원시문자열                                */
/*	            find : 찿는문자열                                */
/*	Return    : 위치                                             */
/*---------------------------------------------------------------*/
int		Char_Check	( char *sorc
					, char *find
					)
{
char    *Loc;
int     loc;

    Loc = strstr(sorc, find);

    if (Loc == NULL)
       loc=-1;
    else
       loc=Loc-sorc;

    return loc;

}


/*---------------------------------------------------------------*/
/*	Function  : trunc_char                                       */
/*	Action    : Left & Right Space Truncate                      */
/*	Parameter : sorc : 문자열                                    */
/*	Return    : 문자열                                           */
/*---------------------------------------------------------------*/
char	*trunc_char	( char *sorc
					)
{
int     i;
int     j;

    size_t tloc;

    tloc = strspn(sorc," ");
    strcpy(str_make,sorc+tloc);

    j=0;
    for (i=strlen(str_make)-1; i >= 0; i--) {
        if (strncmp(str_make+i," ",1)) {
           j=i+1;
           break;
        }
    }

    if (j <= 0)
        strcpy(str_make,"\0");
    else {
        str_make[j] = '\0';
	}

    return (str_make);
}


/*---------------------------------------------------------------*/
/*	Function  : rtrunc_char                                      */
/*	Action    : Left & Right Space Truncate                      */
/*	Parameter : sorc : 문자열                                    */
/*	Return    : 문자열                                           */
/*---------------------------------------------------------------*/
char	*rtrunc_char	( char *sorc
						)
{
int     i;
int     j;

    size_t tloc;

    strcpy(str_make,sorc);

    j=0;
    for (i=strlen(str_make)-1; i >= 0; i--) {
        if (strncmp(str_make+i," ",1)) {
           j=i+1;
           break;
        }
    }
    if (j <= 0)
        strcpy(str_make,"\0");
    else
        str_make[j] = '\0';

    return (str_make);
}


/*---------------------------------------------------------------*/
/*	Function  : left_char                                        */
/*	Action    : 왼쪽 문자열 n자리 Find                           */
/*	Parameter : sorc : 원시문자열                                */
/*	            Lcnt : 문자열수                                  */
/*	Return    : 처리후 문자열                                    */
/*---------------------------------------------------------------*/
char	*left_char	( char *sorc
					, int   Lcnt
					)
{
    if (strlen(sorc) == 0)
        str_make[0] = '\0';

    else if (Lcnt <= 0)
        str_make[0] = '\0';

    else if (strlen(sorc) < Lcnt) {
        strcpy(str_make,sorc);
        str_make[strlen(sorc)] = '\0';
    }

    else if (Lcnt > 0) {
        strncpy(str_make,sorc,Lcnt);
        str_make[Lcnt] = '\0';
    }

    else
        str_make[0] = '\0';

    return (str_make);
}

/*---------------------------------------------------------------*/
/*	Function  : right_char                                       */
/*	Action    : 오른쪽 문자열 n자리 Find                         */
/*	Parameter : sorc : 원시문자열                                */
/*	            Rcnt : 문자열수                                  */
/*	Return    : 처리후 문자열                                    */
/*---------------------------------------------------------------*/
char	*right_char	( char *sorc
					, int  Rcnt
					)
{
int     ret1;

    if (strlen(sorc) < Rcnt) {
        strcpy(str_make,sorc);
        str_make[strlen(sorc)] = '\0';
    }
    else if (Rcnt > 0) {
        ret1 = strlen(sorc);
        strncpy(str_make,sorc+ret1-Rcnt,Rcnt);
        str_make[Rcnt] = '\0';
    }
    else
        str_make[0] = '\0';

    return (str_make);
}

/*---------------------------------------------------------------*/
/*	Function  : mid_char                                         */
/*	Action    : s자리부터 n자리 문자열 Find                      */
/*	Parameter : sorc  : 원시문자열                               */
/*	            start : 시작위치                                 */
/*	            slen  : 문자열수                                 */
/*	Return    : 처리후 문자열                                    */
/*---------------------------------------------------------------*/
char	*mid_char	( char *sorc
					, int   start
					, int   slen
					)
{
    strncpy(str_make,sorc+start-1,slen);
    str_make[slen] = '\0';
    return (str_make);
}


/*---------------------------------------------------------------*/
/*	Function  : rep_char                                         */
/*	Action    : Special Character Change (Replace)               */
/*	Parameter : sorc   : 원시문자열                              */
/*	            find[] : 치환전문자열                            */
/*	            repl[] : 치환후문자열                            */
/*	Return    : 처리후 문자열                                    */
/*---------------------------------------------------------------*/
char	*rep_char	( char *sorc
					, char  find[]
					, char  repl[]
					)
{
int     bef, aft;
int     cnt ;

    strcpy(str_make, "\0");
    if (Char_Check(sorc, find) < 0) {
    	strcpy(str_make, sorc);
    	return (str_make);
    }

    for (bef = 0, aft = 0; bef <= strlen(sorc); ) {
        if (!strncmp(sorc+bef,find,strlen(find)))
            if (strlen(repl) > 0) {
                strcat(str_make, repl);
                aft += strlen(repl);
                bef += strlen(find);
            }
            else
                bef += strlen(find);

        else {
            strncat(str_make,sorc+bef,1);
            bef++;
            aft++;
        }
    }
    str_make[aft] = '\0';
    return (str_make);
}


/*---------------------------------------------------------------*/
/*	Function  : upper_char                                       */
/*	Action    : Special Character Change (소문자 -> 대문자)      */
/*	Parameter : sorc   : 원시문자열                              */
/*	Return    : 처리후 문자열                                    */
/*---------------------------------------------------------------*/
char	*upper_char	( char *sorc
					)
{
	int ff;
    
    memset (str_make, 0x00, sizeof (str_make));
    strcpy (str_make, sorc);
    
    for (ff = 0; ff < strlen (str_make); ff++) {
			str_make[ff] = toupper(str_make[ff]);
		}
    return (str_make);
}

/*---------------------------------------------------------------*/
/*	Function  : lower_char                                       */
/*	Action    : Special Character Change (대문자 -> 소문자)      */
/*	Parameter : sorc   : 원시문자열                              */
/*	Return    : 처리후 문자열                                    */
/*---------------------------------------------------------------*/
char	*lower_char	( char *sorc
					)
{
int ff;

    memset (str_make, 0x00, sizeof (str_make));
    strcpy (str_make, sorc);
    for (ff = 0; ff < strlen (str_make); ff++) {
         str_make[ff] = tolower(str_make[ff]);
	}

    return (str_make);
}

/*---------------------------------------------------------------*/
/*	Function  : Right_Char_Check                                 */
/*	Action    : Special Character Check (INSTR : 오른쪽에서)     */
/*	Parameter : sorc : 원시문자열                                */
/*	            find : 찿는문자열                                */
/*	Return    : 위치                                             */
/*---------------------------------------------------------------*/
int		Right_Char_Check	( char *sorc
							, char  find
							)
{
int i;

	for(i = strlen(sorc) ; i >= 0 ; i--) {
		if(sorc[i-1] == find) {
			return i;
		}
	}

    return -1;

}



/*---------------------------------------------------------------*/
/*	Function  : cmp_trunc_char                                   */
/*	Action    : Left & Right Space Truncate                      */
/*	Parameter : sorc : 문자열                                    */
/*	Return    : 문자열                                           */
/*---------------------------------------------------------------*/
int		cmp_trunc_char	( char *sorc
						, char *cmp_sorc
						)
{
	memset(str_makeck, 0x00, sizeof(str_makeck));
	sprintf(str_makeck, "%s", trunc_char(sorc));

	return(strcmp(str_makeck, cmp_sorc));

}


/*---------------------------------------------------------------*/
/*	Function  : cmp_rtrunc_char                                  */
/*	Action    : Left & Right Space Truncate                      */
/*	Parameter : sorc : 문자열                                    */
/*	Return    : 문자열                                           */
/*---------------------------------------------------------------*/
int		cmp_rtrunc_char	( char *sorc
						, char *cmp_sorc
						)
{
	memset(str_makeck, 0x00, sizeof(str_makeck));
	sprintf(str_makeck, "%s", rtrunc_char(sorc));

	return(strcmp(str_makeck, cmp_sorc));

}


/*---------------------------------------------------------------*/
/*	Function  : left_char                                        */
/*	Action    : 왼쪽 문자열 n자리 Find                           */
/*	Parameter : sorc : 원시문자열                                */
/*	            Lcnt : 문자열수                                  */
/*	Return    : 처리후 문자열                                    */
/*---------------------------------------------------------------*/
int		cmp_left_char	( char *sorc
						, int   Lcnt
						, char *cmp_sorc
						)
{
	memset(str_makeck, 0x00, sizeof(str_makeck));
	sprintf(str_makeck, "%s", left_char(sorc, Lcnt));

	return(strcmp(str_makeck, cmp_sorc));

}

/*---------------------------------------------------------------*/
/*	Function  : right_char                                       */
/*	Action    : 오른쪽 문자열 n자리 Find                         */
/*	Parameter : sorc : 원시문자열                                */
/*	            Rcnt : 문자열수                                  */
/*	Return    : 처리후 문자열                                    */
/*---------------------------------------------------------------*/
int		cmp_right_char	( char *sorc
						, int  Rcnt
						, char *cmp_sorc
						)
{
	memset(str_makeck, 0x00, sizeof(str_makeck));
	sprintf(str_makeck, "%s", right_char(sorc, Rcnt));

	return(strcmp(str_makeck, cmp_sorc));

}

/*---------------------------------------------------------------*/
/*	Function  : mid_char                                         */
/*	Action    : s자리부터 n자리 문자열 Find                      */
/*	Parameter : sorc  : 원시문자열                               */
/*	            start : 시작위치                                 */
/*	            slen  : 문자열수                                 */
/*	Return    : 처리후 문자열                                    */
/*---------------------------------------------------------------*/
int		cmp_mid_char	( char *sorc
					, int   start
					, int   slen
					, char *cmp_sorc
					)
{
	memset(str_makeck, 0x00, sizeof(str_makeck));
	sprintf(str_makeck, "%s", mid_char(sorc, start, slen));

	return(strcmp(str_makeck, cmp_sorc));
}


/*---------------------------------------------------------------*/
/*	Function  : lower_char                                       */
/*	Action    : Special Character Change (대문자 -> 소문자)      */
/*	Parameter : sorc   : 원시문자열                              */
/*	Return    : 처리후 문자열                                    */
/*---------------------------------------------------------------*/
int		cmp_lower_char	( char *sorc
					, char *cmp_sorc
					)
{
	memset(str_makeck, 0x00, sizeof(str_makeck));
	sprintf(str_makeck, "%s", lower_char(sorc));

	return(strcmp(str_makeck, cmp_sorc));
}





/*---------------------------------------------------------------*/
/*	Function  : upper_char                                       */
/*	Action    : Special Character Change (소문자 -> 대문자)      */
/*	Parameter : sorc   : 원시문자열                              */
/*	Return    : 처리후 문자열                                    */
/*---------------------------------------------------------------*/
int		cmp_upper_char	( char *sorc
					, char *cmp_sorc
					)
{
	memset(str_makeck, 0x00, sizeof(str_makeck));
	sprintf(str_makeck, "%s", upper_char(sorc));

	return(strcmp(str_makeck, cmp_sorc));
}

/*---------------------------------------------------------------*/
/*                E N D   O F   F I L E                          */
/*---------------------------------------------------------------*/
