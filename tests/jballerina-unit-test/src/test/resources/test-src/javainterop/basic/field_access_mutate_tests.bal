import ballerina/java;

function testStaticFieldAccess() returns handle {
    return getContractId();
}

function testStaticFieldMutate(string value) {
    setContractId(value);
}

function testStaticPrimitiveFieldAccess() returns int {
    return getAge();
}

function testStaticPrimitiveFieldMutate(int shortValue) {
    return setShortValue(shortValue);
}

function testInstanceFieldAccess(handle receiver) returns handle {
    return getCreatedAt(receiver);
}

function testInstanceFieldMutate(handle receiver, handle value) {
    setUUID(receiver, value);
}

function testInstancePrimitiveFieldAccess(handle receiver) returns boolean {
    return isEmpty(receiver);
}

function testInstancePrimitiveFieldMutate(handle receiver, float value) {
    setLKR(receiver, value);
}


// Java interoperability external functions

// Static field access and mutate
public function getContractId() returns handle = @java:FieldGet {
    name:"contractId",
    'class:"org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate"
} external;

public function setContractId(string contractId) = @java:FieldSet {
    name:"contractId",
    'class:"org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate"
} external;

public function getAge() returns int = @java:FieldGet {
    name:"age",
    'class:"org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate"
} external;

public function setShortValue(int shortValue) = @java:FieldSet {
    name:"aShort",
    'class:"org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate"
} external;


// Instance field access and mutate
public function getCreatedAt(handle receiver) returns handle = @java:FieldGet {
    name:"createdAt",
    'class:"org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate"
} external;

public function setUUID(handle receiver, handle uuid) = @java:FieldSet {
    name:"uuid",
    'class:"org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate"
} external;

public function isEmpty(handle receiver) returns boolean = @java:FieldGet {
    name:"isEmpty",
    'class:"org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate"
} external;

public function setLKR(handle receiver, float value) = @java:FieldSet {
    name:"lkr",
    'class:"org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate"
} external;

