type CCourse record {
    string u\:name;
    int c\:intake?;
    string p\:professor?;
};

type DSubDepartment record {
    string d\:name;
    CCourse[] c\:course?;
    DSubDepartment[] d\:subDepartment?;
};

type DDepartment record {
    string u\:name;
    CCourse[] c\:course;
    DSubDepartment d\:subDepartment?;
};

type FFaculty record {
    string u\:name;
    DDepartment[] d\:department;
};

@xmldata:Name {
    value: "university"
}
type University record {
    FFaculty[] f\:faculty;
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