
import ballerina/java;

# Ballerina object mapping for Java class `org/yaml/snakeyaml/Yaml`.
#
# + _Yaml - The field that represents this Ballerina object, which is used for Java subtyping.
# + _Object - The field that represents the superclass object `Object`.
type Yaml object {

    *JObject;

    YamlT _Yaml = YamlT;
    ObjectT _Object = ObjectT;

    # The init function of the Ballerina object mapping `org/yaml/snakeyaml/Yaml` Java class.
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

    # The function that maps to the `addImplicitResolver` method of `org/yaml/snakeyaml/Yaml`.
    #
    # + arg0 - The `Tag` value required to map with the Java method parameter.
    # + arg1 - The `Pattern` value required to map with the Java method parameter.
    # + arg2 - The `string` value required to map with the Java method parameter.
    function addImplicitResolver(Tag arg0, Pattern arg1, string arg2) {
        () obj = org_yaml_snakeyaml_Yaml_addImplicitResolver(self.jObj, arg0.jObj, arg1.jObj, java:fromString(arg2));
    }

    # The function that maps to the `compose` method of `org/yaml/snakeyaml/Yaml`.
    #
    # + arg0 - The `Reader` value required to map with the Java method parameter.
    # + return - The `Node` value returning from the Java mapping.
    function compose(Reader arg0) returns Node {
        handle externalObj = org_yaml_snakeyaml_Yaml_compose(self.jObj, arg0.jObj);
        Node obj = new(externalObj);
        return obj;
    }

    # The function that maps to the `composeAll` method of `org/yaml/snakeyaml/Yaml`.
    #
    # + arg0 - The `Reader` value required to map with the Java method parameter.
    # + return - The `Iterable` value returning from the Java mapping.
    function composeAll(Reader arg0) returns Iterable {
        handle externalObj = org_yaml_snakeyaml_Yaml_composeAll(self.jObj, arg0.jObj);
        Iterable obj = new(externalObj);
        return obj;
    }

    # The function that maps to the `dump` method of `org/yaml/snakeyaml/Yaml`.
    #
    # + arg0 - The `Object` value required to map with the Java method parameter.
    # + return - The `string?` value returning from the Java mapping.
    function dump1(Object arg0) returns string? {
        handle externalObj = org_yaml_snakeyaml_Yaml_dump1(self.jObj, arg0.jObj);
        return java:toString(externalObj);
    }

    # The function that maps to the `dump` method of `org/yaml/snakeyaml/Yaml`.
    #
    # + arg0 - The `Object` value required to map with the Java method parameter.
    # + arg1 - The `Writer` value required to map with the Java method parameter.
    function dump2(Object arg0, Writer arg1) {
        () obj = org_yaml_snakeyaml_Yaml_dump2(self.jObj, arg0.jObj, arg1.jObj);
    }

    # The function that maps to the `dumpAll` method of `org/yaml/snakeyaml/Yaml`.
    #
    # + arg0 - The `Iterator` value required to map with the Java method parameter.
    # + return - The `string?` value returning from the Java mapping.
    function dumpAll1(Iterator arg0) returns string? {
        handle externalObj = org_yaml_snakeyaml_Yaml_dumpAll1(self.jObj, arg0.jObj);
        return java:toString(externalObj);
    }

    # The function that maps to the `dumpAll` method of `org/yaml/snakeyaml/Yaml`.
    #
    # + arg0 - The `Iterator` value required to map with the Java method parameter.
    # + arg1 - The `Writer` value required to map with the Java method parameter.
    function dumpAll2(Iterator arg0, Writer arg1) {
        () obj = org_yaml_snakeyaml_Yaml_dumpAll2(self.jObj, arg0.jObj, arg1.jObj);
    }

    # The function that maps to the `dumpAs` method of `org/yaml/snakeyaml/Yaml`.
    #
    # + arg0 - The `Object` value required to map with the Java method parameter.
    # + arg1 - The `Tag` value required to map with the Java method parameter.
    # + arg2 - The `FlowStyle` value required to map with the Java method parameter.
    # + return - The `string?` value returning from the Java mapping.
    function dumpAs(Object arg0, Tag arg1, FlowStyle arg2) returns string? {
        handle externalObj = org_yaml_snakeyaml_Yaml_dumpAs(self.jObj, arg0.jObj, arg1.jObj, arg2.jObj);
        return java:toString(externalObj);
    }

    # The function that maps to the `dumpAsMap` method of `org/yaml/snakeyaml/Yaml`.
    #
    # + arg0 - The `Object` value required to map with the Java method parameter.
    # + return - The `string?` value returning from the Java mapping.
    function dumpAsMap(Object arg0) returns string? {
        handle externalObj = org_yaml_snakeyaml_Yaml_dumpAsMap(self.jObj, arg0.jObj);
        return java:toString(externalObj);
    }

    # The function that maps to the `equals` method of `org/yaml/snakeyaml/Yaml`.
    #
    # + arg0 - The `Object` value required to map with the Java method parameter.
    # + return - The `boolean` value returning from the Java mapping.
    function 'equals(Object arg0) returns boolean {
        boolean externalObj = org_yaml_snakeyaml_Yaml_equals(self.jObj, arg0.jObj);
        return externalObj;
    }

    # The function that maps to the `getClass` method of `org/yaml/snakeyaml/Yaml`.
    # 
    # + return - The `Class` value returning from the Java mapping.
    function getClass() returns Class {
        handle externalObj = org_yaml_snakeyaml_Yaml_getClass(self.jObj);
        Class obj = new(externalObj);
        return obj;
    }

    # The function that maps to the `getName` method of `org/yaml/snakeyaml/Yaml`.
    # 
    # + return - The `string?` value returning from the Java mapping.
    function getName() returns string? {
        handle externalObj = org_yaml_snakeyaml_Yaml_getName(self.jObj);
        return java:toString(externalObj);
    }

    # The function that maps to the `hashCode` method of `org/yaml/snakeyaml/Yaml`.
    # 
    # + return - The `int` value returning from the Java mapping.
    function hashCode() returns int {
        int externalObj = org_yaml_snakeyaml_Yaml_hashCode(self.jObj);
        return externalObj;
    }

    # The function that maps to the `load` method of `org/yaml/snakeyaml/Yaml`.
    #
    # + arg0 - The `InputStream` value required to map with the Java method parameter.
    # + return - The `Object` value returning from the Java mapping.
    function load1(InputStream arg0) returns Object {
        handle externalObj = org_yaml_snakeyaml_Yaml_load1(self.jObj, arg0.jObj);
        Object obj = new(externalObj);
        return obj;
    }

    # The function that maps to the `load` method of `org/yaml/snakeyaml/Yaml`.
    #
    # + arg0 - The `Reader` value required to map with the Java method parameter.
    # + return - The `Object` value returning from the Java mapping.
    function load2(Reader arg0) returns Object {
        handle externalObj = org_yaml_snakeyaml_Yaml_load2(self.jObj, arg0.jObj);
        Object obj = new(externalObj);
        return obj;
    }

    # The function that maps to the `load` method of `org/yaml/snakeyaml/Yaml`.
    #
    # + arg0 - The `string` value required to map with the Java method parameter.
    # + return - The `Object` value returning from the Java mapping.
    function load3(string arg0) returns Object {
        handle externalObj = org_yaml_snakeyaml_Yaml_load3(self.jObj, java:fromString(arg0));
        Object obj = new(externalObj);
        return obj;
    }

    # The function that maps to the `loadAll` method of `org/yaml/snakeyaml/Yaml`.
    #
    # + arg0 - The `InputStream` value required to map with the Java method parameter.
    # + return - The `Iterable` value returning from the Java mapping.
    function loadAll1(InputStream arg0) returns Iterable {
        handle externalObj = org_yaml_snakeyaml_Yaml_loadAll1(self.jObj, arg0.jObj);
        Iterable obj = new(externalObj);
        return obj;
    }

    # The function that maps to the `loadAll` method of `org/yaml/snakeyaml/Yaml`.
    #
    # + arg0 - The `Reader` value required to map with the Java method parameter.
    # + return - The `Iterable` value returning from the Java mapping.
    function loadAll2(Reader arg0) returns Iterable {
        handle externalObj = org_yaml_snakeyaml_Yaml_loadAll2(self.jObj, arg0.jObj);
        Iterable obj = new(externalObj);
        return obj;
    }

    # The function that maps to the `loadAll` method of `org/yaml/snakeyaml/Yaml`.
    #
    # + arg0 - The `string` value required to map with the Java method parameter.
    # + return - The `Iterable` value returning from the Java mapping.
    function loadAll3(string arg0) returns Iterable {
        handle externalObj = org_yaml_snakeyaml_Yaml_loadAll3(self.jObj, java:fromString(arg0));
        Iterable obj = new(externalObj);
        return obj;
    }

    # The function that maps to the `loadAs` method of `org/yaml/snakeyaml/Yaml`.
    #
    # + arg0 - The `InputStream` value required to map with the Java method parameter.
    # + arg1 - The `Class` value required to map with the Java method parameter.
    # + return - The `Object` value returning from the Java mapping.
    function loadAs1(InputStream arg0, Class arg1) returns Object {
        handle externalObj = org_yaml_snakeyaml_Yaml_loadAs1(self.jObj, arg0.jObj, arg1.jObj);
        Object obj = new(externalObj);
        return obj;
    }

    # The function that maps to the `loadAs` method of `org/yaml/snakeyaml/Yaml`.
    #
    # + arg0 - The `Reader` value required to map with the Java method parameter.
    # + arg1 - The `Class` value required to map with the Java method parameter.
    # + return - The `Object` value returning from the Java mapping.
    function loadAs2(Reader arg0, Class arg1) returns Object {
        handle externalObj = org_yaml_snakeyaml_Yaml_loadAs2(self.jObj, arg0.jObj, arg1.jObj);
        Object obj = new(externalObj);
        return obj;
    }

    # The function that maps to the `loadAs` method of `org/yaml/snakeyaml/Yaml`.
    #
    # + arg0 - The `string` value required to map with the Java method parameter.
    # + arg1 - The `Class` value required to map with the Java method parameter.
    # + return - The `Object` value returning from the Java mapping.
    function loadAs3(string arg0, Class arg1) returns Object {
        handle externalObj = org_yaml_snakeyaml_Yaml_loadAs3(self.jObj, java:fromString(arg0), arg1.jObj);
        Object obj = new(externalObj);
        return obj;
    }

    # The function that maps to the `notify` method of `org/yaml/snakeyaml/Yaml`.
    function notify() {
        () obj = org_yaml_snakeyaml_Yaml_notify(self.jObj);
    }

    # The function that maps to the `notifyAll` method of `org/yaml/snakeyaml/Yaml`.
    function notifyAll() {
        () obj = org_yaml_snakeyaml_Yaml_notifyAll(self.jObj);
    }

    # The function that maps to the `parse` method of `org/yaml/snakeyaml/Yaml`.
    #
    # + arg0 - The `Reader` value required to map with the Java method parameter.
    # + return - The `Iterable` value returning from the Java mapping.
    function parse(Reader arg0) returns Iterable {
        handle externalObj = org_yaml_snakeyaml_Yaml_parse(self.jObj, arg0.jObj);
        Iterable obj = new(externalObj);
        return obj;
    }

    # The function that maps to the `represent` method of `org/yaml/snakeyaml/Yaml`.
    #
    # + arg0 - The `Object` value required to map with the Java method parameter.
    # + return - The `Node` value returning from the Java mapping.
    function represent(Object arg0) returns Node {
        handle externalObj = org_yaml_snakeyaml_Yaml_represent(self.jObj, arg0.jObj);
        Node obj = new(externalObj);
        return obj;
    }

    # The function that maps to the `serialize` method of `org/yaml/snakeyaml/Yaml`.
    #
    # + arg0 - The `Node` value required to map with the Java method parameter.
    # + return - The `List` value returning from the Java mapping.
    function serialize(Node arg0) returns List {
        handle externalObj = org_yaml_snakeyaml_Yaml_serialize(self.jObj, arg0.jObj);
        List obj = new(externalObj);
        return obj;
    }

    # The function that maps to the `setBeanAccess` method of `org/yaml/snakeyaml/Yaml`.
    #
    # + arg0 - The `BeanAccess` value required to map with the Java method parameter.
    function setBeanAccess(BeanAccess arg0) {
        () obj = org_yaml_snakeyaml_Yaml_setBeanAccess(self.jObj, arg0.jObj);
    }

    # The function that maps to the `setName` method of `org/yaml/snakeyaml/Yaml`.
    #
    # + arg0 - The `string` value required to map with the Java method parameter.
    function setName(string arg0) {
        () obj = org_yaml_snakeyaml_Yaml_setName(self.jObj, java:fromString(arg0));
    }

    # The function that maps to the `wait` method of `org/yaml/snakeyaml/Yaml`.
    # 
    # + return - The `error?` value returning from the Java mapping.
    function 'wait1() returns error? {
        error|() obj = org_yaml_snakeyaml_Yaml_wait1(self.jObj);
        if (obj is error) {
            InterruptedException e = InterruptedException(message = obj.reason(), cause = obj);
            return e;
        }
    }

    # The function that maps to the `wait` method of `org/yaml/snakeyaml/Yaml`.
    #
    # + arg0 - The `int` value required to map with the Java method parameter.
    # + return - The `error?` value returning from the Java mapping.
    function 'wait2(int arg0) returns error? {
        error|() obj = org_yaml_snakeyaml_Yaml_wait2(self.jObj, arg0);
        if (obj is error) {
            InterruptedException e = InterruptedException(message = obj.reason(), cause = obj);
            return e;
        }
    }

    # The function that maps to the `wait` method of `org/yaml/snakeyaml/Yaml`.
    #
    # + arg0 - The `int` value required to map with the Java method parameter.
    # + arg1 - The `int` value required to map with the Java method parameter.
    # + return - The `error?` value returning from the Java mapping.
    function 'wait3(int arg0, int arg1) returns error? {
        error|() obj = org_yaml_snakeyaml_Yaml_wait3(self.jObj, arg0, arg1);
        if (obj is error) {
            InterruptedException e = InterruptedException(message = obj.reason(), cause = obj);
            return e;
        }
    }
};

