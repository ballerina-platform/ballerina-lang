function funcWithRestArgs (int a, int b, int... c) (int, int, int[]) {
    return a, b, c;
}

function test1()(int, int, int[]){
    function (int, int, int[]) returns (int, int, int[]) func = funcWithRestArgs;
    return func(1, 2, 3, 4);
}
