type AnnotRec record {|
    string:RegExp value;
|};

annotation AnnotRec annot on type;

@annot {
    value: re `abc{1}`
}
type Foo record {
    string name;
};

@annot {
    value: re `abc{1,}`
}
type Foo2 record {
    string name;
};

@annot {
    value: re `abc{1,2}`
}
type Foo3 record {
    string name;
};

public function test1() {
    Foo _ = {name: "abc"};
    Foo2 _ = {name: "abc"};
    Foo3 _ = {name: "abc"};
}

public function test2() {
    string:RegExp _ = re `[A-Z]{1}`;
    string:RegExp _ = re `[A-Z]{1,2}`;
    string:RegExp _ = re `[A-Z]{1,}`;
}
