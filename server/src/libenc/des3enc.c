
/*-----------------------------------------------------------------
 ┌──────┬───────────────────────┐
 │ 프로그램명 │ des3enc.c                                    │
 ├──────┼───────────────────────┤
 │ 기      능 │ 암호화/복호화 프로그램                       │
 ├──────┼───────────────────────┤
 │ 작  성  일 │ 2007. 10. 24                                 │
 ├──────┼───────────────────────┤
 │ 작  성  자 │ 최   병   남                                 │
 └──────┴───────────────────────┘
-----------------------------------------------------------------*/

/*---------------------------------------------------------------*/
/*     Header files                                              */
/*---------------------------------------------------------------*/
#include 	<des3.h>

/*---------------------------------------------------------------*/
/*		Definitions / Macros                                     */
/*---------------------------------------------------------------*/
#define BlockCopy(pbDst, pbSrc) {					\
	((DWORD *)(pbDst))[0] = ((DWORD *)(pbSrc))[0];	\
	((DWORD *)(pbDst))[1] = ((DWORD *)(pbSrc))[1];	\
}
#define BlockXor(pbDst, phSrc1, phSrc2) {			\
	((DWORD *)(pbDst))[0] = ((DWORD *)(phSrc1))[0]	\
						  ^ ((DWORD *)(phSrc2))[0];	\
	((DWORD *)(pbDst))[1] = ((DWORD *)(phSrc1))[1]	\
						  ^ ((DWORD *)(phSrc2))[1];	\
}

/*---------------------------------------------------------------*/
/*		Prototypes                                               */
/*---------------------------------------------------------------*/
void	DES3_Encrypt(
		void		*CipherKey,		/* 암/복호용 Round Key       */
		BYTE		*Data);			/* 입출력을 위한 블록을 가리 */
		                            /* 키는 pointer              */

void	DES3_Decrypt(
		void		*CipherKey,		/* 암/복호용 Round Key       */
		BYTE		*Data);			/* 입출력을 위한 블록을 가리 */
		                            /* 키는 pointer              */

void	DES3_SetAlgInfo(
		DWORD			ModeID,
		DWORD			PadType,
		BYTE			*IV,
		DES3_ALG_INFO	*AlgInfo)
{
	AlgInfo->ModeID = ModeID;
	AlgInfo->PadType = PadType;

	if( IV!=NULL )
		memcpy(AlgInfo->IV, IV, DES3_BLOCK_LEN);
	else
		memset(AlgInfo->IV, 0, DES3_BLOCK_LEN);
}

/*---------------------------------------------------------------*/
/*		Function : BLOCK 단위 Padding                            */
/*---------------------------------------------------------------*/
static RET_VAL PaddSet(
			BYTE	*pbOutBuffer,
			DWORD	dRmdLen,
			DWORD	dBlockLen,
			DWORD	dPaddingType)
{
	DWORD dPadLen;

	switch( dPaddingType ) {
		case AI_NO_PADDING :
			if( dRmdLen==0 )	return 0;
			else				return CTR_DATA_LEN_ERROR;

		case AI_PKCS_PADDING :
			dPadLen = dBlockLen - dRmdLen;
			memset(pbOutBuffer+dRmdLen, (char)dPadLen, (int)dPadLen);
			return dPadLen;

		default :
			return CTR_FATAL_ERROR;
	}
}

/*---------------------------------------------------------------*/
/*		Function : BLOCK 단위 Padding Check                      */
/*---------------------------------------------------------------*/
static RET_VAL PaddCheck(
			BYTE	*pbOutBuffer,
			DWORD	dBlockLen,
			DWORD	dPaddingType)
{
	DWORD i, dPadLen;

	switch( dPaddingType ) {
		case AI_NO_PADDING :
			return 0;

		case AI_PKCS_PADDING :
			dPadLen = pbOutBuffer[dBlockLen-1];
			if( ((int)dPadLen<=0) || (dPadLen>(int)dBlockLen) )
				return CTR_PAD_CHECK_ERROR;

			for( i=1; i<=dPadLen; i++)
				if( pbOutBuffer[dBlockLen-i] != dPadLen )
					return CTR_PAD_CHECK_ERROR;
			return dPadLen;

		default :
			return CTR_FATAL_ERROR;
	}
}

/*---------------------------------------------------------------*/
/*		암호화 Initial                                           */
/*---------------------------------------------------------------*/

RET_VAL	DES3_EncInit(
		DES3_ALG_INFO	*AlgInfo)
{
	AlgInfo->BufLen = 0;
	if( AlgInfo->ModeID!=AI_ECB )
		memcpy(AlgInfo->ChainVar, AlgInfo->IV, DES3_BLOCK_LEN);

	return CTR_SUCCESS;
}

