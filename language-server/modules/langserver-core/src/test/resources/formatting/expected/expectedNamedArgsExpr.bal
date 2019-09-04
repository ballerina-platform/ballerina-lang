function test1(string a, string name = "wso2") {

}

function test2() {
    test1("value", name = "lk");
}

function test3() {
    test1("value",
        name
        =
        "lk"
    );
}

function test4() {
    test1("value", name = "lk");
}
