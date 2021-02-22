// Override lang.int with custom import
import ballerina/foo.bar.baz as int:;

// Predeclared prefix in annotation and listener decl
@int:%
listener stream:Element myListner = getListner();

type myObject object: {
    function foo(int a, any b);
    *object:customObj;
};

type myTable table:<rowTypeRecord> key int;

public function foo() returns map: {
    any a = stream: from var b in studentDetails select {name: b.name};
    any b = table: key<int> from var b in studentDetails select {name: b.name};
    any c = stream:
    any c = xml:
    any d = string:!@;
    int:[5] e = [1,2,3,4,5];
    error:<errorDetailRecord> f;
    error: g;

    // Predeclared prefix in object constructor expression
    object {int a;} myObj = object object: {int a = 5;};
    // Predeclared prefix in error type reference in error match pattern
    match (a) {
        error object: => {
            io:println("custom object error");
        }
    }
    int:
}

public function main(transaction) {
    // missing colon in var decl
    transaction Info info;
    // missing colon in expression
    int a = transaction
    // missing open parenthesis in transaction statement
    transaction int a = 5 }
    // invalid colon
    transaction: { int a = 5; }
    // missing identifier and var decl rhs
    transaction:
}
