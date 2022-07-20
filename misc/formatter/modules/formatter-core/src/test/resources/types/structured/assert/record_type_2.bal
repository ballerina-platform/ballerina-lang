record {|string firstName; string lastName;|} name = {firstName: "Joe", lastName: "Root"};

record {|string firstName; string...;|} a = {firstName: "John"};

type Record record {|
    string firstName;
    string...;
|};