# Constructor function to generate an object of type `Yaml` representing the `org/yaml/snakeyaml/Yaml` Java class.
#
# + return - The new `Yaml` object generated.
function newYaml1() returns Yaml {
    handle obj = org_yaml_snakeyaml_Yaml_newYaml1();
    Yaml _yaml = new(obj);
    return _yaml;
}

# Constructor function to generate an object of type `Yaml` representing the `org/yaml/snakeyaml/Yaml` Java class.
#
# + arg0 - The `BaseConstructor` value required to map with the Java constructor parameter.
# + return - The new `Yaml` object generated.
function newYaml2(BaseConstructor arg0) returns Yaml {
    handle obj = org_yaml_snakeyaml_Yaml_newYaml2(arg0.jObj);
    Yaml _yaml = new(obj);
    return _yaml;
}

# Constructor function to generate an object of type `Yaml` representing the `org/yaml/snakeyaml/Yaml` Java class.
#
# + arg0 - The `BaseConstructor` value required to map with the Java constructor parameter.
# + arg1 - The `Representer` value required to map with the Java constructor parameter.
# + return - The new `Yaml` object generated.
function newYaml3(BaseConstructor arg0, Representer arg1) returns Yaml {
    handle obj = org_yaml_snakeyaml_Yaml_newYaml3(arg0.jObj, arg1.jObj);
    Yaml _yaml = new(obj);
    return _yaml;
}

