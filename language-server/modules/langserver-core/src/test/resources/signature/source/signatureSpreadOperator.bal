type Foo record {|
    string s;
    int i;
    float f;
    boolean|float...;
|};

type Bar record {|
    string s;
    int i;
|};

# Description
#
# + a - a Parameter Description
# + return - Return Value Description
function func(int a) returns map<boolean|float> {
    return {a: true};
}

function testSignatureHelp () {
        Bar b = {s: "str", i: 1};
        Foo f = {...b, f: 123.4, ...func()}; // spreads `b` and `m2` returned by `func`
}
