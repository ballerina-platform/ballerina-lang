public type Person object {
    public int age = 0;
    public string name = "";
    
    public function getName() returns string {
        return "Person Name";
    }
};
 
public type Employee object {
    public int age = 0;
    public string name = "";
    
    public function getName() returns string {
        return "Employee Name";
    }
};

function testAbstractAnonObjectInTypeTest() returns [string, string] {
    any p = new Person();
    any e =  new Employee();
    
    [string, string] names = ["", ""];
    if p is abstract object{ public function getName() returns string;} {
        names[0] = p.getName();
    }
    
    if e is abstract object { public function getName() returns string;} {
        names[1] = e.getName();
    }
    
    return names;
}

function testAbstractAnonObjectInFunction() returns [string, string] {
    Person p = new();
    Employee e =  new();
    return [getName(p), getName(e)];
}

function getName(abstract object { public function getName() returns string;} obj)  returns string {
    return obj.getName();
}

function testAbstractAnonObjectInVarDef() returns [string, string] {
    abstract object { public function getName() returns string;} p = new Person();
    abstract object { public function getName() returns string;} e =  new Employee();
    return [getName(p), getName(e)];
}
