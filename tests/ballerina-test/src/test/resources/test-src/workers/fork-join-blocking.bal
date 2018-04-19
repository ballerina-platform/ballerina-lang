import ballerina/http;
import ballerina/runtime;

int i = 0;

function testForkJoin() returns (int, int) {
    endpoint http:Client c {
        url:"http://example.com"
    };
    fork {
        worker w1 {
            http:Request req = new;
            var clientResponse = c->get("", req);
            int code;
            match clientResponse {
               http:Response res => {
                   code = res.statusCode;
               }
               http:HttpConnectorError err => { }
            }
            code -> fork;
        }
        worker w2 {
            runtime:sleepCurrentWorker(5000);
            i = 100;
        }
    } join (all) (map results) {
        int st = check <int> results["w1"];
        int x = i;
        return (st, x);
    }
}

