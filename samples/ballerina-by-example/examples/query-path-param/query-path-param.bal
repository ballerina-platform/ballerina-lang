package samples.ballerina-by- example.examples. query-path- param;

import ballerina. net.http;
                      import ballerina.lang. messages;

@http:BasePath {value:"/hello"}
service HelloService {

    @http:GET {}
    resource sayHello (message m) {
        message response = {};
        messages:setStringPayload(response, "Hello World !!!");
        reply response;
    }
}
