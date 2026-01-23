/*-----------------------------------------------------------------
 ┌──────┬───────────────────────┐
 │ 프로그램명 │ tarutil.c                                    │
 ├──────┼───────────────────────┤
 │ 기      능 │ tar 파일 작성                                │
 ├──────┼───────────────────────┤
 │ 작  성  일 │ 2011. 07. 28                                 │
 ├──────┼───────────────────────┤
 │ 작  성  자 │ 최   병   남                                 │
 └──────┴───────────────────────┘
-----------------------------------------------------------------*/

/*---------------------------------------------------------------*/
/*     Header files                                              */
/*---------------------------------------------------------------*/
#include    <ecamsapi.h>
#include	<md5.h>
#include 	<libtar.h>
#include 	<compat.h>


#define MODE_LIST		1
#define MODE_CREATE		2
#define MODE_EXTRACT	3

int verbose = 1;
int use_gnu = 1;

int
create(char *tarfile, char *rootdir, libtar_list_t *l)
{
	TAR *t;
	char *pathname;
	char buf[MAXPATHLEN];
	libtar_listptr_t lp;

	if (tar_open(&t, tarfile, NULL,
		     O_WRONLY | O_CREAT, 0644,
		     (verbose ? TAR_VERBOSE : 0)
		     | (use_gnu ? TAR_GNU : 0)) == -1)
	{
		fprintf(stderr, "tar_open(): %s\n", strerror(errno));
		return -1;
	}

	libtar_listptr_reset(&lp);
	while (libtar_list_next(l, &lp) != 0)
	{
		pathname = (char *)libtar_listptr_data(&lp);
		if (pathname[0] != '/' && rootdir != NULL)
			snprintf(buf, sizeof(buf), "%s/%s", rootdir, pathname);
		else
			strlcpy(buf, pathname, sizeof(buf));
		if (tar_append_tree(t, buf, pathname) != 0)
		{
			fprintf(stderr,
				"tar_append_tree('%s', '%s'): %s\n", buf,
				pathname, strerror(errno));
			tar_close(t);
			return -1;
		}
	}

	if (tar_append_eof(t) != 0)
	{
		fprintf(stderr, "tar_append_eof(): %s\n", strerror(errno));
		tar_close(t);
		return -1;
	}

	if (tar_close(t) != 0)
	{
		fprintf(stderr, "tar_close(): %s\n", strerror(errno));
		return -1;
	}
	return 0;
}


int
list(char *tarfile)
{
	TAR *t;
	int i;

	if (tar_open(&t, tarfile,
		     NULL,
		     O_RDONLY, 0,
		     (verbose ? TAR_VERBOSE : 0)
		     | (use_gnu ? TAR_GNU : 0)) == -1)
	{
		fprintf(stderr, "tar_open(): %s\n", strerror(errno));
		return -1;
	}

	while ((i = th_read(t)) == 0)
	{
		th_print_long_ls(t);
#ifdef DEBUG
		th_print(t);
#endif
		if (TH_ISREG(t) && tar_skip_regfile(t) != 0)
		{
			fprintf(stderr, "tar_skip_regfile(): %s\n",
				strerror(errno));
			return -1;
		}
	}

#ifdef DEBUG
	printf("th_read() returned %d\n", i);
	printf("EOF mark encountered after %ld bytes\n",
	       lseek(t->fd, 0, SEEK_CUR)
	       );
#endif

	if (tar_close(t) != 0)
	{
		fprintf(stderr, "tar_close(): %s\n", strerror(errno));
		return -1;
	}

	return 0;
}


int
extract(char *tarfile, char *rootdir)
{
	TAR *t;

#ifdef DEBUG
	puts("opening tarfile...");
#endif
	if (tar_open(&t, tarfile,
		     NULL,
		     O_RDONLY, 0,
		     (verbose ? TAR_VERBOSE : 0)
		     | (use_gnu ? TAR_GNU : 0)) == -1)
	{
		fprintf(stderr, "tar_open(): %s\n", strerror(errno));
		return -1;
	}

#ifdef DEBUG
	puts("extracting tarfile...");
#endif
	if (tar_extract_all(t, rootdir) != 0)
	{
		fprintf(stderr, "tar_extract_all(): %s\n", strerror(errno));
		return -1;
	}

#ifdef DEBUG
	puts("closing tarfile...");
#endif
	if (tar_close(t) != 0)
	{
		fprintf(stderr, "tar_close(): %s\n", strerror(errno));
		return -1;
	}

	return 0;
}


