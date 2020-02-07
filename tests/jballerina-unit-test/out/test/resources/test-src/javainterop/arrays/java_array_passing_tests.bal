import ballerinax/java;
import ballerinax/java.arrays as jarrays;


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

public function testNewJStringArrayInstanceFunction() returns handle | error {
    handle jStringClass = check java:getClass("java.lang.String");
    handle jStrArray = jarrays:newInstance(jStringClass, 4);
    jarrays:set(jStrArray, 0, java:fromString("Ballerina"));
    jarrays:set(jStrArray, 1, java:fromString("Programming"));
    jarrays:set(jStrArray, 2, java:fromString("Language"));
    jarrays:set(jStrArray, 3, java:fromString("Specification"));
    return jStrArray;
}

public function testNewJIntArrayInstanceFunction() returns handle | error {
    handle jIntClass = check java:getClass("int");
    handle jIntArray = jarrays:newInstance(jIntClass, 4);
    jarrays:set(jIntArray, 0, wrapInt(10));
    jarrays:set(jIntArray, 1, wrapInt(100));
    jarrays:set(jIntArray, 2, wrapInt(1000));
    jarrays:set(jIntArray, 3, wrapInt(10000));
    return jIntArray;
}

public function testGetArrayElementMethod() returns [handle, handle, handle] {
    handle array = testReturningSortedJavaStringArray();
    handle elem0 = jarrays:get(array, 0);
    handle elem2 = jarrays:get(array, 2);
    handle elem3 = jarrays:get(array, 3);
    return [elem0, elem2, elem3];
}

public function testSetArrayElementMethod(handle elem0, handle elem1, handle elem3) returns handle {
    handle array = testReturningSortedJavaStringArray();
    jarrays:set(array, 0, elem0);
    jarrays:set(array, 1, elem1);
    jarrays:set(array, 3, elem3);
    return array;
}

public function testGetArrayLengthMethod() returns int {
   handle array = testReturningSortedJavaStringArray();
   int length = jarrays:getLength(array);
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

function wrapInt(int i) returns handle = @java:Constructor {
        class: "java.lang.Integer",
        paramTypes: ["int"]
} external;
