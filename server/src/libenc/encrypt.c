
/*-----------------------------------------------------------------
 ┌──────┬───────────────────────┐
 │ 프로그램명 │ encrypt.c                                    │
 ├──────┼───────────────────────┤
 │ 기      능 │ 암호/복호화 처리 SUB                         │
 ├──────┼───────────────────────┤
 │ 작  성  일 │ 2007. 10. 24                                 │
 ├──────┼───────────────────────┤
 │ 작  성  자 │ 최   병   남                                 │
 └──────┴───────────────────────┘
-----------------------------------------------------------------*/

/*---------------------------------------------------------------*/
/*     Header files                                              */
/*---------------------------------------------------------------*/
#include 	<stdio.h>
#include 	<time.h>
#include 	<des3.h>


/*---------------------------------------------------------------*/
/*		Definitions / Macros                                     */
/*---------------------------------------------------------------*/
#define KILO	1024
#define MEGA	(KILO*KILO)

/*---------------------------------------------------------------*/
/*		Prine out BYTE data in ascending order and with no       */
/*		'0x'(hexa type)                                          */
/*---------------------------------------------------------------*/
#undef PrintBYTE
#define PrintBYTE(pfile, msg, Data, DataLen) {			\
	int idx;											\
	fprintf(pfile, "%5s =", msg);						\
	for( idx=0; idx<(int)DataLen; idx++) {				\
		if( (idx==0) || ((idx%16)!=0) )					\
			fprintf(pfile, " %.2x", Data[idx]);			\
		else											\
			fprintf(pfile, "\n\t%.2x", Data[idx]);		\
	}													\
	fprintf(pfile, "\n");								\
}

/*---------------------------------------------------------------*/
/*		한번에 'ByteLen'-bytes의 데이타를 처리하는 연산 'Oper'을 */
/*		'Iter'번 수행하고, 그 결과를 KByte/sec단위로 출력함.     */
/*---------------------------------------------------------------*/
#define SPEED_TEST(str1, str2, Iter, ByteLen, Oper) {	\
	unsigned idx;										\
	clock_t start, finish;								\
	double Sec, Mbps=0.0;								\
	start = clock();									\
	for( idx=0; idx<Iter; idx++)						\
		{	Oper;	}									\
	finish = clock();									\
	Sec = (double)(finish-start) / CLOCKS_PER_SEC;		\
	if( Sec!=0 )	Mbps = 8.0*ByteLen*idx/Sec/MEGA;	\
	printf("%s%7.3fMbps(=%d*", str1, Mbps, Iter);		\
	printf("%d", ByteLen);								\
	printf("/%3.0f)%s", 1000.0*Sec, str2);				\
}


/*---------------------------------------------------------------*/
/*		Constant (Error Code)                                    */
/*---------------------------------------------------------------*/
#define CTR_USAGE_ERROR		0x2001
#define CTR_KEYFILE_ERROR	0x2002

/*---------------------------------------------------------------*/
/*		Global Variables                                         */
/*---------------------------------------------------------------*/
char	Help[] = "\
Usage1 : -[T/S]   (Test Value/Test Speed)\n\
Usage2 : -[E/D] -[ECB/CBC/OFB/CFB/ECBPAD/CBCPAD/OFBPAD/CFBPAD] infile outfile\n\
            (file 'key.dat' contains UserKey and IV)\n";

/*---------------------------------------------------------------*/
/*		Prototypes                                               */
/*			Error Code 관리 함수                                 */
/*---------------------------------------------------------------*/
typedef struct{
	DWORD		ErrorCode;
	BYTE		Message[32];
} ERROR_MESSAGE;

