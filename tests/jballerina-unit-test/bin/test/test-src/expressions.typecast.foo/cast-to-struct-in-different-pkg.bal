
import test_org/typecast.foo;

public type Person record {
    string name;
    int age;
    Person? parent = ();
    json info;
    map<anydata> address = {};
    int[] marks = [];
};

function testCastToStructInDifferentPkg() returns (foo:Student|error) {
    Person p1 = { name:"aaa",
                  age:25, 
                  parent:{ name:"bbb",
                           age:50,
                           address:{"city":"Colombo", "country":"SriLanka"},
                           info:{status:"married"}
                         },
                  info:{status:"single"}
                 };
    string statusKey = "status";
    
    return p1;
}
