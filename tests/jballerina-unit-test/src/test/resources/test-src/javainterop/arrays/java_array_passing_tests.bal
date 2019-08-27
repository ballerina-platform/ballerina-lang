import ballerinax/java;

public function testPassingJavaIntArray(handle arrayValue) returns handle {
    sortJavaIntArray(arrayValue);
    return arrayValue;
}

public function testPassingJavaStringArray(handle arrayValue) returns handle {
    sortJavaStringArray(arrayValue);
    return arrayValue;
}

public function testReturningSortedJavaStringArray() returns handle {
    handle receiver = java:fromString("Ballerina Programming Language Specification");
    handle regex = java:fromString(" ");
    handle parts = splitString(receiver, regex);
    sortJavaStringArray(parts);
    return parts;
}

public function testGetArrayElementMethod() returns [handle, handle, handle] {
    handle array = testReturningSortedJavaStringArray();
    handle elem0 = java:getArrayElement(array, 0);
    handle elem2 = java:getArrayElement(array, 2);
    handle elem3 = java:getArrayElement(array, 3);
    return [elem0, elem2, elem3];
}

public function testSetArrayElementMethod(handle elem0, handle elem1, handle elem3) returns handle {
    handle array = testReturningSortedJavaStringArray();
    java:setArrayElement(array, 0, elem0);
    java:setArrayElement(array, 1, elem1);
    java:setArrayElement(array, 3, elem3);
    return array;
}

public function testGetArrayLengthMethod() returns int {
   handle array = testReturningSortedJavaStringArray();
   int length = java:getArrayLength(array);
   return length;
}


public function sortJavaIntArray(handle arrayValue) = @java:Method {
    name:"sort",
    class: "java.util.Arrays",
    paramTypes:["[I"]
} external;

public function sortJavaStringArray(handle arrayValue) = @java:Method {
    name:"sort",
    class: "java.util.Arrays",
    paramTypes:["[Ljava.lang.String;"]
} external;

public function splitString(handle receiver, handle regex) returns handle = @java:Method {
    name:"split",
    class: "java/lang/String"
} external;

