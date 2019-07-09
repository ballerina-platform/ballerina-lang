public type SomeConfiguration record {
    int numVal;
    string textVal;
    boolean conditionVal;
    record {   int nestNumVal;
        string nextTextVal;
    }   recordVal;
};

public annotation SomeConfiguration ConfigAnnotation on function;
