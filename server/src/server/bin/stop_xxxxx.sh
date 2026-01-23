cnt=`ps -ef | grep "ecams_svr port=29895" | grep -v grep | grep -v tail | grep -v ksh | grep -v "sh -c" | wc -l`
if [ $cnt -ne 0 ]; then
   kill -9 `ps -ef | grep "ecams_svr port=29895" | grep -v grep | grep -v vi | grep -v tail | grep -v ksh | grep -v "sh -c" | awk '{print $2}'`
fi