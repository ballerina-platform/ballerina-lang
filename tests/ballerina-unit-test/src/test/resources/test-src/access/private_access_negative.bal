type Foo object {

    public string s = "";

};

type Bar record {
    string s = "";
};

type Baz 1|2;

public Foo foo = new ();

public Bar b = {s:"K"};

public Baz baz = 1;

object {
             public string s = "";
             public Baz z = 1;
             public Foo foo1 = new;

         } anonObj = new;

public type ParentFoo object {

    public int i = 0;
    public ChildFoo c = new("");
    public Foo f = new;
    public Baz z = 1;


    public new (i, c){}
};

type ChildFoo object {

    private string name = "";


    new (name) {}
};

public function fooFunc2 (string s, int i = 4, Bar r,  Foo... fArg) returns Foo {
    Foo f = new ();
    return f;
}

public function fooFunc3 (string s, int i = 4, Bar r,  Foo... fArg) returns (Foo, string) {
    Foo f = new ();
    return (f, "G");
}

public function fooFunc1 (Foo fArg) {
    Foo fooVar = fArg;
}


public function BazFunc (Foo... fArg) returns (Baz) {
    Baz z = 1;
    return z;
}

public function test1(object {
                                          public string s = "";
                                          public Baz z = 1;
                                          public Foo foo1 = new;

                                      } anonObj1) returns string {
    return "K";
}

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

function test3() returns string {
    object {

        public string s = "";
        public Baz z = 1;
        public Foo foo1 = new;

    } m = new;

    return m.s;
}

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
    ChildFoo c = new("");
    ChildRecord r = {};
    Foo f = new;
    Baz z = 1;
};

type ChildRecord record {
    string name = "";
};

function test5() returns string {
    record {
         string s = "";
         Baz z = 1;
         Foo foo1 = new;
         BarRecord br = {};
    } m = {};

    return m.s;
}

public function test6(record {
                          string s = "";
                          Baz z = 1;
                          Foo foo1 = new;
                          BarRecord br = {};
                      } anonRecord1) returns string {
    return "K";
}

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

type FooTypeObj object {

    public string s = "";

};

type BarTypeRecord record {
    string s = "";
};

type BazTypeFinite 1|2;

public type TypeA FooTypeObj;

public type TypeB BarTypeRecord;

public type TypeC BazTypeFinite;


public type FooTypePublicObj object {

    public string s = "";

};

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

