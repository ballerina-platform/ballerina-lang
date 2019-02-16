import ballerina/io;

type Person object {
string fname;
string lname;

function __init(string fname, string lname) {
self.fname = fname;
self.lname = lname;
}

function getFullName() returns string {
return self.fname + " " + self.lname;
}
};

// This function returns a value of the `any` type.
function getValue() returns any {
string name = "cat";
return name;
}

public function main() {
// In this example, the variable named `a` of the `any` type holds
// a `Person` object.
any a = new Person("John", "Doe");

// Before anything useful can be done with `a`, we need to ascertain
// its type. To this end, a type assertion or a type guard can be used.
Person john = <Person>a;
io:println("Full name: ", john.getFullName());

if (a is Person) {
io:println("First name: ", john.fname);
}

// Variables of type `any` can hold values of any type except for `error`.
int[] ia = [1, 3, 5, 6];
any ar = ia;
io:println(ar);

io:println(getValue());
}
