function foo() {
    a->/games.name;
    a->/[pathSegment](arg1, arg2);
    a->/[pathSegment].name;
    a->/[pathSegment].name(arg1, arg2);
    a->/games/[game](arg1, arg2);
    a->/games/[game].name(arg1, arg2);
    a->/[...restSegment]();
    a->/games/[...restSegment].get;
    a->/games/[game]/[...restSegment].post(paramName1 = arg1, paramName2 = arg2);
    a->/games/[game]/[...restSegment].post(paramName1 = arg1, paramName2 = arg2, ...restArg);
    a->/games/[game]/[...restSegment](paramName1 = arg1, paramName2 = arg2, ...restArg);

    a()->/games.name;
    a[2]->/games.name;
    a:b->/games.name;
}
