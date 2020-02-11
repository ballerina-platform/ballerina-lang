[int, string] theft = [0, ""];

function name8() returns [int, string] {
    [float, json] localVar1;
    [int, float] localVar2 = [0, 0.0];
    int aint;
    string astr;
    [aint, astr] = theft;
    var [intd, stringd] = theft;
    return [0, ""];
}

function name1() returns [int, [string, int, float]] {
    return [1, ["ABC", 42, 0.023]];
}

function name2() returns [int, [string, int, float]] {
    return
    [
        1
        ,
        [
            "ABC"
            ,
            42
            ,
            0.023
        ]
    ]
    ;
}

function searchPeople() returns ([string, int, float]) {
    return (["", 1, 1.0]);
}

function testArrayToTupleAssignment3() returns [string, string[]] {
    string[3] x = ["a", "b", "c"];
    [string, string...][i, ...j] = x;
    return [i, j];
}

function testArrayToTupleAssignment4() returns [string, string[]] {
    string[3] x = ["a", "b", "c"];
    [string, string...][i,
    ...
    j] = x;
    return [i, j];
}
