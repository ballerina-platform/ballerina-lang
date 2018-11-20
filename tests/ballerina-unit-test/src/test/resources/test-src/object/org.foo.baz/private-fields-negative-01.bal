
public type ParentFoo object {

    public int i = 0;
    public ChildFoo c = new("");



    private string s = "";


    function __init (i, c){}
};

type ChildFoo object {

    private string name = "";


    function __init (name) {}
};

type PrivatePerson object {

    public int age = 0;
    public string name = "";


    function __init (age, name){}
    public function getPrivatePersonName() returns string;
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

function PrivatePerson.getPrivatePersonName() returns (string) {
    return self.name;
}
