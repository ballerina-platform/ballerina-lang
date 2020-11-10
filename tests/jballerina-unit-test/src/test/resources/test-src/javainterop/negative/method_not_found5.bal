import ballerina/java;

public function interopWithAnydataReturn() returns boolean {
    var a = acceptIntAnydataReturn(1);
    if (!(a is int)) {
        return false;
    }
    var b = acceptIntAnydataReturn(2);
    if (!(b is string)) {
        return false;
    }
    var c = acceptIntAnydataReturn(3);
    if (!(c is float)) {
        return false;
    }
    var d = acceptIntAnydataReturn(-1);
    if (!(d is boolean)) {
        return false;
    }
    return true;
}

public function acceptIntAnydataReturn(int s) returns anydata= @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods",
    name:"acceptIntStringAndUnionReturn"
} external;
