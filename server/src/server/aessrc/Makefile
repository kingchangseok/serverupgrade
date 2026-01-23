CC	= cc
CFLAGS	= -g
INCLUDE	=	-I. -I../inc

all: AES

.SUFFIXES: .c .o

clean:
	rm -f *.o core

AES:
	$(CC)  $(CFLAGS) -c $@.c -o $@.o $(INCLUDE)
	mv $@.o ../object
