public type SomeConfiguration record {
    int numVal;
    string textVal;
    boolean conditionVal;
    record {   int nestNumVal;
        string nextTextVal;
    }   recordVal;
};

public annotation SomeConfiguration ConfigAnnotation on function;

public type OtherConfiguration record {
    int i;
};

public annotation OtherConfiguration ObjMethodAnnot on function;

public class MyClass {
    public function foo(int i) returns string => i.toString();

    @ObjMethodAnnot {i: 102}
    public function bar(int i) returns string => i.toString();
}
