import ballerina/jballerina.java;


# Load closure map `v` from block to object ctor value
#
# + v - the value map to be cloned
public function setBlockClosureMap(object{} obj, map<any|error> v) = @java:Method {
 'class: "org.ballerinalang.langlib.internal.SetClosureMap",
 name: "setBlockClosureMap"
} external;

# Load param closure map `v` at level `level` from args to object ctor value
#
# + v - the value map to be cloned
# + level - the level the map is found
public function setParamClosureMap(object{} obj, map<any|error> v, int level) = @java:Method {
 'class: "org.ballerinalang.langlib.internal.SetClosureMap",
 name: "setParamClosureMap"
} external;

# Print closure map of object value `obj`
#
# + obj - object with a closure map
public function printClosureMaps(object{} obj) = @java:Method {
 'class: "org.ballerinalang.langlib.internal.PrintClosureMap",
 name: "printClosureMaps"
} external;

# Get closure map of object value `obj`
#
# + obj - object with a closure map
public function getBlockClosureMap(object{} obj) returns map<any|error> = @java:Method {
 'class: "org.ballerinalang.langlib.internal.GetClosureMap",
 name: "getBlockClosureMap"
} external;