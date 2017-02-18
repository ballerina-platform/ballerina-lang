import ballerina.net.http;

function foo ()(message) {

    http:ClientConnector e1 = new http:ClientConnector("http://localhost:8280", {"timeOut": 300});
    message response;

    response = http:get(e1, "/foo", m);

    return response;
}
