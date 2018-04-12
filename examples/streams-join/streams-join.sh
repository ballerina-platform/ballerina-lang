$ ballerina run streams-join.bal
$ curl -H "Content-Type: application/json" -X POST -d '{"name":"Teak","amount":1000.0}' http://localhost:9090/rawmaterial
$ curl -H "Content-Type: application/json" -X POST -d '{"name":"Teak","amount":500.0}' http://localhost:9090/productionmaterial
