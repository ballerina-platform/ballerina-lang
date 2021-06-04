type Foo record {|
    int Fa1;
    Bar Fa2;
|};

class TestClass {
    function func() returns Foo {
        return {}
    }
}

type Bar record {|
    int Ba1;
    string Ba2;
|};
