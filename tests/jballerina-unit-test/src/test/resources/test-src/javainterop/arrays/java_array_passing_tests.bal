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

@java:Method {name:"sort", class: "java.util.Arrays", isStatic:true, paramTypes:["[I"]}
public function sortJavaIntArray(handle arrayValue) = external;

@java:Method {name:"sort", class: "java.util.Arrays", isStatic:true, paramTypes:["[Ljava.lang.String;"]}
public function sortJavaStringArray(handle arrayValue) = external;

@java:Method {name:"split", class: "java/lang/String", isStatic:false}
public function splitString(handle receiver, handle regex) returns handle = external;

