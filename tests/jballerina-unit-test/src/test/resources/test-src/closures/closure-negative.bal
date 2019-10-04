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

public function lambdaInitTest() {
    int localVar;
    int localVar2;

    worker w1 {
        localVar = 4;
        int j = localVar;
        int k = localVar2;
    }

    var f = function () {
        localVar = 2;
        int k = localVar;
        int l = localVar2;
    };

    int copy = localVar;
    int copy2 = localVar2;
}
