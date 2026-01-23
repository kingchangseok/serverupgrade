/*-----------------------------------------------------------------
 ┌──────┬───────────────────────┐
 │ 프로그램명 │ ecams_funclist.h                             │
 ├──────┼───────────────────────┤
 │ 기      능 │ 형상관리 서버프로그램 사용자 FUNCTION LIST   │
 ├──────┼───────────────────────┤
 │ 작  성  일 │ 2011. 07. 08                                 │
 ├──────┼───────────────────────┤
 │ 작  성  자 │ 최   병   남                                 │
 └──────┴───────────────────────┘
-----------------------------------------------------------------*/

/*---------------------------------------------------------------*/
/*     USER FUNCTION  LIST                                       */
/*---------------------------------------------------------------*/

/*---------------------------------------------------------------*/
/*	strcvt.c                                                     */
/*---------------------------------------------------------------*/
int     Char_Check				();
char    *trunc_char				();
char    *rtrunc_char			();
char    *left_char 				();
char    *right_char				();
char    *mid_char  				();
char    *rep_char  				();
char    *upper_char				();
char    *lower_char				();
int 	cmp_trunc_char			();
int 	cmp_rtrunc_char 		();
int 	cmp_left_char 			();
int 	cmp_right_char			();
int 	cmp_mid_char  			();
int 	cmp_upper_char			();
int 	cmp_lower_char			();
int		Right_Char_Check		();

/*---------------------------------------------------------------*/
/*	sockutil.c                                                   */
/*---------------------------------------------------------------*/
int 	Server_Cmd_JOB			();
void 	NotUseDataTruncate		();
void 	LookCMD_INFO			();
void	InitCmdInfo				();
int     FileSizeInf				();
int     GetFileInf				();
void  	Server_Dir_Make			();
void 	Local_Dir_Make2			();
void 	Local_Dir_Make			();
void 	eCAMS_Log				();
void     eCAMS_Logging			();
int		ServerCmd				();
void	Trunc_NewLine			();
int		File_Merge_2			();
int		Msg_Merge				();
void	Convert_Path			();
int 	szg_chg					();
int		Change_Rcv_Mode			();
int 	MD5SUM					();
void	Conver_Date				();
void	Convert_WinDirPath		();


/*---------------------------------------------------------------*/
/*	util.c                                                       */
/*---------------------------------------------------------------*/
char    *Get_String_Time     	();
struct tm   *Get_Struct_Time	();
void 	Get_Sys_Date        	();
void 	Get_File_Date       	();
void 	Get_Sys_Slash_Date  	();
void 	Get_Sys_Time        	();
void 	Get_Sys_Time10      	();
void 	Get_Sys_Time20      	();
void 	DumpData            	();
int  	ExistReadData       	();
int  	ExistSendData       	();
void 	Usleep              	();
uchar	BccCompute          	();
void 	UL2Ch4              	();
void 	Pc2UnixPath         	();
void 	ConvData2Unpack     	();


/*---------------------------------------------------------------*/
/*	lanapi.c                                                     */
/*---------------------------------------------------------------*/
int      DisConnectSocket        ();
int      Rcv_Data_Sock           ();
int      SendDataToSock          ();
int      ReadDataFromSock        ();
int      ReadSizeDataFromSock    ();
ulong    CvtIPchr2ulong          ();
char    *CvtIPulong2chr          ();
int      MakeCommHeader          ();
int      ServerSocketInit        ();
int      ServerAcceptClient      ();
int      GetPeerName             ();
int      ConnectClient2Server    ();
void     SetSocketOpt            ();


/*---------------------------------------------------------------*/
/*	tarutil.c                                                    */
/*---------------------------------------------------------------*/
int 	tar_index_init			();
int		tar_index_insert_Z		();
int		tar_index_insert_X		();
int		tarFileMake_comp_X		();
int		tarFileMake_comp_Z		();
int		tarFileMake_ext_X		();
int		tarFileMake_ext_Z		();
int 	md5sum					();


/*---------------------------------------------------------------*/
/*	ecams_common.pc                                              */
/*---------------------------------------------------------------*/
int 	ConnectDB				();
void	SetTempDir				();
int 	File_Merge				();
void	ChangeVolPath			();
void	Result_Make				();
int 	SrcBack_Local			();
int 	Server_Buff_Size		();


/*---------------------------------------------------------------*/
/*	acct_lib.pc                                                  */
/*---------------------------------------------------------------*/
int 	InsertFileToDB			();
int 	RollBackCheckIn			();
int 	eCAMS_ACComp_Fork		();
void	wait_Accomp_Fork		();
void	CMR1011_Make			();
void	UPDT_RscMaint_List		();
void	Update_SysStep			();
void	WaitComplete			();
int     Get_File_Date_Size		();
void	FileSendErrChk			();


/*---------------------------------------------------------------*/
/*	accomp_lib.pc                                                */
/*---------------------------------------------------------------*/
void	SVRCOND_Check_Ret		();
void    SVRCOND_Check			();
void 	SYSCOM_Rsrc_Delete		();
void	SYSCOM_Rsrc_Put			();



/*---------------------------------------------------------------*/
/*             E N D    O F    P R O G R A M                     */
/*---------------------------------------------------------------*/
