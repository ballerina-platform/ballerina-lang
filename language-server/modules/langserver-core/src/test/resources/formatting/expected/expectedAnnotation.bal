public type SomeConfiguration record {
    int numVal;
    string textVal;
    boolean conditionVal;
    record {
        int nestNumVal;
        string nextTextVal;
    } recordVal;
};

public annotation SomeConfiguration ConfigAnnotation on service, function;

const boolean tet1 = true;

public const annotation tet1 ConfigAnnotation10 on source service, source function;

public type SomeConfiguration1 record {
    int numVal;
    string textVal;
    boolean conditionVal;
    record {
        int nestNumVal;
        string nextTextVal;
    } recordVal;
};

annotation SomeConfiguration1 ConfigAnnotation1 on service, object function;

public type SomeConfiguration2 record {
    int numVal;
    string textVal;
    boolean conditionVal;
    record {
        int nestNumVal;
        string nextTextVal;
    } recordVal;
};

const boolean tet = true;

const annotation tet ConfigAnnotation2 on source object function, source service;

public type SomeConfiguration3 record {
    int numVal;
    string textVal;
    boolean conditionVal;
    record {
        int nestNumVal;
        string nextTextVal;
    } recordVal;
};

public annotation SomeConfiguration3 ConfigAnnotation3;

public
annotation
ConfigAnnotation4
on
service
,
resource
function
;

annotation ConfigAnnotation5 on service, function;

public annotation ConfigAnnotation6 on type;

annotation ConfigAnnotation7 on class;

annotation ConfigAnnotation8;

public type SomeConfiguration4 record {
    int numVal;
    string textVal;
    boolean conditionVal;
    record {
        int nestNumVal;
        string nextTextVal;
    } recordVal;
};

annotation
SomeConfiguration4
ConfigAnnotation9;

type Annot record {
    string foo;
    int bar?;
};

public annotation Annot v1 on type;

annotation Annot[] v2 on class;
public annotation Annot v3 on function;
annotation map<int> v4 on object function;
public annotation map<string> v5 on resource function;
annotation Annot v6 on parameter;
public annotation v7 on return;
annotation Annot[] v8 on service;

string strValue = "v1 value";

@v1 {
    foo: strValue,
    bar: 1
}
public type T1 record {
    string name;
};

T1 a = {name: "John"};

function testTypeAnnotAccess1() returns boolean {
    typedesc t = typeof a;
    Annot? annot = t.@pkg1:v1;
    return annot is Annot && annot.foo == strValue && annot.bar == 1;
}

function testTypeAnnotAccess2() returns boolean {
    T1 b = {name: "John"};
    typedesc t = typeof b;
    Annot[]? annot =
        t
        .@
        v2
    ;
    return annot is ();
}

@v1 {
    foo: strValue
}
@v2 {
    foo: "v2 value 1"
}
@v2 {
    foo: "v2 value 2"
}
type T2 object {
    string name = "ballerina";

    @v3 {
        foo: "v31 value"
    }
    @v4 {
        foo: 41
    }
    public function setName(@v6 {foo: "v61 value required"} string name,
        @v6 {foo: "v61 value defaultable"} int id = 0,
        @v6 {foo: "v61 value rest"} string... others) returns @v7 () {
        self.name = name;
    }

    @v3 {
        foo: "v32 value"
    }
    @v4 {
        val: 42
    }
    public function getLetter(@v6 {foo: "v62 value"} int intVal) returns @v7 string {
        // TODO: FIX
        // return self.name.substring(intVal, intVal + 1);
        return self.name;
    }
};

function testObjectTypeAnnotAccess1() returns boolean {
    T2 c = new;
    typedesc t = typeof c;
    Annot? annot = t.@v1;
    return annot is Annot && annot.foo == strValue;
}

T2 d = new;

function testObjectTypeAnnotAccess2() returns boolean {
    typedesc t = typeof d;
    Annot[]? annots = t.@v2;
    if (annots is Annot[]) {
        if (annots.length() != 2) {
            return false;
        }
        Annot annot1 = annots[0];
        Annot annot2 = annots[1];
        return annot1.foo == "v2 value 1" && annot2.foo == "v2 value 2";
    }
    return false;
}

function testObjectTypeAnnotAccess3() returns boolean {
    T2 e = new;
    typedesc t = typeof e;
    Annot? annot =
        t
        .@
        v3
    ;
    return annot is ();
}

listener Listener lis = new;

string v8a = "v8a";

@v8 {
    foo: v8a
}
@v8 {foo: "v8b"}
service ser on lis {

    @v3 {
        foo: "v34"
    }
    @v5 {val: "54"}
    resource function res(@v6 {foo: "v64"} int intVal) returns @v7 string {
        return "";
    }
}

service serTwo = @v8 {
        foo: "v82"
    } service {

        @v5 {
            val: "542"
        }
        resource function res(@v6 {
                foo: "v642"
            } int intVal) returns @v7 int {
            return 1;
        }
    };

service serTwo2 = @v8 {
        foo: "v82"
    } service {

        @v5 {
            val: "542"
        }
        resource function res(@v6 {
                foo: "v642"
            }
            int intVal) returns
            @v7 {
                bar: "vfg"
            }
            int {
            return 1;
        }
    };

service serTwo3 = @v8 {
        foo: "v82"
    } service {

        @v5 {
            val: "542"
        }
        resource function res(@v6 {
                foo: "v642"
            } record {
                int b = 0;
            } intVal) returns
            @v7 {
                bar: "vfg"
            } record {
                int a = 0;
            } {
            return {};
        }
    };

service serTwo4 = @v8 {
        foo: "v82"
    } service {

        @v5 {
            val: "542"
        }
        resource function res(@v6 {
                foo: "v642"
            }
            record {
                int b = 0;
            } intVal) returns
            @v7 {
                bar: "vfg"
            }
            record {
                int a = 0;
            } {
            return {};
        }
    };

service serTwo5 = @v8 {
        foo: "v82"
    } service {

        @v5 {
            val: "542"
        }
        resource function res(@v6 {
                foo: "v642"
            }
            object {
                int b = 0;
            } intVal) returns
            @v7 {
                bar: "vfg"
            }
            object {
                int a = 0;
            } {
            return new;
        }
    };

service serTwo6 = @v8 {
        foo: "v82"
    } service {

        @v5 {
            val: "542"
        }
        resource function res(@v6 {
                foo: "v642"
            } object {
                int b = 0;
            } intVal) returns
            @v7 {
                bar: "vfg"
            } object {
                int a = 0;
            } {
            return new;
        }
    };
