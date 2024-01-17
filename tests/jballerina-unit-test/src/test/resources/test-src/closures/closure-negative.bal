function testUninitializedClosureVars() {
    string a;

    var bazz = function () {
        string _ = a + "aa";
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

public function lambdaInitTest() {
    int localVar;
    int localVar2;

    worker w1 {
        localVar = 4;
        int _ = localVar;
        int _ = localVar2;
    }

    var _ = function () {
        localVar = 2;
        int _ = localVar;
        int _ = localVar2;
    };

    int _ = localVar;
    int _ = localVar2;
}
