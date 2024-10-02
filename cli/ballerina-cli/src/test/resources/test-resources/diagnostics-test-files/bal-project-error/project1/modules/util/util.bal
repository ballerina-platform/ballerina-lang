import project1.helpers;

# Returns the string `Hello` with the input string name.
#
# + name - name as a string
# + return - "Hello, " with the input string name
public function hello(string name) returns string {
    if !(name is "") {
        Person person = {name: name, age: 10};
        return string `Hello, ${person.name}! You are ${person.age.toString()} years old. ${doACalc().toString()}`;
    }
    
    return "Hello, World!" + helpers:hello2("John");
}
