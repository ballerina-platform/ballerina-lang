import org.foo;

public type employee01 object {
    public {
        int age;
        string name;
        string address;
        string zipcode = "95134";
    }

    private {
        string ssn;
        int id;
        int employeeId = 123456;
    }
};

public type person object {
    public {
        int age;
        string name;
        string address;
        string zipcode = "95134";
        string ssn;
        int id;
    }
};

public type employee object {
    public {
        int age;
        string name;
        string address;
        string zipcode = "95134";
        string ssn;
        int id;
        int employeeId = 123456;
    }
};

public type userA object {
    public {
        int age;
        string name;
    }
};

public type userB object {
    public {
        int age;
        string name;
        string address;
    }
};

public function textPrivateFieldAccess1 () returns (string, string, string, int, int) {
    employee01 e = new;
    e.age = 14;
    e.name = "sam";
    e.ssn = "234-56-7890";
    e.id = 45034;
    return (e.name, e.zipcode, e.ssn, e.id, e.employeeId);
}

public function textPrivateFieldAccess2 () returns (int, string) {
    foo:person p = foo:newPerson();
    return (p.age, p.name);
}

public function testCompileTimeStructEqWithPrivateFields () returns (string, string, string, int) {
    employee e = new;
    e.age = 24;
    e.name = "jay";
    e.ssn = "123-56-7890";
    e.employeeId = 123;
    e.id = 458;

    // This is a safe cast
    person p = <person>e;
    return (p.name, p.zipcode, p.ssn, p.id);
}

public function testCompileTimeStructEqWithPrivateFieldsTwoPackages () returns (int, string, string) {
    employee e = new;
    e.age = 28;
    e.name = "mal";
    e.ssn = "123-56-2345";
    e.employeeId = 123;
    e.id = 458;

    // This is a safe cast
    foo:user u = <foo:user>e;
    return (u.age, u.name, u.zipcode);
}

public function testRuntimeStructEqWithPrivateFields () returns (string, string, string, int, int) {
    employee e = new;
    e.age = 24;
    e.name = "jay";
    e.ssn = "123-56-7890";
    e.employeeId = 123;
    e.id = 458;

    // This is a safe cast
    person p = <person>e;

    // Now I want cat p back to be an employee instance.
    var e1 = check < employee > p;


    return (e1.name, e1.zipcode, e1.ssn, e1.id, e1.employeeId);
}

public function testRuntimeStructEqWithPrivateFieldsTwoPackages1 () returns (string, string, string, int) {
    employee e = new;
    e.age = 24;
    e.name = "jay";
    e.ssn = "123-56-7890";
    e.id = 458;

    // This is a safe cast
    foo:user u = <foo:user>e;

    // Now I want cat u back to be an employee instance.
    var p = check < person > u;


    return (p.name, p.zipcode, p.ssn, p.id);
}

public function testRuntimeStructEqWithPrivateFieldsTwoPackages2 () returns (string, int) {
    foo:user u = foo:newUser();

    // This is a safe cast
    var uA = <userA>u;

    // This is a unsafe cast
    var uB = check < userB > uA;
    return (uB.name, uB.age);
}