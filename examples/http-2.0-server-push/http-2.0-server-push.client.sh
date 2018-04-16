#At the command line, navigate to the directory that contains the 
#`.bal` file, and run the `ballerina run` command to run the main function containing the ballerina client. 
$ ballerina run http-2.0-server-push.bal
Received a promise for /resource1
Received a promise for /resource2
Push promise for resource2 rejected
Received a promise for /resource3
Response : {"response":{"name":"main resource"}}
Promised resource : {"push":{"name":"resource1"}}
Promised resource : {"push":{"name":"resource3"}}
