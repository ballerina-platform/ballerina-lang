
public type person01 object {
    public {
        int age;
        string name;
        string address;
    }
}

public type employee01 object {
    public {
        int age;
        string name;
        string zipcode = "95134";
    }
}

// Field name mismatch
function testEqOfStructsInSamePackage01() returns (string) {
    employee01 e = {age:14, name:"rat"};
    person01 p = <person01> e;
    return p.name;
}

public type person02 object {
    public {
        int age;
        string name;
        string address;
    }
}

public type employee02 object {
    public {
        int age;
        string name;
        int address;
    }
}

// Type name mismatch
function testEqOfStructsInSamePackage02() returns (string) {
    employee02 e = {age:14, name:"rat"};
    person02 p = <person02> e;
    return p.name;
}

public type person03 object {
    public {
        int age;
        string name;
        string address;
    }
}

public type employee03 object {
    public {
        int age;
        string name;
    }
}

// Field count mismatch
function testEqOfStructsInSamePackage03() returns (string) {
    employee03 e = {age:14, name:"rat"};
    person03 p = <person03> e;
    return p.name;
}

public type person04 object {
    public {
        int age;
        string name;
        string address;
    }
}

public type employee04 object {
    public {
        int age;
        string name;
        string address;
    }
    private {
        int id;
        string ss;
    }
}

// Private fields in RHS struct name mismatch
function testEqOfStructsInSamePackage04() returns (string) {
    employee04 e = {age:14, name:"rat"};
    person04 p = <person04> e;
    return p.name;
}


public type person05 object {
    public {
        int age;
        string name;
    }
    private {
        string address;
        string id;
    }
}

public type employee05 object {
    public {
        int age;
        string name;
        string address;
        string id;
        string ssn;
    }
}

// Private fields in LHS struct name mismatch
function testEqOfStructsInSamePackage05() returns (string) {
    employee05 e = {age:14, name:"rat"};
    person05 p = <person05> e;
    return p.name;
}

type person06 object {
    public {
        int age;
        string name;
        int address;
        string id;
    }
}

type employee06 object {
    public {
        int age;
        string name;
        string address;
        string id;
        string ssn;
    }
}

// Private structs type mismatch
function testEqOfStructsInSamePackage06() returns (string) {
    employee06 e = {age:14, name:"rat"};
    person06 p = <person06> e;
    return p.name;
}


public type person07 object {
    public {
        int age;
        string name;
        string address;
        string zipcode = "95134";
        string ssn;
    }
    public function getAge() returns (int);

    public function getName() returns (string);

    public function setSSN(string ssn);
}

public function person07::getAge() returns (int) {
    return age;
}

public function person07::getName() returns (string) {
    return name;
}

public function person07::setSSN(string ssn) {
    self.ssn = ssn;
}

public type employee07 object {
    public {
        int age;
        string name;
        string address;
        string zipcode = "95134";
        string ssn;
    }

    public function getName() returns (string);

    public function getAge() returns (int);
}

public function employee07::getName() returns (string) {
    return name;
}

public function employee07::getAge() returns (int) {
    return age;
}

// Public structs attached function count mismatch
function testEqOfStructsInSamePackage07() returns (string) {
    employee07 e = {age:14, name:"rat"};
    person07 p = <person07> e;
    return p.name;
}


public type person08 object {
    public {
        int age;
        string name;
        string address;
        string zipcode = "95134";
        string ssn;
    }

    public function getAge() returns (int);

    public function getName() returns (string);

    public function setSSN(string ssn);
}

public function person08::getAge() returns (int) {
    return age;
}

public function person08::getName() returns (string) {
    return name;
}

public function person08::setSSN(string ssn) {
    self.ssn = ssn;
}

public type employee08 object {
    public {
        int age;
        string name;
        string address;
        string zipcode = "95134";
        string ssn;
    }

    public function getName() returns (string);

    public function getAge() returns (int);

    public function getSSN() returns (string);
}

public function employee08::getName() returns (string) {
    return name;
}

function employee08::getAge() returns (int) {
    return age;
}

public function employee08::getSSN() returns (string) {
    return ssn;
}

// Public structs attached function visibility mismatch
function testEqOfStructsInSamePackage08() returns (string) {
    employee08 e = {age:14, name:"rat"};
    person08 p = <person08> e;
    return p.name;
}


public type person09 object {
    public {
        int age;
        string name;
        string address;
        string zipcode = "95134";
        string ssn;
    }

    public function getAge() returns (int);

    public function getName() returns (string);

    public function setSSN(string ssn);
}

public function person09::getAge() returns (int) {
    return age;
}

public function person09::getName() returns (string) {
    return name;
}

public function person09::setSSN(string ssn) {
    self.ssn = ssn;
}

public type employee09 object {
    public {
        int age;
        string name;
        string address;
        string zipcode = "95134";
        string ssn;
    }

    public function getName() returns (string);

    public function getAge(int i) returns (int);

    public function getSSN() returns (string);
}

public function employee09::getName() returns (string) {
    return name;
}

public function employee09::getAge(int i) returns (int) {
    return age;
}

public function employee09::getSSN() returns (string) {
    return ssn;
}

// Public structs attached function signature mismatch
function testEqOfStructsInSamePackage09() returns (string) {
    employee09 e = {age:14, name:"rat"};
    person09 p = <person09> e;
    return p.name;
}
