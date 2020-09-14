
import ballerina/java;

# Ballerina object mapping for Java interface `java/util/Map`.
#
# + _Map - The field that represents this Ballerina object, which is used for Java subtyping.
# + _Object - The field that represents the superclass object `Object`.
type Map object {

    *JObject;

    MapT _Map = MapT;
    ObjectT _Object = ObjectT;

    # The init function of the Ballerina object mapping `java/util/Map` Java class.
    #
    # + obj - The `handle` value containing the Java reference of the object.
    function init(handle obj) {
        self.jObj = obj;
    }

    # The function to retrieve the string value of a Ballerina object mapping a Java class.
    #
    # + return - The `string` form of the object instance.
    function toString() returns string {
        return jObjToString(self.jObj);
    }

    # The function that maps to the `clear` method of `java/util/Map`.
    function clear() {
        () obj = java_util_Map_clear(self.jObj);
    }

    # The function that maps to the `compute` method of `java/util/Map`.
    #
    # + arg0 - The `Object` value required to map with the Java method parameter.
    # + arg1 - The `BiFunction` value required to map with the Java method parameter.
    # + return - The `Object` value returning from the Java mapping.
    function compute(Object arg0, BiFunction arg1) returns Object {
        handle externalObj = java_util_Map_compute(self.jObj, arg0.jObj, arg1.jObj);
        Object obj = new(externalObj);
        return obj;
    }

    # The function that maps to the `computeIfAbsent` method of `java/util/Map`.
    #
    # + arg0 - The `Object` value required to map with the Java method parameter.
    # + arg1 - The `Function` value required to map with the Java method parameter.
    # + return - The `Object` value returning from the Java mapping.
    function computeIfAbsent(Object arg0, Function arg1) returns Object {
        handle externalObj = java_util_Map_computeIfAbsent(self.jObj, arg0.jObj, arg1.jObj);
        Object obj = new(externalObj);
        return obj;
    }

    # The function that maps to the `computeIfPresent` method of `java/util/Map`.
    #
    # + arg0 - The `Object` value required to map with the Java method parameter.
    # + arg1 - The `BiFunction` value required to map with the Java method parameter.
    # + return - The `Object` value returning from the Java mapping.
    function computeIfPresent(Object arg0, BiFunction arg1) returns Object {
        handle externalObj = java_util_Map_computeIfPresent(self.jObj, arg0.jObj, arg1.jObj);
        Object obj = new(externalObj);
        return obj;
    }

    # The function that maps to the `containsKey` method of `java/util/Map`.
    #
    # + arg0 - The `Object` value required to map with the Java method parameter.
    # + return - The `boolean` value returning from the Java mapping.
    function containsKey(Object arg0) returns boolean {
        boolean externalObj = java_util_Map_containsKey(self.jObj, arg0.jObj);
        return externalObj;
    }

    # The function that maps to the `containsValue` method of `java/util/Map`.
    #
    # + arg0 - The `Object` value required to map with the Java method parameter.
    # + return - The `boolean` value returning from the Java mapping.
    function containsValue(Object arg0) returns boolean {
        boolean externalObj = java_util_Map_containsValue(self.jObj, arg0.jObj);
        return externalObj;
    }

    # The function that maps to the `entrySet` method of `java/util/Map`.
    # 
    # + return - The `Set` value returning from the Java mapping.
    function entrySet() returns Set {
        handle externalObj = java_util_Map_entrySet(self.jObj);
        Set obj = new(externalObj);
        return obj;
    }

    # The function that maps to the `equals` method of `java/util/Map`.
    #
    # + arg0 - The `Object` value required to map with the Java method parameter.
    # + return - The `boolean` value returning from the Java mapping.
    function 'equals(Object arg0) returns boolean {
        boolean externalObj = java_util_Map_equals(self.jObj, arg0.jObj);
        return externalObj;
    }

    # The function that maps to the `forEach` method of `java/util/Map`.
    #
    # + arg0 - The `BiConsumer` value required to map with the Java method parameter.
    function forEach(BiConsumer arg0) {
        () obj = java_util_Map_forEach(self.jObj, arg0.jObj);
    }

    # The function that maps to the `get` method of `java/util/Map`.
    #
    # + arg0 - The `Object` value required to map with the Java method parameter.
    # + return - The `Object` value returning from the Java mapping.
    function get(Object arg0) returns Object {
        handle externalObj = java_util_Map_get(self.jObj, arg0.jObj);
        Object obj = new(externalObj);
        return obj;
    }

    # The function that maps to the `getOrDefault` method of `java/util/Map`.
    #
    # + arg0 - The `Object` value required to map with the Java method parameter.
    # + arg1 - The `Object` value required to map with the Java method parameter.
    # + return - The `Object` value returning from the Java mapping.
    function getOrDefault(Object arg0, Object arg1) returns Object {
        handle externalObj = java_util_Map_getOrDefault(self.jObj, arg0.jObj, arg1.jObj);
        Object obj = new(externalObj);
        return obj;
    }

    # The function that maps to the `hashCode` method of `java/util/Map`.
    # 
    # + return - The `int` value returning from the Java mapping.
    function hashCode() returns int {
        int externalObj = java_util_Map_hashCode(self.jObj);
        return externalObj;
    }

    # The function that maps to the `isEmpty` method of `java/util/Map`.
    # 
    # + return - The `boolean` value returning from the Java mapping.
    function isEmpty() returns boolean {
        boolean externalObj = java_util_Map_isEmpty(self.jObj);
        return externalObj;
    }

    # The function that maps to the `keySet` method of `java/util/Map`.
    # 
    # + return - The `Set` value returning from the Java mapping.
    function keySet() returns Set {
        handle externalObj = java_util_Map_keySet(self.jObj);
        Set obj = new(externalObj);
        return obj;
    }

    # The function that maps to the `merge` method of `java/util/Map`.
    #
    # + arg0 - The `Object` value required to map with the Java method parameter.
    # + arg1 - The `Object` value required to map with the Java method parameter.
    # + arg2 - The `BiFunction` value required to map with the Java method parameter.
    # + return - The `Object` value returning from the Java mapping.
    function merge(Object arg0, Object arg1, BiFunction arg2) returns Object {
        handle externalObj = java_util_Map_merge(self.jObj, arg0.jObj, arg1.jObj, arg2.jObj);
        Object obj = new(externalObj);
        return obj;
    }

    # The function that maps to the `put` method of `java/util/Map`.
    #
    # + arg0 - The `Object` value required to map with the Java method parameter.
    # + arg1 - The `Object` value required to map with the Java method parameter.
    # + return - The `Object` value returning from the Java mapping.
    function put(Object arg0, Object arg1) returns Object {
        handle externalObj = java_util_Map_put(self.jObj, arg0.jObj, arg1.jObj);
        Object obj = new(externalObj);
        return obj;
    }

    # The function that maps to the `putAll` method of `java/util/Map`.
    #
    # + arg0 - The `Map` value required to map with the Java method parameter.
    function putAll(Map arg0) {
        () obj = java_util_Map_putAll(self.jObj, arg0.jObj);
    }

    # The function that maps to the `putIfAbsent` method of `java/util/Map`.
    #
    # + arg0 - The `Object` value required to map with the Java method parameter.
    # + arg1 - The `Object` value required to map with the Java method parameter.
    # + return - The `Object` value returning from the Java mapping.
    function putIfAbsent(Object arg0, Object arg1) returns Object {
        handle externalObj = java_util_Map_putIfAbsent(self.jObj, arg0.jObj, arg1.jObj);
        Object obj = new(externalObj);
        return obj;
    }

    # The function that maps to the `remove` method of `java/util/Map`.
    #
    # + arg0 - The `Object` value required to map with the Java method parameter.
    # + return - The `Object` value returning from the Java mapping.
    function remove1(Object arg0) returns Object {
        handle externalObj = java_util_Map_remove1(self.jObj, arg0.jObj);
        Object obj = new(externalObj);
        return obj;
    }

    # The function that maps to the `remove` method of `java/util/Map`.
    #
    # + arg0 - The `Object` value required to map with the Java method parameter.
    # + arg1 - The `Object` value required to map with the Java method parameter.
    # + return - The `boolean` value returning from the Java mapping.
    function remove2(Object arg0, Object arg1) returns boolean {
        boolean externalObj = java_util_Map_remove2(self.jObj, arg0.jObj, arg1.jObj);
        return externalObj;
    }

    # The function that maps to the `replace` method of `java/util/Map`.
    #
    # + arg0 - The `Object` value required to map with the Java method parameter.
    # + arg1 - The `Object` value required to map with the Java method parameter.
    # + return - The `Object` value returning from the Java mapping.
    function replace1(Object arg0, Object arg1) returns Object {
        handle externalObj = java_util_Map_replace1(self.jObj, arg0.jObj, arg1.jObj);
        Object obj = new(externalObj);
        return obj;
    }

    # The function that maps to the `replace` method of `java/util/Map`.
    #
    # + arg0 - The `Object` value required to map with the Java method parameter.
    # + arg1 - The `Object` value required to map with the Java method parameter.
    # + arg2 - The `Object` value required to map with the Java method parameter.
    # + return - The `boolean` value returning from the Java mapping.
    function replace2(Object arg0, Object arg1, Object arg2) returns boolean {
        boolean externalObj = java_util_Map_replace2(self.jObj, arg0.jObj, arg1.jObj, arg2.jObj);
        return externalObj;
    }

    # The function that maps to the `replaceAll` method of `java/util/Map`.
    #
    # + arg0 - The `BiFunction` value required to map with the Java method parameter.
    function replaceAll(BiFunction arg0) {
        () obj = java_util_Map_replaceAll(self.jObj, arg0.jObj);
    }

    # The function that maps to the `size` method of `java/util/Map`.
    # 
    # + return - The `int` value returning from the Java mapping.
    function size() returns int {
        int externalObj = java_util_Map_size(self.jObj);
        return externalObj;
    }

    # The function that maps to the `values` method of `java/util/Map`.
    # 
    # + return - The `Collection` value returning from the Java mapping.
    function values() returns Collection {
        handle externalObj = java_util_Map_values(self.jObj);
        Collection obj = new(externalObj);
        return obj;
    }
};

