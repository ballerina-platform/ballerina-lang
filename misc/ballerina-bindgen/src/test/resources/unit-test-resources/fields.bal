import ballerina/jballerina.java;
import ballerinax/java.arrays as jarrays;

# Ballerina class mapping for the Java `org.ballerinalang.bindgen.FieldsTestResource` class.
@java:Binding {'class: "org.ballerinalang.bindgen.FieldsTestResource"}
distinct class FieldsTestResource {

    *java:JObject;

    handle jObj;

    # The init function of the Ballerina class mapping the `org.ballerinalang.bindgen.FieldsTestResource` Java class.
    #
    # + obj - The `handle` value containing the Java reference of the object.
    function init(handle obj) {
        self.jObj = obj;
    }

    # The function to retrieve the string representation of the Ballerina class mapping the `org.ballerinalang.bindgen.FieldsTestResource` Java class.
    #
    # + return - The `string` form of the Java object instance.
    function toString() returns string {
        return java:toString(self.jObj) ?: "null";
    }
    *Object;
    # The function that retrieves the value of the public field `getGetInstanceByte`.
    #
    # + return - The `byte` value of the field.
    function getGetInstanceByte(byte arg) returns byte {
        return org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceByte(self.jObj, arg);
    }

    # The function to set the value of the public field `setGetInstanceByte`.
    #
    # + arg - The `byte` value that is to be set for the field.
    function setGetInstanceByte(byte arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceByte(self.jObj, arg);
    }

    # The function that retrieves the value of the public field `getGetInstanceChar`.
    #
    # + return - The `int` value of the field.
    function getGetInstanceChar(int arg) returns int {
        return org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceChar(self.jObj, arg);
    }

    # The function to set the value of the public field `setGetInstanceChar`.
    #
    # + arg - The `int` value that is to be set for the field.
    function setGetInstanceChar(int arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceChar(self.jObj, arg);
    }

    # The function that retrieves the value of the public field `getGetInstanceShort`.
    #
    # + return - The `int` value of the field.
    function getGetInstanceShort(int arg) returns int {
        return org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceShort(self.jObj, arg);
    }

    # The function to set the value of the public field `setGetInstanceShort`.
    #
    # + arg - The `int` value that is to be set for the field.
    function setGetInstanceShort(int arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceShort(self.jObj, arg);
    }

    # The function that retrieves the value of the public field `getGetInstanceInt`.
    #
    # + return - The `int` value of the field.
    function getGetInstanceInt(int arg) returns int {
        return org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceInt(self.jObj, arg);
    }

    # The function to set the value of the public field `setGetInstanceInt`.
    #
    # + arg - The `int` value that is to be set for the field.
    function setGetInstanceInt(int arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceInt(self.jObj, arg);
    }

    # The function that retrieves the value of the public field `getGetInstanceLong`.
    #
    # + return - The `int` value of the field.
    function getGetInstanceLong(int arg) returns int {
        return org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceLong(self.jObj, arg);
    }

    # The function to set the value of the public field `setGetInstanceLong`.
    #
    # + arg - The `int` value that is to be set for the field.
    function setGetInstanceLong(int arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceLong(self.jObj, arg);
    }

    # The function that retrieves the value of the public field `getGetInstanceFloat`.
    #
    # + return - The `float` value of the field.
    function getGetInstanceFloat(float arg) returns float {
        return org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceFloat(self.jObj, arg);
    }

    # The function to set the value of the public field `setGetInstanceFloat`.
    #
    # + arg - The `float` value that is to be set for the field.
    function setGetInstanceFloat(float arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceFloat(self.jObj, arg);
    }

    # The function that retrieves the value of the public field `getGetInstanceDouble`.
    #
    # + return - The `float` value of the field.
    function getGetInstanceDouble(float arg) returns float {
        return org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceDouble(self.jObj, arg);
    }

    # The function to set the value of the public field `setGetInstanceDouble`.
    #
    # + arg - The `float` value that is to be set for the field.
    function setGetInstanceDouble(float arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceDouble(self.jObj, arg);
    }

    # The function that retrieves the value of the public field `getGetInstanceBoolean`.
    #
    # + return - The `boolean` value of the field.
    function getGetInstanceBoolean(boolean arg) returns boolean {
        return org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceBoolean(self.jObj, arg);
    }

    # The function to set the value of the public field `setGetInstanceBoolean`.
    #
    # + arg - The `boolean` value that is to be set for the field.
    function setGetInstanceBoolean(boolean arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceBoolean(self.jObj, arg);
    }

    # The function that retrieves the value of the public field `getGetInstanceString`.
    #
    # + return - The `string` value of the field.
    function getGetInstanceString(string arg) returns string? {
        return java:toString(org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceString(self.jObj,
        java:fromString(arg)));
    }

    # The function to set the value of the public field `setGetInstanceString`.
    #
    # + arg - The `string` value that is to be set for the field.
    function setGetInstanceString(string arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceString(self.jObj, java:fromString(arg));
    }

    # The function that retrieves the value of the public field `getGetInstanceByteArray`.
    #
    # + return - The `byte[]` value of the field.
    function getGetInstanceByteArray(byte[] arg) returns byte[]|error {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceByteArray(self.jObj, check
        jarrays:toHandle(arg, "byte"));
        return <byte[]>check jarrays:fromHandle(externalObj, "byte[]");
    }

    # The function to set the value of the public field `setGetInstanceByteArray`.
    #
    # + arg - The `byte[]` value that is to be set for the field.
    function setGetInstanceByteArray(byte[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceByteArray(self.jObj, check jarrays:toHandle(arg,
        "byte"));
    }

    # The function that retrieves the value of the public field `getGetInstanceCharArray`.
    #
    # + return - The `int[]` value of the field.
    function getGetInstanceCharArray(int[] arg) returns int[]|error {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceCharArray(self.jObj, check
        jarrays:toHandle(arg, "char"));
        return <int[]>check jarrays:fromHandle(externalObj, "int[]");
    }

    # The function to set the value of the public field `setGetInstanceCharArray`.
    #
    # + arg - The `int[]` value that is to be set for the field.
    function setGetInstanceCharArray(int[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceCharArray(self.jObj, check jarrays:toHandle(arg,
        "char"));
    }

    # The function that retrieves the value of the public field `getGetInstanceShortArray`.
    #
    # + return - The `int[]` value of the field.
    function getGetInstanceShortArray(int[] arg) returns int[]|error {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceShortArray(self.jObj, check
        jarrays:toHandle(arg, "short"));
        return <int[]>check jarrays:fromHandle(externalObj, "int[]");
    }

    # The function to set the value of the public field `setGetInstanceShortArray`.
    #
    # + arg - The `int[]` value that is to be set for the field.
    function setGetInstanceShortArray(int[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceShortArray(self.jObj, check jarrays:toHandle(arg,
        "short"));
    }

    # The function that retrieves the value of the public field `getGetInstanceIntArray`.
    #
    # + return - The `int[]` value of the field.
    function getGetInstanceIntArray(int[] arg) returns int[]|error {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceIntArray(self.jObj, check
        jarrays:toHandle(arg, "int"));
        return <int[]>check jarrays:fromHandle(externalObj, "int[]");
    }

    # The function to set the value of the public field `setGetInstanceIntArray`.
    #
    # + arg - The `int[]` value that is to be set for the field.
    function setGetInstanceIntArray(int[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceIntArray(self.jObj, check
        jarrays:toHandle(arg, "int"));
    }

    # The function that retrieves the value of the public field `getGetInstanceLongArray`.
    #
    # + return - The `int[]` value of the field.
    function getGetInstanceLongArray(int[] arg) returns int[]|error {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceLongArray(self.jObj, check
        jarrays:toHandle(arg, "long"));
        return <int[]>check jarrays:fromHandle(externalObj, "int[]");
    }

    # The function to set the value of the public field `setGetInstanceLongArray`.
    #
    # + arg - The `int[]` value that is to be set for the field.
    function setGetInstanceLongArray(int[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceLongArray(self.jObj, check jarrays:toHandle(arg,
        "long"));
    }

    # The function that retrieves the value of the public field `getGetInstanceFloatArray`.
    #
    # + return - The `float[]` value of the field.
    function getGetInstanceFloatArray(float[] arg) returns float[]|error {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceFloatArray(self.jObj, check
        jarrays:toHandle(arg, "float"));
        return <float[]>check jarrays:fromHandle(externalObj, "float[]");
    }

    # The function to set the value of the public field `setGetInstanceFloatArray`.
    #
    # + arg - The `float[]` value that is to be set for the field.
    function setGetInstanceFloatArray(float[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceFloatArray(self.jObj, check jarrays:toHandle(arg,
        "float"));
    }

    # The function that retrieves the value of the public field `getGetInstanceDoubleArray`.
    #
    # + return - The `float[]` value of the field.
    function getGetInstanceDoubleArray(float[] arg) returns float[]|error {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceDoubleArray(self.jObj, check
        jarrays:toHandle(arg, "double"));
        return <float[]>check jarrays:fromHandle(externalObj, "float[]");
    }

    # The function to set the value of the public field `setGetInstanceDoubleArray`.
    #
    # + arg - The `float[]` value that is to be set for the field.
    function setGetInstanceDoubleArray(float[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceDoubleArray(self.jObj, check jarrays:toHandle(arg,
        "double"));
    }

    # The function that retrieves the value of the public field `getGetInstanceBooleanArray`.
    #
    # + return - The `boolean[]` value of the field.
    function getGetInstanceBooleanArray(boolean[] arg) returns boolean[]|error {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceBooleanArray(self.jObj, check
        jarrays:toHandle(arg, "boolean"));
        return <boolean[]>check jarrays:fromHandle(externalObj, "boolean[]");
    }

    # The function to set the value of the public field `setGetInstanceBooleanArray`.
    #
    # + arg - The `boolean[]` value that is to be set for the field.
    function setGetInstanceBooleanArray(boolean[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceBooleanArray(self.jObj, check jarrays:toHandle(arg,
        "boolean"));
    }

    # The function that retrieves the value of the public field `getGetInstanceStringArray`.
    #
    # + return - The `string[]` value of the field.
    function getGetInstanceStringArray(string[] arg) returns string[]|error {
        return <string[]>check jarrays:fromHandle(org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceStringArray(
        self.jObj, check jarrays:toHandle(arg, "java.lang.String")), "string");
    }

    # The function to set the value of the public field `setGetInstanceStringArray`.
    #
    # + arg - The `string[]` value that is to be set for the field.
    function setGetInstanceStringArray(string[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceStringArray(self.jObj, check jarrays:toHandle(arg,
        "java.lang.String"));
    }

    # The function that retrieves the value of the public field `getGetInstanceObjectArray`.
    #
    # + return - The `StringBuilder[]` value of the field.
    function getGetInstanceObjectArray(StringBuilder[] arg) returns StringBuilder[]|error {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceObjectArray(self.jObj, check
        jarrays:toHandle(arg, "java.lang.StringBuilder"));
        StringBuilder[] newObj = [];
        handle[] anyObj = <handle[]>check jarrays:fromHandle(externalObj, "handle");
        int count = anyObj.length();
        foreach int i in 0 ... count - 1 {
            StringBuilder[] element = new (anyObj[i]);
            newObj[i] = element;
        }
        return newObj;
    }

    # The function to set the value of the public field `setGetInstanceObjectArray`.
    #
    # + arg - The `StringBuilder[]` value that is to be set for the field.
    function setGetInstanceObjectArray(StringBuilder[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceObjectArray(self.jObj, check jarrays:toHandle(arg,
        "java.lang.StringBuilder"));
    }

    # The function that retrieves the value of the public field `getGetInstanceObjectMultiArray1`.
    #
    # + return - The `Integer[]` value of the field.
    function getGetInstanceObjectMultiArray1(Integer[] arg) returns Integer[]|error {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceObjectMultiArray1(self.jObj, check
        jarrays:toHandle(arg, "java.lang.Integer"));
        Integer[] newObj = [];
        handle[] anyObj = <handle[]>check jarrays:fromHandle(externalObj, "handle");
        int count = anyObj.length();
        foreach int i in 0 ... count - 1 {
            Integer[] element = new (anyObj[i]);
            newObj[i] = element;
        }
        return newObj;
    }

    # The function to set the value of the public field `setGetInstanceObjectMultiArray1`.
    #
    # + arg - The `Integer[]` value that is to be set for the field.
    function setGetInstanceObjectMultiArray1(Integer[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceObjectMultiArray1(self.jObj, check jarrays:toHandle(
        arg, "java.lang.Integer"));
    }

    # The function that retrieves the value of the public field `getGetInstanceObjectMultiArray2`.
    #
    # + return - The `Object[]` value of the field.
    function getGetInstanceObjectMultiArray2(Object[] arg) returns Object[]|error {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceObjectMultiArray2(self.jObj, check
        jarrays:toHandle(arg, "java.lang.Object"));
        Object[] newObj = [];
        handle[] anyObj = <handle[]>check jarrays:fromHandle(externalObj, "handle");
        int count = anyObj.length();
        foreach int i in 0 ... count - 1 {
            Object[] element = new (anyObj[i]);
            newObj[i] = element;
        }
        return newObj;
    }

    # The function to set the value of the public field `setGetInstanceObjectMultiArray2`.
    #
    # + arg - The `Object[]` value that is to be set for the field.
    function setGetInstanceObjectMultiArray2(Object[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceObjectMultiArray2(self.jObj, check jarrays:toHandle(
        arg, "java.lang.Object"));
    }

    # The function that retrieves the value of the public field `getGetInstanceInterface`.
    #
    # + return - The `List` value of the field.
    function getGetInstanceInterface(List arg) returns List {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceInterface(self.jObj, arg.jObj);
        List newObj = new (externalObj);
        return newObj;
    }

    # The function to set the value of the public field `setGetInstanceInterface`.
    #
    # + arg - The `List` value that is to be set for the field.
    function setGetInstanceInterface(List arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceInterface(self.jObj, arg.jObj);
    }

    # The function that retrieves the value of the public field `getGetInstanceAbstractClass`.
    #
    # + return - The `AbstractList` value of the field.
    function getGetInstanceAbstractClass(AbstractList arg) returns AbstractList {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceAbstractClass(self.jObj, arg.
        jObj);
        AbstractList newObj = new (externalObj);
        return newObj;
    }

    # The function to set the value of the public field `setGetInstanceAbstractClass`.
    #
    # + arg - The `AbstractList` value that is to be set for the field.
    function setGetInstanceAbstractClass(AbstractList arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceAbstractClass(self.jObj, arg.jObj);
    }

    # The function that retrieves the value of the public field `getGetInstanceObject`.
    #
    # + return - The `Path` value of the field.
    function getGetInstanceObject(Path arg) returns Path {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceObject(self.jObj, arg.jObj);
        Path newObj = new (externalObj);
        return newObj;
    }

    # The function to set the value of the public field `setGetInstanceObject`.
    #
    # + arg - The `Path` value that is to be set for the field.
    function setGetInstanceObject(Path arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceObject(self.jObj, arg.jObj);
    }

    # The function that retrieves the value of the public field `getGetInstanceGenericObject`.
    #
    # + return - The `Set` value of the field.
    function getGetInstanceGenericObject(Set arg) returns Set {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceGenericObject(self.jObj, arg.
        jObj);
        Set newObj = new (externalObj);
        return newObj;
    }

    # The function to set the value of the public field `setGetInstanceGenericObject`.
    #
    # + arg - The `Set` value that is to be set for the field.
    function setGetInstanceGenericObject(Set arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceGenericObject(self.jObj, arg.jObj);
    }

    # The function that retrieves the value of the public field `getGetInstanceEnumeration`.
    #
    # + return - The `Level` value of the field.
    function getGetInstanceEnumeration(Level arg) returns Level {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceEnumeration(self.jObj, arg.jObj);
        Level newObj = new (externalObj);
        return newObj;
    }

    # The function to set the value of the public field `setGetInstanceEnumeration`.
    #
    # + arg - The `Level` value that is to be set for the field.
    function setGetInstanceEnumeration(Level arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceEnumeration(self.jObj, arg.jObj);
    }

    # The function that retrieves the value of the public field `getGetInstanceThrowable`.
    #
    # + return - The `ArithmeticException` value of the field.
    function getGetInstanceThrowable(JArithmeticException arg) returns JArithmeticException {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceThrowable(self.jObj, arg.jObj);
        JArithmeticException newObj = new (externalObj);
        return newObj;
    }

    # The function to set the value of the public field `setGetInstanceThrowable`.
    #
    # + arg - The `JArithmeticException` value that is to be set for the field.
    function setGetInstanceThrowable(JArithmeticException arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceThrowable(self.jObj, arg.jObj);
    }

    # The function that retrieves the value of the public field `getGetStaticObjectArray`.
    #
    # + return - The `StringBuilder[]` value of the field.
    function getGetStaticObjectArray(StringBuilder[] arg) returns StringBuilder[]|error {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticObjectArray(self.jObj, check
        jarrays:toHandle(arg, "java.lang.StringBuilder"));
        StringBuilder[] newObj = [];
        handle[] anyObj = <handle[]>check jarrays:fromHandle(externalObj, "handle");
        int count = anyObj.length();
        foreach int i in 0 ... count - 1 {
            StringBuilder[] element = new (anyObj[i]);
            newObj[i] = element;
        }
        return newObj;
    }

    # The function to set the value of the public field `setGetStaticObjectArray`.
    #
    # + arg - The `StringBuilder[]` value that is to be set for the field.
    function setGetStaticObjectArray(StringBuilder[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetStaticObjectArray(self.jObj, check jarrays:toHandle(arg,
        "java.lang.StringBuilder"));
    }

    # The function that retrieves the value of the public field `getGetStaticObjectMultiArray1`.
    #
    # + return - The `Integer[]` value of the field.
    function getGetStaticObjectMultiArray1(Integer[] arg) returns Integer[]|error {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticObjectMultiArray1(self.jObj, check
        jarrays:toHandle(arg, "java.lang.Integer"));
        Integer[] newObj = [];
        handle[] anyObj = <handle[]>check jarrays:fromHandle(externalObj, "handle");
        int count = anyObj.length();
        foreach int i in 0 ... count - 1 {
            Integer[] element = new (anyObj[i]);
            newObj[i] = element;
        }
        return newObj;
    }

    # The function to set the value of the public field `setGetStaticObjectMultiArray1`.
    #
    # + arg - The `Integer[]` value that is to be set for the field.
    function setGetStaticObjectMultiArray1(Integer[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetStaticObjectMultiArray1(self.jObj, check jarrays:toHandle(arg,
        "java.lang.Integer"));
    }

    # The function that retrieves the value of the public field `getGetStaticObjectMultiArray2`.
    #
    # + return - The `Object[]` value of the field.
    function getGetStaticObjectMultiArray2(Object[] arg) returns Object[]|error {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticObjectMultiArray2(self.jObj, check
        jarrays:toHandle(arg, "java.lang.Object"));
        Object[] newObj = [];
        handle[] anyObj = <handle[]>check jarrays:fromHandle(externalObj, "handle");
        int count = anyObj.length();
        foreach int i in 0 ... count - 1 {
            Object[] element = new (anyObj[i]);
            newObj[i] = element;
        }
        return newObj;
    }

    # The function to set the value of the public field `setGetStaticObjectMultiArray2`.
    #
    # + arg - The `Object[]` value that is to be set for the field.
    function setGetStaticObjectMultiArray2(Object[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetStaticObjectMultiArray2(self.jObj, check jarrays:toHandle(arg,
        "java.lang.Object"));
    }
}

# The constructor function to generate an object of type `org.ballerinalang.bindgen.FieldsTestResource`.
#
# + return - The new `FieldsTestResource` class generated.
function newFieldsTestResource1() returns FieldsTestResource {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_newFieldsTestResource1();
    FieldsTestResource newObj = new (externalObj);
    return newObj;
}

# The function that retrieves the value of the public field `FieldsTestResource_getGetStaticByte`.
#
# + return - The `byte` value of the field.
function FieldsTestResource_getGetStaticByte(byte arg) returns byte {
    return org_ballerinalang_bindgen_FieldsTestResource_getGetStaticByte(arg);
}

# The function to set the value of the public field `FieldsTestResource_setGetStaticByte`.
#
# + arg - The `byte` value that is to be set for the field.
function FieldsTestResource_setGetStaticByte(byte arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticByte(arg);
}

# The function that retrieves the value of the public field `FieldsTestResource_getGetStaticChar`.
#
# + return - The `int` value of the field.
function FieldsTestResource_getGetStaticChar(int arg) returns int {
    return org_ballerinalang_bindgen_FieldsTestResource_getGetStaticChar(arg);
}

# The function to set the value of the public field `FieldsTestResource_setGetStaticChar`.
#
# + arg - The `int` value that is to be set for the field.
function FieldsTestResource_setGetStaticChar(int arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticChar(arg);
}

# The function that retrieves the value of the public field `FieldsTestResource_getGetStaticShort`.
#
# + return - The `int` value of the field.
function FieldsTestResource_getGetStaticShort(int arg) returns int {
    return org_ballerinalang_bindgen_FieldsTestResource_getGetStaticShort(arg);
}

# The function to set the value of the public field `FieldsTestResource_setGetStaticShort`.
#
# + arg - The `int` value that is to be set for the field.
function FieldsTestResource_setGetStaticShort(int arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticShort(arg);
}

# The function that retrieves the value of the public field `FieldsTestResource_getGetStaticInt`.
#
# + return - The `int` value of the field.
function FieldsTestResource_getGetStaticInt(int arg) returns int {
    return org_ballerinalang_bindgen_FieldsTestResource_getGetStaticInt(arg);
}

# The function to set the value of the public field `FieldsTestResource_setGetStaticInt`.
#
# + arg - The `int` value that is to be set for the field.
function FieldsTestResource_setGetStaticInt(int arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticInt(arg);
}

# The function that retrieves the value of the public field `FieldsTestResource_getGetStaticLong`.
#
# + return - The `int` value of the field.
function FieldsTestResource_getGetStaticLong(int arg) returns int {
    return org_ballerinalang_bindgen_FieldsTestResource_getGetStaticLong(arg);
}

# The function to set the value of the public field `FieldsTestResource_setGetStaticLong`.
#
# + arg - The `int` value that is to be set for the field.
function FieldsTestResource_setGetStaticLong(int arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticLong(arg);
}

# The function that retrieves the value of the public field `FieldsTestResource_getGetStaticFloat`.
#
# + return - The `float` value of the field.
function FieldsTestResource_getGetStaticFloat(float arg) returns float {
    return org_ballerinalang_bindgen_FieldsTestResource_getGetStaticFloat(arg);
}

# The function to set the value of the public field `FieldsTestResource_setGetStaticFloat`.
#
# + arg - The `float` value that is to be set for the field.
function FieldsTestResource_setGetStaticFloat(float arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticFloat(arg);
}

# The function that retrieves the value of the public field `FieldsTestResource_getGetStaticDouble`.
#
# + return - The `float` value of the field.
function FieldsTestResource_getGetStaticDouble(float arg) returns float {
    return org_ballerinalang_bindgen_FieldsTestResource_getGetStaticDouble(arg);
}

# The function to set the value of the public field `FieldsTestResource_setGetStaticDouble`.
#
# + arg - The `float` value that is to be set for the field.
function FieldsTestResource_setGetStaticDouble(float arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticDouble(arg);
}

# The function that retrieves the value of the public field `FieldsTestResource_getGetStaticBoolean`.
#
# + return - The `boolean` value of the field.
function FieldsTestResource_getGetStaticBoolean(boolean arg) returns boolean {
    return org_ballerinalang_bindgen_FieldsTestResource_getGetStaticBoolean(arg);
}

# The function to set the value of the public field `FieldsTestResource_setGetStaticBoolean`.
#
# + arg - The `boolean` value that is to be set for the field.
function FieldsTestResource_setGetStaticBoolean(boolean arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticBoolean(arg);
}

# The function that retrieves the value of the public field `FieldsTestResource_getGetStaticString`.
#
# + return - The `string` value of the field.
function FieldsTestResource_getGetStaticString(string arg) returns string? {
    return java:toString(org_ballerinalang_bindgen_FieldsTestResource_getGetStaticString(java:fromString(arg)));
}

# The function to set the value of the public field `FieldsTestResource_setGetStaticString`.
#
# + arg - The `string` value that is to be set for the field.
function FieldsTestResource_setGetStaticString(string arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticString(java:fromString(arg));
}

# The function that retrieves the value of the public field `FieldsTestResource_getGET_STATIC_FINAL_BYTE`.
#
# + return - The `byte` value of the field.
function FieldsTestResource_getGET_STATIC_FINAL_BYTE(byte arg) returns byte {
    return org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_BYTE(arg);
}

# The function that retrieves the value of the public field `FieldsTestResource_getGET_STATIC_FINAL_CHAR`.
#
# + return - The `int` value of the field.
function FieldsTestResource_getGET_STATIC_FINAL_CHAR(int arg) returns int {
    return org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_CHAR(arg);
}

# The function that retrieves the value of the public field `FieldsTestResource_getGET_STATIC_FINAL_SHORT`.
#
# + return - The `int` value of the field.
function FieldsTestResource_getGET_STATIC_FINAL_SHORT(int arg) returns int {
    return org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_SHORT(arg);
}

# The function that retrieves the value of the public field `FieldsTestResource_getGET_STATIC_FINAL_INT`.
#
# + return - The `int` value of the field.
function FieldsTestResource_getGET_STATIC_FINAL_INT(int arg) returns int {
    return org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_INT(arg);
}

# The function that retrieves the value of the public field `FieldsTestResource_getGET_STATIC_FINAL_LONG`.
#
# + return - The `int` value of the field.
function FieldsTestResource_getGET_STATIC_FINAL_LONG(int arg) returns int {
    return org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_LONG(arg);
}

# The function that retrieves the value of the public field `FieldsTestResource_getGET_STATIC_FINAL_FLOAT`.
#
# + return - The `float` value of the field.
function FieldsTestResource_getGET_STATIC_FINAL_FLOAT(float arg) returns float {
    return org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_FLOAT(arg);
}

# The function that retrieves the value of the public field `FieldsTestResource_getGET_STATIC_FINAL_DOUBLE`.
#
# + return - The `float` value of the field.
function FieldsTestResource_getGET_STATIC_FINAL_DOUBLE(float arg) returns float {
    return org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_DOUBLE(arg);
}

# The function that retrieves the value of the public field `FieldsTestResource_getGET_STATIC_FINAL_BOOLEAN`.
#
# + return - The `boolean` value of the field.
function FieldsTestResource_getGET_STATIC_FINAL_BOOLEAN(boolean arg) returns boolean {
    return org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_BOOLEAN(arg);
}

# The function that retrieves the value of the public field `FieldsTestResource_getGET_STATIC_FINAL_STRING`.
#
# + return - The `string` value of the field.
function FieldsTestResource_getGET_STATIC_FINAL_STRING(string arg) returns string? {
    return java:toString(org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_STRING(java:fromString(arg)));
}

# The function that retrieves the value of the public field `FieldsTestResource_getGetStaticByteArray`.
#
# + return - The `byte[]` value of the field.
function FieldsTestResource_getGetStaticByteArray(byte[] arg) returns byte[]|error {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticByteArray(check jarrays:toHandle(arg,
    "byte"));
    return <byte[]>check jarrays:fromHandle(externalObj, "byte[]");
}

# The function to set the value of the public field `FieldsTestResource_setGetStaticByteArray`.
#
# + arg - The `byte[]` value that is to be set for the field.
function FieldsTestResource_setGetStaticByteArray(byte[] arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticByteArray(check jarrays:toHandle(arg, "byte"));
}

# The function that retrieves the value of the public field `FieldsTestResource_getGetStaticCharArray`.
#
# + return - The `int[]` value of the field.
function FieldsTestResource_getGetStaticCharArray(int[] arg) returns int[]|error {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticCharArray(check jarrays:toHandle(arg,
    "char"));
    return <int[]>check jarrays:fromHandle(externalObj, "int[]");
}

# The function to set the value of the public field `FieldsTestResource_setGetStaticCharArray`.
#
# + arg - The `int[]` value that is to be set for the field.
function FieldsTestResource_setGetStaticCharArray(int[] arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticCharArray(check jarrays:toHandle(arg, "char"));
}

# The function that retrieves the value of the public field `FieldsTestResource_getGetStaticShortArray`.
#
# + return - The `int[]` value of the field.
function FieldsTestResource_getGetStaticShortArray(int[] arg) returns int[]|error {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticShortArray(check jarrays:toHandle(arg,
    "short"));
    return <int[]>check jarrays:fromHandle(externalObj, "int[]");
}

# The function to set the value of the public field `FieldsTestResource_setGetStaticShortArray`.
#
# + arg - The `int[]` value that is to be set for the field.
function FieldsTestResource_setGetStaticShortArray(int[] arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticShortArray(check jarrays:toHandle(arg, "short"));
}

# The function that retrieves the value of the public field `FieldsTestResource_getGetStaticIntArray`.
#
# + return - The `int[]` value of the field.
function FieldsTestResource_getGetStaticIntArray(int[] arg) returns int[]|error {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticIntArray(check jarrays:toHandle(arg,
    "int"));
    return <int[]>check jarrays:fromHandle(externalObj, "int[]");
}

# The function to set the value of the public field `FieldsTestResource_setGetStaticIntArray`.
#
# + arg - The `int[]` value that is to be set for the field.
function FieldsTestResource_setGetStaticIntArray(int[] arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticIntArray(check jarrays:toHandle(arg, "int"));
}

# The function that retrieves the value of the public field `FieldsTestResource_getGetStaticLongArray`.
#
# + return - The `int[]` value of the field.
function FieldsTestResource_getGetStaticLongArray(int[] arg) returns int[]|error {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticLongArray(check jarrays:toHandle(arg,
    "long"));
    return <int[]>check jarrays:fromHandle(externalObj, "int[]");
}

# The function to set the value of the public field `FieldsTestResource_setGetStaticLongArray`.
#
# + arg - The `int[]` value that is to be set for the field.
function FieldsTestResource_setGetStaticLongArray(int[] arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticLongArray(check jarrays:toHandle(arg, "long"));
}

# The function that retrieves the value of the public field `FieldsTestResource_getGetStaticFloatArray`.
#
# + return - The `float[]` value of the field.
function FieldsTestResource_getGetStaticFloatArray(float[] arg) returns float[]|error {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticFloatArray(check jarrays:toHandle(arg,
    "float"));
    return <float[]>check jarrays:fromHandle(externalObj, "float[]");
}

# The function to set the value of the public field `FieldsTestResource_setGetStaticFloatArray`.
#
# + arg - The `float[]` value that is to be set for the field.
function FieldsTestResource_setGetStaticFloatArray(float[] arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticFloatArray(check jarrays:toHandle(arg, "float"));
}

# The function that retrieves the value of the public field `FieldsTestResource_getGetStaticDoubleArray`.
#
# + return - The `float[]` value of the field.
function FieldsTestResource_getGetStaticDoubleArray(float[] arg) returns float[]|error {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticDoubleArray(check jarrays:toHandle(arg,
    "double"));
    return <float[]>check jarrays:fromHandle(externalObj, "float[]");
}

# The function to set the value of the public field `FieldsTestResource_setGetStaticDoubleArray`.
#
# + arg - The `float[]` value that is to be set for the field.
function FieldsTestResource_setGetStaticDoubleArray(float[] arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticDoubleArray(check jarrays:toHandle(arg, "double"));
}

# The function that retrieves the value of the public field `FieldsTestResource_getGetStaticBooleanArray`.
#
# + return - The `boolean[]` value of the field.
function FieldsTestResource_getGetStaticBooleanArray(boolean[] arg) returns boolean[]|error {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticBooleanArray(check jarrays:toHandle(
    arg, "boolean"));
    return <boolean[]>check jarrays:fromHandle(externalObj, "boolean[]");
}

# The function to set the value of the public field `FieldsTestResource_setGetStaticBooleanArray`.
#
# + arg - The `boolean[]` value that is to be set for the field.
function FieldsTestResource_setGetStaticBooleanArray(boolean[] arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticBooleanArray(check jarrays:toHandle(arg, "boolean"));
}

# The function that retrieves the value of the public field `FieldsTestResource_getGetStaticStringArray`.
#
# + return - The `string[]` value of the field.
function FieldsTestResource_getGetStaticStringArray(string[] arg) returns string[]|error {
    return <string[]>check jarrays:fromHandle(org_ballerinalang_bindgen_FieldsTestResource_getGetStaticStringArray(check
    jarrays:toHandle(arg, "java.lang.String")), "string");
}

# The function to set the value of the public field `FieldsTestResource_setGetStaticStringArray`.
#
# + arg - The `string[]` value that is to be set for the field.
function FieldsTestResource_setGetStaticStringArray(string[] arg) {

    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticStringArray(check jarrays:toHandle(arg, "java.lang.String"));
}

# The function that retrieves the value of the public field `FieldsTestResource_getGetStaticInterface`.
#
# + return - The `List` value of the field.
function FieldsTestResource_getGetStaticInterface(List arg) returns List {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticInterface(arg.jObj);
    List newObj = new (externalObj);
    return newObj;
}

# The function to set the value of the public field `FieldsTestResource_setGetStaticInterface`.
#
# + arg - The `List` value that is to be set for the field.
function FieldsTestResource_setGetStaticInterface(List arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticInterface(arg.jObj);
}

# The function that retrieves the value of the public field `FieldsTestResource_getGetStaticAbstractClass`.
#
# + return - The `AbstractList` value of the field.
function FieldsTestResource_getGetStaticAbstractClass(AbstractList arg) returns AbstractList {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticAbstractClass(arg.jObj);
    AbstractList newObj = new (externalObj);
    return newObj;
}

# The function to set the value of the public field `FieldsTestResource_setGetStaticAbstractClass`.
#
# + arg - The `AbstractList` value that is to be set for the field.
function FieldsTestResource_setGetStaticAbstractClass(AbstractList arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticAbstractClass(arg.jObj);
}

# The function that retrieves the value of the public field `FieldsTestResource_getGetStaticObject`.
#
# + return - The `Path` value of the field.
function FieldsTestResource_getGetStaticObject(Path arg) returns Path {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticObject(arg.jObj);
    Path newObj = new (externalObj);
    return newObj;
}

# The function to set the value of the public field `FieldsTestResource_setGetStaticObject`.
#
# + arg - The `Path` value that is to be set for the field.
function FieldsTestResource_setGetStaticObject(Path arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticObject(arg.jObj);
}

# The function that retrieves the value of the public field `FieldsTestResource_getGetStaticGenericObject`.
#
# + return - The `Set` value of the field.
function FieldsTestResource_getGetStaticGenericObject(Set arg) returns Set {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticGenericObject(arg.jObj);
    Set newObj = new (externalObj);
    return newObj;
}

# The function to set the value of the public field `FieldsTestResource_setGetStaticGenericObject`.
#
# + arg - The `Set` value that is to be set for the field.
function FieldsTestResource_setGetStaticGenericObject(Set arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticGenericObject(arg.jObj);
}

# The function that retrieves the value of the public field `FieldsTestResource_getGetStaticEnumeration`.
#
# + return - The `Level` value of the field.
function FieldsTestResource_getGetStaticEnumeration(Level arg) returns Level {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticEnumeration(arg.jObj);
    Level newObj = new (externalObj);
    return newObj;
}

# The function to set the value of the public field `FieldsTestResource_setGetStaticEnumeration`.
#
# + arg - The `Level` value that is to be set for the field.
function FieldsTestResource_setGetStaticEnumeration(Level arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticEnumeration(arg.jObj);
}

# The function that retrieves the value of the public field `FieldsTestResource_getGetStaticThrowable`.
#
# + return - The `ArithmeticException` value of the field.
function FieldsTestResource_getGetStaticThrowable(JArithmeticException arg) returns JArithmeticException {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticThrowable(arg.jObj);
    JArithmeticException newObj = new (externalObj);
    return newObj;
}

# The function to set the value of the public field `FieldsTestResource_setGetStaticThrowable`.
#
# + arg - The `JArithmeticException` value that is to be set for the field.
function FieldsTestResource_setGetStaticThrowable(JArithmeticException arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticThrowable(arg.jObj);
}

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceByte(handle receiver, byte arg) returns byte = @java:FieldGet {
    name: "getInstanceByte",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceByte(handle receiver, byte arg) = @java:FieldSet {
    name: "getInstanceByte",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceChar(handle receiver, int arg) returns int = @java:FieldGet {
    name: "getInstanceChar",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceChar(handle receiver, int arg) = @java:FieldSet {
    name: "getInstanceChar",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceShort(handle receiver, int arg) returns int = @java:FieldGet {
    name: "getInstanceShort",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceShort(handle receiver, int arg) = @java:FieldSet {
    name: "getInstanceShort",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceInt(handle receiver, int arg) returns int = @java:FieldGet {
    name: "getInstanceInt",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceInt(handle receiver, int arg) = @java:FieldSet {
    name: "getInstanceInt",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceLong(handle receiver, int arg) returns int = @java:FieldGet {
    name: "getInstanceLong",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceLong(handle receiver, int arg) = @java:FieldSet {
    name: "getInstanceLong",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceFloat(handle receiver, float arg) returns float = @java:FieldGet {
    name: "getInstanceFloat",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceFloat(handle receiver, float arg) = @java:FieldSet {
    name: "getInstanceFloat",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceDouble(handle receiver, float arg) returns float = @java:FieldGet {
    name: "getInstanceDouble",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceDouble(handle receiver, float arg) = @java:FieldSet {
    name: "getInstanceDouble",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceBoolean(handle receiver, boolean arg)
returns boolean = @java:FieldGet {
    name: "getInstanceBoolean",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceBoolean(handle receiver, boolean arg) = @java:FieldSet {
    name: "getInstanceBoolean",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceString(handle receiver, handle arg) returns handle = @java:FieldGet {
    name: "getInstanceString",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceString(handle receiver, handle arg) = @java:FieldSet {
    name: "getInstanceString",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceByteArray(handle receiver, handle arg)
returns handle = @java:FieldGet {
    name: "getInstanceByteArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceByteArray(handle receiver, handle arg) = @java:FieldSet {
    name: "getInstanceByteArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceCharArray(handle receiver, handle arg)
returns handle = @java:FieldGet {
    name: "getInstanceCharArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceCharArray(handle receiver, handle arg) = @java:FieldSet {
    name: "getInstanceCharArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceShortArray(handle receiver, handle arg)
returns handle = @java:FieldGet {
    name: "getInstanceShortArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceShortArray(handle receiver, handle arg) = @java:FieldSet {
    name: "getInstanceShortArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceIntArray(handle receiver, handle arg)
returns handle = @java:FieldGet {
    name: "getInstanceIntArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceIntArray(handle receiver, handle arg) = @java:FieldSet {
    name: "getInstanceIntArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceLongArray(handle receiver, handle arg)
returns handle = @java:FieldGet {
    name: "getInstanceLongArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceLongArray(handle receiver, handle arg) = @java:FieldSet {
    name: "getInstanceLongArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceFloatArray(handle receiver, handle arg)
returns handle = @java:FieldGet {
    name: "getInstanceFloatArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceFloatArray(handle receiver, handle arg) = @java:FieldSet {
    name: "getInstanceFloatArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceDoubleArray(handle receiver, handle arg)
returns handle = @java:FieldGet {
    name: "getInstanceDoubleArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceDoubleArray(handle receiver, handle arg) = @java:FieldSet {
    name: "getInstanceDoubleArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceBooleanArray(handle receiver, handle arg)
returns handle = @java:FieldGet {
    name: "getInstanceBooleanArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceBooleanArray(handle receiver, handle arg) = @java:FieldSet {
    name: "getInstanceBooleanArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceStringArray(handle receiver, handle arg)
returns handle = @java:FieldGet {
    name: "getInstanceStringArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceStringArray(handle receiver, handle arg) = @java:FieldSet {
    name: "getInstanceStringArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceObjectArray(handle receiver, handle arg)
returns handle = @java:FieldGet {
    name: "getInstanceObjectArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceObjectArray(handle receiver, handle arg) = @java:FieldSet {
    name: "getInstanceObjectArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceObjectMultiArray1(handle receiver, handle arg)
returns handle = @java:FieldGet {
    name: "getInstanceObjectMultiArray1",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceObjectMultiArray1(handle receiver, handle arg) = @java:FieldSet {
    name: "getInstanceObjectMultiArray1",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceObjectMultiArray2(handle receiver, handle arg)
returns handle = @java:FieldGet {
    name: "getInstanceObjectMultiArray2",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceObjectMultiArray2(handle receiver, handle arg) = @java:FieldSet {
    name: "getInstanceObjectMultiArray2",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceInterface(handle receiver, handle arg)
returns handle = @java:FieldGet {
    name: "getInstanceInterface",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceInterface(handle receiver, handle arg) = @java:FieldSet {
    name: "getInstanceInterface",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceAbstractClass(handle receiver, handle arg)
returns handle = @java:FieldGet {
    name: "getInstanceAbstractClass",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceAbstractClass(handle receiver, handle arg) = @java:FieldSet {
    name: "getInstanceAbstractClass",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceObject(handle receiver, handle arg) returns handle = @java:FieldGet {
    name: "getInstanceObject",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceObject(handle receiver, handle arg) = @java:FieldSet {
    name: "getInstanceObject",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceGenericObject(handle receiver, handle arg)
returns handle = @java:FieldGet {
    name: "getInstanceGenericObject",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceGenericObject(handle receiver, handle arg) = @java:FieldSet {
    name: "getInstanceGenericObject",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceEnumeration(handle receiver, handle arg)
returns handle = @java:FieldGet {
    name: "getInstanceEnumeration",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceEnumeration(handle receiver, handle arg) = @java:FieldSet {
    name: "getInstanceEnumeration",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceThrowable(handle receiver, handle arg)
returns handle = @java:FieldGet {
    name: "getInstanceThrowable",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceThrowable(handle receiver, handle arg) = @java:FieldSet {
    name: "getInstanceThrowable",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticByte(byte arg) returns byte = @java:FieldGet {
    name: "getStaticByte",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticByte(byte arg) = @java:FieldSet {
    name: "getStaticByte",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticChar(int arg) returns int = @java:FieldGet {
    name: "getStaticChar",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticChar(int arg) = @java:FieldSet {
    name: "getStaticChar",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticShort(int arg) returns int = @java:FieldGet {
    name: "getStaticShort",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticShort(int arg) = @java:FieldSet {
    name: "getStaticShort",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticInt(int arg) returns int = @java:FieldGet {
    name: "getStaticInt",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticInt(int arg) = @java:FieldSet {
    name: "getStaticInt",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticLong(int arg) returns int = @java:FieldGet {
    name: "getStaticLong",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticLong(int arg) = @java:FieldSet {
    name: "getStaticLong",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticFloat(float arg) returns float = @java:FieldGet {
    name: "getStaticFloat",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticFloat(float arg) = @java:FieldSet {
    name: "getStaticFloat",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticDouble(float arg) returns float = @java:FieldGet {
    name: "getStaticDouble",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticDouble(float arg) = @java:FieldSet {
    name: "getStaticDouble",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticBoolean(boolean arg) returns boolean = @java:FieldGet {
    name: "getStaticBoolean",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticBoolean(boolean arg) = @java:FieldSet {
    name: "getStaticBoolean",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticString(handle arg) returns handle = @java:FieldGet {
    name: "getStaticString",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticString(handle arg) = @java:FieldSet {
    name: "getStaticString",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_BYTE(byte arg) returns byte = @java:FieldGet {
    name: "GET_STATIC_FINAL_BYTE",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_CHAR(int arg) returns int = @java:FieldGet {
    name: "GET_STATIC_FINAL_CHAR",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_SHORT(int arg) returns int = @java:FieldGet {
    name: "GET_STATIC_FINAL_SHORT",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_INT(int arg) returns int = @java:FieldGet {
    name: "GET_STATIC_FINAL_INT",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_LONG(int arg) returns int = @java:FieldGet {
    name: "GET_STATIC_FINAL_LONG",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_FLOAT(float arg) returns float = @java:FieldGet {
    name: "GET_STATIC_FINAL_FLOAT",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_DOUBLE(float arg) returns float = @java:FieldGet {
    name: "GET_STATIC_FINAL_DOUBLE",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_BOOLEAN(boolean arg) returns boolean = @java:FieldGet {
    name: "GET_STATIC_FINAL_BOOLEAN",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_STRING(handle arg) returns handle = @java:FieldGet {
    name: "GET_STATIC_FINAL_STRING",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticByteArray(handle arg) returns handle = @java:FieldGet {
    name: "getStaticByteArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticByteArray(handle arg) = @java:FieldSet {
    name: "getStaticByteArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticCharArray(handle arg) returns handle = @java:FieldGet {
    name: "getStaticCharArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticCharArray(handle arg) = @java:FieldSet {
    name: "getStaticCharArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticShortArray(handle arg) returns handle = @java:FieldGet {
    name: "getStaticShortArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticShortArray(handle arg) = @java:FieldSet {
    name: "getStaticShortArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticIntArray(handle arg) returns handle = @java:FieldGet {
    name: "getStaticIntArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticIntArray(handle arg) = @java:FieldSet {
    name: "getStaticIntArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticLongArray(handle arg) returns handle = @java:FieldGet {
    name: "getStaticLongArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticLongArray(handle arg) = @java:FieldSet {
    name: "getStaticLongArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticFloatArray(handle arg) returns handle = @java:FieldGet {
    name: "getStaticFloatArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticFloatArray(handle arg) = @java:FieldSet {
    name: "getStaticFloatArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticDoubleArray(handle arg) returns handle = @java:FieldGet {
    name: "getStaticDoubleArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticDoubleArray(handle arg) = @java:FieldSet {
    name: "getStaticDoubleArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticBooleanArray(handle arg) returns handle = @java:FieldGet {
    name: "getStaticBooleanArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticBooleanArray(handle arg) = @java:FieldSet {
    name: "getStaticBooleanArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticStringArray(handle arg) returns handle = @java:FieldGet {
    name: "getStaticStringArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticStringArray(handle arg) = @java:FieldSet {
    name: "getStaticStringArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticObjectArray(handle receiver, handle arg)
returns handle = @java:FieldGet {
    name: "getStaticObjectArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticObjectArray(handle receiver, handle arg) = @java:FieldSet {
    name: "getStaticObjectArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticObjectMultiArray1(handle receiver, handle arg)
returns handle = @java:FieldGet {
    name: "getStaticObjectMultiArray1",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticObjectMultiArray1(handle receiver, handle arg) = @java:FieldSet {
    name: "getStaticObjectMultiArray1",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticObjectMultiArray2(handle receiver, handle arg)
returns handle = @java:FieldGet {
    name: "getStaticObjectMultiArray2",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticObjectMultiArray2(handle receiver, handle arg) = @java:FieldSet {
    name: "getStaticObjectMultiArray2",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticInterface(handle arg) returns handle = @java:FieldGet {
    name: "getStaticInterface",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticInterface(handle arg) = @java:FieldSet {
    name: "getStaticInterface",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticAbstractClass(handle arg) returns handle = @java:FieldGet {
    name: "getStaticAbstractClass",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticAbstractClass(handle arg) = @java:FieldSet {
    name: "getStaticAbstractClass",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticObject(handle arg) returns handle = @java:FieldGet {
    name: "getStaticObject",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticObject(handle arg) = @java:FieldSet {
    name: "getStaticObject",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticGenericObject(handle arg) returns handle = @java:FieldGet {
    name: "getStaticGenericObject",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticGenericObject(handle arg) = @java:FieldSet {
    name: "getStaticGenericObject",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticEnumeration(handle arg) returns handle = @java:FieldGet {
    name: "getStaticEnumeration",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticEnumeration(handle arg) = @java:FieldSet {
    name: "getStaticEnumeration",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticThrowable(handle arg) returns handle = @java:FieldGet {
    name: "getStaticThrowable",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticThrowable(handle arg) = @java:FieldSet {
    name: "getStaticThrowable",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_newFieldsTestResource1() returns handle = @java:Constructor {
    'class: "org.ballerinalang.bindgen.FieldsTestResource",
    paramTypes: []
} external;
