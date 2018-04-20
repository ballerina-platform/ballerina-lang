# Create a file named `secured-hello-world-service-with-basic-auth.bal`, copy the code into it,
# and use the `ballerina run` command to start the service.
# Make sure the `ballerina.conf` file populated correctly with user information and located in the
# same directory as the `secured-hello-world-service-with-basic-auth.bal` file. Or else, the
# ballerina.conf file can be provided as a runtime parameter, with the `--config` option.
$ ballerina run secured-hello-world-service-with-basic-auth.bal
ballerina: initiating service(s) in 'secured-hello-world-service-with-basic-auth.bal'
ballerina: started HTTPS/WSS endpoint 0.0.0.0:9090

# To build a compiled program file, use the
# `ballerina build` command followed by
# the service package name.
$ ballerina build secured-hello-world-service-with-basic-auth.bal
$ ls
secured-hello-world-service-with-basic-auth.balx	secured-hello-world-service-with-basic-auth.bal
