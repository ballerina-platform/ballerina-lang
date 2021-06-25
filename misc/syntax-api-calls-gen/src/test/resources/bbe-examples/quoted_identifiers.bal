import ballerina/io;

// Use the reserved keyword `function` as the name of the function.
function 'function(int val) returns int {
    return val + 1;
}

// Use different kinds of characters in the name of the function.
function 'get_ɱȇşşağę_for_\#(int val) returns string {
    return val.toString() + " is a numeric value" ;
}

// Use different kinds of characters in the type definition.
// The quoted identifier syntax is used in both the type name and the field names.
type 'Person_\{name\&Ȧɢέ\} record {|
    string 'first\ name;
    int 'Ȧɢέ;
|};

public function main() {

    // Use the reserved keyword `int` as the name of the variable.
    int 'int = 1;

    // Invoke the function named `function` with the variable named `int`.
    // The quoted identifier syntax is used to refer to both the function and
    // the variable.
    int i = 'function('int);
    io:println(i);

    // Define a variable of which the variable name starts with a digit.
    int '1PlusI = 1 + i;
    io:println('1PlusI);

    // Define a variable of which the name contains special characters with a preceding `\`.
    int '\{add\#5\} = 5 + i;
    io:println('\{add\#5\});

    // Define a variable of which the variable name contains unicode characters.
    string 'üňĩćőđę_ňāɱȇ = "John doe";
    io:println('üňĩćőđę_ňāɱȇ);

    // Define a variable of which the variable name contains unicode characters specified by hexadecimal code points.
    string 'unicode_\u{2324} = "Jane doe";
    io:println('unicode_\u{2324});

    // Invoke the function named `get_ɱȇşşağę_for_\#` with the variable named `int`.
    io:println('get_ɱȇşşağę_for_\#('int));

    'Person_\{name\&Ȧɢέ\} person = {'first\ name: "Tom", 'Ȧɢέ:25};

    // Member access can be used to access the members of type `'Person_{name&Ȧɢέ}` by referring the to field name
    // without escaping the special characters using a `\`.
    io:println(person["first name"]);

    // The required field `'Ȧɢέ` can be accessed using field based access.
    io:println(person.'Ȧɢέ);
}
