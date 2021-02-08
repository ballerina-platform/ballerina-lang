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