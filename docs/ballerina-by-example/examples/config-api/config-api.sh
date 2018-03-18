# To explicitly specify the config file, one can use the -Bballerina.conf flag as shown.
# If this is not set, Ballerina will look for a "ballerina.conf" file in the directory from which
# the user executes the program. The path to the config file can either be an absolute path or a relative path.
$ ballerina run config-api.bal -Bballerina.conf=path/to/conf/file/custom-config-file-name.conf
john has RW access
peter has R access

# The same configs given through a config file can also be given through CLI parameters as shown.
# Notice how the instance config keys are prefixed with the instance tag. <br>
# i.e: [john].access.rights
$ ballerina run config-api.bal -Busername.instances=john,peter -B[john].access.rights=RW -B[peter].access.rights=R
john has RW access
peter has R access
