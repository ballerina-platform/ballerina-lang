type Main_Location record {
    @xmldata:Namespace {
        prefix: "main",
        uri: "http://example.com/main"
    }
    string city;
    @xmldata:Namespace {
        prefix: "main",
        uri: "http://example.com/main"
    }
    string state;
};

type Main_CompanyInfo record {
    @xmldata:Namespace {
        prefix: "main",
        uri: "http://example.com/main"
    }
    string name;
    @xmldata:Namespace {
        prefix: "main",
        uri: "http://example.com/main"
    }
    Main_Location location;
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
type Root_Root record {
    @xmldata:Namespace {
        prefix: "main",
        uri: "http://example.com/main"
    }
    Main_CompanyInfo companyInfo;
    Employees employees;
    Mixed mixed;
    @xmldata:Attribute
    string version;
};