# Constructor function to generate an object of type `Yaml` representing the `org/yaml/snakeyaml/Yaml` Java class.
#
# + arg0 - The `BaseConstructor` value required to map with the Java constructor parameter.
# + arg1 - The `Representer` value required to map with the Java constructor parameter.
# + arg2 - The `DumperOptions` value required to map with the Java constructor parameter.
# + return - The new `Yaml` object generated.
function newYaml4(BaseConstructor arg0, Representer arg1, DumperOptions arg2) returns Yaml {
    handle obj = org_yaml_snakeyaml_Yaml_newYaml4(arg0.jObj, arg1.jObj, arg2.jObj);
    Yaml _yaml = new(obj);
    return _yaml;
}

# Constructor function to generate an object of type `Yaml` representing the `org/yaml/snakeyaml/Yaml` Java class.
#
# + arg0 - The `BaseConstructor` value required to map with the Java constructor parameter.
# + arg1 - The `Representer` value required to map with the Java constructor parameter.
# + arg2 - The `DumperOptions` value required to map with the Java constructor parameter.
# + arg3 - The `Resolver` value required to map with the Java constructor parameter.
# + return - The new `Yaml` object generated.
function newYaml5(BaseConstructor arg0, Representer arg1, DumperOptions arg2, Resolver arg3) returns Yaml {
    handle obj = org_yaml_snakeyaml_Yaml_newYaml5(arg0.jObj, arg1.jObj, arg2.jObj, arg3.jObj);
    Yaml _yaml = new(obj);
    return _yaml;
}

