import ballerina.lang.system;

function testForkJoinReturnAnyType()(int, string) {
    map m = {"name":"Abhaya", "era":"Anuradhapura"};
    int p;
    string q;
    fork {
    worker W1 {
    system:println(m["name"]);
    m["time"] = "120 BC";
    int x = 23;
    x -> ;
    }
    worker W2 {
    system:println(m["name"]);
    m["period"] = "30 years";
    string s = "test";
    s -> ;
    }
    } join (all) (any[] results) {
        system:println(m["time"]);
        system:println(m["period"]);
        p = (int) results[0];
        q = (string) results[1];
        system:println(results[0]);
        system:println(results[1]);
    }

system:println("After the fork-join statement");
return p, q;
}
