class Foo {
    public string s = "";
}

type Bar record {
    string s = "";
};

type Baz 1|2;

public const Baz BAZ = 1; //:11:1:: 'Baz'

object { // No errors due to anon types.
    public string s;
    public Baz z;
    public Foo foo1;

} anonObj = object {public string s = ""; public Baz z = 1; public Foo foo1 = new;};

public class ParentFoo {

    public int i = 0;
    public ChildFoo c = new (""); // :23:5:: 'ChildFoo'
    public Foo f = new; // :24:5:: 'Foo'
    public Baz z = 1; // :25:5:: 'Baz'

    public function init(int i, ChildFoo c) { // :27:5:: 'ChildFoo'
        self.i = i;
        self.c = c;
    }
}

class ChildFoo {

    private string name = "";

    isolated function init(string name) {
        self.name = name;
    }
}

// :43:1:: 'Bar', :43:54:: 'Foo', :43:75:: 'Foo'
public function fooFunc2(string s, Bar r, int i = 4, Foo... fArg) returns Foo {
    Foo f = new ();
    return f;
}

// :49:1:: 'Bar', :49:54:: 'Foo', :49:75:: 'Foo'
public function fooFunc3(string s, Bar r, int i = 4, Foo... fArg) returns [Foo, string] {
    Foo f = new ();
    return [f, "G"];
}

public function fooFunc1(Foo fArg) { // :54:1:: 'Foo'
    Foo _ = fArg;
}

public function BazFunc(Foo... fArg) returns (Baz) { // :58:25:: 'Foo', :59:48:: 'Baz'
    Baz z = 1;
    return z;
}

public function test1(object {
            public string s;
            public Baz z;
            public Foo foo1;

        } anonObj1) returns string {
    return "K";
}

public function test10(object {
            string s;
            Baz z;
            Foo foo1;

        } anonObj1) returns string {
    return "K";
}

public function test2() returns object {

    public string s;
    public Baz z;
    public Foo foo1;

} {
    var m = object {

        public string s = "";
        public Baz z = 1;
        public Foo foo1 = new;

    };

    return m;
}


function test3() returns string {
    var m = object {

        public string s = "";
        public Baz z = 1;
        public Foo foo1 = new;

    };

    return m.s;
}


function test4() returns string {
    var m = object {

        public string s = "";
        public Baz z = 1;
        public Foo foo1 = new;

    };

    return m.s;
}

type FooRecord record {
    string s = "";
    BarRecord br = {};
};

type BarRecord record {
    string s = "";
};

record {
    string s = "";
    Baz z = 1;
    Foo foo1 = new;
    BarRecord br = {};
} anonRecord = {};

public type ParentRecord record {
    int i = 0;
    ChildFoo c = new (""); // :143:5:: 'ChildFoo'
    ChildRecord r = {}; // :144:5:: 'ChildRecord'
    Foo f = new; // :145:5:: 'Foo'
    Baz z = 1; // :146:5:: 'Baz'
};

type ChildRecord record {
    string name = "";
};

function test5() returns string {
    record {
        string s = "";
        Baz z = 1;
        Foo foo1 = new; // No error
        BarRecord br = {}; // No error
    } m = {};

    return m.s;
}

// :165:1:: 'Baz', :165:1:: 'Foo', :165:1:: 'BarRecord'
public function test6(record {
            string s = "";
            Baz z = 1;
            Foo foo1 = new;
            BarRecord br = {};
        } anonRecord1) returns string {
    return "K";
}

// 175:33:: 'Baz', :175:33:: 'Foo', :175:33:: 'BarRecord'
public function test7() returns record {
    string s = "";
    Baz z = 1;
    Foo foo1 = new;
    BarRecord br = {};
} {
    record {
        string s = "";
        Baz z = 1;
        Foo foo1 = new;
        BarRecord br = {};
    } m = {};

    return m;
}

class FooTypeObj {

    public string s = "";

}

type BarTypeRecord record {
    string s = "";
};

type BazTypeFinite 1|2;

public type TypeA FooTypeObj;

public type TypeB BarTypeRecord;

public type TypeC BazTypeFinite;

public class FooTypePublicObj {

    public string s = "";

}

public type BarTypePublicRecord record {
    string s = "";
};

public type BazTypePublicFinite 1|2;

public type TypeD FooTypePublicObj;

public type TypeE BarTypePublicRecord;

public type TypeF BazTypePublicFinite;

public type Person record {
    string name = "";
};

type TypeAliasOne Person;

type TypeAliasTwo TypeAliasOne;

public type TypeAliasThree TypeAliasTwo;

type A int;

type B A;

public type C B;

type Employee1 record {|
|};

type Employee2 record {|
    int x;
|};

public type Employee3 record {|
    string y;
|};

public Employee1|Employee2 emp1 = {};
public Employee1|Employee2|Employee3 emp2 = {};

public function test8() returns Employee1|Employee3 {
    return {};
}

client class Client {
    resource function 'select employees() returns stream<Employee1, error?>? {
        return;
    }
}

public function test9(record {
            string s = "";
            Employee1|Employee2|Employee3 x = {};
        } anonRecord1) returns string {
    return "K";
}

public object{Employee1 emp1;} obj1 = object {
    Employee1 emp1 = {};
};

public object {
    Employee1|Employee2|Employee3 emp;
    int age;
} obj2 = object {
    Employee1 emp = {};
    int age = 25;
};

public function test11() returns object {
    Employee1|Employee2|Employee3 emp;
    int age;
}? {
    return;
}