ERROR_MESSAGE	ErrorMessage[] = {
	{CTR_FATAL_ERROR,		"CTR_FATAL_ERROR"},
	{CTR_INVALID_USERKEYLEN,"CTR_INVALID_USERKEYLEN"},
	{CTR_PAD_CHECK_ERROR,	"CTR_PAD_CHECK_ERROR"},
	{CTR_DATA_LEN_ERROR,	"CTR_DATA_LEN_ERROR"},
	{CTR_CIPHER_LEN_ERROR,	"CTR_CIPHER_LEN_ERROR"},
	{CTR_USAGE_ERROR,		"CTR_USAGE_ERROR"},
	{CTR_KEYFILE_ERROR,		"CTR_KEYFILE_ERROR"},
	{0, ""},
};

/*---------------------------------------------------------------*/
/*		Error 처리 Function                                      */
/*---------------------------------------------------------------*/
void	Error(
		DWORD	ErrorCode,
		char	*Message)
{
	DWORD	i;

	for( i=0; ErrorMessage[i].ErrorCode!=0; i++)
		if( ErrorMessage[i].ErrorCode==ErrorCode )	break;

	printf("ERROR(%s) :::: %s\n", ErrorMessage[i].Message, Message);
	if( ErrorCode==CTR_USAGE_ERROR )	printf("\n%s", Help);
	exit(1);
}

/*---------------------------------------------------------------*/
/*		Validity Test 관련 함수			                         */
/*---------------------------------------------------------------*/
typedef struct{
	DWORD		ModeType;
	DWORD		PadType;
	char		Description[32];
	DWORD		UkLen;
	BYTE		UK[56];
	DWORD		IVLen;
	BYTE		IV[16];
	DWORD		PtLen;
	BYTE		PT[48];
	DWORD		EtLen;
	BYTE		ET[48];
} TEST_STRUCT;

