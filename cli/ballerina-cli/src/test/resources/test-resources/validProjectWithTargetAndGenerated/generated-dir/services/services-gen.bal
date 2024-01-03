# Description
#
# + name - Parameter Description
# + return - Return Value Description
public function helloServices(string name) returns string {
    if !(name is "") {
        return "Hello, " + name;
    }
    return "Hello, World!";
}
