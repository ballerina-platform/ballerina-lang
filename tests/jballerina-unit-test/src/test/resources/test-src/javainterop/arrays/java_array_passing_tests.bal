import ballerina/java;

public function testPassingJavaIntArray(handle arrayValue) returns handle {
    sortJavaIntArray(arrayValue);
    return arrayValue;
}

public function testPassingJavaStringArray(handle arrayValue) returns handle {
    sortJavaStringArray(arrayValue);
    return arrayValue;
}

public function testReturningSortedJavaStringArray() returns handle {
    handle receiver = java:toJString("Ballerina Programming Language Specification");
    handle regex = java:toJString(" ");
    handle parts = splitString(receiver, regex);
    sortJavaStringArray(parts);
    return parts;
}

public function sortJavaIntArray(handle arrayValue) = @java:Method {
    name:"sort",
    class: "java.util.Arrays",
    isStatic:true,
    paramTypes:["[I"]
} external;

public function sortJavaStringArray(handle arrayValue) = @java:Method {
    name:"sort",
    class: "java.util.Arrays",
    isStatic:true,
    paramTypes:["[Ljava.lang.String;"]
} external;

public function splitString(handle receiver, handle regex) returns handle = @java:Method {
    name:"split",
    class: "java/lang/String",
    isStatic:false
} external;

