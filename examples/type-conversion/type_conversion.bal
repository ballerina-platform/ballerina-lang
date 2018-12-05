import ballerina/io;

type Person record {
    string name = "";
    int age = 0;
};

type Employee record {
    string name = "";
    int age = 0;
    int empNo = 0;
};

function convertType(Employee emp) returns () {
    // The `convert()` creates a new value and changes its type without editing provided value's inherent type.
    Person|error empPerson = Person.convert(emp);
    io:println("empPerson name: ", (empPerson is Person) ? empPerson["name"] : empPerson.reason());
}

function convertSimpleTypes() {

    // The `convert()` can be used to conversion of simple values as below.
    string s1 = "45";
    string s2 = "abc";
    string s3 = "true";
    float f = 10.2;
    any a = 3.14;
    
    int|error intVal1 = int.convert(s1);

    int|error intVal2 = int.convert(s2);

    int intVal3 = int.convert(f);

    boolean|error b = boolean.convert(s3);

    float|error af = float.convert(a);

    if (intVal1 is int) {
        io:println("Int value 1 : " + intVal1);
    } else {
        io:println("Error: " + intVal1.reason());
    }

    if (intVal2 is int) {
        io:println("Int value 2 : " + intVal2);
    } else {
        io:println("Error: " + intVal2.reason());
    }

    io:println("Int value 3 : " + intVal3);

    if (b is boolean) {
        io:println("Boolean value : " + b);
    } else {
        io:println("Error: " + b.reason());
    }

    if (af is float) {
        io:println("Float value : " + af);
    } else {
        io:println("Error: " + af.reason());
    }
}

public function main() {
    Employee emp = {name: "Jack Sparrow", age: 54, empNo: 100};
    convertType(emp);
    convertSimpleTypes();
}
