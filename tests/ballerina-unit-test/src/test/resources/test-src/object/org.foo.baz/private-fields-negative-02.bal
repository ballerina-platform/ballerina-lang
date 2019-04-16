
public type FooDepartment object {
    public string dptName = "";
    public FooPerson?[] employees;

    public function __init (FooPerson?[] employees) {
        self.employees = employees;
    }
};

public type FooPerson object {
    public string name = "default first name";
    public string lname = "";
    public map<any> adrs = {};
    public int age = 999;
    public FooFamily family = new;

    public function __init (string name, map<any> adrs, int age) {
        self.age = age;
        self.name = name;
        self.adrs = adrs;
    }
};

type FooFamily object {
    public string spouse = "";
    public int noOfChildren = 0;
    public string[] children = [];
};

public type FooEmployee object {
    public string fname;
    public string lname;
    public int age;

    private object {
            public string city;
            public string state;
            public string zipcode;

            function __init (string city, string state, string zipcode) {
                self.city = city;
                self.state = state;
                self.zipcode = zipcode;
            }
        } address;


    public function __init (string fname, string lname, int age, object {
            public string city = "";
            public string state = "";
            public string zipcode = "";

            function __init (string city, string state, string zipcode) {}
        } address) {
        self.fname = fname;
        self.lname = lname;
        self.age = age;
        self.address = address;
    }
};

public function createObj() returns (FooPerson) {
    map<any> address1 = {};
    map<any> address = {"country":"USA", "state":"CA"};
    FooPerson emp = new("Jack", address, 25);
    return emp;
}

public function createObjOfObj () returns (FooDepartment) {

    map<any> address = {"country":"USA", "state":"CA"};
    FooPerson emp1 = new("Jack", address, 25);
    FooPerson emp2 = new ("Bob",  address, 27);
    FooPerson?[] emps = [emp1, emp2];
    FooDepartment dpt = new (emps);

    return dpt;
}

public function createAnonObj() returns (FooEmployee) {

    FooEmployee e = new ("sam", "json", 100, new ("Los Altos", "CA", "95123"));
    return e;
}
