import ballerina.net.http;

function testFunc() {

    endpoint<HttpClient> en2 {
    }
    
    endpoint<HttpClient> en {
        create HttpClient("http://localhost:9090", 12);
    }
    
}

connector HttpClient(string url, int timeOut) {
    action get(string path)(string) {
        println("HttpClient -> get " + path);
        return path;
    }
}