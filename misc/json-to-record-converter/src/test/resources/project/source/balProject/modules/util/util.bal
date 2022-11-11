# public isolated function hello(string firstName) returns string => firstName;

# Returns the string `Hello` with the input string name.
#
# + name - name as a string
# + return - "Hello, " with the input string name
function hello(string name) returns string {
    if !(name is "") {
        return "Hello, " + name;
    }
    return "Hello, World!";
}

type UtilRec record {
    string utilMessage;
};
