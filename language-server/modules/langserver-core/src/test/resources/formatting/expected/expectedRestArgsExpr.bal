function test1(string a, string... name) {

}

string[] names = ["john", "asd"];
function test2() {
    test1("value", ...names);
}

function test3() {
    test1("value",
    ...
    names
    );
}
