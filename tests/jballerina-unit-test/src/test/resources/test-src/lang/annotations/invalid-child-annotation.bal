@Args{val: {}}
function foo (string args) {
    // do nothing
}

type Arguments record {
    string value;
    Property prop;
};

type Property record {
    string name;
};

annotation Arguments Args;
