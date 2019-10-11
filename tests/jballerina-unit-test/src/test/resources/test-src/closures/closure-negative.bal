function testUninitializedClosureVars() {
    string a;

    var bazz = function () {
        string str = a + "aa";
    };

    bazz();

    string b;
    int count;

    var bar = function () {
        count += 10;

        b = b + "bb";
    };

    bar();
}
