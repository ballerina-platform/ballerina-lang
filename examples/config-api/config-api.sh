# To explicitly specify a config file, use the --config or the -c flag.
# If this flag is not set, Ballerina looks for a `ballerina.conf` file in the directory from which
# the user executes the program. The path to the config file can either be an absolute or a relative path.
$ ballerina run config-api.bal --config path/to/conf/file/custom-config-file-name.conf
john has RW access
peter has R access
Before changing sum.limit in code: 5
After changing sum.limit: 10

# The same configs given through a config file can also be given through CLI parameters. <br>
# E.g., john.access.rights
$ ballerina run config-api.bal -e username.instances=john,peter -e john.access.rights=RW -e peter.access.rights=R -e sum.limit=5
john has RW access
peter has R access
Before changing sum.limit in code: 5
After changing sum.limit: 10
