type Foo record {
    string 'type;
    int id;
};

type Bar readonly & record {
    string 'type;
    int id;
};

type Baz record {
    readonly string 'type; 
    int id; 
};

function testFillRecordFields() {
    Foo foo = {'type: "foo"};
    
    Bar bar = {'type: "bar"};
    
    Baz baz = {'type: "baz"};
    
    Foo & readonly fooReadonly = {'type: "foo"};
    
    record {
        string 'type;
        int id;
    } & readonly fooReadonly2 = {'type: "foo"};
}
