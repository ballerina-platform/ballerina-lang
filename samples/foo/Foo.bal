import ballerina.net.http;

function foo () {

    HttpConnector e1 = new HttpConnector("http://localhost:8280", {"timeOut": 300});

    //HttpConnector e2 = new HttpConnector("https://localhost:8443", {"username" : "user", "password" : "pass", "timeOut": 300});

    message response = http.get(e1, "/foo", m);

}