/*---------------------------------------------------------------*/
/*		암호화 처리 : ECB                                        */
/*---------------------------------------------------------------*/
static RET_VAL ECB_EncUpdate(
		DES3_ALG_INFO	*AlgInfo,
		BYTE		*PlainTxt,		/* 입력되는 평문의 pointer   */
		DWORD		PlainTxtLen,	/* 입력되는 평문의 바이트 수 */
		BYTE		*CipherTxt, 	/* 암호문이 출력될 pointer   */
		DWORD		*CipherTxtLen)	/* 출력되는 암호문의 바이트수*/
{
	DWORD		*ScheduledKey=AlgInfo->RoundKey;
	DWORD		BlockLen=DES3_BLOCK_LEN, BufLen=AlgInfo->BufLen;

	*CipherTxtLen = BufLen + PlainTxtLen;

	/*-----------------------------------------------------------*/
	/*	No one block                                             */
	/*-----------------------------------------------------------*/
	if( *CipherTxtLen<BlockLen ) {
		memcpy(AlgInfo->Buffer+BufLen, PlainTxt, (int)PlainTxtLen);
		AlgInfo->BufLen += PlainTxtLen;
		*CipherTxtLen = 0;
		return CTR_SUCCESS;
	}

	/*-----------------------------------------------------------*/
	/*	control the case that PlainTxt and CipherTxt are the     */
	/*  same buffer                                              */
	/*-----------------------------------------------------------*/
	if( PlainTxt==CipherTxt )
		return CTR_FATAL_ERROR;

	/*-----------------------------------------------------------*/
	/*	first block                                              */
	/*-----------------------------------------------------------*/
	memcpy(AlgInfo->Buffer+BufLen, PlainTxt, (int)(BlockLen - BufLen));
	PlainTxt += BlockLen - BufLen;
	PlainTxtLen -= BlockLen - BufLen;

	/*-----------------------------------------------------------*/
	/*	core part                                                */
	/*-----------------------------------------------------------*/
	BlockCopy(CipherTxt, AlgInfo->Buffer);
	DES3_Encrypt(ScheduledKey, CipherTxt);
	CipherTxt += BlockLen;
	while( PlainTxtLen>=BlockLen ) {
		BlockCopy(CipherTxt, PlainTxt);
		DES3_Encrypt(ScheduledKey, CipherTxt);
		PlainTxt += BlockLen;
		CipherTxt += BlockLen;
		PlainTxtLen -= BlockLen;
	}

	/*-----------------------------------------------------------*/
	/*	save remained data                                       */
	/*-----------------------------------------------------------*/
	memcpy(AlgInfo->Buffer, PlainTxt, (int)PlainTxtLen);
	AlgInfo->BufLen = PlainTxtLen;
	*CipherTxtLen -= PlainTxtLen;

	return CTR_SUCCESS;
}

/*---------------------------------------------------------------*/
/*		암호화 처리 : CBC                                        */
/*---------------------------------------------------------------*/
static RET_VAL CBC_EncUpdate(
		DES3_ALG_INFO	*AlgInfo,
		BYTE		*PlainTxt,		/* 입력되는 평문의 pointer   */
		DWORD		PlainTxtLen,	/* 입력되는 평문의 바이트 수 */
		BYTE		*CipherTxt, 	/* 암호문이 출력될 pointer   */
		DWORD		*CipherTxtLen)	/* 출력되는 암호문의 바이트수*/
{
	DWORD		*ScheduledKey=AlgInfo->RoundKey;
	DWORD		BlockLen=DES3_BLOCK_LEN, BufLen=AlgInfo->BufLen;

	*CipherTxtLen = BufLen + PlainTxtLen;

	/*-----------------------------------------------------------*/
	/*	No one block                                             */
	/*-----------------------------------------------------------*/
	if( *CipherTxtLen<BlockLen ) {
		memcpy(AlgInfo->Buffer+BufLen, PlainTxt, (int)PlainTxtLen);
		AlgInfo->BufLen += PlainTxtLen;
		*CipherTxtLen = 0;
		return CTR_SUCCESS;
	}

	/*-----------------------------------------------------------*/
	/*	control the case that PlainTxt and CipherTxt are the     */
	/*  same buffer                                              */
	/*-----------------------------------------------------------*/
	if( PlainTxt==CipherTxt )
		return CTR_FATAL_ERROR;

	/*-----------------------------------------------------------*/
	/*	first block                                              */
	/*-----------------------------------------------------------*/
	memcpy(AlgInfo->Buffer+BufLen, PlainTxt, (int)(BlockLen - BufLen));
	PlainTxt += BlockLen - BufLen;
	PlainTxtLen -= BlockLen - BufLen;

	/*-----------------------------------------------------------*/
	/*	core part                                                */
	/*-----------------------------------------------------------*/
	BlockXor(CipherTxt, AlgInfo->ChainVar, AlgInfo->Buffer);
	DES3_Encrypt(ScheduledKey, CipherTxt);
	CipherTxt += BlockLen;
	while( PlainTxtLen>=BlockLen ) {
		BlockXor(CipherTxt, CipherTxt-BlockLen, PlainTxt);
		DES3_Encrypt(ScheduledKey, CipherTxt);
		PlainTxt += BlockLen;
		CipherTxt += BlockLen;
		PlainTxtLen -= BlockLen;
	}
	BlockCopy(AlgInfo->ChainVar, CipherTxt-BlockLen);

	/*-----------------------------------------------------------*/
	/*	save remained data                                       */
	/*-----------------------------------------------------------*/
	memcpy(AlgInfo->Buffer, PlainTxt, (int)PlainTxtLen);
	AlgInfo->BufLen = PlainTxtLen;
	*CipherTxtLen -= PlainTxtLen;

	return CTR_SUCCESS;
}

