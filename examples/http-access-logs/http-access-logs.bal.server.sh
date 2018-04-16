#At the command line, navigate to the directory that contains the 
#`.bal` file and do one of the following depending on whether you want the logs to be logged on the console, or in a file. 
#Run the service by setting `-B[accesslog.http].logto=console` to log to console. 
$ ballerina run http-access-logs.bal -B[accesslog.http].logto=console
ballerina: initiating service(s) in 'http-access-logs.bal'
ballerina: started HTTP/WS server connector 0.0.0.0:9095
127.0.0.1 - - [05/Mar/2018:10:16:38 +0530] "GET /hello HTTP/1.1" 200 10 "-" "curl/7.55.1"

#Or, run the service by setting `-B[accesslog.http].logto=path/to/file.txt` to log to the specified file. 
$ ballerina run http-access-logs.bal -B[accesslog.http].logto=path/to/file.txt
ballerina: initiating service(s) in 'http-access-logs.bal'
ballerina: started HTTP/WS server connector 0.0.0.0:9095
