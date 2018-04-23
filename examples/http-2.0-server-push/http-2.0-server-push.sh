#At the command line, navigate to the directory that contains the `.bal` file.
#Run the `ballerina run` command to start the server and run the main function containing the ballerina client.
$ ballerina run http-2.0-server-push.bal
ballerina: initiating service(s) in 'http-2.0-server-push.bal'
ballerina: started HTTPS/WSS endpoint http-7090
Received a promise for /resource1
Received a promise for /resource2
Push promise for resource2 rejected
Received a promise for /resource3
Response : {"response":{"name":"main resource"}}
Promised resource : {"push":{"name":"resource1"}}
Promised resource : {"push":{"name":"resource3"}}


ballerina: started HTTP/WS endpoint 0.0.0.0:7090
INFO  [] - Request received
INFO  [] - Received a promise for /resource1
INFO  [] - Received a promise for /resource2
INFO  [] - Push promise for resource2 rejected
INFO  [] - Received a promise for /resource3
INFO  [] - Response : {"response":{"name":"main resource"}}
INFO  [] - Promised resource : {"push":{"name":"resource1"}}
INFO  [] - Promised resource : {"push":{"name":"resource3"}}
ballerina: stopped HTTP/WS endpoint 0.0.0.0:7090