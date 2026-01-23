
# ┌──────┬───────────────────────┐
# │ 프로그램명 │ Makefile                                     │
# ├──────┼───────────────────────┤
# │ 기      능 │ UNIX c Compile Script                        │
# ├──────┼───────────────────────┤
# │ 작  성  일 │ 2007. 10. 25                                 │
# ├──────┼───────────────────────┤
# │ 작  성  자 │ 최   병   남                                 │
# └──────┴───────────────────────┘

#CFLAGS=-DLOGGING -g

AR=ar rc
#CC=cc -m64 -O2 -D MULTIARG -D TEST_MODE -D ON_LINE -D LOGGING
CC=cc +DD64 -D MULTIARG -D TEST_MODE -D ON_LINE -D LOGGING
CCcurses=cc
eCAMS_HOME=$(APPHOME)
INCLUDE= $(eCAMS_HOME)/inc
LIBcurses=-lcurses -ltermcap
BINPATH=$(eCAMS_HOME)/bin
LIBPATH=$(eCAMS_HOME)/lib
TARGET =$(LIBPATH)/libecams_comm.a
OBJECT_MODE=64

ZIPLIB= adler32.o	\
		compress.o	\
		crc32.o 	\
		gzio.o 		\
		uncompr.o 	\
		deflate.o 	\
		trees.o 	\
		zutil.o 	\
		inflate.o 	\
		infback.o 	\
		inftrees.o 	\
		inffast.o

all: libzip

.SUFFIXES: .c .o

clean:
	rm -f *.o core

.c.o:
#	$(CC)  $(CFLAGS) -c $*.c -o $@ -I$(INCLUDE)
	$(CC)  -c $*.c -o $@ -I$(INCLUDE)

libzip: $(ZIPLIB)
	$(AR) $(TARGET) $(ZIPLIB)
	rm *.o
