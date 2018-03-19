package org.foo.baz;

public struct ParentFoo {
    int i;
    ChildFoo c;
}

struct ChildFoo {
    string name;
}

struct privatePerson {
    int age;
    string name;
}

public function newPrivatePerson() (privatePerson) {
    return {age:12, name:"mad"};
}

public function privatePersonAsParam(privatePerson p) (string){
    return p.name;
}

public function privatePersonAsParamAndReturn(privatePerson p) (privatePerson) {
    return p;
}

public function <privatePerson p> getPrivatePersonName() returns (string) {
    return p.name;
}