TEST_STRUCT		TestData[] = {
	{	AI_ECB, AI_NO_PADDING, "ECB-NO_PADDING",
		24,		{0x21,0x23,0x45,0x67,0x89,0xAB,0xCD,0xEF,
				 0xf0,0xe1,0xd2,0xc3,0xb4,0xa5,0x96,0x87,
				 0xfe,0xdc,0xba,0x98,0x76,0x54,0x32,0x30},
		 0,		{0},
		32,		{0x37,0x36,0x35,0x34,0x33,0x32,0x31,0x20,
				 0x4E,0x6F,0x77,0x20,0x69,0x73,0x20,0x74,
				 0x68,0x65,0x20,0x74,0x69,0x6D,0x65,0x20,
				 0x66,0x6F,0x72,0x20,0x00,0x00,0x00,0x00},
		32,		{0x62,0xC1,0x0C,0xC9,0xEF,0xBF,0x15,0xAA,
				 0xA5,0xAE,0x2E,0x48,0x7B,0x69,0x0E,0x56,
				 0xD8,0xB1,0xDF,0xB8,0xF5,0xC5,0xB2,0x93,
				 0x85,0x5E,0x77,0xDD,0x90,0x24,0xB1,0xB1}	},
	{	AI_CBC, AI_NO_PADDING, "CBC-NO_PADDING",
		24,		{0x21,0x23,0x45,0x67,0x89,0xAB,0xCD,0xEF,
				 0xf0,0xe1,0xd2,0xc3,0xb4,0xa5,0x96,0x87,
				 0xfe,0xdc,0xba,0x98,0x76,0x54,0x32,0x30},
		 8,		{0,0,0,0,0,0,0,0},
		32,		{0x37,0x36,0x35,0x34,0x33,0x32,0x31,0x20,
				 0x4E,0x6F,0x77,0x20,0x69,0x73,0x20,0x74,
				 0x68,0x65,0x20,0x74,0x69,0x6D,0x65,0x20,
				 0x66,0x6F,0x72,0x20,0x00,0x00,0x00,0x00},
		32,		{0x62,0xC1,0x0C,0xC9,0xEF,0xBF,0x15,0xAA,
				 0x15,0xEE,0xA1,0xEE,0xC4,0x84,0x42,0x93,
				 0x8D,0x6F,0x78,0x06,0x47,0x0E,0x7C,0xD3,
				 0x33,0x66,0xF2,0x80,0xCC,0x60,0x02,0x64}	},
	{	AI_CBC, AI_PKCS_PADDING, "CBC-PKCS_PADDING",
		24,		{0x21,0x23,0x45,0x67,0x89,0xAB,0xCD,0xEF,
				 0xf0,0xe1,0xd2,0xc3,0xb4,0xa5,0x96,0x87,
				 0xfe,0xdc,0xba,0x98,0x76,0x54,0x32,0x30},
		 8,		{0xFE,0xDC,0xBA,0x98,0x76,0x54,0x32,0x10},
		28,		"7654321 Now is the time for ",
		32,		{0x3F,0xE3,0x01,0xC9,0x62,0xAC,0x01,0xD0,
				 0x22,0x13,0x76,0x3C,0x1C,0xBD,0x4C,0xDC,
				 0x79,0x96,0x57,0xC0,0x64,0xEC,0xF5,0xD4,
				 0x14,0xC2,0x05,0x05,0x04,0x45,0x70,0x37}	},
	{	AI_OFB, 0, "OFB",
		24,		{0x21,0x23,0x45,0x67,0x89,0xAB,0xCD,0xEF,
				 0xf0,0xe1,0xd2,0xc3,0xb4,0xa5,0x96,0x87,
				 0xfe,0xdc,0xba,0x98,0x76,0x54,0x32,0x30},
		 8,		{0,0,0,0,0,0,0,0},
		28,		"7654321 Now is the time for ",
		28,		{0x95,0x2B,0xD1,0x53,0x2E,0x77,0x50,0x18,
				 0xAA,0x24,0xCF,0x50,0x02,0xC1,0xE4,0x12,
				 0x2A,0xAC,0xAE,0x06,0x99,0xB8,0xF5,0x8C,
				 0xCD,0x67,0x39,0xC6}	},
	{	AI_CFB, 0, "CFB",
		24,		{0x21,0x23,0x45,0x67,0x89,0xAB,0xCD,0xEF,
				 0xf0,0xe1,0xd2,0xc3,0xb4,0xa5,0x96,0x87,
				 0xfe,0xdc,0xba,0x98,0x76,0x54,0x32,0x30},
		 8,		{0,0,0,0,0,0,0,0},
		28,		"7654321 Now is the time for ",
		28,		{0x95,0x2B,0xD1,0x53,0x2E,0x77,0x50,0x18,
				 0x9A,0xE4,0x15,0x19,0x90,0xB9,0x45,0xD9,
				 0x30,0x6E,0x84,0x70,0x4C,0x03,0xAC,0x7A,
				 0x9D,0x86,0x74,0x90}	},
	{	0, 0, ""},
};

