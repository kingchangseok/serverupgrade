# ┌──────┬───────────────────────┐
# │ 프로그램명 │ makefile (ecamsnoh)                          │
# ├──────┼───────────────────────┤
# │ 기      능 │ UNIX c Compile Script                        │
# ├──────┼───────────────────────┤
# │ 작  성  일 │ 2009. 04. 07                                 │
# ├──────┼───────────────────────┤
# │ 작  성  자 │ 최   병   남                                 │
# └──────┴───────────────────────┘


eCAMS_HOME=$(APPHOME)
BINPATH=$(eCAMS_HOME)/bin
LIBPATH=$(eCAMS_HOME)/lib
INCLIB =$(eCAMS_HOME)/inc

ECAMSLIB = -L$(LIBPATH) -lecams_comm32

CC=cc -D MULTIARG -D TEST_MODE -D ON_LINE -D LOGGING -I$(INCLIB)
CCcurses=cc\
CFLAGS= -DLOGGING -g
AR=ar rc
LIBcurses=-lcurses -ltermcap

### UNIX C Compile
all: 				\
	zen				\
	ecams_dir		\
	ecams_zip		\
	ecams_maint		\
	ecams_fileconv	\
	ecams_ih_cs		\
	ecams_ih_file	\
    ecams_SubFile_GetList	\
    ecams_GetInnerList	\
	ecams_md5sum	\
	ecams_comp				\
	ecams_ckmgr				\
	ecams_batexec				\


.SUFFIXES: .pc .c .o

clean:
	rm -f *.o core

.c.o:
	$(CC)  -c $*.c -o $@ -I$(INCLIB)

### UNIX C
zen				\
ecams_dir		\
ecams_zip		\
ecams_maint		\
ecams_fileconv	\
ecams_ih_cs		\
ecams_ih_file	\
ecams_md5sum	\
ecams_SubFile_GetList	\
ecams_GetInnerList	\
ecams_ckmgr 	\
ecams_comp			\
ecams_batexec			\
:
	$(CC) $@.c $(ECAMSLIB) -o $@
	mv $@ $(BINPATH)

