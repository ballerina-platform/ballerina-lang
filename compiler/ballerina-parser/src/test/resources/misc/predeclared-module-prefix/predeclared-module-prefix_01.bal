// Override lang.int with custom import
import foobar/foo.bar.baz as int;

// Predeclared prefix in annotation and listener decl
@int:annot
listener stream:Element myListner = getListner();

xml:Element x = xmlElement;

type myRecord record {
    int a;
    xml:Element b;
    *table:tableRecod;
};

type myObject object {
    function foo(int a, any b);
    *object:customObj;
};

public function foo() returns int:c {
    // Using module prefix with predeclared prefix
    int a = int:sum(1, 2, 3);
    int b = int:b;
    any|error c = function:call(value, 2, 3);
    // Predeclared prefix in new expression
    Person p = new int:myClass();
    // Predeclared prefix in object constructor expression
    object {int a;} myObj = object object:myObjType {int a = 5;};
    // Predeclared prefix int error constructor expression
    error e = error transaction:transactionError(msg);
    // Predeclared prefix in error type reference in error match pattern
    match (a) {
        error object:customObjectError() => {
            io:println("custom object error");
        }
    }
    transaction:Info info;
    int:IntType info;
    string:lastIndexOf();
    string:fromBytes(byteArray1);
    int:sum(4, 5);
}

transaction:Info info2;

// Predeclared module prefix in conditional expression
public function bar() {
    any a = condition ? int : float;
    any b = condition ? int : x;
    any c = condition ? int:x : y;
    any d = condition ? int:x : boolean;
}

// Predeclared module prefix in array length
int[int:SIGNED8_MAX_VALUE] x = [];

xmlns boolean:qux as ns1;
