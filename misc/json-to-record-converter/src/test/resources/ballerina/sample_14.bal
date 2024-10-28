type Items record {|
    string 'type;
    string default?;
    string example?;
    int minItems?;
    Items items?;
    json...;
|};

type OneOfItem record {|
    string 'type;
    string example?;
    Items items?;
    int minItems?;
    string default?;
    json...;
|};

type Foo record {|
    (Bar|string) bar;
    json...;
|};

type Bar record {|
    Foo foo;
    (int|string) baz;
    Bar bar?;
    json...;
|};

type NewRecord record {|
    string description;
    string default;
    boolean nullable;
    OneOfItem[] oneOf;
    Foo foo;
    json...;
|};