# Constructor function to generate an object of type `Yaml` representing the `org/yaml/snakeyaml/Yaml` Java class.
#
# + arg0 - The `DumperOptions` value required to map with the Java constructor parameter.
# + return - The new `Yaml` object generated.
function newYaml6(DumperOptions arg0) returns Yaml {
    handle obj = org_yaml_snakeyaml_Yaml_newYaml6(arg0.jObj);
    Yaml _yaml = new(obj);
    return _yaml;
}

# Constructor function to generate an object of type `Yaml` representing the `org/yaml/snakeyaml/Yaml` Java class.
#
# + arg0 - The `Representer` value required to map with the Java constructor parameter.
# + return - The new `Yaml` object generated.
function newYaml7(Representer arg0) returns Yaml {
    handle obj = org_yaml_snakeyaml_Yaml_newYaml7(arg0.jObj);
    Yaml _yaml = new(obj);
    return _yaml;
}

# Constructor function to generate an object of type `Yaml` representing the `org/yaml/snakeyaml/Yaml` Java class.
#
# + arg0 - The `Representer` value required to map with the Java constructor parameter.
# + arg1 - The `DumperOptions` value required to map with the Java constructor parameter.
# + return - The new `Yaml` object generated.
function newYaml8(Representer arg0, DumperOptions arg1) returns Yaml {
    handle obj = org_yaml_snakeyaml_Yaml_newYaml8(arg0.jObj, arg1.jObj);
    Yaml _yaml = new(obj);
    return _yaml;
}

