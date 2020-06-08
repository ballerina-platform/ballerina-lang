//------------ Testing a function with all types of parameters ---------

public function functionWithAllTypesParams(int a, float b, public string c = "John", public int d = 5, public string e = "Doe")
        returns [int, float, string, int, string, int[]] {
    return [a, b, c, d, e, []];
}

public function getIntArray() returns int[] {
    return [1,2,3,4];
}


//------------- Testing a function having required and rest parameters --------

public function functionWithoutRestParams(public int a, public float b, public string c = "John", public int d = 5, public string e = "Doe") returns
            [int, float, string, int, string] {
    return [a, b, c, d, e];
}


//------------- Testing a function having only named parameters --------

public function functionWithOnlyNamedParams(public int a=5, public float b=6.0, public string c = "John", public int d = 7, public string e = "Doe")
                                                                                                    returns [int, float, string, int, string] {
    return [a, b, c, d, e];
}

//------------- Testing a function having only rest parameters --------

public function functionWithOnlyRestParam(int... z) returns int[] {
    return z;
}


//------------- Testing a function with rest parameter of any type --------

public function functionAnyRestParam(any... z) returns any[] {
    return z;
}


// ------------------- Test function signature with union types for default parameter

public function funcWithUnionTypedDefaultParam(string|int? s = "John") returns string|int? {
    return s;
}


// ------------------- Test function signature with null as default parameter value

public function funcWithNilDefaultParamExpr_1(string? s = ()) returns string? {
    return s;
}

public type Student record {
    int a;
};

public function funcWithNilDefaultParamExpr_2(Student? s = ()) returns Student? {
    return s;
}

// ------------------- Test function signature with rest param
public function bar(public int i, boolean... b) returns int {
    return i;
}

public function baz(string s, public float f = 2.0, boolean... b) {
}

public function bazTwo(int i, boolean... b) returns [int, boolean[]] {
    return [i, checkpanic b.cloneWithType(boolean[])];
}

public function barTwo(int i, string s = "hello", string... t) returns [int, string, string[]] {
    return [i, s, checkpanic t.cloneWithType(string[])];
}


// ------------------- Test function signature with 'never' return type ----------------
public function sigma() returns never {
    int age = 1;
}

// ------------------- Test function signature for attached functions ------------------

public type Employee object {

    public string name;
    public int salary;

    public function __init (string name = "supun", int salary = 100) {
        self.name = name;
        self.salary = salary;
    }

    public function getSalary (string n, public int b = 0) returns int {
        return self.salary + b;
    }
};


public type Person object {
    public int age = 0;

    public function test1(public int a = 77, string n = "hello") returns [int, string] {
        string val = n + " world";
        int intVal = a + 10;
        return [intVal, val];
    }

    public function test2(public int a = 89, string n = "hello") returns [int, string] {
        string val = n + " world";
        int intVal = a + 10;
        return [intVal, val];
    }
};

public type Foo client object {
    public function bar(public int i, boolean... b) returns int {
        return i;
    }

    public remote function baz(string s, public float f = 2.0, boolean... b) {
    }
};

public type FooTwo client object {
    public function baz(int i, boolean... b) returns [int, boolean[]] {
        return [i, checkpanic b.cloneWithType(boolean[])];
    }

    public remote function bar(int i, string s = "hello", string... t) returns [int, string, string[]] {
        return [i, s, checkpanic t.cloneWithType(string[])];
    }
};