/*---------------------------------------------------------------*/
/*		암호화 처리 : ECB                                        */
/*---------------------------------------------------------------*/
static RET_VAL OFB_EncUpdate(
		DES3_ALG_INFO	*AlgInfo,
		BYTE		*PlainTxt,		/* 입력되는 평문의 pointer   */
		DWORD		PlainTxtLen,	/* 입력되는 평문의 바이트 수 */
		BYTE		*CipherTxt, 	/* 암호문이 출력될 pointer   */
		DWORD		*CipherTxtLen)	/* 출력되는 암호문의 바이트수*/
{
	DWORD		*ScheduledKey=AlgInfo->RoundKey;
	DWORD		BlockLen=DES3_BLOCK_LEN;
	DWORD		BufLen=AlgInfo->BufLen;

	/*-----------------------------------------------------------*/
	/*	Check Output Memory Size                                 */
	/*-----------------------------------------------------------*/
	*CipherTxtLen = BufLen + PlainTxtLen;

	/*-----------------------------------------------------------*/
	/*	No one block                                             */
	/*-----------------------------------------------------------*/
	if( *CipherTxtLen<=BlockLen ) {
		memcpy(AlgInfo->Buffer+BufLen, PlainTxt, (int)PlainTxtLen);
		AlgInfo->BufLen += PlainTxtLen;
		*CipherTxtLen = 0;
		return CTR_SUCCESS;
	}

	/*-----------------------------------------------------------*/
	/*	control the case that PlainTxt and CipherTxt are the     */
	/*  same buffer                                              */
	/*-----------------------------------------------------------*/
	if( PlainTxt==CipherTxt )
		return CTR_FATAL_ERROR;

	/*-----------------------------------------------------------*/
	/*	first block                                              */
	/*-----------------------------------------------------------*/
	memcpy(AlgInfo->Buffer+BufLen, PlainTxt, (int)(BlockLen - BufLen));
	PlainTxt += BlockLen - BufLen;
	PlainTxtLen -= BlockLen - BufLen;

	/*-----------------------------------------------------------*/
	/*	core part                                                */
	/*-----------------------------------------------------------*/
	DES3_Encrypt(ScheduledKey, AlgInfo->ChainVar);
	BlockXor(CipherTxt, AlgInfo->ChainVar, AlgInfo->Buffer);
	CipherTxt += BlockLen;
	while( PlainTxtLen>BlockLen ) {
		DES3_Encrypt(ScheduledKey, AlgInfo->ChainVar);
		BlockXor(CipherTxt, AlgInfo->ChainVar, PlainTxt);
		PlainTxt += BlockLen;
		CipherTxt += BlockLen;
		PlainTxtLen -= BlockLen;
	}

	/*-----------------------------------------------------------*/
	/*	save remained data                                       */
	/*-----------------------------------------------------------*/
	memcpy(AlgInfo->Buffer, PlainTxt, (int)PlainTxtLen);
	AlgInfo->BufLen = (AlgInfo->BufLen&0xF0000000) + PlainTxtLen;
	*CipherTxtLen -= PlainTxtLen;

	return CTR_SUCCESS;
}

/*---------------------------------------------------------------*/
/*		암호화 처리 : ECB                                        */
/*---------------------------------------------------------------*/
static RET_VAL CFB_EncUpdate(
		DES3_ALG_INFO	*AlgInfo,
		BYTE		*PlainTxt,		/* 입력되는 평문의 pointer   */
		DWORD		PlainTxtLen,	/* 입력되는 평문의 바이트 수 */
		BYTE		*CipherTxt, 	/* 암호문이 출력될 pointer   */
		DWORD		*CipherTxtLen)  /* 출력되는 암호문의 바이트수*/
{
	DWORD		*ScheduledKey=AlgInfo->RoundKey;
	DWORD		BlockLen=DES3_BLOCK_LEN;
	DWORD		BufLen=AlgInfo->BufLen;

	/*-----------------------------------------------------------*/
	/*	Check Output Memory Size                                 */
	/*-----------------------------------------------------------*/
	*CipherTxtLen = BufLen + PlainTxtLen;

	/*-----------------------------------------------------------*/
	/*	No one block                                             */
	/*-----------------------------------------------------------*/
	if( *CipherTxtLen<=BlockLen ) {
		memcpy(AlgInfo->Buffer+BufLen, PlainTxt, (int)PlainTxtLen);
		AlgInfo->BufLen += PlainTxtLen;
		*CipherTxtLen = 0;
		return CTR_SUCCESS;
	}

	/*-----------------------------------------------------------*/
	/*	control the case that PlainTxt and CipherTxt are the     */
	/*  same buffer                                              */
	/*-----------------------------------------------------------*/
	if( PlainTxt==CipherTxt )
		return CTR_FATAL_ERROR;

	/*-----------------------------------------------------------*/
	/*	first block                                              */
	/*-----------------------------------------------------------*/
	memcpy(AlgInfo->Buffer+BufLen, PlainTxt, (int)(BlockLen - BufLen));
	PlainTxt += BlockLen - BufLen;
	PlainTxtLen -= BlockLen - BufLen;

	/*-----------------------------------------------------------*/
	/*	core part                                                */
	/*-----------------------------------------------------------*/
	DES3_Encrypt(ScheduledKey, AlgInfo->ChainVar);
	BlockXor(AlgInfo->ChainVar, AlgInfo->ChainVar, AlgInfo->Buffer);
	BlockCopy(CipherTxt, AlgInfo->ChainVar);
	CipherTxt += BlockLen;
	while( PlainTxtLen>=BlockLen ) {
		DES3_Encrypt(ScheduledKey, AlgInfo->ChainVar);
		BlockXor(AlgInfo->ChainVar, AlgInfo->ChainVar, PlainTxt);
		BlockCopy(CipherTxt, AlgInfo->ChainVar);
		PlainTxt += BlockLen;
		CipherTxt += BlockLen;
		PlainTxtLen -= BlockLen;
	}

	/*-----------------------------------------------------------*/
	/*	save remained data                                       */
	/*-----------------------------------------------------------*/
	memcpy(AlgInfo->Buffer, PlainTxt, (int)PlainTxtLen);
	AlgInfo->BufLen = (AlgInfo->BufLen&0xF0000000) + PlainTxtLen;
	*CipherTxtLen -= PlainTxtLen;

	return CTR_SUCCESS;
}

