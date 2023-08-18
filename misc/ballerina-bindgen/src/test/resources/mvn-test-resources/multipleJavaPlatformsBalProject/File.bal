import ballerina/jballerina.java;

# Ballerina class mapping for the Java `java.io.File` class.
@java:Binding {'class: "java.io.File"}
distinct class File {

    *java:JObject;

    # The `handle` field that stores the reference to the `java.io.File` object.
    public handle jObj;

    # The init function of the Ballerina class mapping the `java.io.File` Java class.
    #
    # + obj - The `handle` value containing the Java reference of the object.
    function init(handle obj) {
        self.jObj = obj;
    }

    # The function to retrieve the string representation of the Ballerina class mapping the `java.io.File` Java class.
    #
    # + return - The `string` form of the Java object instance.
    function toString() returns string {
        return java:toString(self.jObj) ?: "null";
    }
}

