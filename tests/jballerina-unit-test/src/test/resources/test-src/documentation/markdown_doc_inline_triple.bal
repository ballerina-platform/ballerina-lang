# Example of a string template:
#   ```string s = string `hello ${name}`;```
# Example for an xml literal:
#   ```xml x = xml `<{{tagName}}>hello</{{tagName}}>`;```
final string testConst = "TestConstantDocumentation";

class DummyObject {

    # This is a test function
    # ``` Purpose of adding
    # this documentation is
    # to check backtic documentations ```
    public function func1() {
    }

    # This is a test function
    # ```
    # Purpose of adding
    # this documentation is
    # to check backtic documentations
    # ```
    public function func2() {
    }
}