/*---------------------------------------------------------------*/
/*		암호화 처리                                              */
/*---------------------------------------------------------------*/
RET_VAL	DES3_EncUpdate(
		DES3_ALG_INFO	*AlgInfo,
		BYTE		*PlainTxt,		/*입력되는 평문의 pointer    */
		DWORD		PlainTxtLen,	/*입력되는 평문의 바이트 수  */
		BYTE		*CipherTxt, 	/*암호문이 출력될 pointer    */
		DWORD		*CipherTxtLen)	/*출력되는 암호문의 바이트수 */
{
	switch( AlgInfo->ModeID ) {
		case AI_ECB :	return ECB_EncUpdate(AlgInfo, PlainTxt, PlainTxtLen,
											 CipherTxt, CipherTxtLen);
		case AI_CBC :	return CBC_EncUpdate(AlgInfo, PlainTxt, PlainTxtLen,
											 CipherTxt, CipherTxtLen);
		case AI_OFB :	return OFB_EncUpdate(AlgInfo, PlainTxt, PlainTxtLen,
											 CipherTxt, CipherTxtLen);
		case AI_CFB :	return CFB_EncUpdate(AlgInfo, PlainTxt, PlainTxtLen,
											 CipherTxt, CipherTxtLen);
		default :		return CTR_FATAL_ERROR;
	}
}

/*---------------------------------------------------------------*/
/*		암호화 Padding 처리 : ECB                                */
/*---------------------------------------------------------------*/
static RET_VAL ECB_EncFinal(
		DES3_ALG_INFO	*AlgInfo,
		BYTE		*CipherTxt, 	/* 암호문이 출력될 pointer   */
		DWORD		*CipherTxtLen)	/* 출력되는 암호문의 바이트수*/
{
	DWORD		*ScheduledKey=AlgInfo->RoundKey;
	DWORD		BlockLen=DES3_BLOCK_LEN, BufLen=AlgInfo->BufLen;
	DWORD		PaddByte;

	/*-----------------------------------------------------------*/
	/*	Padding                                                  */
	/*-----------------------------------------------------------*/
	PaddByte = PaddSet(AlgInfo->Buffer, BufLen, BlockLen, AlgInfo->PadType);
	if( PaddByte>BlockLen )		return PaddByte;

	if( PaddByte==0 ) {
		*CipherTxtLen = 0;
		return CTR_SUCCESS;
	}

	/*-----------------------------------------------------------*/
	/*	core part                                                */
	/*-----------------------------------------------------------*/
	BlockCopy(CipherTxt, AlgInfo->Buffer);
	DES3_Encrypt(ScheduledKey, CipherTxt);

	*CipherTxtLen = BlockLen;

	return CTR_SUCCESS;
}

/*---------------------------------------------------------------*/
/*		암호화 Padding 처리 : ECB                                */
/*---------------------------------------------------------------*/
static RET_VAL CBC_EncFinal(
		DES3_ALG_INFO	*AlgInfo,
		BYTE		*CipherTxt, 	/* 암호문이 출력될 pointer   */
		DWORD		*CipherTxtLen)	/* 출력되는 암호문의 바이트수*/
{
	DWORD		*ScheduledKey=AlgInfo->RoundKey;
	DWORD		BlockLen=DES3_BLOCK_LEN, BufLen=AlgInfo->BufLen;
	DWORD		PaddByte;

	/*-----------------------------------------------------------*/
	/*	Padding                                                  */
	/*-----------------------------------------------------------*/
	PaddByte = PaddSet(AlgInfo->Buffer, BufLen, BlockLen, AlgInfo->PadType);
	if( PaddByte>BlockLen )		return PaddByte;

	if( PaddByte==0 ) {
		*CipherTxtLen = 0;
		return CTR_SUCCESS;
	}

	/*-----------------------------------------------------------*/
	/*	core part                                                */
	/*-----------------------------------------------------------*/
	BlockXor(CipherTxt, AlgInfo->Buffer, AlgInfo->ChainVar);
	DES3_Encrypt(ScheduledKey, CipherTxt);
	BlockCopy(AlgInfo->ChainVar, CipherTxt);

	*CipherTxtLen = BlockLen;

	return CTR_SUCCESS;
}

/*---------------------------------------------------------------*/
/*		암호화 Padding 처리 : ECB                                */
/*---------------------------------------------------------------*/
static RET_VAL OFB_EncFinal(
		DES3_ALG_INFO	*AlgInfo,
		BYTE		*CipherTxt, 	/* 암호문이 출력될 pointer   */
		DWORD		*CipherTxtLen)	/* 출력되는 암호문의 바이트수*/
{
	DWORD		*ScheduledKey=AlgInfo->RoundKey;
	DWORD		BlockLen=DES3_BLOCK_LEN;
	DWORD		BufLen=AlgInfo->BufLen;
	DWORD		i;

	/*-----------------------------------------------------------*/
	/*	Check Output Memory Size                                 */
	/*-----------------------------------------------------------*/
	*CipherTxtLen = BlockLen;

	/*-----------------------------------------------------------*/
	/*	core part                                                */
	/*-----------------------------------------------------------*/
	DES3_Encrypt(ScheduledKey, AlgInfo->ChainVar);
	for( i=0; i<BufLen; i++)
		CipherTxt[i] = (BYTE) (AlgInfo->Buffer[i] ^ AlgInfo->ChainVar[i]);

	*CipherTxtLen = BufLen;

	return CTR_SUCCESS;
}

/*---------------------------------------------------------------*/
/*		암호화 Padding 처리 : ECB                                */
/*---------------------------------------------------------------*/
static RET_VAL CFB_EncFinal(
		DES3_ALG_INFO	*AlgInfo,
		BYTE		*CipherTxt, 	/* 암호문이 출력될 pointer   */
		DWORD		*CipherTxtLen)	/* 출력되는 암호문의 바이트수*/
{
	DWORD		*ScheduledKey=AlgInfo->RoundKey;
	DWORD		BufLen=AlgInfo->BufLen;

	/*-----------------------------------------------------------*/
	/*	Check Output Memory Size                                 */
	/*-----------------------------------------------------------*/
	*CipherTxtLen = BufLen;

	/*-----------------------------------------------------------*/
	/*	core part                                                */
	/*-----------------------------------------------------------*/
	DES3_Encrypt(ScheduledKey, AlgInfo->ChainVar);
	BlockXor(AlgInfo->ChainVar, AlgInfo->ChainVar, AlgInfo->Buffer);
	memcpy(CipherTxt, AlgInfo->ChainVar, BufLen);

	*CipherTxtLen = BufLen;

	return CTR_SUCCESS;
}

