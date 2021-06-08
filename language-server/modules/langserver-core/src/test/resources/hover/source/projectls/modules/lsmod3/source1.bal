import ballerina/module1;

public function mod3Function1() {
}

# This is an example record in module3
#
# + field1 - Parameter Description for field1
# + field2 - Parameter Description for field2
public type Mod3Record1 record {
    int field1;
    module1:TestRecord1 field2;
};

# Mod3
#
# + name - Name
# + age - Age
# + PERSON_KW - PERSON Keyword
public class Mod3Class {
    private string name;
    private int age;
    public final string PERSON_KW = "PERSON";

    function init(string name, int age) {
        self.name = name;
        self.age = age;
    }

    # Get name of the person
    # + return - String
    function getName() returns string{
        return self.name;
    }

    private function incrementAge() {
        self.age += 1;
    }
}
