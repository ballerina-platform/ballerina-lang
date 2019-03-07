import ballerina/io;

public function main() {
    any a = "Hello, world!";

    // Check whether the actual value inside the `any` type variable is a `string`.
    boolean b = a is string;
    io:println("Is 'a' a string? ", b);

    // The `type-check` expression can be used as a condition of an `if` statement.
    if (a is int) {
        io:println("'a' is an int with value: ", a);
    } else if (a is string) {
        io:println("'a' is a string with value: ", a);
    } else {
        io:println("'a' is not an int or string, with value: ", a);
    }

    // The `type-check` expression can be used to find the runtime type of `union` type variables.
    Student alex = { name : "Alex" };
    Student|Person|Vehicle x = alex;

    // Type of `x` is `Student`. Therefore, the `if` check will pass.
    if (x is Student) {
        io:println("Alex is a student");
    } else {
        io:println("Alex is not a student");
    }

    // Type of `x` is `Student`. However, it is structurally equivalent to `Person`. 
    // Therefore, the `if` check will pass.
    if (x is Person) {
        io:println("Alex is a person");
    } else {
        io:println("Alex is not a person");
    }

    // Type of `x` is Student. However, it is not structurally equivalent to `Vehicle`.
    if (x is Vehicle) {
        io:println("Alex is a vehicle");
    } else {
        io:println("Alex is not a vehicle");
    }

    // Type check expression can be used for any expression.
    boolean isStudent = foo("student") is Student;
    io:println("Is foo returns a student?", isStudent);
    isStudent = foo("vehicle") is Student;
    io:println("Is foo returns a student?", isStudent);
}

type Person record {
    string name;
};

type Student record {
    string name;
    int age = 0;
};

type Vehicle record {
    string brand;
};

function foo(string t) returns any {
    if (t == "student") {
        return <Student>{ name: "Alex" };
    } else if (t == "vehicle"){
        return <Vehicle>{ brand: "Honda" };
    }
    return "invalid type";
}
