
public type FooDepartment object {

    public string dptName = "";
    public FooPerson[] employees = [];

    public new (employees) {}
};

public type FooPerson object {

    public string name = "default first name";
    public string lname = "";
    public map adrs = {};
    public int age = 999;
    public FooFamily family = new;


    public new (name, adrs, age){}
};

type FooFamily object {

    public string spouse = "";
    public int noOfChildren = 0;
    public string[] children = [];

};

public type FooEmployee object {

    public string fname = "";
    public string lname = "";
    public int age = 0;


    private object {

            public string city = "";
            public string state = "";
            public string zipcode = "";

            new (city, state, zipcode) {}
        } address;


    public new (fname, lname, age, address){}
};

public function createObj() returns (FooPerson) {
    map address1 = {};
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
