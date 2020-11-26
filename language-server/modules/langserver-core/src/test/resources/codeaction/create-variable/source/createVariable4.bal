type Foo record {|
    string s?;
|};

public function main() {
    Foo f = {};
    f.remove("s");
    [[1, 2, 3], [10, 20, 30], [5, 6, 7]];
    [[1, "", 3], [10, "", 30], [5, "", 7]];
    [[1, "", 3], [true, 1.1]];
    [1, ""];
    [1, 2, 3];
}
