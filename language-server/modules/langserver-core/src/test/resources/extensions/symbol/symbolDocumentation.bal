import ballerina/module1;

# Adds two integers.

# + x - an integer
# + y - another integer

# + return - the sum of `x` and `y`
public function add(int x, int y)
                    returns int {

    return x + y;
}

public function main() returns error? {
    int result = add(2,3);
    module1:function3(1,2);
    module1:function1();
    createPerson("test","colombo");
}

# Creates and returns a `Person` object given the parameters.
#
# + fname - First name of the person
# + street - Street the person is living at
# + return - A new Person string
#
# # Deprecated parameters
# + street - deprecated for removal
# # Deprecated
# This function is deprecated in favour of `Person` type.
@deprecated
public function createPerson(string fname, @deprecated string street) returns string {
    return "";
}
