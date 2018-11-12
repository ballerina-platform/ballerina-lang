
public type ParentFoo record {
    int i;
    ChildFoo c;
};

type ChildFoo record {
    string name;
};

type PrivatePerson record {
    int age;
    string name;
};

public function newPrivatePerson() returns PrivatePerson {
    return {age:12, name:"mad"};
}

public function privatePersonAsParam(PrivatePerson p) returns string {
    return p.name;
}

public function privatePersonAsParamAndReturn(PrivatePerson p) returns PrivatePerson {
    return p;
}
