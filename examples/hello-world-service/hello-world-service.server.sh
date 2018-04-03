# To start the service, put the code in "hello-world-service.bal"
# and use "ballerina run" command.
$ ballerina run hello-world-service.bal
ballerina: initiating service(s) in 'hello-world-service.bal'
ballerina: started HTTP/WS server connector 0.0.0.0:9090

# To build a compiled program file, we can use the
# "ballerina build" command followed by
# the service package.
$ ballerina build hello-world-service.bal
$ ls
hello-world-service.balx	hello-world-service.bal
