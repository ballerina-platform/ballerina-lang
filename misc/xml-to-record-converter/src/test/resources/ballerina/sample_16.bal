type C_Course record {
    string u\:name;
    int c\:intake?;
    string p\:professor?;
};

type D_SubDepartment record {
    string d\:name;
    C_Course[] c\:course?;
    D_SubDepartment[] d\:subDepartment?;
};

type D_Department record {
    string u\:name;
    C_Course[] c\:course;
    D_SubDepartment d\:subDepartment?;
};

type F_Faculty record {
    string u\:name;
    D_Department[] d\:department;
};

@xmldata:Name {
    value: "university"
}
type University record {
    F_Faculty[] f\:faculty;
    @xmldata:Attribute
    string xmlns\:c = "http://example.com/course";
    @xmldata:Attribute
    string xmlns\:d = "http://example.com/department";
    @xmldata:Attribute
    string xmlns\:f = "http://example.com/faculty";
    @xmldata:Attribute
    string xmlns\:p = "http://example.com/professor";
    @xmldata:Attribute
    string xmlns\:u = "http://example.com/university";
};
