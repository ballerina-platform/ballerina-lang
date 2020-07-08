import ballerina/io;

public function recordOfTwoUnordered() returns record {| int a; int b; |} {
    @strand{thread:"any"}
    worker w3 {
        250 -> w2;
    }

    @strand{thread:"any"}
    worker w1 {
        100 -> w2;
    }

    @strand{thread:"any"}
    worker w2 returns  record {| int a; int b; |} {
        record {|
            int a;
            int b;
        |} r = <- { a:w1, b:w3 };
        io:print("r: ", r);
        return r;
    }

     record {| int a; int b; |} x = wait w2;
     return x;

}

public function recordOfTwoUnorderedWithSingleReceive() returns record {| int a; int b; |} {
    @strand{thread:"any"}
    worker w3 {
        250 -> w2;
    }

    @strand{thread:"any"}
    worker w1 {
        100 -> w2;
    }

    @strand{thread:"any"}
    worker w4 {
        100 -> w2;
    }

    @strand{thread:"any"}
    worker w2 returns  record {| int a; int b; |} {
        int a = <- w4;
        record {|
            int a;
            int b;
        |} r = <- { a:w1, b:w3 };
        io:print("r: ", r);
        return r;
    }

     record {| int a; int b; |} x = wait w2;
     return x;
}

public function singleStringMap() returns map<string> {
    @strand{thread:"any"}
    worker w1 {
        "abc" -> w2;
    }

    @strand{thread:"any"}
    worker w2 returns  map<string> {

        map<string> m = <- { w1 };
        io:print("m: ", m);
        return m;
    }

     map<string> x = wait w2;
     return x;
}

public function multipleStringMap() returns map<string> {

    @strand{thread:"any"}
    worker w1 {
        "100" -> w2;
    }

    @strand{thread:"any"}
    worker w2 returns map<string> {
        map<string> m = <- { w1, w3, w4 };
        io:println("m: ", m);
        return m;
    }

    @strand{thread:"any"}
    worker w3 {
        "200" -> w2;
    }

    @strand{thread:"any"}
    worker w4 {
        "300" -> w2;
    }

    map<string> i = wait w2;
    return i;
}

public function singleIntMap() returns map<int> {

    @strand{thread:"any"}
    worker w1 {
        150 -> w2;
    }

    @strand{thread:"any"}
    worker w2 returns map<int> {
        map<int> m = <- { w1 };
        io:println("m: ", m);
        return m;
    }

    map<int> i = wait w2;
    return i;
}

public function multipleIntMap() returns map<int> {

    @strand{thread:"any"}
    worker w1 {
        50 -> w2;
    }

    @strand{thread:"any"}
    worker w2 returns map<int> {
        map<int> m = <- { w1, w3, a:w4, w5 };
        io:println("m: ", m);
        return m;
    }

    @strand{thread:"any"}
    worker w3 {
        250 -> w2;
    }

    @strand{thread:"any"}
    worker w4 {
        350 -> w2;
    }

    @strand{thread:"any"}
    worker w5 {
        500 -> w2;
    }

    map<int> i = wait w2;
    return i;
}

public function mixedTypesMap() returns map<int|string> {



    @strand{thread:"any"}
    worker w3 {
        "350" -> w2;
        200 -> w2;
    }

    @strand{thread:"any"}
    worker w1 {
        100 -> w2;
    }

    @strand{thread:"any"}
    worker w2 returns map<int|string> {
        map<int|string> m = <- { c:w1, d:w3 };
        int x = <- w3;
        io:println("x is: ", x);
        io:println("m: ", m);
        return m;
    }

    map<int|string> i = wait w2;
    return i;
}