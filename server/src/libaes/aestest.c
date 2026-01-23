#include <AES.h>

main()
{
	u8	szKey[16] = { 0x06, 0xa9, 0x21, 0x40, 0x36, 0xb8, 0xa1, 0x5b,
					  0x51, 0x2e, 0x03, 0xd5, 0x34, 0x12, 0x00, 0x06 };
	u8	szIV[16] =  { 0x3d, 0xaf, 0xba, 0x42, 0x9d, 0x9e, 0xb4, 0x30,
					  0xb4, 0x22, 0xda, 0x80, 0x2c, 0x9f, 0xac, 0x41 };
	u8	szPlainData[256] = "Single block msg";
	u8	szEncrypted[256] = "";
	u8	szDecrypted[256] = "";
	u8	szCompare[16] = { 0xe3, 0x53, 0x77, 0x9c, 0x10, 0x79, 0xae, 0xb8,
						  0x27, 0x08, 0x94, 0x2d, 0xbe, 0x77, 0x18, 0x1a  };

	/* Encryption Test */
	strncpy(szEncrypted, szPlainData, 16);
	aes_128_cbc_encrypt(szKey, szIV, szEncrypted, 16);
	if(memcmp(szEncrypted, szCompare, 16) != 0) {
		printf("Encrypt Failed...\n");
		exit(0);
	}

	/* Decryption Test */
	strncpy(szDecrypted, szEncrypted, 16);
	aes_128_cbc_decrypt(szKey, szIV, szDecrypted, 16);
	if(memcmp(szDecrypted, szPlainData, 16) != 0) {
		printf("Decrypt Failed...\n");
		exit(0);
	}

	printf("AES-128bit En/Decryption Test O.K.\n");
}