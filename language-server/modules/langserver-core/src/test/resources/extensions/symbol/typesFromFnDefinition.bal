import ballerina/lang.'typedesc;

type Person record {
    int id;
    string firstName;
    string lastName;
    int age?;
};

type Course record {
    string id;
    string name;
    int credits;
};

type Submission record {
    int id = 0;
    string date;
    'typedesc:ModuleId moduleId;
};

type Student record {
    int? id;
    string fullName;
    string age;
    record {
        string title;
        int credits;
    }[] courses;
    int totalCredits;
};

function transform1(string str) returns string => "";

function transform2(Person person) returns Student => {};

function transform3(Person person, Course course, Submission submission) returns Student => {};

function transform4(Person person, int i) returns float => 0.0;

function transform5('typedesc:ModuleId moduleId) returns 'typedesc:TypeId => {};

function transform6(string[] names) returns string[] => [];

function transform7(Person[] people, Course[] courses) returns Student[] => [];

function transform8('typedesc:ModuleId[] moduleIds) returns 'typedesc:TypeId[] => [];

function transform9(Person? person) returns Student? => {};

function transform10(string str = "") returns string|error => "";

function transform11(record {string houseNo; string street; string city;} address)
    returns record {string number; string street; string city; int zip;} => {};

function transform12() => ();

function transform13(string str) => ();

function transform14() returns string => "";
