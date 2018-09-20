public type person record {
    int age;
    string name;
    string address;
    string zipcode = "95134";
    string ssn;
    int id;
};

public type closedPerson record {
    int age;
    string name;
    string address;
    string zipcode = "95134";
    string ssn;
    int id;
    !...
};

public type employee record {
    int age;
    string name;
    string address;
    string zipcode = "95134";
    string ssn;
    int id;
    int employeeId = 123456;
};

public type closedEmployee record {
    int age;
    string name;
    string address;
    string zipcode = "95134";
    string ssn;
    int id;
    int employeeId = 123456;
    !...
};
