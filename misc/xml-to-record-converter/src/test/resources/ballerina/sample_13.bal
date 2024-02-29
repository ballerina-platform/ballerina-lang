type Task record {
    string id;
    string description;
    @xmldata:Attribute
    string name?;
};

type Tasks record {
    Task[] task;
};

type Project record {
    string id;
    string name;
    Tasks tasks?;
};

type Projects record {
    Project[] project;
};

type Manager record {
    string id;
    string name;
};

type Employee record {
    int id;
    string name;
    string position;
    int phone?;
    string dob?;
    Projects projects;
    Manager manager?;
};

type Employees record {
    Employee[] employee;
};

type Members record {
    string[] member;
};

type Team record {
    string id;
    string name;
    Members members;
};

type Teams record {
    Team[] team;
};

type Department record {
    string id;
    string name;
    Teams teams;
};

type Departments record {
    Department[] department;
};

@xmldata:Name {
    value: "company"
}
type Company record {
    string name;
    Employees employees;
    Departments departments;
};
