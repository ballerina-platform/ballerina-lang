type C_Course record {
    @xmldata:Namespace {
        prefix: "u",
        uri: "http://example.com/university"
    }
    string name;
    @xmldata:Namespace {
        prefix: "c",
        uri: "http://example.com/course"
    }
    int intake?;
    @xmldata:Namespace {
        prefix: "p",
        uri: "http://example.com/professor"
    }
    string professor?;
};

type D_SubDepartment record {
    @xmldata:Namespace {
        prefix: "d",
        uri: "http://example.com/department"
    }
    string name;
    @xmldata:Namespace {
        prefix: "c",
        uri: "http://example.com/course"
    }
    C_Course[] course?;
    @xmldata:Namespace {
        prefix: "d",
        uri: "http://example.com/department"
    }
    D_SubDepartment[] subDepartment?;
};

type D_Department record {
    @xmldata:Namespace {
        prefix: "u",
        uri: "http://example.com/university"
    }
    string name;
    @xmldata:Namespace {
        prefix: "c",
        uri: "http://example.com/course"
    }
    C_Course[] course;
    @xmldata:Namespace {
        prefix: "d",
        uri: "http://example.com/department"
    }
    D_SubDepartment subDepartment?;
};

type F_Faculty record {
    @xmldata:Namespace {
        prefix: "u",
        uri: "http://example.com/university"
    }
    string name;
    @xmldata:Namespace {
        prefix: "d",
        uri: "http://example.com/department"
    }
    D_Department[] department;
};

@xmldata:Name {
    value: "university"
}
type University record {
    @xmldata:Namespace {
        prefix: "f",
        uri: "http://example.com/faculty"
    }
    F_Faculty[] faculty;
};
