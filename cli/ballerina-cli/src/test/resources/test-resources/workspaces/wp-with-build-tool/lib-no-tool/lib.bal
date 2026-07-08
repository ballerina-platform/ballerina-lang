# Returns a greeting string.
#
# + name - name as a string or nil
# + return - "Hello, " with the input string name
public function hello(string? name) returns string {
    if name !is () {
        return string `Hello, ${name}`;
    }
    return "Hello, World!";
}
