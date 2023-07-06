import projectls.lsmod2;
import ballerina/module1 as bMod;

# Description for mod1Src1Function1
#
# + param1 - Parameter Description  
# + param2 - Parameter Description
#  + param3 - Parameter Description
function mod1Src1Function1(bMod:TestRecord1 param1, Mod1Record1 param2, lsmod2:Mod2Record1 param3) {
    
}

# This is an example record in module1
#
# + field1 - Parameter Description for field1
# + field2 - Parameter Description for field2
public type Mod1Record1 record {
    int field1;
    bMod:TestRecord1 field2;
};