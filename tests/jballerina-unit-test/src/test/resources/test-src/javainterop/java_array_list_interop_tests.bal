import ballerina/java;

public function interopWithJavaArrayList() returns handle {
        handle aList = newArrayList();
        addElement(aList, java:toJString("Ballerina"));
        addElement(aList, java:toJString("Language"));
        addElement(aList, java:toJString("Specification"));
        return toString(aList);
}

public function newArrayList() returns handle = @java:Constructor {
    class:"java.util.ArrayList"
} external;

public function addElement(handle receiver, handle e) = @java:Method {
    name:"add",
    class: "java.util.ArrayList"
} external;

public function toString(handle receiver) returns handle = @java:Method {
    class: "java.util.ArrayList",
    name:"toString"
} external;

