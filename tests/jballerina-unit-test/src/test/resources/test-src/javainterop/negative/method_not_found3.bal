import ballerina/java;

public type Employee record {
    string name = "";
};


public function interopWithRecordReturn() returns boolean {
    string newVal = "new name";
    Employee p = {name:"name begin"};
    Employee a = acceptRecordAndRecordReturn(p, 45,  newVal);
    if (a.name != newVal) {
        return false;
    }
    if (p.name != newVal) {
        return false;
    }
    return true;
}

public function acceptRecordAndRecordReturn(Employee e, int a,  string newVal) returns Employee = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;
