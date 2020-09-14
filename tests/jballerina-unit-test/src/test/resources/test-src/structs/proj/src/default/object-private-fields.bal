import org.foo;

public class employee01 {
    public int age = 0;
    public string name = "";
    public string address = "";
    public string zipcode = "95134";

    string ssn = "";
    int id = 0;
    int employeeId = 123456;
}

public class person {
    public int age = 0;
    public string name = "";
    public string address = "";
    public string zipcode = "95134";
    public string ssn = "";
    public int id = 0;
}

public class employeeObj {
    public int age = 0;
    public string name = "";
    public string address = "";
    public string zipcode = "95134";
    public string ssn = "";
    public int id = 0;
    public int employeeId = 123456;
}

public class userA {
    public int age = 0;
    public string name = "";
}

public class userB {
    public int age = 0;
    public string name = "";
    public string address = "";
}

public function textPrivateFieldAccess1 () returns [string, string, string, int, int] {
    employee01 e = new;
    e.age = 14;
    e.name = "sam";
    e.ssn = "234-56-7890";
    e.id = 45034;
    return [e.name, e.zipcode, e.ssn, e.id, e.employeeId];
}

public function textPrivateFieldAccess2 () returns [int, string] {
    foo:person p = foo:newPerson();
    return [p.age, p.name];
}

public function testCompileTimeStructEqWithPrivateFields () returns [string, string, string, int] {
    employeeObj e = new;
    e.age = 24;
    e.name = "jay";
    e.ssn = "123-56-7890";
    e.employeeId = 123;
    e.id = 458;

    person p = e;
    return [p.name, p.zipcode, p.ssn, p.id];
}

public function testCompileTimeStructEqWithPrivateFieldsTwoPackages () returns [int, string, string] {
    employeeObj e = new;
    e.age = 28;
    e.name = "mal";
    e.ssn = "123-56-2345";
    e.employeeId = 123;
    e.id = 458;

    foo:userObject u = e;
    return [u.age, u.name, u.zipcode];
}

public function testRuntimeStructEqWithPrivateFields () returns [string, string, string, int, int]|error {
    employeeObj e = new;
    e.age = 24;
    e.name = "jay";
    e.ssn = "123-56-7890";
    e.employeeId = 123;
    e.id = 458;

    person p = e;

    // Now I want p back to be an employeeObj instance
    var e1 = check trap <employeeObj> p;


    return [e1.name, e1.zipcode, e1.ssn, e1.id, e1.employeeId];
}

public function testRuntimeStructEqWithPrivateFieldsTwoPackages1 () returns [string, string, string, int]|error {
    employeeObj e = new;
    e.age = 24;
    e.name = "jay";
    e.ssn = "123-56-7890";
    e.id = 458;

    foo:userObject u = e;

    // Now I want convert u back to be an employeeObj instance.
    if(u is person) {
        return [u.name, u.zipcode, u.ssn, u.id];
    } else {
        error err = error("'u' is not a person");
        return err;
    }
}

public function testRuntimeStructEqWithPrivateFieldsTwoPackages2 () returns [string, int] {
    foo:userObject u = foo:newUserObject();

    var uA = u;

    // This is a unsafe cast
    var uB = uA;
    return [uB.name, uB.age];
}