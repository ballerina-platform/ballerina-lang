import ballerina/module1;
import ballerina/lang.'string;

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
    Counter counter = new (12);
    File f = check new File("test.txt", "Hello World");
    Person pr = new;
    int leng = "test".length();
    int length2 = 'string:length("test");
    Student student = new("Ballerina");
    int stId = student -> setId(1);
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

public class Counter {
    private int num;

    # Counter constructor.
    #
    # + num - Number to increment
    public function init(int num) {
        self.num = num;
    }
}


class File {
    string path;
    string contents;

    # File constructor.
    #
    # + path - Path of the file
    # + contents - Contents of the file
    function init(string path, string? contents) returns error? {
        self.path = path;
        self.contents = check contents.ensureType(string);
        return;
    }
}

class Person {
    string name = "";
    int id = 123;

    function getName() returns string {
        return self.name;
    }
}

public client class Student {
    string name;
    int id;

    function init(string name) {
        self.name = name;
    }
    # Set the Id of the student
    #
    # + id - Id of the student
    # + return - Student id
    remote function setId(int id) returns int {
        self.id = id;
        return self.id;
    }
}
