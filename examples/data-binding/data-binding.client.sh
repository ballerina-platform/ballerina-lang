$ curl -v http://localhost:9090/hello/bindJson -X POST
-d '{ "Details": { "ID": "77999", "Name": "XYZ"} , "Location": { "No": "01", "City": "Colombo"}}'
-H "Content-Type:application/json"

{"ID":"77999","Name":"XYZ"}

$ curl -v http://localhost:9090/hello/bindXML -X POST
-d "<h:Store id = \"AST\" xmlns:h=\"http://www.test.com\"><h:street>Main</h:street><h:city>94</h:city></h:Store>"
-H "Content-Type:application/xml"

<h:city xmlns:h="http://www.test.com">94</h:city>

$  curl -v http://localhost:9090/hello/bindStruct -X POST
-d '{ "Name": "John", "Grade": 12, "Marks": {"English" : "85", "IT" : "100"}}'
-H "Content-Type:application/json"

{"Name":"John","Grade":12}

