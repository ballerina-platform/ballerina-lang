
public type FooDepartment record {
    string dptName = "";
    FooPerson[] employees = [];
};

public type FooPerson record {
    string name = "default first name";
    string lname = "";
    map<any> adrs = {};
    int age = 999;
    FooFamily family = {};
    FooPerson? parent = ();
};

type FooFamily record {
    string spouse = "";
    int noOfChildren = 0;
    string[] children = [];
};

public type FooEmployee record {
    string fname = "";
    string lname = "";
    int age = 0;
    record {
        string city = "";
        string state = "";
        string zipcode = "";
    } address = {};
    object { public string desc; public string title; } job = object {
        public string desc = "";
        public string title = "";
    };
};

public function createRecord() returns FooPerson {
    map<any> address1 = {};
    map<any> address = {"country":"USA", "state":"CA"};
    FooPerson emp = {name:"Jack", adrs:address, age:25};
    return emp;
}

public function createRecordOfRecord () returns FooDepartment {

    map<any> address = {"country":"USA", "state":"CA"};
    FooPerson emp1 = {name:"Jack", adrs:address, age:25};
    FooPerson emp2 = {};
    FooPerson[] emps = [emp1, emp2];
    FooDepartment dpt = {employees:emps};

    return dpt;
}

public function createAnonRecord() returns FooEmployee {

    FooEmployee e = {fname:"sam", lname:"json", age:100,
                     address:{city:"Los Altos", state:"CA", zipcode:"95123"}};
    return e;
}
