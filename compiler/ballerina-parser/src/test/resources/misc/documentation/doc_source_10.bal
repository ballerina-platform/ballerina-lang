# Example for an xml literal:
#   ```xml x = xml `<{{tagName}}>hello</{{tagName}}>`;```
final string testConst = "TestConstantDocumentation";

# ```this is to test `(single backtick) and ``(double backtick) within triple backticks```
final int testConst = 21;

# This is a dummy object
# ``` Purpose of adding
# this documentation is
# to check backtick documentations ```
type DummyObject object {

    # This is a test function
    # ``` Purpose of adding
    # this documentation is
    # to check backtick documentations ```
    public function func1();
};
