import ballerinax/java;

public function createBoxedBooleanFromBBoolean(boolean value) returns handle = @java:Constructor {
    class:"java.lang.Boolean",
    paramTypes:["boolean"]
} external;

public function testCreateBoxedBooleanFromBBoolean(boolean value) returns handle {
    return createBoxedBooleanFromBBoolean(value);
}

public function createBoxedByteFromBInt(int value) returns handle = @java:Constructor {
    class:"java.lang.Byte",
    paramTypes:["byte"]
} external;

public function createBoxedByteFromJString(handle stringValue) returns handle = @java:Constructor {
    class:"java.lang.Byte",
    paramTypes:["java.lang.String"]
} external;

public function testCreateBoxedByteFromBInt(int value)  returns handle {
    return createBoxedByteFromBInt(value);
}

public function testCreateBoxedByteFromJString(handle stringValue)  returns handle {
    return createBoxedByteFromJString(stringValue);
}

public function createBoxedShortFromBInt(int value) returns handle = @java:Constructor {
    class:"java.lang.Short",
    paramTypes:["short"]
} external;

public function testCreateBoxedShortFromBInt(int value) returns handle {
    return createBoxedShortFromBInt(value);
}

public function createBoxedCharacterFromBInt(int value) returns handle = @java:Constructor {
    class:"java.lang.Character",
    paramTypes:["char"]
} external;

public function testCreateBoxedCharacterFromBInt(int value) returns handle {
    return createBoxedCharacterFromBInt(value);
}

public function createBoxedIntegerFromBInt(int value) returns handle = @java:Constructor {
    class:"java.lang.Integer",
    paramTypes:["int"]
} external;

public function testCreateBoxedIntegerFromBInt(int value) returns handle {
    return createBoxedIntegerFromBInt(value);
}

public function createBoxedLongFromBInt(int value) returns handle = @java:Constructor {
    class:"java.lang.Long",
    paramTypes:["long"]
} external;

public function testCreateBoxedLongFromBInt(int value) returns handle {
    return createBoxedLongFromBInt(value);
}

// From java double from

public function createBoxedByteFromBFloat(float value) returns handle = @java:Constructor {
    class:"java.lang.Byte",
    paramTypes:["byte"]
} external;

public function testCreateBoxedByteFromBFloat(float value)  returns handle {
    return createBoxedByteFromBFloat(value);
}

public function createBoxedShortFromBFloat(float value) returns handle = @java:Constructor {
    class:"java.lang.Short",
    paramTypes:["short"]
} external;

public function testCreateBoxedShortFromBFloat(float value) returns handle {
    return createBoxedShortFromBFloat(value);
}

public function createBoxedCharacterFromBFloat(float value) returns handle = @java:Constructor {
    class:"java.lang.Character",
    paramTypes:["char"]
} external;

public function testCreateBoxedCharacterFromBFloat(float value) returns handle {
    return createBoxedCharacterFromBFloat(value);
}

public function createBoxedIntegerFromBFloat(float value) returns handle = @java:Constructor {
    class:"java.lang.Integer",
    paramTypes:["int"]
} external;

public function testCreateBoxedIntegerFromBFloat(float value) returns handle {
    return createBoxedIntegerFromBFloat(value);
}

public function createBoxedLongFromBFloat(float value) returns handle = @java:Constructor {
    class:"java.lang.Long",
    paramTypes:["long"]
} external;

public function testCreateBoxedLongFromBFloat(float value) returns handle {
    return createBoxedLongFromBFloat(value);
}

public function createBoxedFloatFromBFloat(float value) returns handle = @java:Constructor {
    class:"java.lang.Float",
    paramTypes:["float"]
} external;

public function testCreateBoxedFloatFromBFloat(float value) returns handle {
    return createBoxedFloatFromBFloat(value);
}

public function createBoxedDoubleFromBFloat(float value) returns handle = @java:Constructor {
    class:"java.lang.Double",
    paramTypes:["double"]
} external;

public function testCreateBoxedDoubleFromBFloat(float value) returns handle {
    return createBoxedDoubleFromBFloat(value);
}
