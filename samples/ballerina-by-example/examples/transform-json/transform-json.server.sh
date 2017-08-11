# To start the service, put the code in "transform-json.bal"
# and use "ballerina run service" command.
$ ballerina run service transform-json.bal
ballerina: deploying service(s) in 'transform-json.bal'
ballerina: started server connector http-9090

# To build a service archive, we can use the
# "ballerina build service" command followed by
# one or more packages to be included into the archive.
$ ballerina build service transform-json.bal
$ ls
transform-json.bsz	transform-json.bal