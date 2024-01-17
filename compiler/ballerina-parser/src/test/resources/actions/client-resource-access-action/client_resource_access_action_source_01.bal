function foo() {
    a->/;

    a->/games;
    a->/[pathSegment];
    a->/games/carrom;
    a->/games/[game];
    a->/games/[game]/details;
    a->/[...restSegment];
    a->/games/[...restSegment];
    a->/games/[game]/[...restSegment];

    a->/.get;
    a->/();
    a->/(arg1, arg2);
    a->/(paramName1 = arg1, paramName2 = arg2);
    a->/(arg1, paramName2 = arg2);
    a->/.post();
    a->/.post(arg1, arg2);
    a->/.post(paramName1 = arg1, paramName2 = arg2);
    a->/.post(arg1, paramName2 = arg2, ...restArg);
}
