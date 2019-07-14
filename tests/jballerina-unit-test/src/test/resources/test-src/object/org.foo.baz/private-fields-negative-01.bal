
public type ParentFoo object {

    public int i;
    public ChildFoo c;
    private string s = "";

    function __init (int i, ChildFoo c){
        self.i = i;
        self.c = c;
    }
};

type ChildFoo object {
    private string name = "";

    function __init (string name) {
        self.name = name;
    }
};

type PrivatePerson object {

    public int age = 0;
    public string name = "";

    function __init (int age, string name){
        self.age = age;
        self.name = name;
    }
    public function getPrivatePersonName() returns string { return self.name; }
};

public function newPrivatePerson() returns (PrivatePerson) {
    return new PrivatePerson(12, "mad");
}

public function privatePersonAsParam(PrivatePerson p) returns (string){
    return p.name;
}

public function privatePersonAsParamAndReturn(PrivatePerson p) returns (PrivatePerson) {
    return p;
}