/* 함수명 :tar_index_init												*/
/* 내용:  새로운 index파일 생성을 위해 기존 Idx파일 삭제				*/


int tar_index_init(char *pAcptno,char *reqPath){
	char szCommand[1024];
	int ret;
	char rpath[512];



	memset(rpath,0x00,sizeof(rpath));
	sprintf(rpath,"%s",reqPath);

	if (access(rpath, F_OK) != 0){
		Local_Dir_Make(rpath);
	}

	memset(szCommand,0x00,sizeof(szCommand));
	sprintf(szCommand,"\\rm -rf %s/%s*.rst",reqPath,pAcptno);

	ret = system(szCommand) /256;

	if (ret != 0)
		return 1;

	memset(szCommand,0x00,sizeof(szCommand));
	sprintf(szCommand,"\\rm -rf %s/%s*.tar",reqPath,pAcptno);

	ret = system(szCommand) /256;

	if (ret != 0)
		return 1;

	memset(szCommand,0x00,sizeof(szCommand));
	sprintf(szCommand,"\\rm -rf %s/%s*.idx",reqPath,pAcptno);

	ret = system(szCommand) /256;


	if (ret != 0)
		return 1;
	else
		return 0;

}

/* 함수명 :tar_index_insert												*/
/* 내용:  index 파일에 내용추가     									*/
/* Index파일 구조
	line: from file name구분자to file name구분자to dirpath구분자md5sum  */
/* 리턴값: 0 정상
		   1 에러														*/
int 	tar_index_insert_Z	( char *pAcptno
							, char *reqPath
							, char *svrIp
							, int   nPort
							, char *fFilename
							, char *tFilename
							, char *tPath
							, char *pchmod
							, char *pchown
							, char *pchgrp
							, char *touchtime
							, char *bPath
							, char *bFilename
							)
{
char	filename                   [512];
char 	md5val                      [33];
char 	indexname                  [512];
char 	szchmod                      [4];
char 	szchown                     [50];
char 	szchgrp                     [50];
char 	sztime                      [13];
char 	szbackpath                 [512];
char 	szbackname                 [512];
FILE 	*indexfptr                      ;


	memset(szchmod   , 0x00,sizeof(szchmod   ));
	memset(szchown   , 0x00,sizeof(szchown   ));
	memset(szchgrp   , 0x00,sizeof(szchgrp   ));
	memset(sztime    , 0x00,sizeof(sztime    ));
	memset(szbackname, 0x00,sizeof(szbackname));
	memset(indexname,0x00,sizeof(indexname));
	memset(md5val,0x00,sizeof(md5val));
	memset(filename,0x00,sizeof(filename));
	sprintf(indexname,"%s/%s_%s_%d.idx",reqPath,pAcptno,svrIp,nPort);
	sprintf(filename,"%s/%s",reqPath,fFilename);


	indexfptr = fopen(indexname,"a+");

	if (indexfptr == NULL)
		return 1;
	
												
	if (MD5SUM(filename,md5val) != 0){
		fclose(indexfptr);
		return 1;
	}
												
	if (strlen(pchmod) < 3){
		strcpy(szchmod,"@");
	}
	else{
		strcpy(szchmod,pchmod);
	}
	if (strlen(pchown) < 1){
		strcpy(szchown,"@");
	}
	else{
		strcpy(szchown,pchown);
	}
	if (strlen(pchgrp) < 1){
		strcpy(szchgrp,"@");
	}
	else{
		strcpy(szchgrp,pchgrp);
	}
	if (strlen(touchtime) < 12){
		strcpy(sztime,"@");
	}
	else{
		strcpy(sztime,touchtime);
	}
	if (strlen(bFilename) == 0 || strlen(bFilename) < 1){
		strcpy(szbackname,"@");
	}
	else{
		strcpy(szbackname,bFilename);
	}
	if (strlen(bPath) == 0 || strlen(bPath) < 1){
		strcpy(szbackpath,"@");
	}
	else{
		strcpy(szbackpath,bPath);
	}	
	fprintf(indexfptr,"%s|%s|%s|%s|%s|%s|%s|%s|%s|%s\n",fFilename,tFilename,tPath,md5val,
														szchmod,szchown,szchgrp,sztime,szbackpath,szbackname);

	fflush(indexfptr);

	fclose(indexfptr);

	return 0;
}

