@Args{val: {}}
function foo (string args) {
    // do nothing
}

type Arguments {
    string value;
    Property prop;
};

type Property {
    string name;
};

annotation Args Arguments;