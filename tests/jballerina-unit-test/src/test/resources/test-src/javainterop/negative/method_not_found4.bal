import ballerina/java;

public function interopWithAnyReturn() returns boolean {
    var a = acceptIntAnyReturn(1);
    if (!(a is int)) {
        return false;
    }
    var b = acceptIntAnyReturn(2);
    if (!(b is string)) {
        return false;
    }
    var c = acceptIntAnyReturn(3);
    if (!(c is float)) {
        return false;
    }
    var d = acceptIntAnyReturn(-1);
    if (!(d is boolean)) {
        return false;
    }
    return true;
}

public function acceptIntAnyReturn(int s) returns any = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods",
    name:"acceptIntAndUnionReturn"
} external;
