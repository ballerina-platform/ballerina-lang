#At the command line, navigate to the directory that contains the 
#`.bal` file and run the `ballerina run` command. 
$ ballerina run http_client_connector.bal
GET request:
{"args":{"test":"123"},"headers":{"host":"postman-echo.com","user-agent":"ballerina/0.92.1-SNAPSHOT","x-forwarded-port":"443","x-forwarded-proto":"https"},"url":"https://postman-echo.com/get?test=123"}

POST request:
{"args":{},"data":"POST: Hello World","files":{},"form":{},"headers":{"host":"postman-echo.com","content-length":"17","content-type":"text/plain","user-agent":"ballerina/0.92.1-SNAPSHOT","x-forwarded-port":"443","x-forwarded-proto":"https"},"json":null,"url":"https://postman-echo.com/post"}

PUT request:
{"args":{},"data":{"method":"PUT","payload":"Hello World"},"files":{},"form":{},"headers":{"host":"postman-echo.com","content-length":"40","content-type":"application/json","user-agent":"ballerina/0.92.1-SNAPSHOT","x-forwarded-port":"443","x-forwarded-proto":"https"},"json":{"method":"PUT","payload":"Hello World"},"url":"https://postman-echo.com/put"}

PATCH request:
{"args":{},"data":"<request><method>PATCH</method><payload>Hello World!</payload></request>","files":{},"form":{},"headers":{"host":"postman-echo.com","content-length":"72","content-type":"application/xml","user-agent":"ballerina/0.92.1-SNAPSHOT","x-forwarded-port":"443","x-forwarded-proto":"https"},"json":null,"url":"https://postman-echo.com/patch"}

DELETE request:
{"args":{},"data":"DELETE: Hello World","files":{},"form":{},"headers":{"host":"postman-echo.com","content-length":"19","content-type":"text/plain","user-agent":"ballerina/0.92.1-SNAPSHOT","x-forwarded-port":"443","x-forwarded-proto":"https"},"json":null,"url":"https://postman-echo.com/delete"}

Content-Type: application/json; charset=utf-8
Status code: 200
