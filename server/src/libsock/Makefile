
# ┌──────┬───────────────────────┐
# │ 프로그램명 │ Makefile                                     │
# ├──────┼───────────────────────┤
# │ 기      능 │ UNIX c Compile Script                        │
# ├──────┼───────────────────────┤
# │ 작  성  일 │ 2007. 10. 25                                 │
# ├──────┼───────────────────────┤
# │ 작  성  자 │ 최   병   남                                 │
# └──────┴───────────────────────┘

CFLAGS=-DLOGGING -g

#AR=ar -X64 rc
AR=ar rc
#CC=cc -m64 -D MULTIARG -D TEST_MODE -D ON_LINE -D LOGGING
CC=cc +DD64 -D MULTIARG -D TEST_MODE -D ON_LINE -D LOGGING
CCcurses=cc
eCAMS_HOME=$(APPHOME)
INCLUDE= $(eCAMS_HOME)/inc
LIBcurses=-lcurses -ltermcap
BINPATH=$(eCAMS_HOME)/bin
LIBPATH=$(eCAMS_HOME)/lib
TARGET =$(LIBPATH)/libecams_comm.a

object_mode=64

SOCKLIB= lanapi.o	\
	 strcvt.o   	\
	 util.o 	\
	 ftplib.o	\
	 sockutil.o	\
	 md5.o		\
	 tarutil.o	\

all: libsock

.SUFFIXES: .c .o

clean:
	rm -f *.o core

.c.o:
	$(CC)  -c $*.c -o $@ -I$(INCLUDE)

libsock: $(SOCKLIB)
	$(AR) $(TARGET) $(SOCKLIB)
	rm -rf *.o
