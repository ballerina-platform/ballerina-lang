type Person record {
    string name;
};

type Employee record {
    string name;
    boolean intern;
};

Person person1 = {name: "Person 1"};
Employee employee1 = {name: "Employee 1", intern: true};
Employee employee2 = {name: "Employee 2", intern: false};

function tupleDestructureTest1() returns [int, string] {
    [int, string] x = [1, "a"];
    int a;
    string b;
    [a, b] = x;
    return [a, b];
}

function tupleDestructureTest2() returns [int, int[]] {
    [int, int, int, int] x = [1, 2, 3, 4];
    int a;
    int[] b;
    [a, ...b] = x;
    return [a, b];
}

function tupleDestructureTest3() returns [int, int[]] {
    [int, int, int...] x = [1, 2, 3, 4];
    int a;
    int[] b;
    [
        a
        ,
        ...
        b
    ]
    =
        x
    ;
    return [a, b];
}

function tupleDestructureTest4() returns int[] {
    [int...] x = [1, 2, 3, 4];
    int[] a;
    [...a] = x;
    return a;
}

function tupleDestructureTest5() returns [int, int[]] {
    [int...] x = [1, 2, 3, 4];
    int a;
    int[] b;
    [a, ...b] = x;
    return [a, b];
}

function tupleDestructureTest6() returns [int, string[]] {
    [int, string, string] x = [1, "a", "b"];
    int a;
    string[] b;
    [a, ...b] = x;
    return [a, b];
}

function tupleDestructureTest7() returns [int, string[]] {
    [int, string...] x = [1, "a", "b"];
    int a;
    string[] b;
    [a, ...b] = x;
    return [a, b];
}

function tupleDestructureTest8() returns [int, string, string[]] {
    [int, string...] x = [1, "a", "b"];
    int a;
    string b;
    string[] c;
    [a, b, ...c] = x;
    return [a, b, c];
}
