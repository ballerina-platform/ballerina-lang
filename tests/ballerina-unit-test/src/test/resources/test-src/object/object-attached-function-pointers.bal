type Person object {
    public int age = 3;
    public string name = "sample name";

    private int year = 5;
    private string month = "february";

    function attachedFn1(int a, float b) returns (int) {
        return 7 + a + <int>b;
    }

    function attachedFn2() returns (function (int, float) returns (int)) {
        var foo = function (int a, float b) returns (int) {
            return 7 + a + <int>b;
        };
        return foo;
    }

    function attachedFn3(int a, float b) returns (int);

    function attachedFn4() returns (function (int, float) returns (int));

    function attachedFn5(int a, float b) returns (function (float) returns ((function (boolean) returns (int)))) {
        var fooOut = function (float f) returns (function (boolean) returns (int)) {
            var fooIn = function (boolean boo) returns (int) {
                return 7 + a + <int>b + <int>f;
            };
            return fooIn;
        };
        return fooOut;
    }

    function attachedFn6(int a, float b) returns (int) {
        var foo = self.attachedFn3;
        return a + <int>b + foo(43, 10.8);
    }

    function attachedFn7(int a, float b) returns (int) {
        var foo = self.attachedFn3;
        return a + <int>b + foo(43, 10.8);
    }
};


function Person.attachedFn3(int a, float b) returns (int) {
    return a + <int>b;
}

function Person.attachedFn4() returns (function (int, float) returns (int)) {
    var foo = function (int a, float b) returns (int) {
        return 7 + a + <int>b;
    };
    return foo;
}

function test1() returns (int) {
    Person p = new;
    var foo = p.attachedFn1;
    return foo(43, 10.8);
}

function test2() returns (int) {
    Person p = new;
    var foo = p.attachedFn2;
    var bar = foo();
    return bar(43, 10.8);
}

function test3() returns (int) {
    Person p = new;
    var foo = p.attachedFn3;
    return foo(43, 10.8);
}

function test4() returns (int) {
    Person p = new;
    var foo = p.attachedFn4;
    var bar = foo();
    return bar(43, 10.8);
}

function test5() returns (int) {
    Person p = new;
    var foo = p.attachedFn5;
    var bar = foo(43, 10.8);
    var baz = bar(5.8);
    return baz(true);
}

function test6() returns (int) {
    Person p = new;
    var foo = p.attachedFn6;
    return foo(43, 10.8);
}

function test7() returns (int) {
    Person p = new;
    var foo = p.attachedFn7;
    return foo(43, 10.8);
}

public type FooObj object {
    private (function(string[]) returns string) fp1;
    private (function(int[]) returns int) fp2;

    new (fp1, fp2){
        string[] s = ["abc", "afg"];
        int[] i = [1,2,3,4,5];
        string a = fp1(s);
        int b = fp2(i);
    }

    public function processStrArray(string[] vals) returns string{
        return self.fp1(vals);
    }

    public function processIntArray(int[] vals) returns int{
        return self.fp2(vals);
    }
};


function test8() returns (string, int) {
    string[] s = ["B", "A"];
    int[] i = [1,2,3,4,5];
    var foo = function (string[] v) returns string { return v[1];};
    var bar = function (int[] v) returns int { return v[1];};
    FooObj fooObj = new (foo, bar);
    _ = fooObj.processStrArray(s);
    var x = fooObj.processStrArray;
    string q = x(s);
    _ = fooObj.processIntArray(i);
    var y = fooObj.processIntArray;
    int r = y(i);

    return(q, r);
}

function test9() returns string {
    string[] vals = ["finally", "ballerina"];
    O1 o1 = new (function (string[] v) returns string { return vals[0]; });
    O2 o2 = new(o1.process);
    O3 o3 = new(o2.process);
    O4 o4 = new(o3.process);
    O5 o5 = new(o4.process);
    return o5.process(vals);
}

public type O1 object {
    private (function(string[]) returns string) fpO1;

    new (fpO1){
    }

    public function process(string[] vals) returns string{
        return self.fpO1(vals);
    }
};

public type O2 object {
    private (function(string[]) returns string) fpO2;

    new (fpO2){}

    public function process(string[] vals) returns string{
        return self.fpO2(vals);
    }
};

public type O3 object {
    private (function(string[]) returns string) fpO3;

    new (fpO3){}

    public function process(string[] vals) returns string{
        return self.fpO3(vals);
    }
};

public type O4 object {
    private (function(string[]) returns string) fpO4;

    new (fpO4){}

    public function process(string[] vals) returns string{
        return self.fpO4(vals);
    }
};

public type O5 object {
    private (function(string[]) returns string) fpO5;

    new (fpO5){}

    public function process(string[] vals) returns string{
        return self.fpO5(vals);
    }
};
