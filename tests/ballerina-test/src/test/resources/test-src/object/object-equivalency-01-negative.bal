
public type person01 object {
    public {
        int age;
        string name;
        string address;
    }
};

public type employee01 object {
    public {
        int age;
        string name;
        string zipcode = "95134";
    }
    new (age, name) {}
};

// Field name mismatch
function testEqOfObjectsInSamePackage01() returns (string) {
    employee01 e = new (14, "rat");
    person01 p = <person01> e;
    return p.name;
}

public type person02 object {
    public {
        int age;
        string name;
        string address;
    }
};

public type employee02 object {
    public {
        int age;
        string name;
        int address;
    }
    new (age, name) {}
};

// Type name mismatch
function testEqOfObjectsInSamePackage02() returns (string) {
    employee02 e = new (14, "rat");
    person02 p = <person02> e;
    return p.name;
}

public type person03 object {
    public {
        int age;
        string name;
        string address;
    }
};

public type employee03 object {
    public {
        int age;
        string name;
    }
    new (age, name) {}
};

// Field count mismatch
function testEqOfObjectsInSamePackage03() returns (string) {
    employee03 e = new (14, "rat");
    person03 p = <person03> e;
    return p.name;
}

public type person04 object {
    public {
        int age;
        string name;
        string address;
    }
};

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
    new (age, name) {}
};

// Private fields in RHS object name mismatch
function testEqOfObjectsInSamePackage04() returns (string) {
    employee04 e = new (14, "rat");
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
    new (age, name) {}
};

public type employee05 object {
    public {
        int age;
        string name;
        string address;
        string id;
        string ssn;
    }
    new (age, name) {}
};

// Private fields in LHS object name mismatch
function testEqOfObjectsInSamePackage05() returns (string) {
    employee05 e = new (14, "rat");
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
};

type employee06 object {
    public {
        int age;
        string name;
        string address;
        string id;
        string ssn;
    }
    new (age, name) {}
};

// Private Objects type mismatch
function testEqOfObjectsInSamePackage06() returns (string) {
    employee06 e = new (14, "rat");
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
};

public function person07::getAge() returns (int) {
    return self.age;
}

public function person07::getName() returns (string) {
    return self.name;
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

    new (age, name) {}

    public function getName() returns (string);

    public function getAge() returns (int);
};

public function employee07::getName() returns (string) {
    return self.name;
}

public function employee07::getAge() returns (int) {
    return self.age;
}

// Public Objects attached function count mismatch
function testEqOfObjectsInSamePackage07() returns (string) {
    employee07 e = new (14, "rat");
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
};

public function person08::getAge() returns (int) {
    return self.age;
}

public function person08::getName() returns (string) {
    return self.name;
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

    new (age, name) {}

    public function getName() returns (string);

    public function getAge() returns (int);

    public function getSSN() returns (string);
};

public function employee08::getName() returns (string) {
    return self.name;
}

public function employee08::getAge() returns (int) {
    return self.age;
}

public function employee08::getSSN() returns (string) {
    return self.ssn;
}

// Public Objects attached function visibility mismatch
function testEqOfObjectsInSamePackage08() returns (string) {
    employee08 e = new (14, "rat");
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
};

public function person09::getAge() returns (int) {
    return self.age;
}

public function person09::getName() returns (string) {
    return self.name;
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

    new (age, name) {}

    public function getName() returns (string);

    public function getAge(int i) returns (int);

    public function getSSN() returns (string);
};

public function employee09::getName() returns (string) {
    return self.name;
}

public function employee09::getAge(int i) returns (int) {
    return self.age;
}

public function employee09::getSSN() returns (string) {
    return self.ssn;
}

// Public Objects attached function signature mismatch
function testEqOfObjectsInSamePackage09() returns (string) {
    employee09 e = new (14, "rat");
    person09 p = <person09> e;
    return p.name;
}