/*---------------------------------------------------------------*/
/*		암호화 Padding 처리                                      */
/*---------------------------------------------------------------*/
RET_VAL	DES3_EncFinal(
		DES3_ALG_INFO	*AlgInfo,
		BYTE		*CipherTxt, 	/* 암호문이 출력될 pointer   */
		DWORD		*CipherTxtLen)	/* 출력되는 암호문의 바이트수*/
{
	switch( AlgInfo->ModeID ) {
		case AI_ECB :	return ECB_EncFinal(AlgInfo, CipherTxt, CipherTxtLen);
		case AI_CBC :	return CBC_EncFinal(AlgInfo, CipherTxt, CipherTxtLen);
		case AI_OFB :	return OFB_EncFinal(AlgInfo, CipherTxt, CipherTxtLen);
		case AI_CFB :	return CFB_EncFinal(AlgInfo, CipherTxt, CipherTxtLen);
		default :		return CTR_FATAL_ERROR;
	}
}

/*---------------------------------------------------------------*/
/*		복호화  Initial                                          */
/*---------------------------------------------------------------*/
RET_VAL	DES3_DecInit(
		DES3_ALG_INFO	*AlgInfo)
{
	AlgInfo->BufLen = 0;
	if( AlgInfo->ModeID!=AI_ECB )
		memcpy(AlgInfo->ChainVar, AlgInfo->IV, DES3_BLOCK_LEN);
	return CTR_SUCCESS;
}

/*---------------------------------------------------------------*/
/*		복호화 처리 : ECB                                        */
/*---------------------------------------------------------------*/
static RET_VAL ECB_DecUpdate(
		DES3_ALG_INFO	*AlgInfo,
		BYTE		*CipherTxt, 	/* 입력되는 암호문의 pointer */
		DWORD		CipherTxtLen,	/* 입력되는 암호문의 바이트수*/
		BYTE		*PlainTxt,		/* 평문이 출력될 pointer     */
		DWORD		*PlainTxtLen)	/* 출력되는 평문의 바이트 수 */
{
	DWORD		*ScheduledKey=AlgInfo->RoundKey;
	DWORD		BlockLen=DES3_BLOCK_LEN;
	DWORD		BufLen=AlgInfo->BufLen;

	*PlainTxtLen = BufLen + CipherTxtLen;

	/*-----------------------------------------------------------*/
	/*	No one block                                             */
	/*-----------------------------------------------------------*/
	if( BufLen+CipherTxtLen <= BlockLen ) {
		memcpy(AlgInfo->Buffer+BufLen, CipherTxt, (int)CipherTxtLen);
		AlgInfo->BufLen += CipherTxtLen;
		*PlainTxtLen = 0;
		return CTR_SUCCESS;
	}

	/*-----------------------------------------------------------*/
	/*	control the case that PlainTxt and CipherTxt are the     */
	/*  same buffer                                              */
	/*-----------------------------------------------------------*/
	if( CipherTxt==PlainTxt )	return CTR_FATAL_ERROR;

	/*-----------------------------------------------------------*/
	/*	first block                                              */
	/*-----------------------------------------------------------*/
	*PlainTxtLen = BufLen + CipherTxtLen;
	memcpy(AlgInfo->Buffer+BufLen, CipherTxt, (int)(BlockLen - BufLen));
	CipherTxt += BlockLen - BufLen;
	CipherTxtLen -= BlockLen - BufLen;

	/*-----------------------------------------------------------*/
	/*	core part                                                */
	/*-----------------------------------------------------------*/
	BlockCopy(PlainTxt, AlgInfo->Buffer);
	DES3_Decrypt(ScheduledKey, PlainTxt);
	PlainTxt += BlockLen;
	while( CipherTxtLen>BlockLen ) {
		BlockCopy(PlainTxt, CipherTxt);
		DES3_Decrypt(ScheduledKey, PlainTxt);
		CipherTxt += BlockLen;
		PlainTxt += BlockLen;
		CipherTxtLen -= BlockLen;
	}

	/*-----------------------------------------------------------*/
	/*	save remained data                                       */
	/*-----------------------------------------------------------*/
	memcpy(AlgInfo->Buffer, CipherTxt, (int)CipherTxtLen);
	AlgInfo->BufLen = (AlgInfo->BufLen&0xF0000000) + CipherTxtLen;
	*PlainTxtLen -= CipherTxtLen;

	return CTR_SUCCESS;
}