// External interop functions for mapping public constructors.

function org_yaml_snakeyaml_Yaml_newYaml1() returns handle = @java:Constructor {
    'class: "org.yaml.snakeyaml.Yaml",
    paramTypes: []
} external;

function org_yaml_snakeyaml_Yaml_newYaml2(handle arg0) returns handle = @java:Constructor {
    'class: "org.yaml.snakeyaml.Yaml",
    paramTypes: ["org.yaml.snakeyaml.constructor.BaseConstructor"]
} external;

function org_yaml_snakeyaml_Yaml_newYaml3(handle arg0, handle arg1) returns handle = @java:Constructor {
    'class: "org.yaml.snakeyaml.Yaml",
    paramTypes: ["org.yaml.snakeyaml.constructor.BaseConstructor", "org.yaml.snakeyaml.representer.Representer"]
} external;

function org_yaml_snakeyaml_Yaml_newYaml4(handle arg0, handle arg1, handle arg2) returns handle = @java:Constructor {
    'class: "org.yaml.snakeyaml.Yaml",
    paramTypes: ["org.yaml.snakeyaml.constructor.BaseConstructor", "org.yaml.snakeyaml.representer.Representer", "org.yaml.snakeyaml.DumperOptions"]
} external;

function org_yaml_snakeyaml_Yaml_newYaml5(handle arg0, handle arg1, handle arg2, handle arg3) returns handle = @java:Constructor {
    'class: "org.yaml.snakeyaml.Yaml",
    paramTypes: ["org.yaml.snakeyaml.constructor.BaseConstructor", "org.yaml.snakeyaml.representer.Representer", "org.yaml.snakeyaml.DumperOptions", "org.yaml.snakeyaml.resolver.Resolver"]
} external;

function org_yaml_snakeyaml_Yaml_newYaml6(handle arg0) returns handle = @java:Constructor {
    'class: "org.yaml.snakeyaml.Yaml",
    paramTypes: ["org.yaml.snakeyaml.DumperOptions"]
} external;

function org_yaml_snakeyaml_Yaml_newYaml7(handle arg0) returns handle = @java:Constructor {
    'class: "org.yaml.snakeyaml.Yaml",
    paramTypes: ["org.yaml.snakeyaml.representer.Representer"]
} external;

function org_yaml_snakeyaml_Yaml_newYaml8(handle arg0, handle arg1) returns handle = @java:Constructor {
    'class: "org.yaml.snakeyaml.Yaml",
    paramTypes: ["org.yaml.snakeyaml.representer.Representer", "org.yaml.snakeyaml.DumperOptions"]
} external;

