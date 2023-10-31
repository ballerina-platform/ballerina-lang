type MainLocation record {
    string main\:city;
    string main\:state;
};

type MainCompanyInfo record {
    string main\:name;
    MainLocation main\:location;
};

type Project record {
    string[] task?;
    @xmldata:Attribute
    string name;
};

type Manager record {
    @xmldata:Attribute
    string id;
    @xmldata:Attribute
    string name;
};

type Employee record {
    string name;
    string position?;
    Project[] project;
    Manager manager?;
    @xmldata:Attribute
    string id?;
};

type Employees record {
    Employee[] employee;
};

type Mixed record {
    string b;
    string i;
};

@xmldata:Name {
    value: "root"
}
@xmldata:Namespace {
    prefix: "root",
    uri: "http://example.com/root"
}
type RootRoot record {
    MainCompanyInfo main\:companyInfo;
    Employees employees;
    Mixed mixed;
    @xmldata:Attribute
    string version;
    @xmldata:Attribute
    string xmlns\:main = "http://example.com/main";
};
