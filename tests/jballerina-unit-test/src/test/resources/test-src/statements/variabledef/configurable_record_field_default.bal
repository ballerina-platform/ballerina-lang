configurable string|string[]? fooConfig = ();

type Foo record {|
    string|string[]? foo = fooConfig;
|};

public function testConfigurableVarUsageInRecordDefault() {
    Foo foo = {};
    // Ensure default field initialization succeeds without diagnostics.
    if foo.foo is () {
        return;
    }
}

