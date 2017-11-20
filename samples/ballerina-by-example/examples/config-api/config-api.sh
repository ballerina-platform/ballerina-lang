# As it can be seen, configs given in a file can also be provided through CLI params
$ ballerina run config-api.bal -Busername.instances=john,peter -B[john].access.rights=RW -B[peter].access.rights=R
john has RW access
peter has R access
