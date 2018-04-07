package expressions.typecast.foo;

import expressions.typecast.foo.bar;

public type Person {
    string name,
    int age,
    Person? parent,
    json info,
    map address,
    int[] marks,
};

function testCastToStructInDifferentPkg() returns (bar:Student|error) {
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
    
    return <bar:Student>p1;
}
