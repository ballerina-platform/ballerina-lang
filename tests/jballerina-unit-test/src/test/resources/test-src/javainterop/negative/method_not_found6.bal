import ballerina/java;

function getFuture(typedesc<anydata> td, future<anydata> f) returns future<anydata> = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.StaticMethods",
    name: "getFuture",
    paramTypes: ["io.ballerina.runtime.api.values.BTypedesc"]
} external;

function getTypeDesc(typedesc<anydata> td, future<anydata> f) returns typedesc<anydata> = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.StaticMethods",
    name: "getTypeDesc",
    paramTypes: ["io.ballerina.runtime.api.values.BFuture"]
} external;

function getFutureOnly(future<anydata> f) returns future<anydata> = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.StaticMethods",
    name: "getFutureOnly",
    paramTypes: ["io.ballerina.runtime.api.values.BFuture", "io.ballerina.runtime.api.values.BTypedesc"]
} external;

function getTypeDescOnly(typedesc<anydata> td) returns typedesc<anydata> = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.StaticMethods",
    name: "getTypeDescOnly",
    paramTypes: ["io.ballerina.runtime.api.values.BTypedesc", "io.ballerina.runtime.api.values.BFuture"]
} external;
