public type SomeConfiguration record {
    int numVal;
    string textVal;
    boolean conditionVal;
    record {
        int nestNumVal;
        string nextTextVal;
    } recordVal;
};

public annotation<function, service> ConfigAnnotation SomeConfiguration;

public type SomeConfiguration1 record {
    int numVal;
    string textVal;
    boolean conditionVal;
    record {
        int nestNumVal;
        string nextTextVal;
    } recordVal;
};

annotation<function, service> ConfigAnnotation1 SomeConfiguration1;

public type SomeConfiguration2 record {
    int numVal;
    string textVal;
    boolean conditionVal;
    record {
        int nestNumVal;
        string nextTextVal;
    } recordVal;
};

annotation<function> ConfigAnnotation2 SomeConfiguration2;

public type SomeConfiguration3 record {
    int numVal;
    string textVal;
    boolean conditionVal;
    record {
        int nestNumVal;
        string nextTextVal;
    } recordVal;
};

public annotation ConfigAnnotation3 SomeConfiguration3;

public annotation<function, service> ConfigAnnotation4;

annotation<function, service> ConfigAnnotation5;

public annotation<function> ConfigAnnotation6;

annotation<function> ConfigAnnotation7;

annotation ConfigAnnotation7;

public type SomeConfiguration4 record {
    int numVal;
    string textVal;
    boolean conditionVal;
    record {
        int nestNumVal;
        string nextTextVal;
    } recordVal;
};

annotation ConfigAnnotation8 SomeConfiguration4;
