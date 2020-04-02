type Foo1 record {|
    string s;
    int...;
|};

annotation Foo1 f1 on type;

@f1 {
    s: "str",
    i: 1 // invalid key 'i': identifiers cannot be used as rest field keys, expected a string literal or an expression
}
type Bar1 object {
};

type Foo2 record {
    string s1;
    string? s2;
};

annotation Foo2 f2 on type;

@f2 {
    s1: "str",
    s2: null // 'null' literal is only supported for 'json'
}
type Bar2 object {

};

type Foo3 record {|
    string s;
    int i;
    float fl;
|};

type Foo4 record {|
    string s;
    int i;
|};

Foo4 fl = {s: "str", i: 2};

annotation Foo3 f3 on type;

@f3 {
    i: 1,
    fl: 1.0,
    ...fl, // invalid usage of record literal: duplicate key 'i' via spread operator '...f'
    s: "hi" // invalid usage of record literal: duplicate key 's'
}
type Bar3 object {
};
