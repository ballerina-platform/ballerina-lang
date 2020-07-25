public function function1() {
    int var1 = 10;
    int var2 = 20;
    int total = var1 + var2;
}

# This is function2
public function function2() {
    int var1 = 10;
    int var2 = 20;
    int total = var1 + var2;
}

# This is function3 with input parameters
#
# + param1 - param1 Parameter Description 
# + param2 - param2 Parameter Description
public function function3(int param1, int param2) {
    int var1 = param1;
    int var2 = param2;
    int total = var1 + var2;
}

# This is function3 with input parameters
#
# + param1 - param1 Parameter Description 
# + param2 - param2 Parameter Description
public function function4(int param1, int param2) {
    int var1 = param1;
    int var2 = param2;
    int total = var1 + var2;
}

# This is function3 with input parameters
#
# + param1 - param1 Parameter Description 
# + param2 - param2 Parameter Description
function function5(int param1, int param2) {
    int var1 = param1;
    int var2 = param2;
    int total = var1 + var2;
}

# This is function4 with input parameters and return type
#
# + param1 - param1 Parameter Description 
# + param2 - param2 Parameter Description
# + return - Return Value Description
function function6(int param1, int param2) returns TestRecord3 {
    int var1 = param1;
    int var2 = param2;
    int total = var1 + var2;
    TestRecord3 rec = {};
    
    return rec;
}