int	tar_index_insert_X(char *pAcptno,char *reqPath,char *svrIp,int nPort,char *fFilename,char *tFilename,char *tPath){
	char filename[512];
	char md5val[33];
	char indexname[512];
	FILE *indexfptr;

	memset(indexname,0x00,sizeof(indexname));
	memset(md5val,0x00,sizeof(md5val));
	memset(filename,0x00,sizeof(filename));


	sprintf(indexname,"%s/%s_%s_%d.idx",reqPath,pAcptno,svrIp,nPort);
	sprintf(filename,"%s/%s",reqPath,fFilename);


	indexfptr = fopen(indexname,"a+");

	if (indexfptr == NULL)
		return 1;


	/*
	if (md5sum(filename,md5val) != 0){
		fclose(indexfptr);
		return 1;
	}
	*/

	fprintf(indexfptr,"%s|%s|%s\n",fFilename,tFilename,tPath);

	fflush(indexfptr);

	fclose(indexfptr);

	return 0;
}


int	tarFileMake_comp_X(char *idxfile){
	FILE *fptr;
	FILE *fptr3;
	char linebuffer[2048];
	char linebuffer2[2048];
	char targetFile[512];
	char newIndexfile[512];
	char lsallfile[512];
	char tarfile[512];
	char filename[512];
	char szCommand[512];
	int	 nRet;
	char dirpath[512];
	int	 Index_key=0;
	char *szKeys[10];
	char *toktmp;
	char md5val[33];
	libtar_list_t *l;

	nRet = Right_Char_Check(idxfile,(char)'/');

	if (nRet < 0){
		fprintf(stderr,"idxfile not found '/' =[%s][%d]\n",idxfile,nRet);
		return 1;
	}

	memset(dirpath,0x00,sizeof(dirpath));
	strncpy(dirpath,idxfile,nRet);


	nRet = Char_Check(idxfile,".idx");


	if (nRet < 0){
		fprintf(stderr,"idxfile not found .idx  =[%s][%d]\n",idxfile,nRet);
		return 1;
	}

	memset(lsallfile,0x00,sizeof(lsallfile));
	memset(tarfile,0x00,sizeof(tarfile));
	memset(newIndexfile,0x00,sizeof(newIndexfile));

	strncpy(tarfile,idxfile,strlen(idxfile)-4);
	strcat(tarfile,".tar");

	strncpy(newIndexfile,idxfile,strlen(idxfile)-4);
	strcat(newIndexfile,".nix");

	strncpy(lsallfile,idxfile,strlen(idxfile)-4);
	strcat(lsallfile,".lsf");

	fptr = fopen (idxfile,"r");


	if (fptr == NULL){
		fprintf(stderr,"idxfile not found=[%s]\n",idxfile);
		return 1;
	}

	fptr3 = fopen (newIndexfile,"w+");


	if (fptr3 == NULL){
		fprintf(stderr,"newIndexfile cant make=[%s]\n",newIndexfile);
		return 1;
	}


	l = libtar_list_new(LIST_QUEUE, NULL);

	while (fgets(linebuffer, 2048, fptr) != (char *) NULL) {
        /*-------------------------------------------------------*/
    	/* Return Valure Truncate                                */
        /*-------------------------------------------------------*/

        sprintf(linebuffer, "%s", rep_char(linebuffer,"\n"  , ""));
        sprintf(linebuffer, "%s", rep_char(linebuffer,"\r\n", ""));
        sprintf(linebuffer, "%s", trunc_char(linebuffer));

		memset(linebuffer2,0x00,sizeof(linebuffer2));
		strcpy(linebuffer2,linebuffer);



		Index_key = 0;
		while(1){
			if (Index_key == 0)
				szKeys[Index_key] = strtok_r(linebuffer,"|",&toktmp);
			else
				szKeys[Index_key] = strtok_r(NULL,"|",&toktmp);

			if (szKeys[Index_key] == NULL)
				break;
			Index_key++;
		}

		if (Index_key == 0){
			fprintf(stderr,"Index_key == 0\n");
			return 1;
		}

		memset(szCommand,0x00,sizeof(szCommand));

		sprintf(szCommand,"ls -al '%s/%s' >> '%s'",szKeys[2],szKeys[1],lsallfile);

		nRet = system(szCommand) /256;

		memset(filename,0x00,sizeof(filename));
		sprintf(filename,"%s/%s",szKeys[2],szKeys[1]);


		if (access(filename,F_OK) == 0){
			memset(szCommand,0x00,sizeof(szCommand));
			memset(filename,0x00,sizeof(filename));

			sprintf(szCommand,"cp '%s/%s' '%s/%s'",szKeys[2],szKeys[1],dirpath,szKeys[0]);

			nRet = system(szCommand) /256;

			if (nRet != 0){
				fprintf(stderr,"szCommand fail=[%s][%d]\n",szCommand,nRet);
				return 1;
			}


			sprintf(filename,"%s/%s",dirpath,szKeys[0]);

			memset(md5val,0x00,sizeof(md5val));

			if (md5sum(filename,md5val) != 0){
				fclose(fptr);
				fclose(fptr3);
				return 1;
			}

			fprintf(fptr3,"%s|%s\n",linebuffer2,md5val);
			fflush(fptr3);

			libtar_list_add(l, szKeys[0]);
		}
		else{
			fprintf(fptr3,"%s|9999\n",linebuffer2);
			fflush(fptr3);
		}
	}

	nRet = Right_Char_Check(idxfile,(char)'/');

	if (nRet < 0){
		fprintf(stderr,"indexfile not found '/' =[%s][%d]\n",idxfile,nRet);
		return 1;
	}

	libtar_list_add(l, idxfile+nRet);


	fclose(fptr);
	fclose(fptr3);

	memset(szCommand,0x00,sizeof(szCommand));

	sprintf(szCommand,"cp '%s' '%s'",newIndexfile,idxfile);

	nRet = system(szCommand) /256;

	if (nRet != 0){
		fprintf(stderr,"newIndexfile cp fail=[%s][%d]\n",szCommand,nRet);
		return 1;
	}

	if (create(tarfile, dirpath, l) != 0)
		return 1;

	return 0;
}