// External interop functions for mapping public methods.

function org_yaml_snakeyaml_Yaml_addImplicitResolver(handle receiver, handle arg0, handle arg1, handle arg2) = @java:Method {
    name: "addImplicitResolver",
    'class: "org.yaml.snakeyaml.Yaml",
    paramTypes: ["org.yaml.snakeyaml.nodes.Tag", "java.util.regex.Pattern", "java.lang.String"]
} external;

function org_yaml_snakeyaml_Yaml_compose(handle receiver, handle arg0) returns handle = @java:Method {
    name: "compose",
    'class: "org.yaml.snakeyaml.Yaml",
    paramTypes: ["java.io.Reader"]
} external;

function org_yaml_snakeyaml_Yaml_composeAll(handle receiver, handle arg0) returns handle = @java:Method {
    name: "composeAll",
    'class: "org.yaml.snakeyaml.Yaml",
    paramTypes: ["java.io.Reader"]
} external;

function org_yaml_snakeyaml_Yaml_dump1(handle receiver, handle arg0) returns handle = @java:Method {
    name: "dump",
    'class: "org.yaml.snakeyaml.Yaml",
    paramTypes: ["java.lang.Object"]
} external;

function org_yaml_snakeyaml_Yaml_dump2(handle receiver, handle arg0, handle arg1) = @java:Method {
    name: "dump",
    'class: "org.yaml.snakeyaml.Yaml",
    paramTypes: ["java.lang.Object", "java.io.Writer"]
} external;

function org_yaml_snakeyaml_Yaml_dumpAll1(handle receiver, handle arg0) returns handle = @java:Method {
    name: "dumpAll",
    'class: "org.yaml.snakeyaml.Yaml",
    paramTypes: ["java.util.Iterator"]
} external;

function org_yaml_snakeyaml_Yaml_dumpAll2(handle receiver, handle arg0, handle arg1) = @java:Method {
    name: "dumpAll",
    'class: "org.yaml.snakeyaml.Yaml",
    paramTypes: ["java.util.Iterator", "java.io.Writer"]
} external;

function org_yaml_snakeyaml_Yaml_dumpAs(handle receiver, handle arg0, handle arg1, handle arg2) returns handle = @java:Method {
    name: "dumpAs",
    'class: "org.yaml.snakeyaml.Yaml",
    paramTypes: ["java.lang.Object", "org.yaml.snakeyaml.nodes.Tag", "org.yaml.snakeyaml.DumperOptions$FlowStyle"]
} external;

function org_yaml_snakeyaml_Yaml_dumpAsMap(handle receiver, handle arg0) returns handle = @java:Method {
    name: "dumpAsMap",
    'class: "org.yaml.snakeyaml.Yaml",
    paramTypes: ["java.lang.Object"]
} external;

function org_yaml_snakeyaml_Yaml_equals(handle receiver, handle arg0) returns boolean = @java:Method {
    name: "equals",
    'class: "org.yaml.snakeyaml.Yaml",
    paramTypes: ["java.lang.Object"]
} external;

function org_yaml_snakeyaml_Yaml_getClass(handle receiver) returns handle = @java:Method {
    name: "getClass",
    'class: "org.yaml.snakeyaml.Yaml",
    paramTypes: []
} external;

function org_yaml_snakeyaml_Yaml_getName(handle receiver) returns handle = @java:Method {
    name: "getName",
    'class: "org.yaml.snakeyaml.Yaml",
    paramTypes: []
} external;

function org_yaml_snakeyaml_Yaml_hashCode(handle receiver) returns int = @java:Method {
    name: "hashCode",
    'class: "org.yaml.snakeyaml.Yaml",
    paramTypes: []
} external;

function org_yaml_snakeyaml_Yaml_load1(handle receiver, handle arg0) returns handle = @java:Method {
    name: "load",
    'class: "org.yaml.snakeyaml.Yaml",
    paramTypes: ["java.io.InputStream"]
} external;

function org_yaml_snakeyaml_Yaml_load2(handle receiver, handle arg0) returns handle = @java:Method {
    name: "load",
    'class: "org.yaml.snakeyaml.Yaml",
    paramTypes: ["java.io.Reader"]
} external;

function org_yaml_snakeyaml_Yaml_load3(handle receiver, handle arg0) returns handle = @java:Method {
    name: "load",
    'class: "org.yaml.snakeyaml.Yaml",
    paramTypes: ["java.lang.String"]
} external;

