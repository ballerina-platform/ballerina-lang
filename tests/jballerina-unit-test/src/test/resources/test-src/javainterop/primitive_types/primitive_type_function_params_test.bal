import ballerina/java;

@java:Constructor {class:"java.lang.Boolean", paramTypes:["boolean"]}
public function createBoxedBooleanFromBBoolean(boolean value) returns handle = external;

public function testCreateBoxedBooleanFromBBoolean(boolean value) returns handle {
    return createBoxedBooleanFromBBoolean(value);
}

@java:Constructor {class:"java.lang.Byte", paramTypes:["byte"]}
public function createBoxedByteFromBInt(int value) returns handle = external;

@java:Constructor {class:"java.lang.Byte", paramTypes:["java.lang.String"]}
public function createBoxedByteFromJString(handle stringValue) returns handle = external;

public function testCreateBoxedByteFromBInt(int value)  returns handle {
    return createBoxedByteFromBInt(value);
}

public function testCreateBoxedByteFromJString(handle stringValue)  returns handle {
    return createBoxedByteFromJString(stringValue);
}

@java:Constructor {class:"java.lang.Short", paramTypes:["short"]}
public function createBoxedShortFromBInt(int value) returns handle = external;

public function testCreateBoxedShortFromBInt(int value) returns handle {
    return createBoxedShortFromBInt(value);
}

@java:Constructor {class:"java.lang.Character", paramTypes:["char"]}
public function createBoxedCharacterFromBInt(int value) returns handle = external;

public function testCreateBoxedCharacterFromBInt(int value) returns handle {
    return createBoxedCharacterFromBInt(value);
}

@java:Constructor {class:"java.lang.Integer", paramTypes:["int"]}
public function createBoxedIntegerFromBInt(int value) returns handle = external;

public function testCreateBoxedIntegerFromBInt(int value) returns handle {
    return createBoxedIntegerFromBInt(value);
}

@java:Constructor {class:"java.lang.Long", paramTypes:["long"]}
public function createBoxedLongFromBInt(int value) returns handle = external;

public function testCreateBoxedLongFromBInt(int value) returns handle {
    return createBoxedLongFromBInt(value);
}

// From java double from

@java:Constructor {class:"java.lang.Byte", paramTypes:["byte"]}
public function createBoxedByteFromBFloat(float value) returns handle = external;

public function testCreateBoxedByteFromBFloat(float value)  returns handle {
    return createBoxedByteFromBFloat(value);
}

@java:Constructor {class:"java.lang.Short", paramTypes:["short"]}
public function createBoxedShortFromBFloat(float value) returns handle = external;

public function testCreateBoxedShortFromBFloat(float value) returns handle {
    return createBoxedShortFromBFloat(value);
}

@java:Constructor {class:"java.lang.Character", paramTypes:["char"]}
public function createBoxedCharacterFromBFloat(float value) returns handle = external;

public function testCreateBoxedCharacterFromBFloat(float value) returns handle {
    return createBoxedCharacterFromBFloat(value);
}

@java:Constructor {class:"java.lang.Integer", paramTypes:["int"]}
public function createBoxedIntegerFromBFloat(float value) returns handle = external;

public function testCreateBoxedIntegerFromBFloat(float value) returns handle {
    return createBoxedIntegerFromBFloat(value);
}

@java:Constructor {class:"java.lang.Long", paramTypes:["long"]}
public function createBoxedLongFromBFloat(float value) returns handle = external;

public function testCreateBoxedLongFromBFloat(float value) returns handle {
    return createBoxedLongFromBFloat(value);
}

@java:Constructor {class:"java.lang.Float", paramTypes:["float"]}
public function createBoxedFloatFromBFloat(float value) returns handle = external;

public function testCreateBoxedFloatFromBFloat(float value) returns handle {
    return createBoxedFloatFromBFloat(value);
}

@java:Constructor {class:"java.lang.Double", paramTypes:["double"]}
public function createBoxedDoubleFromBFloat(float value) returns handle = external;

public function testCreateBoxedDoubleFromBFloat(float value) returns handle {
    return createBoxedDoubleFromBFloat(value);
}