int	tarFileMake_comp_Z(char *tarfile){
	FILE *fptr;
	char linebuffer[2048];
	int	 Index_key=0;
	char *szKeys[20];
	char *toktmp;
	char dirpath[512];
	int	 nRet;
	char szCommand[512];
	char indexfile[512];

	libtar_list_t *l;

	nRet = Right_Char_Check(tarfile,(char)'/');

	if (nRet < 0){
		fprintf(stderr,"tarfile not found3 '/' =[%s][%d]\n",tarfile,nRet);
		return 1;
	}

	memset(dirpath,0x00,sizeof(dirpath));
	strncpy(dirpath,tarfile,nRet-1);


	nRet = Char_Check(tarfile,".tar");


	if (nRet < 0){
		fprintf(stderr,"tarfile not found .tar  =[%s][%d]\n",tarfile,nRet);
		return 1;
	}


	memset(indexfile,0x00,sizeof(indexfile));


	strncpy(indexfile,tarfile,strlen(tarfile)-4);
	strcat(indexfile,".idx");


	fptr = fopen (indexfile,"r");


	if (fptr == NULL){
		fprintf(stderr,"indexfile not found=[%s]\n",indexfile);
		return 1;
	}



	l = libtar_list_new(LIST_QUEUE, NULL);


	while (fgets(linebuffer, 2048, fptr) != (char *) NULL) {
        /*-------------------------------------------------------*/
    	/* Return Valure Truncate                                */
        /*-------------------------------------------------------*/
        sprintf(linebuffer, "%s", rep_char(linebuffer,"\n"  , ""));
        sprintf(linebuffer, "%s", rep_char(linebuffer,"\r\n", ""));
        sprintf(linebuffer, "%s", trunc_char(linebuffer));

		Index_key = 0;
		while(1){
			if (Index_key == 0)
				szKeys[Index_key] = strtok_r(linebuffer,"|",&toktmp);
			else
				szKeys[Index_key] = strtok_r(NULL,"|",&toktmp);

			if (szKeys[Index_key] == NULL)
				break;
			Index_key++;
		}

		if (Index_key == 0){
			fprintf(stderr,"Index_key == 0\n");
			return 1;
		}
		libtar_list_add(l, szKeys[0]);

	}

	nRet = Right_Char_Check(indexfile,(char)'/');

	if (nRet < 0){
		fprintf(stderr,"indexfile not found '/' =[%s][%d]\n",indexfile,nRet);
		return 1;
	}


	libtar_list_add(l, indexfile+nRet);


	fclose(fptr);
	if (create(tarfile, dirpath, l) != 0)
		return 1;



	return 0;

}


