
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

LIBTAR= append.o	\
		 block.o	\
		 decode.o	\
		 encode.o	\
		 extract.o	\
		 handle.o	\
		 output.o	\
		 tutil.o	\
		 wrapper.o	\
		 libtar_hash.o	\
		 libtar_list.o	\
		 strsep.o		\
		 strlcpy.o		\
		 basename.o		\
		 strmode.o

all: libtar

.SUFFIXES: .c .o

clean:
	rm -f *.o core

.c.o:
	$(CC) -g -c $*.c -o $@ -I$(INCLUDE)

libtar: $(LIBTAR)
	$(AR) $(TARGET) $(LIBTAR)
	rm -rf *.o


