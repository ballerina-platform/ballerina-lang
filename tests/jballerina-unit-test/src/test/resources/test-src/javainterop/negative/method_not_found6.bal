import ballerina/java;

function getFuture(typedesc<anydata> td, future<anydata> f) returns future<anydata> = @java:Method {
    class: "org.ballerinalang.nativeimpl.jvm.tests.StaticMethods",
    name: "getFuture",
    paramTypes: ["org.ballerinalang.jvm.values.api.BTypedesc"]
} external;

function getTypeDesc(typedesc<anydata> td, future<anydata> f) returns future<anydata> = @java:Method {
    class: "org.ballerinalang.nativeimpl.jvm.tests.StaticMethods",
    name: "getTypeDesc",
    paramTypes: ["org.ballerinalang.jvm.values.api.BFuture"]
} external;