/*---------------------------------------------------------------*/
/*		복호화 처리 : ECB                                        */
/*---------------------------------------------------------------*/
static RET_VAL CBC_DecUpdate(
		DES3_ALG_INFO	*AlgInfo,
		BYTE		*CipherTxt, 	/* 입력되는 암호문의 pointer */
		DWORD		CipherTxtLen,	/* 입력되는 암호문의 바이트수*/
		BYTE		*PlainTxt,		/* 평문이 출력될 pointer     */
		DWORD		*PlainTxtLen)	/* 출력되는 평문의 바이트 수 */
{
	DWORD		*ScheduledKey=AlgInfo->RoundKey;
	DWORD		BlockLen=DES3_BLOCK_LEN, BufLen=AlgInfo->BufLen;

	/*-----------------------------------------------------------*/
	/*	Check Output Memory Size                                 */
	/*-----------------------------------------------------------*/
	*PlainTxtLen = BufLen + CipherTxtLen;

	/*-----------------------------------------------------------*/
	/*	No one block                                             */
	/*-----------------------------------------------------------*/
	if( BufLen+CipherTxtLen <= BlockLen ) {
		memcpy(AlgInfo->Buffer+BufLen, CipherTxt, (int)CipherTxtLen);
		AlgInfo->BufLen += CipherTxtLen;
		*PlainTxtLen = 0;
		return CTR_SUCCESS;
	}

	/*-----------------------------------------------------------*/
	/*	control the case that PlainTxt and CipherTxt are the     */
	/*  same buffer                                              */
	/*-----------------------------------------------------------*/
	if( CipherTxt==PlainTxt )	return CTR_FATAL_ERROR;

	/*-----------------------------------------------------------*/
	/*	first block                                              */
	/*-----------------------------------------------------------*/
	*PlainTxtLen = BufLen + CipherTxtLen;
	memcpy(AlgInfo->Buffer+BufLen, CipherTxt, (int)(BlockLen - BufLen));
	CipherTxt += BlockLen - BufLen;
	CipherTxtLen -= BlockLen - BufLen;

	/*-----------------------------------------------------------*/
	/*	core part                                                */
	/*-----------------------------------------------------------*/
	BlockCopy(PlainTxt, AlgInfo->Buffer);
	DES3_Decrypt(ScheduledKey, PlainTxt);
	BlockXor(PlainTxt, PlainTxt, AlgInfo->ChainVar);
	PlainTxt += BlockLen;
	if( CipherTxtLen>BlockLen ) {
		BlockCopy(PlainTxt, CipherTxt);
		DES3_Decrypt(ScheduledKey, PlainTxt);
		BlockXor(PlainTxt, PlainTxt, AlgInfo->Buffer);
		CipherTxt += BlockLen;
		PlainTxt += BlockLen;
		CipherTxtLen -= BlockLen;
	}
	while( CipherTxtLen>BlockLen ) {
		BlockCopy(PlainTxt, CipherTxt);
		DES3_Decrypt(ScheduledKey, PlainTxt);
		BlockXor(PlainTxt, PlainTxt, CipherTxt-BlockLen);
		CipherTxt += BlockLen;
		PlainTxt += BlockLen;
		CipherTxtLen -= BlockLen;
	}
	BlockCopy(AlgInfo->ChainVar, CipherTxt-BlockLen);

	/*-----------------------------------------------------------*/
	/*	save remained data                                       */
	/*-----------------------------------------------------------*/
	memcpy(AlgInfo->Buffer, CipherTxt, (int)CipherTxtLen);
	AlgInfo->BufLen = (AlgInfo->BufLen&0xF0000000) + CipherTxtLen;
	*PlainTxtLen -= CipherTxtLen;

	return CTR_SUCCESS;
}

/*---------------------------------------------------------------*/
/*		복호화 처리 : ECB                                        */
/*---------------------------------------------------------------*/
static RET_VAL OFB_DecUpdate(
		DES3_ALG_INFO	*AlgInfo,
		BYTE		*CipherTxt, 	/* 입력되는 암호문의 pointer */
		DWORD		CipherTxtLen,	/* 입력되는 암호문의 바이트수*/
		BYTE		*PlainTxt,		/* 평문이 출력될 pointer     */
		DWORD		*PlainTxtLen)	/* 출력되는 평문의 바이트 수 */
{
	DWORD		*ScheduledKey=AlgInfo->RoundKey;
	DWORD		BlockLen=DES3_BLOCK_LEN;
	DWORD		BufLen=AlgInfo->BufLen;

	/*-----------------------------------------------------------*/
	/*	Check Output Memory Size                                 */
	/*-----------------------------------------------------------*/
	*PlainTxtLen = BufLen + CipherTxtLen;

	/*-----------------------------------------------------------*/
	/*	No one block                                             */
	/*-----------------------------------------------------------*/
	if( BufLen+CipherTxtLen <= BlockLen ) {
		memcpy(AlgInfo->Buffer+BufLen, CipherTxt, (int)CipherTxtLen);
		AlgInfo->BufLen += CipherTxtLen;
		*PlainTxtLen = 0;
		return CTR_SUCCESS;
	}

	/*-----------------------------------------------------------*/
	/*	control the case that PlainTxt and CipherTxt are the     */
	/*  same buffer                                              */
	/*-----------------------------------------------------------*/
	if( PlainTxt==CipherTxt )
		return CTR_FATAL_ERROR;

	/*-----------------------------------------------------------*/
	/*	first block                                              */
	/*-----------------------------------------------------------*/
	*PlainTxtLen = BufLen + CipherTxtLen;
	memcpy(AlgInfo->Buffer+BufLen, CipherTxt, (int)(BlockLen - BufLen));
	CipherTxt += BlockLen - BufLen;
	CipherTxtLen -= BlockLen - BufLen;

	/*-----------------------------------------------------------*/
	/*	core part                                                */
	/*-----------------------------------------------------------*/
	DES3_Encrypt(ScheduledKey, AlgInfo->ChainVar);
	BlockXor(PlainTxt, AlgInfo->ChainVar, AlgInfo->Buffer);
	PlainTxt += BlockLen;
	while( CipherTxtLen>BlockLen ) {
		DES3_Encrypt(ScheduledKey, AlgInfo->ChainVar);
		BlockXor(PlainTxt, AlgInfo->ChainVar, CipherTxt);
		CipherTxt += BlockLen;
		PlainTxt += BlockLen;
		CipherTxtLen -= BlockLen;
	}

	/*-----------------------------------------------------------*/
	/*	save remained data                                       */
	/*-----------------------------------------------------------*/
	memcpy(AlgInfo->Buffer, CipherTxt, (int)CipherTxtLen);
	AlgInfo->BufLen = (AlgInfo->BufLen&0xF0000000) + CipherTxtLen;
	*PlainTxtLen -= CipherTxtLen;

	return CTR_SUCCESS;
}