// External interop functions for mapping public methods.

function java_util_Map_clear(handle receiver) = @java:Method {
    name: "clear",
    'class: "java.util.Map",
    paramTypes: []
} external;

function java_util_Map_compute(handle receiver, handle arg0, handle arg1) returns handle = @java:Method {
    name: "compute",
    'class: "java.util.Map",
    paramTypes: ["java.lang.Object", "java.util.function.BiFunction"]
} external;

function java_util_Map_computeIfAbsent(handle receiver, handle arg0, handle arg1) returns handle = @java:Method {
    name: "computeIfAbsent",
    'class: "java.util.Map",
    paramTypes: ["java.lang.Object", "java.util.function.Function"]
} external;

function java_util_Map_computeIfPresent(handle receiver, handle arg0, handle arg1) returns handle = @java:Method {
    name: "computeIfPresent",
    'class: "java.util.Map",
    paramTypes: ["java.lang.Object", "java.util.function.BiFunction"]
} external;

function java_util_Map_containsKey(handle receiver, handle arg0) returns boolean = @java:Method {
    name: "containsKey",
    'class: "java.util.Map",
    paramTypes: ["java.lang.Object"]
} external;

function java_util_Map_containsValue(handle receiver, handle arg0) returns boolean = @java:Method {
    name: "containsValue",
    'class: "java.util.Map",
    paramTypes: ["java.lang.Object"]
} external;

