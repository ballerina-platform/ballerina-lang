import ballerina/http;
import ballerina/runtime;

int i = 0;

function testForkJoin() returns [int, int]|error {
    http:Client c = new ("http://example.com");

    fork {
        worker w1 returns int {
            var clientResponse = c -> get("");
            int code = 0;
            if clientResponse is http:Response {
                code = clientResponse.statusCode;
            }
            return code;
        }
        worker w2 {
            runtime:sleep(3000);
            i = 100;
        }
    }
    map<any> results = wait {w1, w2};
    int st = <int> results["w1"];
    int x = i;
    return [st, x];
}
