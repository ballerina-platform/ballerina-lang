import ballerina/io;

type Student record {
    string name;
    int age;
    Grades grades;
    string...
};

type Grades record {
    int maths;
    int physics;
    int chemistry;
    !...
};

function getThatStudent() returns Student {
    Grades g = { maths: 100, physics:100, chemistry:100 };
    Student s = { name: "Mic", age:17, grades:g };
    return s;
}
