
public type FooDepartment object {
    public {
        string dptName;
        FooPerson[] employees;
    }
    public new (employees) {}
};

public type FooPerson object {
    public {
        string name = "default first name";
        string lname;
        map adrs;
        int age = 999;
        FooFamily family;
    }

    public new (name, adrs, age){}
};

type FooFamily object {
    public {
        string spouse;
        int noOfChildren;
        string[] children;
    }
};

public type FooEmployee object {
    public {
        string fname;
        string lname;
        int age;
    }
    private {
        object {
            public {
                string city;
                string state;
                string zipcode;
            }
            new (city, state, zipcode) {}
        } address;
    }

    public new (fname, lname, age, address){}
};

public function createObj() returns (FooPerson) {
    map address1;
    map address = {"country":"USA", "state":"CA"};
    FooPerson emp = new("Jack", address, 25);
    return emp;
}

public function createObjOfObj () returns (FooDepartment) {

    map address = {"country":"USA", "state":"CA"};
    FooPerson emp1 = new("Jack", address, 25);
    FooPerson emp2 = new ("Bob",  address, 27);
    FooPerson[] emps = [emp1, emp2];
    FooDepartment dpt = new (emps);

    return dpt;
}

public function createAnonObj() returns (FooEmployee) {

    FooEmployee e = new ("sam", "json", 100, new ("Los Altos", "CA", "95123"));
    return e;
}
