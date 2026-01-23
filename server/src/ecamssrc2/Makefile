# ┌──────┬───────────────────────┐
# │ 프로그램명 │ Makefile                                     │
# ├──────┼───────────────────────┤
# │ 기      능 │ Oracle Pro*C Compile Script                  │
# ├──────┼───────────────────────┤
# │ 작  성  일 │ 2007. 10. 25                                 │
# ├──────┼───────────────────────┤
# │ 작  성  자 │ 최   병   남                                 │
# └──────┴───────────────────────┘

include $(ORACLE_HOME)/precomp/lib/env_precomp.mk

eCAMS_HOME=$(APPHOME)
SMS_HOME=$(SMSHOME)

BINPATH = $(eCAMS_HOME)/bin
LIBPATH = $(eCAMS_HOME)/lib
INCLIB  = $(eCAMS_HOME)/inc
SMSPATH = $(SMS_HOME)/lib

OBJECT_MODE=64

COMMAR    = $(LIBPATH)/libecams_comm.a
ACCTAR    = $(LIBPATH)/libecams_acct.a
ACCOMPAR  = $(LIBPATH)/libecams_accomp.a

COMMLIB   = -L$(LIBPATH) -lecams_comm
ACCTLIB   = -L$(LIBPATH) -lecams_acct
ACCOMPLIB = -L$(LIBPATH) -lecams_accomp
SMSLIB    = -L$(SMSPATH) -lksnetSms
ORALIB    = -L$(LIBHOME) $(PROLDLIBS)

PROC=$(ORACLE_HOME)/bin/proc
USERID=$(DBUSER)/$(DBPASS)@$(DBCONN)
#PROCFLAG= SQLCHECK=full userid=$(USERID) include=$(INCLIB) unsafe_null=yes mode=oracle dbms=v8 CODE=ANSI_C define=_H_SYSRAS define=_H_BUF define=__64BIT__ define=_IBM_C define=_LONG_LONG define=_LONG_LONG_LTOH
#PROCFLAG= SQLCHECK=full userid=$(DBUSER) include=$(INCLIB) unsafe_null=yes mode=oracle dbms=v8 CODE=ANSI_C define=_H_SYSRAS define=_H_BUF define=__64BIT__ define=_IBM_C define=_LONG_LONG define=_LONG_LONG_LTOH
PROCFLAG= SQLCHECK=full userid=$(USERID) include=$(INCLIB) unsafe_null=yes mode=oracle dbms=v8 CODE=ANSI_C define=_H_SYSRAS define=_H_BUF define=__64BIT__ define=_IBM_C define=_LONG_LONG
#PROCFLAG= SQLCHECK=full userid=$(DBUSER)  include=$(INCLIB) unsafe_null=yes mode=oracle dbms=v8 define=_H_SYSRAS define=_H_BUF define=__64BIT__ define=_IBM_C define=_LONG_LONG
CFLAGS= -DLOGGING -g
LIBHOME=$(ORACLE_HOME)/lib/
LD_LIBRARY_PATH=$(ORACLE_HOME)/lib:/usr/lib/


CC=cc +DD64 -O2 -D MULTIARG -D TEST_MODE -D ON_LINE -D LOGGING -I$(INCLIB) -lxnet
#CC=cc -m64 -O2 -D MULTIARG -D TEST_MODE -D ON_LINE -D LOGGING -I$(INCLIB)

CCcurses=cc
AR=ar rc

OBJECT_MODE=64

LIBcurses=-lcurses -ltermcap

all:						\
		arall               \
		binall				\


arall:						\
		commar              \
		acctar				\
		accompar   			\

binall:                  	\
		ecams_mgr       	\
		ecams_acct			\
		ecams_accomp		\
		FileUpdate_cmr0025  \
		FileWrite_cmr0027	\
		FileRead_cmr0025	\
		FileRead_cmr0027	\
		ecams_cmm0070		\
		ecams_gensrc		\
		ecams_ih_csnew		\
		ecams_cmpsrc		\
		ecams_file_diff		\
		ecams_md5update		\
		ecams_sendmsg		\
		ecams_sendmsg_sub	\
		ecams_batexec       \
		ecams_svrck         \
		ecams_req_maint		\
        ecams_InsertInnerData \
        ecams_InsertInnerData_win \
		ecams_chkdoc_filediff	\
		sync_user   	    \
		ecams_filemake 	    \
		ecams_filemake_sub  \

commar:                 	\
		ecams_common     	\

