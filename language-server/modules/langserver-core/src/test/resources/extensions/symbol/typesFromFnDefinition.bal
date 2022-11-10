import ballerina/http;
import ballerina/jwt;

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
    http:QueryParams params;
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

function transform5(http:CertKey certKey) returns jwt:CertKey => {};

function transform6(string[] names) returns string[] => [];

function transform7(Person[] people, Course[] courses) returns Student[] => [];

function transform8(http:CertKey[] keys) returns jwt:CertKey[] => [];

function transform9(Person? person) returns Student? => {};

function transform10(string str = "") returns string|error => "";

function transform11(record {string houseNo; string street; string city;} address)
    returns record {string number; string street; string city; int zip;} => {};

function transform12() => ();

function transform13(string str) => ();

function transform14() returns string => "";
