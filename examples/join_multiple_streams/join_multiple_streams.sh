# At the command line, navigate to the directory that contains the
# `join_multiple_streams.bal` file and run the `ballerina run` command.
$ ballerina run join_multiple_streams.bal
$ curl -H "Content-Type: application/json" -X POST -d '{"name":"Teak","amount":1000.0}' http://localhost:9090/rawmaterial
$ curl -H "Content-Type: application/json" -X POST -d '{"name":"Teak","amount":500.0}' http://localhost:9090/productionmaterial
