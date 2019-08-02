import ballerinax/java;

// Functions returns Ballerina boolean
function testReturningBBooleanJBoolean(handle receiver, handle strValue) returns boolean {
    return getBBooleanJBoolean(receiver, strValue);
}

// Functions returns Ballerina byte
function testReturningBByteJByte(handle receiver) returns byte {
    return getBByteJByte(receiver);
}

// Functions returning Ballerina int
function testReturningBIntJByte(handle receiver) returns int {
    return getBIntJByte(receiver);
}

function testReturningBIntJShort(handle receiver) returns int {
    return getBIntJShort(receiver);
}

//function testReturningBIntJChar(handle receiver) returns int {
//    return getBIntJChar(receiver);
//}

function testReturningBIntJInt(handle receiver) returns int {
    return getBIntJInt(receiver);
}

function testReturningBIntJLong(handle receiver) returns int {
    return getBIntJLong(receiver);
}

// Functions returning Ballerina float
function testReturningBFloatJByte(handle receiver) returns float {
    return getBFloatJByte(receiver);
}

function testReturningBFloatJShort(handle receiver) returns float {
    return getBFloatJShort(receiver);
}

//function testReturningBFloatJChar(handle receiver) returns float {
//    return getBFloatJChar();
//}

function testReturningBFloatJInt(handle receiver) returns float {
    return getBFloatJInt(receiver);
}

function testReturningBFloatJFloat(handle receiver) returns float {
    return getBFloatJFloat(receiver);
}

function testReturningBFloatJDouble(handle receiver) returns float {
    return getBFloatJDouble(receiver);
}

// Interop Functions returns Ballerina boolean
public function getBBooleanJBoolean(handle receiver, handle strValue) returns boolean = @java:Method {
    name:"contentEquals",
    class:"java.lang.String",
    paramTypes: ["java.lang.String", "java.lang.String"]
} external;


// Interop Functions returns Ballerina byte
public function getBByteJByte(handle receiver) returns byte = @java:Method {
    name:"byteValue",
    class:"java.lang.Long"
} external;


// Interop Functions returns Ballerina int
public function getBIntJByte(handle receiver) returns int = @java:Method {
    name:"byteValue",
    class:"java.lang.Long"
} external;

public function getBIntJShort(handle receiver) returns int = @java:Method {
    name:"shortValue",
    class:"java.lang.Long"
} external;

//@java:Method {name:"shortValue", class:"java.lang.Long"}
//public function getBIntJChar(handle receiver) returns int = external;

public function getBIntJInt(handle receiver) returns int = @java:Method {
    name:"intValue",
    class:"java.lang.Long"
} external;

public function getBIntJLong(handle receiver) returns int = @java:Method {
    name:"longValue",
    class:"java.lang.Long"
} external;





// Interop Functions returns Ballerina float
public function getBFloatJByte(handle receiver) returns float = @java:Method {
    name:"byteValue",
    class:"java.lang.Double"
} external;

public function getBFloatJShort(handle receiver) returns float = @java:Method {
    name:"shortValue",
    class:"java.lang.Double"
} external;

//@java:Method {name:"charValue", class:"java.lang.Double"}
//public function getBFloatJChar(handle receiver) returns float = external;

public function getBFloatJInt(handle receiver) returns float = @java:Method {
    name:"intValue",
    class:"java.lang.Double"
} external;

public function getBFloatJFloat(handle receiver) returns float = @java:Method {
    name:"floatValue",
    class:"java.lang.Double"
} external;

public function getBFloatJDouble(handle receiver) returns float = @java:Method {
    name:"doubleValue",
    class:"java.lang.Double"
} external;

