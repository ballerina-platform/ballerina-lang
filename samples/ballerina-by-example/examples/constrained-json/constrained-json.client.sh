// Invoke the service using "curl".

// Invoke the get employee resource
$ curl http://localhost:9090/people/employee?index=2
{"first_name":"John","last_name":"Bernes","age":62,"address"

// Invoke the update person resource
$ curl -H "Content-Type: application/json" -X POST -d '{"name": "John Bernes Grisham","age": 62,"city": "Melbourne"}' http://localhost:9090/people/update?index=2

// To view all persons
$ curl http://localhost:9090/people/
[{"name":"Ann Frank","age":30,"city":"London"},
{"name":"Tim Yank","age":20,"city":"New York"},
{"name":"John Grisham","age":25,"city":"Brisbane"},
{"name":"Sarah Paulin","age":10,"city":"Chicago"},
{"name":"Trevor Noah","age":45,"city":"Cape Town"}]
