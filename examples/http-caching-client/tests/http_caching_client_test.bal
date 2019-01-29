import ballerina/test;
import ballerina/http;

@test:Config
function testCachingClient() {
    http:Client httpEndpoint = new("http://localhost:9090");

    var response = httpEndpoint->get("/cache");
    if (response is http:Response) {
        test:assertEquals(response.getHeader("etag"), "620328e8");
        test:assertEquals(response.getHeader("cache-control"), "must-revalidate,public,max-age=15");
        test:assertFalse(response.hasHeader("age"));
        test:assertEquals(response.getPayloadAsString(), "{\"message\":\"Hello, World!\"}");
    } else {
        test:assertFail(msg = "Failed to call the endpoint:");
    }

    response = httpEndpoint->get("/cache");
    if (response is http:Response) {
        test:assertEquals(response.getHeader("etag"), "620328e8");
        test:assertEquals(response.getHeader("cache-control"), "must-revalidate,public,max-age=15");
        test:assertTrue(response.hasHeader("age"));
        test:assertEquals(response.getPayloadAsString(), "{\"message\":\"Hello, World!\"}");
    } else {
        test:assertFail(msg = "Failed to call the endpoint:");
    }
}
