
type Foo record {
    string s;
    int i?;
};

type Bar record {
    string s;
    int i?;
};

public function mains() {
    Foo f = { s: "hello world", i: 1 };
    Foo|Bar fb = f;

    fb.
}