function org_yaml_snakeyaml_Yaml_loadAll1(handle receiver, handle arg0) returns handle = @java:Method {
    name: "loadAll",
    'class: "org.yaml.snakeyaml.Yaml",
    paramTypes: ["java.io.InputStream"]
} external;

function org_yaml_snakeyaml_Yaml_loadAll2(handle receiver, handle arg0) returns handle = @java:Method {
    name: "loadAll",
    'class: "org.yaml.snakeyaml.Yaml",
    paramTypes: ["java.io.Reader"]
} external;

function org_yaml_snakeyaml_Yaml_loadAll3(handle receiver, handle arg0) returns handle = @java:Method {
    name: "loadAll",
    'class: "org.yaml.snakeyaml.Yaml",
    paramTypes: ["java.lang.String"]
} external;

function org_yaml_snakeyaml_Yaml_loadAs1(handle receiver, handle arg0, handle arg1) returns handle = @java:Method {
    name: "loadAs",
    'class: "org.yaml.snakeyaml.Yaml",
    paramTypes: ["java.io.InputStream", "java.lang.Class"]
} external;

function org_yaml_snakeyaml_Yaml_loadAs2(handle receiver, handle arg0, handle arg1) returns handle = @java:Method {
    name: "loadAs",
    'class: "org.yaml.snakeyaml.Yaml",
    paramTypes: ["java.io.Reader", "java.lang.Class"]
} external;

function org_yaml_snakeyaml_Yaml_loadAs3(handle receiver, handle arg0, handle arg1) returns handle = @java:Method {
    name: "loadAs",
    'class: "org.yaml.snakeyaml.Yaml",
    paramTypes: ["java.lang.String", "java.lang.Class"]
} external;

function org_yaml_snakeyaml_Yaml_notify(handle receiver) = @java:Method {
    name: "notify",
    'class: "org.yaml.snakeyaml.Yaml",
    paramTypes: []
} external;

function org_yaml_snakeyaml_Yaml_notifyAll(handle receiver) = @java:Method {
    name: "notifyAll",
    'class: "org.yaml.snakeyaml.Yaml",
    paramTypes: []
} external;

function org_yaml_snakeyaml_Yaml_parse(handle receiver, handle arg0) returns handle = @java:Method {
    name: "parse",
    'class: "org.yaml.snakeyaml.Yaml",
    paramTypes: ["java.io.Reader"]
} external;

function org_yaml_snakeyaml_Yaml_represent(handle receiver, handle arg0) returns handle = @java:Method {
    name: "represent",
    'class: "org.yaml.snakeyaml.Yaml",
    paramTypes: ["java.lang.Object"]
} external;

function org_yaml_snakeyaml_Yaml_serialize(handle receiver, handle arg0) returns handle = @java:Method {
    name: "serialize",
    'class: "org.yaml.snakeyaml.Yaml",
    paramTypes: ["org.yaml.snakeyaml.nodes.Node"]
} external;

function org_yaml_snakeyaml_Yaml_setBeanAccess(handle receiver, handle arg0) = @java:Method {
    name: "setBeanAccess",
    'class: "org.yaml.snakeyaml.Yaml",
    paramTypes: ["org.yaml.snakeyaml.introspector.BeanAccess"]
} external;

function org_yaml_snakeyaml_Yaml_setName(handle receiver, handle arg0) = @java:Method {
    name: "setName",
    'class: "org.yaml.snakeyaml.Yaml",
    paramTypes: ["java.lang.String"]
} external;

function org_yaml_snakeyaml_Yaml_wait1(handle receiver) returns error? = @java:Method {
    name: "wait",
    'class: "org.yaml.snakeyaml.Yaml",
    paramTypes: []
} external;

function org_yaml_snakeyaml_Yaml_wait2(handle receiver, int arg0) returns error? = @java:Method {
    name: "wait",
    'class: "org.yaml.snakeyaml.Yaml",
    paramTypes: ["long"]
} external;

function org_yaml_snakeyaml_Yaml_wait3(handle receiver, int arg0, int arg1) returns error? = @java:Method {
    name: "wait",
    'class: "org.yaml.snakeyaml.Yaml",
    paramTypes: ["long", "int"]
} external;


