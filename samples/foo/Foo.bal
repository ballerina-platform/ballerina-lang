import ballerina.net.http;

function foo (message m) (message) {
   HttpConnector connector = new HttpConnector("http://localhost:8280", { "timeOut" : 300 });
   message response = http.get(connector, "/foo", m);
   return response;
}
