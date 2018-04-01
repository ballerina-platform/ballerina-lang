$ ballerina run http-circuit-breaker.bal
ballerina: initiating service(s) in 'http-circuit-breaker.bal'
ballerina: started HTTP/WS server connector 0.0.0.0:9090
ballerina: started HTTP/WS server connector 0.0.0.0:8080
2018-03-23 07:34:10,474 INFO  [ballerina.net.http] - CircuitBreaker failure threshold exceeded. Circuit tripped from CLOSE to OPEN state.
2018-03-23 07:34:24,624 INFO  [ballerina.net.http] - CircuitBreaker reset timeout reached. Circuit switched from OPEN to HALF_OPEN state.
2018-03-23 07:34:29,071 INFO  [ballerina.net.http] - CircuitBreaker trial run  was successful. Circuit switched from HALF_OPEN to CLOSE state.
2018-03-23 07:34:38,072 INFO  [ballerina.net.http] - CircuitBreaker failure threshold exceeded. Circuit tripped from CLOSE to OPEN state.
2018-03-23 07:34:51,155 INFO  [ballerina.net.http] - CircuitBreaker reset timeout reached. Circuit switched from OPEN to HALF_OPEN state.
2018-03-23 07:34:53,061 INFO  [ballerina.net.http] - CircuitBreaker trial run  was successful. Circuit switched from HALF_OPEN to CLOSE state.
