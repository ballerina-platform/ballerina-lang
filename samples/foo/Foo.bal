import ballerina.net.http;

function foo ()(message) {

    HttpConnector e1 = new HttpConnector("http://localhost:8280", {"timeOut": 300});

    message response = http:get(e1, "/foo", m);

    return response;
}
