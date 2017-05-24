import ballerina.lang.system;

function testForkJoinReturnAnyType()(int, string) {
    map m = {"name":"Abhaya", "era":"Anuradhapura"};
    int p;
    string q;
    string r;
    float t;
    fork {
    worker W1 {
    system:println(m["name"]);
    m["time"] = "120 BC";
    int x = 23;
    string a = "aaaaa";
    x, a -> ;
    }
    worker W2 {
    system:println(m["name"]);
    m["period"] = "30 years";
    string s = "test";
    float u = 10.23;
    s, u -> ;
    }
    } join (all) (any[][] results) {
        system:println(m["time"]);
        system:println(m["period"]);
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
