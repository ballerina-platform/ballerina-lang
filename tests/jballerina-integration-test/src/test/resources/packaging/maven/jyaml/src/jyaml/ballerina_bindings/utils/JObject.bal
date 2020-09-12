import ballerina/java;

# The Ballerina abstract object which is to be extended by Ballerina objects representing Ballerina bindings for Java classes.
#
# + jObj - The `handle` reference to the corresponding Java object.
type JObject abstract object {

    handle jObj;
};

# The function that could be used to obtain the string value of a Ballerina object mapping a Java class.
#
# + jObj - The `handle` reference to the Java object.
# + return - The `string` value of the Java object.
public function jObjToString(handle jObj) returns string {
    handle jStringValue = toStringInternal(jObj);
    return java:toString(jStringValue) ?: "null";
}

function toStringInternal(handle jObj) returns handle = @java:Method {
    name: "toString",
    'class: "java.lang.Object"
} external;

