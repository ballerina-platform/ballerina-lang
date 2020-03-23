type Foo record {|
    string s?;
|};

public function main() {
    Foo f = {};
    f.remove("s");
}
