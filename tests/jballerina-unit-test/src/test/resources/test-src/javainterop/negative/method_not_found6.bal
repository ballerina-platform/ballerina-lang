import ballerina/java;

function getFuture(typedesc<anydata> td, future<anydata> f) returns future<anydata> = @java:Method {
    class: "org.ballerinalang.nativeimpl.jvm.tests.StaticMethods",
    name: "getFuture",
    paramTypes: ["org.ballerinalang.jvm.values.api.BTypedesc"]
} external;

function getTypeDesc(typedesc<anydata> td, future<anydata> f) returns typedesc<anydata> = @java:Method {
    class: "org.ballerinalang.nativeimpl.jvm.tests.StaticMethods",
    name: "getTypeDesc",
    paramTypes: ["org.ballerinalang.jvm.values.api.BFuture"]
} external;

function getFutureOnly(future<anydata> f) returns future<anydata> = @java:Method {
    class: "org.ballerinalang.nativeimpl.jvm.tests.StaticMethods",
    name: "getFutureOnly",
    paramTypes: ["org.ballerinalang.jvm.values.api.BFuture", "org.ballerinalang.jvm.values.api.BTypedesc"]
} external;

function getTypeDescOnly(typedesc<anydata> td) returns typedesc<anydata> = @java:Method {
    class: "org.ballerinalang.nativeimpl.jvm.tests.StaticMethods",
    name: "getTypeDescOnly",
    paramTypes: ["org.ballerinalang.jvm.values.api.BTypedesc", "org.ballerinalang.jvm.values.api.BFuture"]
} external;