/*---------------------------------------------------------------*/
/*		Validity Test                                            */
/*---------------------------------------------------------------*/
void	ValidityTest()
{
	BYTE	EncText[4*DES3_BLOCK_LEN], DecText[4*DES3_BLOCK_LEN];
	DWORD	i, tt, EncLen, DecLen;
	RET_VAL	ret;
	DES3_ALG_INFO	AlgInfo;

	for( i=0; TestData[i].ModeType!=0; i++) {
		DES3_SetAlgInfo(TestData[i].ModeType, TestData[i].PadType,
						TestData[i].IV, &AlgInfo);

		ret = DES3_KeySchedule(TestData[i].UK, TestData[i].UkLen, &AlgInfo);
		if( ret!=CTR_SUCCESS )	Error(ret, "DES3_KeySchedule() returns.");

		EncLen = tt = 0;
		ret = DES3_EncInit(&AlgInfo);
		if( ret!=CTR_SUCCESS )	Error(ret, "DES3_EncInit() returns.");
		ret = DES3_EncUpdate(&AlgInfo, TestData[i].PT, TestData[i].PtLen,
							EncText, &EncLen);
		if( ret!=CTR_SUCCESS )	Error(ret, "DES3_EncUpdate() returns.");
		ret = DES3_EncFinal(&AlgInfo, EncText+EncLen, &tt);
		if( ret!=CTR_SUCCESS )	Error(ret, "DES3_EncFinal() returns.");
		EncLen += tt;

		DecLen = tt = 0;
		ret = DES3_DecInit(&AlgInfo);
		if( ret!=CTR_SUCCESS )	Error(ret, "DES3_DecInit() returns.");
		ret = DES3_DecUpdate(&AlgInfo, EncText, EncLen, DecText, &DecLen);
		if( ret!=CTR_SUCCESS )	Error(ret, "DES3_DecUpdate() returns.");
		ret = DES3_DecFinal(&AlgInfo, DecText+DecLen, &tt);
		if( ret!=CTR_SUCCESS )	Error(ret, "DES3_DecFinal() returns.");
		DecLen += tt;

		fprintf(stdout, "\n==== test %d : %s\n", i, TestData[i].Description);
		PrintBYTE(stdout, "UK", TestData[i].UK, TestData[i].UkLen);
		if( TestData[i].IVLen!=0 )
			PrintBYTE(stdout, "IV", TestData[i].IV, TestData[i].IVLen);
		PrintBYTE(stdout, "PT", TestData[i].PT, TestData[i].PtLen);
		PrintBYTE(stdout, "ET", EncText, EncLen);
		if( memcmp(TestData[i].PT, DecText, TestData[i].PtLen)==0 )
			printf("   DT = (OK)\n");
		else
			PrintBYTE(stdout, "DT", DecText, DecLen);
	}
}

/*---------------------------------------------------------------*/
/*		Speed Check                                              */
/*---------------------------------------------------------------*/
void	SpeedTest()
{
#define DATA_LEN	1024
	BYTE	UserKey[DES3_USER_KEY_LEN], IV[DES3_BLOCK_LEN];
	BYTE	PT[DATA_LEN+32], ET[DATA_LEN+32], DT[DATA_LEN+32];
	DWORD	i, tt, Iter, EncLen=0, DecLen;
	RET_VAL	ret;
	DES3_ALG_INFO	AlgInfo;

	for( tt=0; tt<DES3_USER_KEY_LEN; tt++)	UserKey[tt] = 0;
	for( tt=0; tt<DES3_BLOCK_LEN; tt++)		IV[tt] = 0;
	DES3_SetAlgInfo(AI_CBC, AI_NO_PADDING, IV, &AlgInfo);

	ret = DES3_KeySchedule(UserKey, DES3_USER_KEY_LEN, &AlgInfo);
	if( ret!=CTR_SUCCESS )	Error(ret, "DES3_KeySchedule() returns.");

	for( i=0; i<3; i++) {
		fprintf(stdout, "\n==== test %d\n", i);
		Iter = 2 * 2500;

		for( tt=0; tt<DATA_LEN; tt++)	PT[tt] = 0;

		SPEED_TEST("Enc:", " / ", Iter, DATA_LEN,
			{	EncLen = tt = 0;
				ret = DES3_EncInit(&AlgInfo);
				ret = DES3_EncUpdate(&AlgInfo, PT, DATA_LEN,
									ET, &EncLen);
				ret = DES3_EncFinal(&AlgInfo, ET+EncLen, &tt);
				EncLen += tt;									}	);
		SPEED_TEST("Dec:", " // ", Iter, DATA_LEN,
			{	DecLen = tt = 0;
				ret = DES3_DecInit(&AlgInfo);
				ret = DES3_DecUpdate(&AlgInfo, ET, EncLen,
									DT, &DecLen);
				ret = DES3_DecFinal(&AlgInfo, DT+DecLen, &tt);
				DecLen += tt;									}	);

		for( tt=0; tt<DATA_LEN; tt++)
			if( DT[tt]!=0 )	break;
		if( tt==DATA_LEN )	printf("OK");
		else				printf("FAIL");
	}
#undef DATA_LEN
}

