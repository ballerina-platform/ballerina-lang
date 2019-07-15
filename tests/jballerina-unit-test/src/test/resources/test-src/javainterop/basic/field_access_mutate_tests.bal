import ballerina/java;

function testStaticFieldAccess() returns handle {
    return getContractId();
}

function testStaticFieldMutate(handle value) {
    setContractId(value);
}

function testInstanceFieldAccess(handle receiver) returns handle {
    return getCreatedAt(receiver);
}

function testInstanceFieldMutate(handle receiver, handle value) {
    setUUID(receiver, value);
}


// Java interoperability external functions

// Static field access and mutate
@java:FieldGet {name:"contractId",
                class:"org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate",
                isStatic:true}
public function getContractId() returns handle = external;

@java:FieldSet {name:"contractId",
                class:"org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate",
                isStatic:true}
public function setContractId(handle contractId) = external;


// Instance field access and mutate
@java:FieldGet {name:"createdAt",
                class:"org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate"}
public function getCreatedAt(handle receiver) returns handle = external;

@java:FieldSet {name:"uuid",
                class:"org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate"}
public function setUUID(handle receiver, handle uuid) = external;

