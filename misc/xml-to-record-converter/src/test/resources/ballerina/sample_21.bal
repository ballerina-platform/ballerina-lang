type Course_Title record {
    string \#content;
    @xmldata:Attribute
    string lang;
    @xmldata:Attribute
    string level;
};

type Instructor_Name record {
    string \#content;
    @xmldata:Attribute
    string gender;
};

type Instructor_Instructor record {
    @xmldata:Namespace {
        prefix: "instructor",
        uri: "http://example.com/instructors"
    }
    Instructor_Name name;
    @xmldata:Namespace {
        prefix: "instructor",
        uri: "http://example.com/instructors"
    }
    string office;
};

type Enrollment_Enrollment record {
    @xmldata:Attribute
    string capacity;
    @xmldata:Attribute
    string enrolled;
};

type Course_Course record {
    @xmldata:Namespace {
        prefix: "course",
        uri: "http://example.com/courses"
    }
    string code;
    @xmldata:Namespace {
        prefix: "course",
        uri: "http://example.com/courses"
    }
    Course_Title title;
    @xmldata:Namespace {
        prefix: "instructor",
        uri: "http://example.com/instructors"
    }
    Instructor_Instructor instructor;
    @xmldata:Namespace {
        prefix: "enrollment",
        uri: "http://example.com/enrollment"
    }
    Enrollment_Enrollment enrollment;
};

type Dept_Department record {
    @xmldata:Namespace {
        prefix: "dept",
        uri: "http://example.com/departments"
    }
    string name;
    @xmldata:Namespace {
        prefix: "dept",
        uri: "http://example.com/departments"
    }
    string location;
    @xmldata:Namespace {
        prefix: "course",
        uri: "http://example.com/courses"
    }
    Course_Course course;
};

@xmldata:Name {value: "university"}
type University record {
    @xmldata:Namespace {
        prefix: "dept",
        uri: "http://example.com/departments"
    }
    Dept_Department department;
};