/*---------------------------------------------------------------*/
/*		암호화 KEY Read                                          */
/*---------------------------------------------------------------*/
void	GetKeyIV(
		BYTE	Key[DES3_USER_KEY_LEN],
		DWORD	*KeyLen,
		BYTE	IV[DES3_BLOCK_LEN],
		DWORD	*IVLen)
{
	DWORD	ch, i, j;
	FILE	*pfile;

	/*
	if( (pfile=fopen("key.dat", "r"))==NULL ) {
		printf( "The file 'key.dat' was not opened\n");
		Error(0, "File(key.dat) Open Error");
	}

	fscanf(pfile, "%d", &j);
	*KeyLen = j;
	for( i=0; i<j; i++) {
		fscanf(pfile, "%X", &ch);
		Key[i] = (BYTE) ch;
	}

	fscanf(pfile, "%d", &j);
	if( j!=DES3_BLOCK_LEN ) {
		*IVLen = 0;
		for( i=0; i<DES3_BLOCK_LEN; i++)
			IV[i] = (BYTE) 0;
	}
	else {
		*IVLen = DES3_BLOCK_LEN;
		for( i=0; i<DES3_BLOCK_LEN; i++) {
			fscanf(pfile, "%X", &ch);
			IV[i] = (BYTE) ch;
		}
	}

	fclose(pfile);
	*/

	j = 24;
	*KeyLen = j;

	j = 8;
	*IVLen = DES3_BLOCK_LEN;

}

