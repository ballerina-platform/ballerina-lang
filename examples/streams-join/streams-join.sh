# At the command line, navigate to the directory that contains the
# `streams-join.bal` file and run the `ballerina run` command.
$ ballerina run streams-join.bal
$ curl -H "Content-Type: application/json" -X POST -d '{"name":"Teak","amount":1000.0}' http://localhost:9090/rawmaterial
$ curl -H "Content-Type: application/json" -X POST -d '{"name":"Teak","amount":500.0}' http://localhost:9090/productionmaterial
