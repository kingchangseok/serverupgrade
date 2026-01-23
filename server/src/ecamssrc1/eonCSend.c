#include <stdio.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h> 

#define BUFFER_SIZE 1024

int eonUmsSend(char *serverIp, char *serverPort, char *sendtype, char *sendinfo, char *revinfo, char *messageinfo);

int SendMessage(int hSocket, char *message, int nMessageLen);

int main(int argc, char * argv[])
{
	if(argc < 7)
	{
		printf("Usage : eonCSend \"[serverIp]\" \"[serverPort]\" \"[sendtype]\" \"[sendinfo]\" \"[revinfo]\" \"[messageinfo]\"\n");
		return 0;
	}
		
	if( strstr(argv[1], ",")  == NULL ) {
		eonUmsSend(argv[1], argv[2], argv[3], argv[4], argv[5], argv[6]);
	}else{
		char ips[BUFFER_SIZE + 1];
	  memset(ips, 0x00, BUFFER_SIZE + 1); 
		sprintf(ips, "%s", argv[1]);
		
		char *severip = strtok(ips,",");
		int ret = eonUmsSend(severip, argv[2], argv[3], argv[4], argv[5], argv[6]); /* 197번 연결*/

		if( ret == -1 ) { /* 오류 시 198번에 다시 연결 한다.*/
				severip = strtok(NULL,",");
				ret = eonUmsSend(severip, argv[2], argv[3], argv[4], argv[5], argv[6]);
		}
	}
	
	return 0; 
} 

int eonUmsSend(char *serverIp, char *serverPort, char *sendtype, char *sendinfo, char *revinfo, char *messageinfo)
{
	int hSocket = socket(PF_INET, SOCK_STREAM, IPPROTO_TCP);

	if(hSocket < 0)
	{
		printf("socket() failed\n");
		return 0;
	}

	struct sockaddr_in sai;
  
	memset(&sai, 0x00, sizeof(struct sockaddr_in));
	
	sai.sin_family = AF_INET;
	sai.sin_addr.s_addr = inet_addr(serverIp);
	sai.sin_port = htons(atoi(serverPort));
  
  printf("[%s,%s] Connecting\n",serverIp,serverPort);
  fflush(stdout);
      
	if(connect(hSocket, (struct sockaddr *)&sai, sizeof(struct sockaddr)) < 0)
	{
		printf("[%s,%s] connect failed\n",serverIp, serverPort);
  	fflush(stdout);		
		close(hSocket);
		return -1;
	} 
			  	
	char recvBuffer[BUFFER_SIZE + 1];
	int nBufferOffset = 0;
	int nRecved = 0;
	char okMessage[] = "200 OK[^]";
	int okMessageLen = strlen(okMessage); 

	int nProcessLevel = 0; 

	memset(&recvBuffer, 0x00, BUFFER_SIZE + 1); 

	for(;;) 
	{ 
		nRecved = recv(hSocket, &recvBuffer[nBufferOffset], BUFFER_SIZE - nBufferOffset, 0); 
   
		if(nRecved <= 0) 
		{ 
			printf("recv() failed\n"); 
			close(hSocket); 
			return 0; 
		} 
				
		nBufferOffset += nRecved; 
		  	
		if(nBufferOffset >= okMessageLen) 
		{ 
			if(strncmp(recvBuffer, okMessage, okMessageLen) != 0) 
			{ 
				printf("%s\n", recvBuffer); 
				close(hSocket); 
				return 0; 
			} 
			else 
			{ 
				switch(nProcessLevel) 
				{ 
					case 0:
					{ 
						char message[BUFFER_SIZE + 1]; 
						memset(message, 0x00, BUFFER_SIZE + 1); 
						sprintf(message, "%s;%s;%s;%s", sendtype, sendinfo, revinfo, messageinfo); 
																		
						int i;
						
						sprintf(message, "%s %s", message, "[^]");
												  					
						if(SendMessage(hSocket, message, strlen(message)) != strlen(message)) 
						{ 
							printf("send() failure\n"); 
							close(hSocket); 
							return 0; 
						} 

						nProcessLevel++; 
						nBufferOffset = 0; 

						memset(&recvBuffer, 0x00, BUFFER_SIZE + 1); 

						continue; 
					} 
					case 1: 
					{ 
						printf("%s\n", recvBuffer); 
						close(hSocket); 
						return 0; 
					}
					default: 
					{ 
						printf("Unknown error\n"); 
						close(hSocket); 
						return 0; 
					}
				}
			}
		}
		else 
			continue; 
	} 	
}

int SendMessage(int hSocket, char *message, int nMessageLen) 
{ 
	int nSendOffset = 0; 
	int nSended = 0; 

	for(;;) 
	{ 
		nSended = send(hSocket, &message[nSendOffset], nMessageLen - nSendOffset, 0); 
  	
		if(nSended <= 0) 
		return 0; 

		nSendOffset += nSended; 

		if(nSendOffset >= nMessageLen) 
		return nSendOffset; 
	}

	return 0; 
}
