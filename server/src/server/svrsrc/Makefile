CC	= cc
CFLAGS	= -g
INCLUDE	=	-I. -I../inc


all:	lanapi	\
	strcvt	\
	util	\
	ecams_svr


.SUFFIXES: .c .o

clean:
	rm -f *.o core

lanapi:
	$(CC)  -c $@.c -o $@.o $(INCLUDE)
	mv $@.o ../object

ecams_svr:
	$(CC)  -c $@.c -o $@.o $(INCLUDE)
	mv $@.o ../object

strcvt:
	$(CC)  -c $@.c -o $@.o $(INCLUDE)
	mv $@.o ../object

util:
	$(CC)  -c $@.c -o $@.o $(INCLUDE)
	mv $@.o ../object

