
public type ParentFoo record {
    int i = 0;
    ChildFoo c = {};
};

type ChildFoo record {
    string name = "";
};

type PrivatePerson record {
    int age = 0;
    string name = "";
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
