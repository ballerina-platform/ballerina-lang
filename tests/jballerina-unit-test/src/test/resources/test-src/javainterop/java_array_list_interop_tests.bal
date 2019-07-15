import ballerina/java;

public function interopWithJavaArrayList() returns handle {
        handle aList = newArrayList();
        addElement(aList, java:toJString("Ballerina"));
        addElement(aList, java:toJString("Language"));
        addElement(aList, java:toJString("Specification"));
        return toString(aList);
}

@java:Constructor {class:"java.util.ArrayList"}
public function newArrayList() returns handle = external;

@java:Method {class: "java.util.ArrayList", name:"add"}
public function addElement(handle receiver, handle e) = external;

@java:Method {class: "java.util.ArrayList", name:"toString"}
public function toString(handle receiver) returns handle = external;

