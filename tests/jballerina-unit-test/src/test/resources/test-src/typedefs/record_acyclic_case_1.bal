public type Foo record {
    string code?;
};

public type Bar record { // needs Qux with null restFieldType
    Qux[] reasons?;
};

public type Baz record {    // needs Bar
    *Bar;
    Foo[] reasons?;
};

public type Qux record {   // restFieldType is null
    string code?;
};

public function foo() {
    Baz baz = {};
}