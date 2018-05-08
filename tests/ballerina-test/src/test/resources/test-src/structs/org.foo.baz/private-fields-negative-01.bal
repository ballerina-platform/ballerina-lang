
public type ParentFoo {
    int i;
    ChildFoo c;
};

type ChildFoo {
    string name;
};

type privatePerson {
    int age;
    string name;
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
