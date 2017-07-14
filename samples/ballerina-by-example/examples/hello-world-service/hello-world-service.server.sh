# To start the service, put the code in "hello-world-service.bal"
# and use "ballerina run service" command.
$ ballerina run service hello-world-service.bal
ballerina: deploying service(s) in 'hello-world-service.bal'
ballerina: started server connector http-9090

# To build a service archive, we can use the
# "ballerina build service" command followed by
# one or more packages to be included into the archive.
$ ballerina build service hello-world-service.bal
$ ls
hello-world-service.bsz	hello-world-service.bal