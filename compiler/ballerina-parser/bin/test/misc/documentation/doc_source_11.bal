# Example for an xml literal:
# ``xml x = xml `<{{tagName}}>hello</{{tagName}}>`;``
final string testConst = "TestConstantDocumentation";

# ``this is to test `(single backtick) and ```(triple backtick) within double backticks``
final int testConst = 21;

# This is a dummy object
# ``
# Purpose of adding
# this documentation is
# to check backtick documentations
# ``
type DummyObject object {

    # This is a test function
    # ``
    # Purpose of adding
    # this documentation is
    # to check backtick documentations
    # ``
    public function func1();
};

# Prints value(s) to the STDOUT.
# ``ballerina
# io:print("Start processing the CSV file from ", srcFileName);
#
# io:print("Start processing the CSV file from ", srcFileName);
# ``
public function print() {
}