int	tarFileMake_ext_X(char *tarfile){
	FILE *fptr;
	FILE *fptr2;
	char linebuffer[2048];
	char linebuffer2[2048];
	int	 Index_key=0;
	char *szKeys[10];
	char *toktmp;
	char dirpath[512];
	int	 nRet;
	char szCommand[512];
	char indexfile[512];
	char filename[512];
	char rstfile[512];
	char md5val[33];


	nRet = Right_Char_Check(tarfile,(char)'/');

	if (nRet < 0){
		fprintf(stderr,"tarfile not found1 '/' =[%s][%d]\n",tarfile,nRet);
		return 1;
	}

	memset(dirpath,0x00,sizeof(dirpath));
	strncpy(dirpath,tarfile,nRet);


	nRet = Char_Check(tarfile,".tar");


	if (nRet < 0){
		fprintf(stderr,"tarfile not found .idx  =[%s][%d]\n",tarfile,nRet);
		return 1;
	}

	memset(indexfile,0x00,sizeof(indexfile));
	memset(rstfile,0x00,sizeof(rstfile));

	strncpy(indexfile,tarfile,strlen(tarfile)-4);
	strcat(indexfile,".idx");

	strncpy(rstfile,tarfile,strlen(tarfile)-4);
	strcat(rstfile,".rst");


	memset(szCommand,0x00,sizeof(szCommand));

	if (extract(tarfile, dirpath) != 0)
		return 1;


	fptr = fopen (indexfile,"r");


	if (fptr == NULL)
		return 1;

	fptr2 = fopen (rstfile,"w");

	if (fptr2 == NULL)
		return 1;


	while (fgets(linebuffer, 2048, fptr) != (char *) NULL) {
        /*-------------------------------------------------------*/
    	/* Return Valure Truncate                                */
        /*-------------------------------------------------------*/

        sprintf(linebuffer, "%s", rep_char(linebuffer,"\n"  , ""));
        sprintf(linebuffer, "%s", rep_char(linebuffer,"\r\n", ""));
        sprintf(linebuffer, "%s", trunc_char(linebuffer));

        memset(linebuffer2,0x00,sizeof(linebuffer2));
        strcpy(linebuffer2,linebuffer);

		Index_key = 0;
		while(1){
			if (Index_key == 0)
				szKeys[Index_key] = strtok_r(linebuffer,"|",&toktmp);
			else
				szKeys[Index_key] = strtok_r(NULL,"|",&toktmp);

			if (szKeys[Index_key] == NULL)
				break;
			Index_key++;
		}

		if (Index_key == 0){
			return 1;
		}
		memset(filename,0x00,sizeof(filename));
		memset(md5val,0x00,sizeof(md5val));
		sprintf(filename,"%s/%s",dirpath,szKeys[0]);

		if (access(filename, F_OK) == 0){
			nRet = MD5SUM(filename,md5val);

			if (nRet != 0)
				return 1;

			if (strncmp(md5val,szKeys[3],32) == 0){

				fprintf(fptr2,"%s|0\n",linebuffer2);
				fflush(fptr2);


				continue;
			}
			else{
				fprintf(fptr2,"%s|2|%s\n",linebuffer2,md5val);
				fflush(fptr2);				
			}
		}
		else{

			fprintf(fptr2,"%s|4|File Not found\n",linebuffer2);
			fflush(fptr2);


		}

	}

	fclose(fptr);
	fclose(fptr2);
	remove(indexfile);

	return 0;

}

