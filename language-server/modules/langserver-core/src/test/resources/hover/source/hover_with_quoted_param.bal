# Description.
#
# + 'type - type of the parameter
# + val - value needed to be changed
function foo(string 'type, string val) {}

# Description.
#
# + 'type - type of the parameter  
# + val - value needed to be changed
# + 'order - execution order
function bar(string 'type, string val, string 'order) {}

public function test1() {
    foo();
    bar();
    baz();
}

# Description.
#
# + 'type - type of the parameter  
# + val - value needed to be changed
# + 'order - execution order
function baz(string 'type, string val, string ...'order) {}

type ParamConfig record {|
    int firstParam?;
    int secondParam?;
|};

# Description.
# 
# + 'type - Configuration for the function.
function name(*ParamConfig 'type) {
    
}

public function test2() {
    name();
}
