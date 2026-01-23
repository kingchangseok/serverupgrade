CC	=	cc
CFLAGS	=	-g
SUBDIR	=	aessrc ftpsrc md5src svrsrc tarsrc 
TARGET	=	ecams_svr
LIBS	= 	-lxnet -lnsl 
all: svr_bin

.SUFFIXES: .c .o

clean:
	rm -f object/*.o core
	rm -f ecams_svr

svr_bin:
	cd aessrc && make 
	cd ftpsrc && make
	cd md5src && make
	cd svrsrc && make
	cd tarsrc && make
	$(CC) -o ecams_svr object/*.o $(CFLAGS) $(LIBS)	
	mv ecams_svr bin
	cd fileinf && make

