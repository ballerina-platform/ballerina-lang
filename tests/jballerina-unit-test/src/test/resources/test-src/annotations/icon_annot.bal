@icon { path: "/fooIconPath.icon" }
function foo(int i, string k) returns int {
    return i;
}

@icon { path: "/barIconPath.icon" }
type Bar object {
    @icon { path: "/kMemberFuncIconPath.icon" }
    function k() {

    }
};
