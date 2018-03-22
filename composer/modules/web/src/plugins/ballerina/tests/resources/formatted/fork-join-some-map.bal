import ballerina/lang.system;

function testForkJoinReturnAnyType () (int, string) {
    return testForkJoinReturnAnyTypeVM();
}

function testForkJoinReturnAnyTypeVM () (int, string) {
    int p;
    string q;
    string r;
    float t;
    fork {
        worker W1 {
            system:println("Worker W1 started");
            int x = 23;
            string a = "aaaaa";
            x, a -> fork;
        }
        worker W2 {
            system:println("Worker W2 started");
            string s = "test";
            float u = 10.23;
            s, u -> fork;
        }
    } join (some 1) (map results) {
        system:println("Within join block");
        //any[] t1;
        //t1,_ = (any[]) results["W1"];
        system:println("After any array cast");
        p = (int)results[0];
        //system:println("After int cast");
        //q = (string) results[1][0];
        //r = (string) results[0][1];
        //t = (float) results[1][1];
        system:println(p);
        //system:println(r);
        //system:println(q);
        //system:println(t);
    }

    system:println("After the fork-join statement");
    p = 111;
    q = "eeee";
    return p, q;
}
