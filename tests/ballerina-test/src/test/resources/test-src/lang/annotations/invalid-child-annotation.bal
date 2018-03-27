@Args{val: {}}
function foo (string args) {
    // do nothing
}

struct Arguments {
    string value;
    Property prop;
}

struct Property {
    string name;
}

annotation Args Arguments;