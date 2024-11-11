import ballerina/module1;

public function mod2Function1() {
}

# This is an example record in module2
#
# + field1 - Parameter Description for field1
# + field2 - Parameter Description for field2
public type Mod2Record1 record {
    int field1 = 123;
    module1:TestRecord1 field2 = {};
};

public class Person {
    # Name of the person
    public string name = "MyName";
}

# A function with a defaultable parameter
#
# + name - Defaultable param
public function functionWithDefaultableParam(string name = "defaultName") {
    
}
