/*-----------------------------------------------------------------
 ┌──────┬───────────────────────┐
 │ 프로그램명 │ dostounix.c                                  │
 ├──────┼───────────────────────┤
 │ 기      능 │ Server Directory/File List Get               │
 ├──────┼───────────────────────┤
 │ 작  성  일 │ 2013. 12. 06                                 │
 ├──────┼───────────────────────┤
 │ 작  성  자 │ 최   인   숙                                 │
 └──────┴───────────────────────┘
-----------------------------------------------------------------*/

#define		dfMain	1

#include	<ecamsapi.h>


/*****************************************************************/
/*                                                               */
/*		Server Directory/File List Get 처리   M A I N            */
/*                                                               */
/*****************************************************************/
int		main (int argc, char **argv, char **envp)
{
char	filename            [dfFullPath];


    if (argc != 2) {
        printf ("\nUSAGE : %s <FILENAME>  \n\n", argv[0]);
        exit (1);
    }

    memset (filename, 0x00, sizeof (filename));

	FILE *in, *out;
	int ch,
	    rval = FALSE;
	char temppath [20];
	struct utimbuf { time_t actime, modtime; } ut_buf;

	strcpy (temppath, "./tmp/clntmp");
#if !defined (MSDOS)
	strcat (temppath, "XXXXXX");
	mktemp (temppath);
#endif	
	if ((in=fopen (path, R_CNTRL)) == (FILE *) 0)
		return TRUE;
	if ((out=fopen (temppath, W_CNTRL)) == (FILE *) 0)
	{
		fclose (in);
		return TRUE;
	}
	while ((ch = getc (in)) != EOF)
		if ((ch != '\015' && ch != '\032') &&
			(putc (ch, out) == EOF)           )
		{
			rval = TRUE;
			break;
		}
	if (fclose (in) == EOF)
	{
		rval = TRUE;
	}
	if (fclose (out) == EOF)
	{
		rval = TRUE;
	}
	ut_buf.actime = s_buf.st_atime;
	ut_buf.modtime = s_buf.st_mtime;
	if (utime (temppath, &ut_buf) == -1)
		rval = TRUE;
	if (unlink (path) == -1)
		rval = TRUE;
	if (rval)
	{
		unlink (temppath);
		return TRUE;
	}
	if (link (temppath,path) == -1)
	{
		fprintf (stderr, "Dos2Unix: Problems renaming '%s' to '%s'\n", temppath, path);
		fprintf (stderr, "          However, file '%s' remains\n", temppath);
		exit (1);
	}
	unlink (temppath);

}


/*---------------------------------------------------------------*/
/*             E N D    O F    P R O G R A M                     */
/*---------------------------------------------------------------*/
