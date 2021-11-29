configurable string nameVal = "User";

public function hello(string name) returns string {
    if !(name is "") {
        return "Hello, " + name;
    }
    return "Hello, World!" + nameVal;
}
