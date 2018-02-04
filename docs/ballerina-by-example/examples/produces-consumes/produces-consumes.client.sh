# To invoke the service, use following client.
$ curl -v http://localhost:9090/infoService -H "Accept:application/xml" -H "Content-Type:application/json" -d '{"name":"Ballerina"}'
# Server response:
<name>Ballerina</name>
