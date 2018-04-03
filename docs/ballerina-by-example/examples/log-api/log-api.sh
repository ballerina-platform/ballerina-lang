# As it can be seen from the output, only Info and higher level logs are logged
$ ballerina run log-api.bal
2017-11-05 21:20:12,313 ERROR [] - error log
2017-11-05 21:20:12,319 ERROR [] - error log with cause : {message:"error occurred", cause:null}
2017-11-05 21:20:12,320 INFO  [] - info log
2017-11-05 21:20:12,321 WARN  [] - warn log

$ ballerina run log-api.bal -e[ballerina.log].level=TRACE
2017-11-05 21:21:35,394 DEBUG [] - debug log
2017-11-05 21:21:35,400 ERROR [] - error log
2017-11-05 21:21:35,402 ERROR [] - error log with cause : {message:"error occurred", cause:null}
2017-11-05 21:21:35,403 INFO  [] - info log
2017-11-05 21:21:35,403 TRACE [] - trace log
2017-11-05 21:21:35,404 WARN  [] - warn log 