import ballerina.lang.system;

function testForkJoinReturnAnyType()(int, string) {
    int p;
    string q;
    string r;
    float t;
    fork {
    worker W1 {
    int x = 23;
    string a = "aaaaa";
    x, a -> fork;
    }
    worker W2 {
    string s = "test";
    float u = 10.23;
    s, u -> fork;
    }
    } join (all) (any[][] results) {
        p = (int) results[0][0];
        q = (string) results[1][0];
        r = (string) results[0][1];
        t = (float) results[1][1];
        system:println(p);
        system:println(r);
        system:println(q);
        system:println(t);
    }

system:println("After the fork-join statement");
return p, q;
}
