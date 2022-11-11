# public isolated function hello(string firstName) returns string => firstName;

# Returns the string `Hello` with the input string name.
#
# + name - name as a string
# + return - "Hello, " with the input string name
function Author(string name) returns string {
    if !(name is "") {
        return "Hello, " + name;
    }
    return "Hello, World!";
}

type Book record {
    string name;
    int publishedYear?;
    decimal price?;
};

type State record {
    string name;
    string code;
};
