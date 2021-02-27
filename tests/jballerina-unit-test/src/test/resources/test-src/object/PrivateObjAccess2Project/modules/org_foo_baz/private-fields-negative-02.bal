
public class FooDepartment {
    public string dptName = "";
    public FooPerson?[] employees;

    public function init (FooPerson?[] employees) {
        self.employees = employees;
    }
}

public class FooPerson {
    public string name = "default first name";
    public string lname = "";
    public map<any> adrs = {};
    public int age = 999;
    public FooFamily family = new;

    public function init (string name, map<any> adrs, int age) {
        self.age = age;
        self.name = name;
        self.adrs = adrs;
    }
}

public class FooFamily {
    public string spouse = "";
    public int noOfChildren = 0;
    public string[] children = [];
}

public class AddressI {
    public string city;
    public string state;
    public string zipcode;

    function init(string city, string state, string zipcode) {
        self.city = city;
        self.state = state;
        self.zipcode = zipcode;
    }
}

public class FooEmployee {
    public string fname;
    public string lname;
    public int age;

    private AddressI address;


    public function init (string fname,
            string lname,
            int age,
            AddressI address) {
        self.fname = fname;
        self.lname = lname;
        self.age = age;
        self.address = address;
    }
}

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
