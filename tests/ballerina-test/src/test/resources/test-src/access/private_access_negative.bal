type Foo object {
    public {
        string s;
    }
};

type Bar {
    string s;
};

type Baz 1|2;

public Foo f = new ();

public Bar b = {s:"K"};

public Baz z = 1;

object {public {
                 string s;
                 Baz z = 1;
                 Foo foo;
             }
         } anonObj;

public type ParentFoo object {
    public {
        int i;
        ChildFoo c;
        Foo f;
        Baz z = 1;
    }

    public new (i, c){}
};

type ChildFoo object {
    private {
        string name;
    }

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

public function test1(object {public {
                                              string s;
                                              Baz z = 1;
                                              Foo foo;
                                          }
                                      } anonObj) returns string {
    return "K";
}

public function test2() returns object {
                                            public {
                                                string s;
                                                Baz z = 1;
                                                Foo foo;
                                            }
                                        }{
    object {
        public {
            string s;
            Baz z = 1;
            Foo foo;
        }
    } m = new;

    return m;
}

function test3() returns string {
    object {
        public {
            string s;
            Baz z = 1;
            Foo foo;
        }
    } m = new;

    return m.s;
}

function test4() returns string {
    object {
        public {
            string s;
            Baz z = 1;
            Foo foo;
        }
    } m = new;

    return m.s;
}