function java_util_Map_entrySet(handle receiver) returns handle = @java:Method {
    name: "entrySet",
    'class: "java.util.Map",
    paramTypes: []
} external;

function java_util_Map_equals(handle receiver, handle arg0) returns boolean = @java:Method {
    name: "equals",
    'class: "java.util.Map",
    paramTypes: ["java.lang.Object"]
} external;

function java_util_Map_forEach(handle receiver, handle arg0) = @java:Method {
    name: "forEach",
    'class: "java.util.Map",
    paramTypes: ["java.util.function.BiConsumer"]
} external;

function java_util_Map_get(handle receiver, handle arg0) returns handle = @java:Method {
    name: "get",
    'class: "java.util.Map",
    paramTypes: ["java.lang.Object"]
} external;

function java_util_Map_getOrDefault(handle receiver, handle arg0, handle arg1) returns handle = @java:Method {
    name: "getOrDefault",
    'class: "java.util.Map",
    paramTypes: ["java.lang.Object", "java.lang.Object"]
} external;

function java_util_Map_hashCode(handle receiver) returns int = @java:Method {
    name: "hashCode",
    'class: "java.util.Map",
    paramTypes: []
} external;

function java_util_Map_isEmpty(handle receiver) returns boolean = @java:Method {
    name: "isEmpty",
    'class: "java.util.Map",
    paramTypes: []
} external;