/*---------------------------------------------------------------*/
/*		암호화 대상 파일 READ 하여 결과 파일 작성                */
/*---------------------------------------------------------------*/
void	GeneralTest(
		DWORD	EncFile,
		DWORD	EncType,
		DWORD	ModeType,
		DWORD	PadType,
		char	*infile,
		char	*outfile)
{
	FILE	*pIn, *pOut;
	BYTE	UserKey[DES3_USER_KEY_LEN] = {0x31, 0x33, 0x45, 0x67, 0x89, 0xAB, 0xCD, 0xEF,	\
										  0xf0, 0xe1, 0xd2, 0xc3, 0xb4, 0xa5, 0x96, 0x87, 	\
										  0xfe, 0xdc, 0xba, 0x98, 0x76, 0x54, 0x32, 0x30};
	BYTE	IV[DES3_BLOCK_LEN] = {0xFE, 0xDC, 0xBA, 0x98, 0x76, 0x54, 0x32, 0x30};
	BYTE	SrcData[1024+32], DstData[1024+32];
	DWORD	UKLen, IVLen, SrcLen, DstLen, OffSet;
	RET_VAL	ret;
	DES3_ALG_INFO	AlgInfo;


	puts("GeneralTest Start!!!");

	GetKeyIV(UserKey, &UKLen, IV, &IVLen);

	DES3_SetAlgInfo(ModeType, PadType, IV, &AlgInfo);
	ret = DES3_KeySchedule(UserKey, UKLen, &AlgInfo);
	if( ret!=CTR_SUCCESS ){
		printf("DES3_KeySchedule fail\n");
		Error(ret, "DES3_KeySchedule() returns.");
	}

	/*-----------------------------------------------------------*/
	/*		파일 암호화 (IN : 파일, OUT : 파일)                  */
	/*-----------------------------------------------------------*/
	if (EncFile == 1)
	{
		if( (pIn=fopen(infile, "rb"))==NULL ) {
			printf( "The file '%s' was not opened\n", infile);
			Error(0, "File(infile) Open Error");
		}
		if( (pOut=fopen(outfile, "wb"))==NULL ) {
			printf( "The file '%s' was not opened\n", outfile);
			Error(0, "File(outfile) Open Error");
		}

		/*-------------------------------------------------------*/
		/*		Encryption	                                     */
		/*-------------------------------------------------------*/
		if( EncType == 0 ) {
			ret = DES3_EncInit(&AlgInfo);
			if( ret!=CTR_SUCCESS )	Error(ret, "DES3_EncInit() returns.");

			for(  ;  ;  ) {
				SrcLen = fread(SrcData, sizeof(BYTE), 1024, pIn);
				if( SrcLen==0 )	break;

				DstLen = 1024;
				memset(DstData, 0x00, sizeof(DstData));
				ret = DES3_EncUpdate(&AlgInfo, SrcData, SrcLen, DstData, &DstLen);
				if( ret!=CTR_SUCCESS )	Error(ret, "DES3_EncUpdate() returns.");

				fwrite(DstData, sizeof(BYTE), DstLen, pOut);
			}

			DstLen = 1024;
			memset(DstData, 0x00, sizeof(DstData));
			ret = DES3_EncFinal(&AlgInfo, DstData, &DstLen);
			if( ret!=CTR_SUCCESS )	Error(ret, "DES3_EncFinal() returns.");
			fwrite(DstData, sizeof(BYTE), DstLen, pOut);
		}

		/*-------------------------------------------------------*/
		/*		Decryption	                                     */
		/*-------------------------------------------------------*/
		else {
			ret = DES3_DecInit(&AlgInfo);
			if( ret!=CTR_SUCCESS )	Error(ret, "DES3_DecInit() returns.");

			for(  ;  ;  ) {
				SrcLen = fread(SrcData, sizeof(BYTE), 1024, pIn);
				if( SrcLen==0 )	break;

				DstLen = 1024;
				memset(DstData, 0x00, sizeof(DstData));
				ret = DES3_DecUpdate(&AlgInfo, SrcData, SrcLen, DstData, &DstLen);
				if( ret!=CTR_SUCCESS )	Error(ret, "DES3_DecUpdate() returns.");

				fwrite(DstData, sizeof(BYTE), DstLen, pOut);
			}

			DstLen = 1024;
			memset(DstData, 0x00, sizeof(DstData));
			ret = DES3_DecFinal(&AlgInfo, DstData, &DstLen);
			if( ret!=CTR_SUCCESS )	Error(ret, "DES3_DecFinal() returns.");
			fwrite(DstData, sizeof(BYTE), DstLen, pOut);
		}

		fclose(pIn);
		fclose(pOut);
	}

	/*-----------------------------------------------------------*/
	/*	 문자열 암호화 (IN : 문자열(변수), OUT : 문자열(변수)    */
	/*-----------------------------------------------------------*/
	else
	{
		/*-------------------------------------------------------*/
		/*		Encryption	                                     */
		/*-------------------------------------------------------*/
		OffSet = 0;
		memset(outfile, 0x00, sizeof(outfile));

		if( EncType == 0 ) {
			ret = DES3_EncInit(&AlgInfo);
			if( ret!=CTR_SUCCESS ){
				puts("DES3_EncInit Fail");
				Error(ret, "DES3_EncInit() returns.");
			}

			for(  ;  ;  ) {

				if (strlen(infile) > OffSet + 1024)
					SrcLen = 1024;
				else
					SrcLen = strlen(infile) - OffSet;

				memset(SrcData, 0x00, sizeof(SrcData));
				memcpy(SrcData, &infile[OffSet], SrcLen);

				if( SrcLen==0 )	break;

				OffSet = SrcLen;
				DstLen = 1024;

				memset(DstData, 0x00, sizeof(DstData));
				ret = DES3_EncUpdate(&AlgInfo, SrcData, SrcLen, DstData, &DstLen);
				if( ret!=CTR_SUCCESS )	Error(ret, "DES3_EncUpdate() returns.");
				strcat(outfile, (char *)DstData);
			}

			DstLen = 1024;
			memset(DstData, 0x00, sizeof(DstData));
			ret = DES3_EncFinal(&AlgInfo, DstData, &DstLen);
			if( ret!=CTR_SUCCESS )	Error(ret, "DES3_EncFinal() returns.");
			strcat(outfile, (char *)DstData);
		}

		/*-------------------------------------------------------*/
		/*		Decryption	                                     */
		/*-------------------------------------------------------*/
		else {
			ret = DES3_DecInit(&AlgInfo);
			if( ret!=CTR_SUCCESS )	Error(ret, "DES3_DecInit() returns.");

			for(  ;  ;  ) {
				if (strlen(infile) > OffSet + 1024)
					SrcLen = 1024;
				else
					SrcLen = strlen(infile) - OffSet;

				if (SrcLen <= 0 ) 	break;

				memset(SrcData, 0x00, sizeof(SrcData));
				memcpy(SrcData, &infile[OffSet], SrcLen);

				OffSet = SrcLen;
				DstLen = 1024;
				memset(DstData, 0x00, sizeof(DstData));
				ret = DES3_DecUpdate(&AlgInfo, SrcData, SrcLen, DstData, &DstLen);
				if( ret!=CTR_SUCCESS )	Error(ret, "DES3_DecUpdate() returns.");

				strcat(outfile, (char *)DstData);
			}

			DstLen = 1024;
			memset(DstData, 0x00, sizeof(DstData));
			ret = DES3_DecFinal(&AlgInfo, DstData, &DstLen);

			if( ret!=CTR_SUCCESS )	Error(ret, "DES3_DecFinal() returns.");
			strcat(outfile, (char *)DstData);
		}
	}
}


