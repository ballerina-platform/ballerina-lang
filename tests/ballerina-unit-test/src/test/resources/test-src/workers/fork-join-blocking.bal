import ballerina/http;
import ballerina/runtime;

int i = 0;

function testForkJoin() returns (int, int)|error {
    endpoint http:Client c {
        url:"http://example.com"
    };
    fork {
        worker w1 {
            var clientResponse = c->get("");
            int code = 0;
            match clientResponse {
               http:Response res => {
                   code = res.statusCode;
               }
               error err => { }
            }
            code -> fork;
        }
        worker w2 {
            runtime:sleep(5000);
            i = 100;
        }
    } join (all) (map results) {
        int st = check <int> results["w1"];
        int x = i;
        return (st, x);
    }
}