function java_util_Map_keySet(handle receiver) returns handle = @java:Method {
    name: "keySet",
    'class: "java.util.Map",
    paramTypes: []
} external;

function java_util_Map_merge(handle receiver, handle arg0, handle arg1, handle arg2) returns handle = @java:Method {
    name: "merge",
    'class: "java.util.Map",
    paramTypes: ["java.lang.Object", "java.lang.Object", "java.util.function.BiFunction"]
} external;

function java_util_Map_put(handle receiver, handle arg0, handle arg1) returns handle = @java:Method {
    name: "put",
    'class: "java.util.Map",
    paramTypes: ["java.lang.Object", "java.lang.Object"]
} external;

function java_util_Map_putAll(handle receiver, handle arg0) = @java:Method {
    name: "putAll",
    'class: "java.util.Map",
    paramTypes: ["java.util.Map"]
} external;

function java_util_Map_putIfAbsent(handle receiver, handle arg0, handle arg1) returns handle = @java:Method {
    name: "putIfAbsent",
    'class: "java.util.Map",
    paramTypes: ["java.lang.Object", "java.lang.Object"]
} external;

function java_util_Map_remove1(handle receiver, handle arg0) returns handle = @java:Method {
    name: "remove",
    'class: "java.util.Map",
    paramTypes: ["java.lang.Object"]
} external;

function java_util_Map_remove2(handle receiver, handle arg0, handle arg1) returns boolean = @java:Method {
    name: "remove",
    'class: "java.util.Map",
    paramTypes: ["java.lang.Object", "java.lang.Object"]
} external;

function java_util_Map_replace1(handle receiver, handle arg0, handle arg1) returns handle = @java:Method {
    name: "replace",
    'class: "java.util.Map",
    paramTypes: ["java.lang.Object", "java.lang.Object"]
} external;

function java_util_Map_replace2(handle receiver, handle arg0, handle arg1, handle arg2) returns boolean = @java:Method {
    name: "replace",
    'class: "java.util.Map",
    paramTypes: ["java.lang.Object", "java.lang.Object", "java.lang.Object"]
} external;

function java_util_Map_replaceAll(handle receiver, handle arg0) = @java:Method {
    name: "replaceAll",
    'class: "java.util.Map",
    paramTypes: ["java.util.function.BiFunction"]
} external;

function java_util_Map_size(handle receiver) returns int = @java:Method {
    name: "size",
    'class: "java.util.Map",
    paramTypes: []
} external;

function java_util_Map_values(handle receiver) returns handle = @java:Method {
    name: "values",
    'class: "java.util.Map",
    paramTypes: []
} external;


