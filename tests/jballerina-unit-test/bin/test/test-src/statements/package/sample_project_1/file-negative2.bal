
public function bar() {
    http:Client httpClient = new("url");    // type without import
    io:println("Hello world!");             // function invocation without import
    string csv = io:CSV;                    // variable/constant without import
}
