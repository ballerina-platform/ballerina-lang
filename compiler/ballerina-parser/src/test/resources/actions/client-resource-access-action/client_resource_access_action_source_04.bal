function foo() {
    a->games.name;
    a->[pathSegment](arg1, arg2);
    a->[pathSegment].name;
    a->[pathSegment].name(arg1, arg2);
    a->games/[game](arg1, arg2);
    a->games/[game].name(arg1, arg2);
    a->[...restSegment]();
    a->games/[...restSegment].get;
    a->games/[game]/[...restSegment];
    a->.post;
    a->.post(arg);
    a->.();
    a->.(arg, arg2);
    a->.(paramName = arg);
}
