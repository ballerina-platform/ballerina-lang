import ballerina/java;

public function interopWithJavaArrayList() returns [handle, int, string] {
        handle aList = newArrayListWithInitialSize(10);
        addElement(aList, java:toJString("Ballerina"));
        addElement(aList, java:toJString("Language"));
        addElement(aList, java:toJString("Specification"));
        handle element = getElement(aList, 2);
        string? strElement = java:fromJString(element);
        int listSize = size(aList);
        return [toString(aList), listSize, strElement?:""];
}

public function newArrayList() returns handle = @java:Constructor {
    class:"java.util.ArrayList"
} external;

public function newArrayListWithInitialSize(int initialSize) returns handle = @java:Constructor {
    class:"java.util.ArrayList",
    paramTypes:["int"]
} external;

public function addElement(handle receiver, handle e) = @java:Method {
    name:"add",
    class: "java.util.ArrayList"
} external;

public function toString(handle receiver) returns handle = @java:Method {
    class: "java.util.ArrayList",
    name:"toString"
} external;

public function size(handle receiver) returns int = @java:Method {
    class: "java.util.ArrayList"
} external;

public function getElement(handle receiver, int index) returns handle = @java:Method {
    name: "get",
    class: "java.util.ArrayList"
} external;

