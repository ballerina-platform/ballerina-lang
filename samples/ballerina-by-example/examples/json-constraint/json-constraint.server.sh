# To start the service, put the code in "json-constraint.bal"
# and use "ballerina run service" command.
$ ballerina run service json-constraint.bal
ballerina: deploying service(s) in 'json-constraint.bal'
ballerina: started server connector http-9090

# To build a service archive, we can use the
# "ballerina build service" command followed by
# one or more packages to be included into the archive.
$ ballerina build service json-constraint.bal
$ ls
json-constraint.bsz	json-constraint.bal