/*---------------------------------------------------------------*/
/*		복호화 처리 : ECB                                        */
/*---------------------------------------------------------------*/
static RET_VAL CFB_DecUpdate(
		DES3_ALG_INFO	*AlgInfo,
		BYTE		*CipherTxt, 	/* 입력되는 암호문의 pointer */
		DWORD		CipherTxtLen,	/* 입력되는 암호문의 바이트수*/
		BYTE		*PlainTxt,		/* 평문이 출력될 pointer     */
		DWORD		*PlainTxtLen)	/* 출력되는 평문의 바이트 수 */
{
	DWORD		*ScheduledKey=AlgInfo->RoundKey;
	DWORD		BlockLen=DES3_BLOCK_LEN;
	DWORD		BufLen=AlgInfo->BufLen;

	/*-----------------------------------------------------------*/
	/*	Check Output Memory Size                                 */
	/*-----------------------------------------------------------*/
	*PlainTxtLen = BufLen + CipherTxtLen;

	/*-----------------------------------------------------------*/
	/*	No one block                                             */
	/*-----------------------------------------------------------*/
	if( BufLen+CipherTxtLen <= BlockLen ) {
		memcpy(AlgInfo->Buffer+BufLen, CipherTxt, (int)CipherTxtLen);
		AlgInfo->BufLen += CipherTxtLen;
		*PlainTxtLen = 0;
		return CTR_SUCCESS;
	}

	/*-----------------------------------------------------------*/
	/*	control the case that PlainTxt and CipherTxt are the     */
	/*  same buffer                                              */
	/*-----------------------------------------------------------*/
	if( PlainTxt==CipherTxt )
		return CTR_FATAL_ERROR;

	/*-----------------------------------------------------------*/
	/*	first block                                              */
	/*-----------------------------------------------------------*/
	*PlainTxtLen = BufLen + CipherTxtLen;
	memcpy(AlgInfo->Buffer+BufLen, CipherTxt, (int)(BlockLen - BufLen));
	CipherTxt += BlockLen - BufLen;
	CipherTxtLen -= BlockLen - BufLen;

	/*-----------------------------------------------------------*/
	/*	core part                                                */
	/*-----------------------------------------------------------*/
	DES3_Encrypt(ScheduledKey, AlgInfo->ChainVar);
	BlockXor(PlainTxt, AlgInfo->ChainVar, AlgInfo->Buffer);
	BlockCopy(AlgInfo->ChainVar, AlgInfo->Buffer);
	PlainTxt += BlockLen;
	while( CipherTxtLen>BlockLen ) {
		DES3_Encrypt(ScheduledKey, AlgInfo->ChainVar);
		BlockXor(PlainTxt, AlgInfo->ChainVar, CipherTxt);
		BlockCopy(AlgInfo->ChainVar, CipherTxt);
		CipherTxt += BlockLen;
		PlainTxt += BlockLen;
		CipherTxtLen -= BlockLen;
	}

	/*-----------------------------------------------------------*/
	/*	save remained data                                       */
	/*-----------------------------------------------------------*/
	memcpy(AlgInfo->Buffer, CipherTxt, (int)CipherTxtLen);
	AlgInfo->BufLen = (AlgInfo->BufLen&0xF0000000) + CipherTxtLen;
	*PlainTxtLen -= CipherTxtLen;

	return CTR_SUCCESS;
}

/*---------------------------------------------------------------*/
/*		복호화 처리                                              */
/*---------------------------------------------------------------*/
RET_VAL	DES3_DecUpdate(
		DES3_ALG_INFO	*AlgInfo,
		BYTE		*CipherTxt, 	/* 암호문이 출력될 pointer   */
		DWORD		CipherTxtLen,	/* 출력되는 암호문의 바이트수*/
		BYTE		*PlainTxt,		/* 입력되는 평문의 pointer   */
		DWORD		*PlainTxtLen)	/* 입력되는 평문의 바이트 수 */
{
	switch( AlgInfo->ModeID ) {
		case AI_ECB :	return ECB_DecUpdate(AlgInfo, CipherTxt, CipherTxtLen,
											 PlainTxt, PlainTxtLen);
		case AI_CBC :	return CBC_DecUpdate(AlgInfo, CipherTxt, CipherTxtLen,
											 PlainTxt, PlainTxtLen);
		case AI_OFB :	return OFB_DecUpdate(AlgInfo, CipherTxt, CipherTxtLen,
											 PlainTxt, PlainTxtLen);
		case AI_CFB :	return CFB_DecUpdate(AlgInfo, CipherTxt, CipherTxtLen,
											 PlainTxt, PlainTxtLen);
		default :		return CTR_FATAL_ERROR;
	}
}

/*---------------------------------------------------------------*/
/*		복호화 Padding 처리 : ECB                                */
/*---------------------------------------------------------------*/
RET_VAL ECB_DecFinal(
		DES3_ALG_INFO	*AlgInfo,
		BYTE		*PlainTxt,		/* 평문이 출력될 pointer     */
		DWORD		*PlainTxtLen)	/* 출력되는 평문의 바이트 수 */
{
	DWORD		*ScheduledKey=AlgInfo->RoundKey;
	DWORD		BlockLen=DES3_BLOCK_LEN, BufLen=AlgInfo->BufLen;
	RET_VAL		ret;

	/*-----------------------------------------------------------*/
	/*	Check Output Memory Size                                 */
	/*-----------------------------------------------------------*/
	if( BufLen==0 ) {
		*PlainTxtLen = 0;
		return CTR_SUCCESS;
	}
	*PlainTxtLen = BlockLen;

	if( BufLen!=BlockLen )	return CTR_CIPHER_LEN_ERROR;

	/*-----------------------------------------------------------*/
	/*	core part                                                */
	/*-----------------------------------------------------------*/
	BlockCopy(PlainTxt, AlgInfo->Buffer);
	DES3_Decrypt(ScheduledKey, PlainTxt);

	/*-----------------------------------------------------------*/
	/*	Padding                                                  */
	/*-----------------------------------------------------------*/
	ret = PaddCheck(PlainTxt, BlockLen, AlgInfo->PadType);
	if( ret==(DWORD)-3 )	return CTR_PAD_CHECK_ERROR;
	if( ret==(DWORD)-1 )	return CTR_FATAL_ERROR;

	*PlainTxtLen = BlockLen - ret;

	return CTR_SUCCESS;
}

