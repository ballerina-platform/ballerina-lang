import ballerina/jballerina.java;


# Load closure map from map value `v`
#
# + v - the value map to be cloned
public function loadClosureMap(object{} obj, map<any|error> v) = @java:Method {
 'class: "org.ballerinalang.langlib.internal.SetClosureMap",
 name: "setClosureMap"
} external;


# Print closure map of object value `obj`
#
# + obj - object with a closure map
public function printClosureMap(object{} obj) = @java:Method {
 'class: "org.ballerinalang.langlib.internal.PrintClosureMap",
 name: "printClosureMap"
} external;

# Get closure map of object value `obj`
#
# + obj - object with a closure map
public function getClosureMap(object{} obj) returns map<any|error> = @java:Method {
 'class: "org.ballerinalang.langlib.internal.GetClosureMap",
 name: "getClosureMap"
} external;