acctar:                 	\
		acct_lib        	\
		ecams_acct_sysdn	\
		ecams_acct_sysdnc   \
		ecams_acct_sysdl	\
		ecams_acct_syscom	\
		ecams_acct_sysed	\
		ecams_acct_sysgb	\
		ecams_acct_sysrc	\
		ecams_acct_sysup	\
		ecams_acct_syssf	\
		ecams_acct_syscl	\
		ecams_acct_syspf	\

accompar:                   \
		accomp_lib          \
		ecams_accomp_comp	\
		ecams_accomp_put	\
		ecams_accomp_get	\
		ecams_accomp_tput	\


.SUFFIXES: .pc .c .o

clean:
	rm -f *.o core

.c.o:
	$(CC)  $(CFLAGS) -c $*.c -o $@ -I$(INCLIB)


# ############################################################
# ORACLE PRO*C
# ############################################################
ecams_sendmsg_sub	  	\
:
	$(PROC) $(PROCFLAG) iname=$@.pc oname=$@.c
	$(CC) $@.c $(COMMLIB) $(ORALIB) -o $@
	rm $@.c $@.lis
	mv $@ $(BINPATH)
	
ecams_cmpsrc          	\
ecams_file_diff          	\
ecams_mgr             	\
ecams_sendmsg         	\
FileUpdate_cmr0025    	\
FileRead_cmr0025      	\
FileRead_cmr0027      	\
ecams_cmm0070		  	\
ecams_gensrc		  	\
ecams_ih_csnew		  	\
ecams_md5update		  	\
ecams_req_maint		  	\
ecams_chkdoc_filediff   \
ecams_batexec	        \
sync_user 	            \
ecams_filemake          \
ecams_filemake_sub      \
:
	$(PROC) $(PROCFLAG) iname=$@.pc oname=$@.c
	$(CC) $@.c $(COMMLIB) $(ORALIB) -o $@
	rm $@.c $@.lis
	mv $@ $(BINPATH)



ecams_accomp          	\
:
	$(PROC) $(PROCFLAG) iname=$@.pc oname=$@.c
	$(CC) $@.c $(COMMLIB) $(ACCOMPLIB) $(ORALIB) -o $@
	rm $@.c $@.lis
	mv $@ $(BINPATH)

ecams_acct          	\
FileWrite_cmr0027   	\
ecams_InsertInnerData          \
ecams_InsertInnerData_win      \
ecams_svrck			  	\
:
	$(PROC) $(PROCFLAG) iname=$@.pc oname=$@.c
	$(CC) $@.c $(COMMLIB) $(ACCTLIB) $(ORALIB) -o $@
	rm $@.c $@.lis
	mv $@ $(BINPATH)


# ############################################################
# 아카이브 파일
# ############################################################
ecams_common        \
:
	$(PROC) $(PROCFLAG) iname=$@.pc
	$(CC) -c $@.c
	$(AR) $(COMMAR) $@.o
	rm $@.c $@.o $@.lis


acct_lib            \
ecams_acct_sysdn	\
ecams_acct_sysdnc   \
ecams_acct_sysdl	\
ecams_acct_syscom	\
ecams_acct_sysed	\
ecams_acct_sysgb	\
ecams_acct_sysrc	\
ecams_acct_sysup	\
ecams_acct_syssf	\
ecams_acct_syscl	\
ecams_acct_syspf	\
:
	$(PROC) $(PROCFLAG) iname=$@.pc
	$(CC) -c $@.c
	$(AR) $(ACCTAR) $@.o
	rm $@.c $@.o $@.lis


accomp_lib          \
ecams_accomp_comp	\
ecams_accomp_put	\
ecams_accomp_get	\
ecams_accomp_tput	\
:
	$(PROC) $(PROCFLAG) iname=$@.pc
	$(CC) -c $@.c
	$(AR) $(ACCOMPAR) $@.o
	rm $@.c $@.o $@.lis


# ############################################################
# JAVA 프로그램
# ############################################################	
	
HttpCom:
	javac $@.java -classpath $(BINPATH)
	mv $@.class $(BINPATH)
	
HttpCall:
	javac $@.java -classpath $(BINPATH) -encoding "utf-8"
	mv $@.class $(BINPATH)
		
PFHttpCom:
	javac $@.java -classpath $(BINPATH)
	mv $@.class $(BINPATH)
	
TextRW:
	javac $@.java -classpath $(BINPATH)
	mv $@.class $(BINPATH)
	
Encryptor:
	javac $@.java -classpath $(BINPATH)
	mv $@.class $(BINPATH)
	
eCAMSConf:
	javac $@.java -classpath $(BINPATH)
	mv $@.class $(BINPATH)

