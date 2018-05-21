//------------ Testing a function with all types of parameters ---------

public function functionWithAllTypesParams(int a, float b, string c = "John", int d = 5, string e = "Doe", int... z)
        returns (int, float, string, int, string, int[]) {
    return (a, b, c, d, e, z);
}

public function getIntArray() returns (int[]) {
    return [1,2,3,4];
}


//------------- Testing a function having required and rest parameters --------

public function functionWithoutRestParams(int a, float b, string c = "John", int d = 5, string e = "Doe") returns
            (int, float, string, int, string) {
    return (a, b, c, d, e);
}


//------------- Testing a function having only named parameters --------

public function functionWithOnlyNamedParams(int a=5, float b=6.0, string c = "John", int d = 7, string e = "Doe")
                                                                                                    returns (int, float, string, int, string) {
    return (a, b, c, d, e);
}

//------------- Testing a function having only rest parameters --------

public function functionWithOnlyRestParam(int... z) returns (int[]) {
    return z;
}


//------------- Testing a function with rest parameter of any type --------

public function functionAnyRestParam(any... z) returns (any[]) {
    return z;
}


// ------------------- Test function signature with union types for default parameter

public function funcWithUnionTypedDefaultParam(string|int? s = "John") returns string|int? {
    return s;
}


// ------------------- Test function signature with null as default parameter value

public function funcWithNilDefaultParamExpr_1(string? s = null) returns string? {
    return s;
}

// type Student {
//     int a;
// };

// function funcWithNilDefaultParamExpr_2(Student? s = ()) returns Student? {
//     return s;
// }


// ------------------- Test function signature for attached functions ------------------

// public type Employee object {
//
//     public {
//         string name;
//         int salary;
//     }
//
//     new (name = "supun", salary = 100) {
//     }
//
//     public function getSalary (string name, int bonus = 0) returns int {
//         return salary + bonus;
//     }
// };


// type Person object {
//     public {
//         int age,
//     }
//
//     function test1(int age = 77, string name = "inner default") returns (int, string);

//     function test2(int age = 89, string name = "hello") returns (int, string) {
//         string val = name + " world";
//         int intVal = age + 10;
//         return (intVal, val);
//     }
// };

// function Person::test1(int age = 77, string name = "hello") returns (int, string) {
//     string val = name + " world";
//     int intVal = age + 10;
//     return (intVal, val);
// }
