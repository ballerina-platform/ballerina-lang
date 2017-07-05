# To start the service, place the code in a "hello-world-service.bal" file
# and use the "ballerina run service" command.
$ ballerina run service hello-world-service.bal
ballerina: deploying service(s) in 'hello-world-service.bal'
ballerina: started server connector http-9090

# To build a service archive, you can use the
# "ballerina build service" command followed by
# one or more packages to be included into the archive.
$ ballerina build service hello-world-service.bal
$ ls
hello-world-service.bsz	hello-world-service.go
