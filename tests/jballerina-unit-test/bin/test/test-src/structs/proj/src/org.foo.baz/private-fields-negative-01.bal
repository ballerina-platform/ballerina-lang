
public type ParentFoo record {
    int i = 0;
    ChildFoo c = {};
};

type ChildFoo record {
    string name = "";
};

type privatePerson record {
    int age = 0;
    string name = "";
};

public function newPrivatePerson() returns (privatePerson) {
    return {age:12, name:"mad"};
}

public function privatePersonAsParam(privatePerson p) returns (string){
    return p.name;
}

public function privatePersonAsParamAndReturn(privatePerson p) returns (privatePerson) {
    return p;
}

public function <privatePerson p> getPrivatePersonName() returns (string) {
    return p.name;
}
