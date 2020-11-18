import ballerina/http;
import ballerina/io;

public function main() {
    string s = 'string:concat("a", "b");
}

public function simpleVersion() {
    http:Client ep = new ("http://example.com");
    http:Response | error rp = ep -> get("/");
    if (rp is http:Response) {
        (string | error) txt = rp.getTextPayload();
        // 2nd level if block
        if (txt is string) {
            io:println("response.../n", txt);
        } else {
            io:println("error 2...");
            panic txt;
        }
    } else {
        io:println("error 1...");
        panic rp;
    }
}
