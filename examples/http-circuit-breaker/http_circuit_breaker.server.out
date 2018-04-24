#At the command line, navigate to the directory that contains the 
#`http_circuit_breaker.bal` file and run the `ballerina run` command to start the server.
$ ballerina run http_circuit_breaker.bal
ballerina: initiating service(s) in 'http_circuit_breaker.bal'
ballerina: started HTTP/WS server connector 0.0.0.0:9090
ballerina: started HTTP/WS server connector 0.0.0.0:8080
2018-04-23 15:35:04,407 INFO  [ballerina.http] - CircuitBreaker failure threshold exceeded. Circuit tripped from CLOSE to OPEN state.
2018-04-23 15:35:31,257 INFO  [ballerina.http] - CircuitBreaker reset timeout reached. Circuit switched from OPEN to HALF_OPEN state.
2018-04-23 15:35:33,032 INFO  [ballerina.http] - CircuitBreaker trial run  was successful. Circuit switched from HALF_OPEN to CLOSE state.
2018-04-23 15:35:37,621 INFO  [ballerina.http] - CircuitBreaker failure threshold exceeded. Circuit tripped from CLOSE to OPEN state.
2018-04-23 15:35:38,041 ERROR [] - Error sending response from mock service : {message:"Remote client closed the connection before completing outbound response", cause:null, statusCode:0}
2018-04-23 15:35:50,612 INFO  [ballerina.http] - CircuitBreaker reset timeout reached. Circuit switched from OPEN to HALF_OPEN state.
2018-04-23 15:35:51,613 INFO  [ballerina.http] - CircuitBreaker trial run  was successful. Circuit switched from HALF_OPEN to CLOSE state.
