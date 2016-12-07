import ballerina.net.http;

function foo ()(message) {

    http:HttpConnector e1 = new http:HttpConnector("http://localhost:8280", {"timeOut": 300});
    message response;

    response = http:get(e1, "/foo", m);

    return response;
}
