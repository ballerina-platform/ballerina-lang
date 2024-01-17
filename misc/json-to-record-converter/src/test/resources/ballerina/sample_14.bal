type Items record {
    string 'type;
    string default?;
    string example?;
    int minItems?;
    Items items?;
};

type OneOfItem record {
    string 'type;
    string example?;
    Items items?;
    int minItems?;
    string default?;
};

type Foo record {
    (Bar|string) bar;
};

type Bar record {
    Foo foo;
    (int|string) baz;
    Bar bar?;
};

type NewRecord record {
    string description;
    string default;
    boolean nullable;
    OneOfItem[] oneOf;
    Foo foo;
};
