function name0() {

}

type Person record {
    string name;
    string location?;
};

function name1() {
    float c = 0;
}

function name2(int a, string b) {
    // Test comment.
    float c = 0;
}

function name3() {}

function name4(string s, float i) {}

# test annotation
function name5() {}

public function main(string... args) {}

function name6(int a, string... i) {}

function name7() returns int {
    return 0;
}

function name8() returns (int, string) {
    return (0, "");
}

function name9() returns (Person) {
    return {name: ""};
}

private function name10() {

}

function name11() = external;

public function name12() = external;

private function name13()
=
external
;

function name14(int i, string name = "john", string... rest) {

}

function name15(string name = "john", int i, string... rest) {

}

function name16(string name = "john", int i, int id = 0, string... rest) {

}

function name17(string name = "john", int i,
int id = 0, string... rest) {

}