/*****************************************************************/
/*                                                               */
/*       암 호 화 / 복 호 화  처리 M A I N                       */
/*                                                               */
/*****************************************************************/
int	  EnCryption (sEncFile, sEncType, sEncMode, sEncSrc, sEncDst)
char	*sEncFile;
char	*sEncType;
char	*sEncMode;
char	*sEncSrc ;
char	*sEncDst ;
{
	DWORD	i;
	DWORD	EncType=0, ModeType=0, PadType=0;
	DWORD	EncFile=0;

	if( strcmp(sEncFile, "F")==0 )      EncFile = 1;
	else                                EncFile = 0;

	if( strcmp(sEncType, "E")==0 )		EncType = 0;
	else
	if( strcmp(sEncType, "D")==0 )	EncType = 1;
	else	Error(CTR_USAGE_ERROR, "Invalid Use of Argument");

	if( strcmp(sEncMode, "ECB")==0 ) {
		ModeType = AI_ECB;
		PadType = AI_NO_PADDING;
	}
	else if( strcmp(sEncMode, "CBC")==0 ) {
		ModeType = AI_CBC;
		PadType = AI_NO_PADDING;
	}
	else if( strcmp(sEncMode, "OFB")==0 ) {
		ModeType = AI_OFB;
		PadType = AI_NO_PADDING;
	}
	else if( strcmp(sEncMode, "CFB")==0 ) {
		ModeType = AI_CFB;
		PadType = AI_NO_PADDING;
	}
	else if( strcmp(sEncMode, "ECBPAD")==0 ) {
		ModeType = AI_ECB;
		PadType = AI_PKCS_PADDING;
	}
	else if( strcmp(sEncMode, "CBCPAD")==0 ) {
		ModeType = AI_CBC;
		PadType = AI_PKCS_PADDING;
	}
	else if( strcmp(sEncMode, "OFBPAD")==0 ) {
		ModeType = AI_OFB;
		PadType = AI_PKCS_PADDING;
	}
	else if( strcmp(sEncMode, "CFBPAD")==0 ) {
		ModeType = AI_CFB;
		PadType = AI_PKCS_PADDING;
	}
	else	Error(CTR_USAGE_ERROR, "Invalid Use of Argument");

	GeneralTest(EncFile, EncType, ModeType, PadType, sEncSrc, sEncDst);

	return 0;
}

/*---------------------------------------------------------------*/
/*             E N D    O F    P R O G R A M                     */
/*---------------------------------------------------------------*/
