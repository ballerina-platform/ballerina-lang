# Invoke the service using "curl".
$ curl -H "Content-Type: application/json" -X POST -d '{"name":"Ann", "age": 30 ,"city":"New York"}' http://localhost:9090/person/
{"name":"Ann","age":30,"city":"London"}