/*---------------------------------------------------------------*/
/*		복호화 Padding 처리 : ECB                                */
/*---------------------------------------------------------------*/
RET_VAL CBC_DecFinal(
		DES3_ALG_INFO	*AlgInfo,
		BYTE		*PlainTxt,		/* 평문이 출력될 pointer     */
		DWORD		*PlainTxtLen)	/* 출력되는 평문의 바이트 수 */
{
	DWORD		*ScheduledKey=AlgInfo->RoundKey;
	DWORD		BlockLen=DES3_BLOCK_LEN, BufLen=AlgInfo->BufLen;
	RET_VAL		ret;

	/*-----------------------------------------------------------*/
	/*	Check Output Memory Size                                 */
	/*-----------------------------------------------------------*/
	if( BufLen==0 ) {
		*PlainTxtLen = 0;
		return CTR_SUCCESS;
	}
	*PlainTxtLen = BlockLen;

	if( BufLen!=BlockLen )	return CTR_CIPHER_LEN_ERROR;

	/*-----------------------------------------------------------*/
	/*	core part                                                */
	/*-----------------------------------------------------------*/
	BlockCopy(PlainTxt, AlgInfo->Buffer);
	DES3_Decrypt(ScheduledKey, PlainTxt);
	BlockXor(PlainTxt, PlainTxt, AlgInfo->ChainVar);
	BlockCopy(AlgInfo->ChainVar, AlgInfo->Buffer);

	/*-----------------------------------------------------------*/
	/*	Padding                                                  */
	/*-----------------------------------------------------------*/
	ret = PaddCheck(PlainTxt, BlockLen, AlgInfo->PadType);
	if( ret==(DWORD)-3 )	return CTR_PAD_CHECK_ERROR;
	if( ret==(DWORD)-1 )	return CTR_FATAL_ERROR;

	*PlainTxtLen = BlockLen - ret;

	return CTR_SUCCESS;
}

/*---------------------------------------------------------------*/
/*		복호화 Padding 처리 : ECB                                */
/*---------------------------------------------------------------*/
RET_VAL OFB_DecFinal(
		DES3_ALG_INFO	*AlgInfo,
		BYTE		*PlainTxt,		/* 평문이 출력될 pointer     */
		DWORD		*PlainTxtLen)	/* 출력되는 평문의 바이트 수 */
{
	DWORD		*ScheduledKey=AlgInfo->RoundKey;
	DWORD		i, BufLen=AlgInfo->BufLen;

	/*-----------------------------------------------------------*/
	/*	Check Output Memory Size                                 */
	/*-----------------------------------------------------------*/
	*PlainTxtLen = BufLen;

	/*-----------------------------------------------------------*/
	/*	core part                                                */
	/*-----------------------------------------------------------*/
	DES3_Encrypt(ScheduledKey, AlgInfo->ChainVar);
	for( i=0; i<BufLen; i++)
		PlainTxt[i] = (BYTE) (AlgInfo->Buffer[i] ^ AlgInfo->ChainVar[i]);

	*PlainTxtLen = BufLen;

	return CTR_SUCCESS;
}


/*---------------------------------------------------------------*/
/*		복호화 Padding 처리 : ECB                                */
/*---------------------------------------------------------------*/
RET_VAL CFB_DecFinal(
		DES3_ALG_INFO	*AlgInfo,
		BYTE		*PlainTxt,		/* 평문이 출력될 pointer     */
		DWORD		*PlainTxtLen)	/* 출력되는 평문의 바이트 수 */
{
	DWORD		*ScheduledKey=AlgInfo->RoundKey;
	DWORD		BufLen=AlgInfo->BufLen;

	/*-----------------------------------------------------------*/
	/*	Check Output Memory Size                                 */
	/*-----------------------------------------------------------*/
	*PlainTxtLen = BufLen;

	/*-----------------------------------------------------------*/
	/*	core part                                                */
	/*-----------------------------------------------------------*/
	DES3_Encrypt(ScheduledKey, AlgInfo->ChainVar);
	BlockXor(AlgInfo->ChainVar, AlgInfo->ChainVar, AlgInfo->Buffer);
	memcpy(PlainTxt, AlgInfo->ChainVar, BufLen);

	*PlainTxtLen = BufLen;

	return CTR_SUCCESS;
}

/*---------------------------------------------------------------*/
/*		복호화 Padding 처리                                      */
/*---------------------------------------------------------------*/
RET_VAL	DES3_DecFinal(
		DES3_ALG_INFO	*AlgInfo,
		BYTE		*PlainTxt,		/* 입력되는 평문의 pointer   */
		DWORD		*PlainTxtLen)	/* 입력되는 평문의 바이트 수 */
{
	switch( AlgInfo->ModeID ) {
		case AI_ECB :	return ECB_DecFinal(AlgInfo, PlainTxt, PlainTxtLen);
		case AI_CBC :	return CBC_DecFinal(AlgInfo, PlainTxt, PlainTxtLen);
		case AI_OFB :	return OFB_DecFinal(AlgInfo, PlainTxt, PlainTxtLen);
		case AI_CFB :	return CFB_DecFinal(AlgInfo, PlainTxt, PlainTxtLen);
		default :		return CTR_FATAL_ERROR;
	}
}

/*---------------------------------------------------------------*/
/*             E N D    O F    P R O G R A M                     */
/*---------------------------------------------------------------*/
