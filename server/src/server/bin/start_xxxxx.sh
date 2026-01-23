cd /sw/ecams_dev/bin
cnt=`ps -ef | grep "ecams_svr port=29895" | grep -v "sh -c" | grep -v grep | grep -v vi | grep -v tail | grep -v ksh | wc -l`
if [ $cnt -eq 0 ]; then
   nohup ecams_svr port=29895 buffer=1000000 1>/dev/null 2>&1 &
fi