int	tarFileMake_ext_Z(char *tarfile){
	FILE *fptr;
	FILE *fptr2;
	char linebuffer[2048];
	char linebuffer2[2048];
	int	 Index_key=0;
	char *szKeys[20];
	char *toktmp;
	char dirpath[512];
	int	 nRet;
	char szCommand[512];
	char indexfile[512];
	char filename[512];
	char rstfile[512];
	char lsallfile[512];
	char md5val[33];


	nRet = Right_Char_Check(tarfile,(char)'/');

	if (nRet < 0){
		fprintf(stderr,"tarfile not found2 '/' =[%s][%d]\n",tarfile,nRet);
		return 1;
	}

	memset(dirpath,0x00,sizeof(dirpath));
	strncpy(dirpath,tarfile,nRet);


	nRet = Char_Check(tarfile,".tar");


	if (nRet < 0){
		fprintf(stderr,"tarfile not found .idx  =[%s][%d]\n",tarfile,nRet);
		return 1;
	}

	memset(indexfile,0x00,sizeof(indexfile));
	memset(rstfile,0x00,sizeof(rstfile));
	memset(lsallfile,0x00,sizeof(lsallfile));

	strncpy(indexfile,tarfile,strlen(tarfile)-4);
	strcat(indexfile,".idx");

	strncpy(rstfile,tarfile,strlen(tarfile)-4);
	strcat(rstfile,".rst");

	strncpy(lsallfile,tarfile,strlen(tarfile)-4);
	strcat(lsallfile,".lsf");


	if (extract(tarfile, dirpath) != 0)
		return 1;


	fptr = fopen (indexfile,"r");


	if (fptr == NULL)
		return 1;

	fptr2 = fopen (rstfile,"w");

	if (fptr2 == NULL)
		return 1;


	while (fgets(linebuffer, 2048, fptr) != (char *) NULL) {
        /*-------------------------------------------------------*/
    	/* Return Valure Truncate                                */
        /*-------------------------------------------------------*/
        sprintf(linebuffer, "%s", rep_char(linebuffer,"\n"  , ""));
        sprintf(linebuffer, "%s", rep_char(linebuffer,"\r\n", ""));
        sprintf(linebuffer, "%s", trunc_char(linebuffer));

        memset(linebuffer2,0x00,sizeof(linebuffer2));
        strcpy(linebuffer2,linebuffer);

		Index_key = 0;
		while(1){
			if (Index_key == 0)
				szKeys[Index_key] = strtok_r(linebuffer,"|",&toktmp);
			else
				szKeys[Index_key] = strtok_r(NULL,"|",&toktmp);

			if (szKeys[Index_key] == NULL)
				break;
			Index_key++;
		}

		if (Index_key == 0){
			return 1;
		}
		memset(filename,0x00,sizeof(filename));
		memset(md5val,0x00,sizeof(md5val));
		sprintf(filename,"%s/%s",dirpath,szKeys[0]);

		if (access(filename, F_OK) == 0){

			nRet = md5sum(filename,md5val);

			if (nRet != 0)
				return 1;

			if (strncmp(md5val,szKeys[3],32) == 0){
				if (access(szKeys[2],F_OK) != 0){
					Local_Dir_Make2(szKeys[2],szKeys[5],szKeys[6],szKeys[4]);
				}
			
				if (strcmp(szKeys[8],"@") != 0) {
					if (access(szKeys[8], F_OK) != 0){
						Local_Dir_Make2(szKeys[8],szKeys[5],szKeys[6],szKeys[4]);
					}
				}

				if (strcmp(szKeys[9],"@") != 0){
					/*	
					memset(szCommand,0x00,sizeof(szCommand));
					sprintf(szCommand,"rm -rf %s/%s.*.backup",szKeys[2],szKeys[1]);
					nRet = system(szCommand) /256;
					*/
					
					memset(szCommand,0x00,sizeof(szCommand));
					sprintf(szCommand,"cp -p '%s/%s' '%s/%s' > /dev/null",szKeys[2],szKeys[1],szKeys[8],szKeys[9]);
					nRet = system(szCommand) /256;
				}


				memset(szCommand,0x00,sizeof(szCommand));

				sprintf(szCommand,"cp '%s/%s' '%s/%s' > /dev/null",dirpath,szKeys[0],szKeys[2],szKeys[1]);

				nRet = system(szCommand) /256;

				if (nRet != 0){
					fprintf(fptr2,"%s|1|%s|%d\n",linebuffer2,szCommand,nRet);
					fflush(fptr2);

					memset(szCommand,0x00,sizeof(szCommand));
					sprintf(szCommand,"ls -al '%s/%s' >> '%s'",szKeys[2],szKeys[1],lsallfile);
					nRet = system(szCommand) /256;

					continue;
				}

				if (strcmp(szKeys[4],"@") != 0){
					memset(szCommand,0x00,sizeof(szCommand));
					sprintf(szCommand,"chmod %s '%s/%s'",szKeys[4],szKeys[2],szKeys[1]);
					nRet = system(szCommand) /256;

					if (nRet != 0){
						fprintf(fptr2,"%s|3|%s|%d\n",linebuffer2,szCommand,nRet);
						fflush(fptr2);

						memset(szCommand,0x00,sizeof(szCommand));
						sprintf(szCommand,"ls -al '%s/%s' >> '%s'",szKeys[2],szKeys[1],lsallfile);
						nRet = system(szCommand) /256;
						continue;
					}
				}

				if (strcmp(szKeys[5],"@") != 0){
					memset(szCommand,0x00,sizeof(szCommand));
					sprintf(szCommand,"chown %s '%s/%s'",szKeys[5],szKeys[2],szKeys[1]);
					nRet = system(szCommand) /256;

					if (nRet != 0){
						fprintf(fptr2,"%s|3|%s|%d\n",linebuffer2,szCommand,nRet);
						fflush(fptr2);

						memset(szCommand,0x00,sizeof(szCommand));
						sprintf(szCommand,"ls -al '%s/%s' >> '%s'",szKeys[2],szKeys[1],lsallfile);
						nRet = system(szCommand) /256;
						continue;
					}
				}

				if (strcmp(szKeys[6],"@") != 0){
					memset(szCommand,0x00,sizeof(szCommand));
					sprintf(szCommand,"chgrp %s '%s/%s'",szKeys[6],szKeys[2],szKeys[1]);
					nRet = system(szCommand) /256;

					if (nRet != 0){
						fprintf(fptr2,"%s|3|%s|%d\n",linebuffer2,szCommand,nRet);
						fflush(fptr2);

						memset(szCommand,0x00,sizeof(szCommand));
						sprintf(szCommand,"ls -al '%s/%s' >> '%s'",szKeys[2],szKeys[1],lsallfile);
						nRet = system(szCommand) /256;
						continue;
					}

				}

				if (strcmp(szKeys[7],"@") != 0){
					memset(szCommand,0x00,sizeof(szCommand));
					sprintf(szCommand,"touch -c -t %s '%s/%s'",szKeys[7],szKeys[2],szKeys[1]);
					nRet = system(szCommand) /256;

				}

				memset(szCommand,0x00,sizeof(szCommand));
				sprintf(szCommand,"ls -al '%s/%s' >> '%s'",szKeys[2],szKeys[1],lsallfile);
				nRet = system(szCommand) /256;

				fprintf(fptr2,"%s|0\n",linebuffer2);
				fflush(fptr2);
			}
			else{
				fprintf(fptr2,"%s|2|%s\n",linebuffer2,md5val);
				fflush(fptr2);
			}
		}
		else {
			fprintf(fptr2,"%s|4|File Not found\n",linebuffer2);
			fflush(fptr2);

		}

	}

	fclose(fptr);
	fclose(fptr2);
	/*remove(indexfile);*/

	return 0;

}

/* 함수명 :md5sum			 		*/
/* 내용:  tar  Index  파일 close 	*/
/* 리턴값: 0 정상
		   1 에러					*/

int md5sum(char *filename,char *md5val){
	FILE *fptr;
	struct md5_ctx context;
	int len;
	int i;
	unsigned char buffer[1024];
	unsigned char digest[16];
	char md5sumval[33];
	int	j;

	fptr = fopen (filename,"rb");

	if (fptr == NULL)
		return 1;

	md5_init_ctx(&context);


	while (len = fread(buffer,1,1024,fptr)){
		md5_process_bytes(buffer,len,&context);
	}


	md5_finish_ctx(&context,digest);

	fclose(fptr);



	memset(md5sumval,0x00,sizeof(md5sumval));
	for (i=0;i<16;i++){
		sprintf(md5sumval,"%s%02x",md5sumval,digest[i]);
	}


	strcpy(md5val,md5sumval);


	return 0;
}

/*---------------------------------------------------------------*/
/*                E N D   O F   F I L E                          */
/*---------------------------------------------------------------*/
