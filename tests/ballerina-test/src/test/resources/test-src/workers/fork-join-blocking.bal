import ballerina/http;

int i = 0;

function testForkJoin() (int x, int st){
    endpoint<http:HttpClient> c {
        create http:HttpClient("http://example.com", {});
    }
    fork {
        worker w1 {
            http:Request req = {};
            http:Response res;
            res, _ = c.get("", req);
            int code = res.getStatusCode();
            code -> fork;
        }
        worker w2 {
            int a = testWorkers();
        }
    } join (all) (map results) {
        var abc,_ = (any[]) results["w1"];
        st, _ = (int) abc[0];
        x = i;
    }
    return;
}


function testWorkers() (int){
    worker w1 {
        sleep(5000);
        testNew();
    }
    worker w2 {
        return 1;
    }
}

function testNew() {
    worker w1 {
    }
    worker w2 {
        i = 10;
    }
}

