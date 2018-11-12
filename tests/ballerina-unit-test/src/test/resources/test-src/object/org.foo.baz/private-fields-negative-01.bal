
public type ParentFoo object {

    public int i;
    public ChildFoo c;



    private string s;


    new (i, c){}
};

type ChildFoo object {

    private string name;


    new (name) {}
};

type PrivatePerson object {

    public int age;
    public string name;


    new (age, name){}
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

function PrivatePerson::getPrivatePersonName() returns (string) {
    return self.name;
}
