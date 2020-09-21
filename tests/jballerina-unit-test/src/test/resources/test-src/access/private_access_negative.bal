class Foo {
    public string s = "";
}

type Bar record {
    string s = "";
};

type Baz 1|2;

public const Baz BAZ = 1; //:11:1:: 'Baz'

object {    // No errors due to anon types.
             public string s = "";
             public Baz z = 1;
             public Foo foo1 = new;

         } anonObj = new;

public class ParentFoo {

    public int i = 0;
    public ChildFoo c = new("");    // :23:5:: 'ChildFoo'
    public Foo f = new;             // :24:5:: 'Foo'
    public Baz z = 1;               // :25:5:: 'Baz'


    public function init (int i, ChildFoo  c){    // :28:5:: 'ChildFoo'
        self.i = i;
        self.c = c;
    }
}

class ChildFoo {

    private string name = "";


    function init(string name) {
        self.name = name;
    }
}
// :44:1:: 'Bar', :44:55:: 'Foo', :44:76:: 'Foo'
public function fooFunc2 (string s, Bar r, int i = 4, Foo... fArg) returns Foo {
    Foo f = new ();
    return f;
}
// :49:1:: 'Bar', :49:55:: 'Foo', :49:76:: 'Foo'
public function fooFunc3 (string s, Bar r, int i = 4, Foo... fArg) returns [Foo, string] {
    Foo f = new ();
    return [f, "G"];
}

public function fooFunc1 (Foo fArg) { // :54:1:: 'Foo'
    Foo fooVar = fArg;
}


public function BazFunc (Foo... fArg) returns (Baz) { // :59:26:: 'Foo', :59:48:: 'Baz'
    Baz z = 1;
    return z;
}
// TODO: Fix me. This is a bug.
public function test1(object {
                                          public string s = "";
                                          public Baz z = 1;
                                          public Foo foo1 = new;

                                      } anonObj1) returns string {
    return "K";
}
// TODO: Fix me. This is a bug.
public function test2() returns object {

                                            public string s = "";
                                            public Baz z = 1;
                                            public Foo foo1 = new;

                                        }{
    object {

        public string s = "";
        public Baz z = 1;
        public Foo foo1 = new;

    } m = new;

    return m;
}
// TODO: Fix me. This is a bug.
function test3() returns string {
    object {

        public string s = "";
        public Baz z = 1;
        public Foo foo1 = new;

    } m = new;

    return m.s;
}
// TODO: Fix me. This is a bug.
function test4() returns string {
    object {

        public string s = "";
        public Baz z = 1;
        public Foo foo1 = new;

    } m = new;

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
    ChildFoo c = new(""); // :133:5:: 'ChildFoo'
    ChildRecord r = {}; // :134:5:: 'ChildRecord'
    Foo f = new;    // :135:5:: 'Foo'
    Baz z = 1;  // :136:5:: 'Baz'
};

type ChildRecord record {
    string name = "";
};

function test5() returns string {
    record {
         string s = "";
         Baz z = 1;
         Foo foo1 = new;        // No error
         BarRecord br = {};     // No error
    } m = {};

    return m.s;
}
// :154:1:: 'Baz', :154:1:: 'Foo', :154:1:: 'BarRecord'
public function test6(record {
                          string s = "";
                          Baz z = 1;
                          Foo foo1 = new;
                          BarRecord br = {};
                      } anonRecord1) returns string {
    return "K";
}
// 163:33:: 'Baz', :163:33:: 'Foo', :163:33:: 'BarRecord'
public function test7() returns record {
                                    string s = "";
                                    Baz z = 1;
                                    Foo foo1 = new;
                                    BarRecord br = {};
                                }{
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

