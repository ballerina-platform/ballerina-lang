# To explicitly specify the config file, one can use the --config or -c flag as shown.
# If this is not set, Ballerina will look for a "ballerina.conf" file in the directory from which
# the user executes the program. The path to the config file can either be an absolute path or a relative path.
$ ballerina run config-api.bal --config path/to/conf/file/custom-config-file-name.conf
john has RW access
peter has R access
Before changing sum.limit in code: 5
After changing sum.limit: 10

# The same configs given through a config file can also be given through CLI parameters as shown. <br>
# i.e: john.access.rights
$ ballerina run config-api.bal -e username.instances=john,peter -e john.access.rights=RW -e peter.access.rights=R -e sum.limit=5
john has RW access
peter has R access
Before changing sum.limit in code: 5
After changing sum.limit: 10
