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

public function main() {
    foo();
    bar();
}
