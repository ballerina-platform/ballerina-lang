import ballerina/java;

// Test a function that accepts a Ballerina byte for a Java short
public function testCreateJShortFromBByte(byte value)  returns handle {
    return createJShortFromBByte(value);
}

public function createJShortFromBByte(byte value) returns handle = @java:Constructor {
    'class:"java.lang.Short",
    paramTypes:["short"]
} external;


// Test a function that accepts a Ballerina byte for a Java char
public function testCreateJCharacterFromBByte(byte value)  returns handle {
    return createJCharacterFromBByte(value);
}

public function createJCharacterFromBByte(byte value) returns handle = @java:Constructor {
    'class:"java.lang.Character",
    paramTypes:["char"]
} external;


// Test a function that accepts a Ballerina byte for a Java int
public function testCreateJIntegerFromBByte(byte value)  returns handle {
    return createJIntegerFromBByte(value);
}

public function createJIntegerFromBByte(byte value) returns handle = @java:Constructor {
    'class:"java.lang.Integer",
    paramTypes:["int"]
} external;


// Test a function that accepts a Ballerina byte for a Java long
public function testCreateJLongFromBByte(byte value)  returns handle {
    return createJLongFromBByte(value);
}

public function createJLongFromBByte(byte value) returns handle = @java:Constructor {
    'class:"java.lang.Long",
    paramTypes:["long"]
} external;


// Test a function that accepts a Ballerina byte for a Java float
public function testCreateJFloatFromBByte(byte value)  returns handle {
    return createJFloatFromBByte(value);
}

public function createJFloatFromBByte(byte value) returns handle = @java:Constructor {
    'class:"java.lang.Float",
    paramTypes:["float"]
} external;


// Test a function that accepts a Ballerina byte for a Java double
public function testCreateJDoubleFromBByte(byte value)  returns handle {
    return createJDoubleFromBByte(value);
}

public function createJDoubleFromBByte(byte value) returns handle = @java:Constructor {
    'class:"java.lang.Double",
    paramTypes:["double"]
} external;


// Test a function that accepts a Ballerina int for a Java byte
public function testCreateJByteFromBInt(int value)  returns handle {
    return createJByteFromBInt(value);
}

public function createJByteFromBInt(int value) returns handle = @java:Constructor {
    'class:"java.lang.Byte",
    paramTypes:["byte"]
} external;


// Test a function that accepts a Ballerina int for a Java short
public function testCreateJShortFromBInt(int value)  returns handle {
    return createJShortFromBInt(value);
}

public function createJShortFromBInt(int value) returns handle = @java:Constructor {
    'class:"java.lang.Short",
    paramTypes:["short"]
} external;


// Test a function that accepts a Ballerina int for a Java char
public function testCreateJCharacterFromBInt(int value)  returns handle {
    return createJCharacterFromBInt(value);
}

public function createJCharacterFromBInt(int value) returns handle = @java:Constructor {
    'class:"java.lang.Character",
    paramTypes:["char"]
} external;


// Test a function that accepts a Ballerina int for a Java int
public function testCreateJIntFromBInt(int value)  returns handle {
    return createJIntFromBInt(value);
}

public function createJIntFromBInt(int value) returns handle = @java:Constructor {
    'class:"java.lang.Integer",
    paramTypes:["int"]
} external;


// Test a function that accepts a Ballerina int for a Java float
public function testCreateJFloatFromBInt(int value)  returns handle {
    return createJFloatFromBInt(value);
}

public function createJFloatFromBInt(int value) returns handle = @java:Constructor {
    'class:"java.lang.Float",
    paramTypes:["float"]
} external;


// Test a function that accepts a Ballerina int for a Java double
public function testCreateJDoubleFromBInt(int value)  returns handle {
    return createJDoubleFromBInt(value);
}

public function createJDoubleFromBInt(int value) returns handle = @java:Constructor {
    'class:"java.lang.Double",
    paramTypes:["double"]
} external;


// Test a function that accepts a Ballerina float for a Java byte
public function testCreateJByteFromBFloat(float value)  returns handle {
    return createJByteFromBFloat(value);
}

public function createJByteFromBFloat(float value) returns handle = @java:Constructor {
    'class:"java.lang.Byte",
    paramTypes:["byte"]
} external;


// Test a function that accepts a Ballerina float for a Java short
public function testCreateJShortFromBFloat(float value)  returns handle {
    return createJShortFromBFloat(value);
}

public function createJShortFromBFloat(float value) returns handle = @java:Constructor {
    'class:"java.lang.Short",
    paramTypes:["short"]
} external;


// Test a function that accepts a Ballerina float for a Java char
public function testCreateJCharacterFromBFloat(float value)  returns handle {
    return createJCharacterFromBFloat(value);
}

public function createJCharacterFromBFloat(float value) returns handle = @java:Constructor {
    'class:"java.lang.Character",
    paramTypes:["char"]
} external;


// Test a function that accepts a Ballerina float for a Java int
public function testCreateJIntFromBFloat(float value)  returns handle {
    return createJIntFromBFloat(value);
}

public function createJIntFromBFloat(float value) returns handle = @java:Constructor {
    'class:"java.lang.Integer",
    paramTypes:["int"]
} external;


// Test a function that accepts a Ballerina float for a Java long
public function testCreateJLongFromBFloat(float value)  returns handle {
    return createJLongFromBFloat(value);
}

public function createJLongFromBFloat(float value) returns handle = @java:Constructor {
    'class:"java.lang.Long",
    paramTypes:["long"]
} external;


// Test a function that accepts a Ballerina float for a Java float
public function testCreateJFloatFromBFloat(float value)  returns handle {
    return createJFloatFromBFloat(value);
}

public function createJFloatFromBFloat(float value) returns handle = @java:Constructor {
    'class:"java.lang.Float",
    paramTypes:["float"]
} external;
