import ballerina/module1;
import projectls.lsmod2;

# Description for the testDefaultModuleFunction1
#
# + param1 - Parameter1 Description 
# + param2 - Parameter2 Description
# + param3 - Parameter3 Description 
public function testDefaultModuleFunction1(DefaultModRecord1 param1, module1:TestRecord1 param2, lsmod2:Mod2Record1 param3) {
    // logic goes here
}

# This is an example record in default module.
#
# + field1 - Parameter Description for field1
# + field2 - Parameter Description for field2
public type DefaultModRecord1 record {
    int field1;
    module1:TestRecord1 field2;
};
