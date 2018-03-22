import ballerina/lang.system;

function testForkJoinReturnAnyType()(int, string) {
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
    } join (all) (map results) {
        system:println("Within join block");
        any[] t1;
        t1,_ = (any[]) results["W1"];
        system:println("After any array cast");
        p, _ = (int) t1[0];
        q, _ = (string) t1[1];
        t1,_ = (any[]) results["W2"];
        r, _ = (string) t1[0];
        t, _ = (float) t1[1];
        system:println(p);
        system:println(r);
        system:println(q);
        system:println(t);
    }

p = 111;
q = "eeee";
system:println("After the fork-join statement");
return p, q;
}
