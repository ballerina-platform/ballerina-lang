import ballerina/jballerina.java;
import ballerina/jballerina.java.arrays as jarrays;

# Ballerina class mapping for the Java `org.ballerinalang.bindgen.FieldsTestResource` class.
@java:Binding {'class: "org.ballerinalang.bindgen.FieldsTestResource"}
distinct class FieldsTestResource {

    *java:JObject;
    *Object;

    # The `handle` field that stores the reference to the `org.ballerinalang.bindgen.FieldsTestResource` object.
    public handle jObj;

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
    # The function that maps to the `equals` method of `org.ballerinalang.bindgen.FieldsTestResource`.
    #
    # + arg0 - The `Object` value required to map with the Java method parameter.
    # + return - The `boolean` value returning from the Java mapping.
    function 'equals(Object arg0) returns boolean {
        return org_ballerinalang_bindgen_FieldsTestResource_equals(self.jObj, arg0.jObj);
    }

    # The function that maps to the `getClass` method of `org.ballerinalang.bindgen.FieldsTestResource`.
    #
    # + return - The `Class` value returning from the Java mapping.
    function getClass() returns Class {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getClass(self.jObj);
        Class newObj = new (externalObj);
        return newObj;
    }

    # The function that maps to the `hashCode` method of `org.ballerinalang.bindgen.FieldsTestResource`.
    #
    # + return - The `int` value returning from the Java mapping.
    function hashCode() returns int {
        return org_ballerinalang_bindgen_FieldsTestResource_hashCode(self.jObj);
    }

    # The function that maps to the `notify` method of `org.ballerinalang.bindgen.FieldsTestResource`.
    function notify() {
        org_ballerinalang_bindgen_FieldsTestResource_notify(self.jObj);
    }

    # The function that maps to the `notifyAll` method of `org.ballerinalang.bindgen.FieldsTestResource`.
    function notifyAll() {
        org_ballerinalang_bindgen_FieldsTestResource_notifyAll(self.jObj);
    }

    # The function that maps to the `returnStringArray` method of `org.ballerinalang.bindgen.FieldsTestResource`.
    #
    # + return - The `string[]` value returning from the Java mapping.
    function returnStringArray() returns string[]|error {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_returnStringArray(self.jObj);
        return <string[]>check jarrays:fromHandle(externalObj, "string");
    }

    # The function that maps to the `testMethod` method of `org.ballerinalang.bindgen.FieldsTestResource`.
    #
    # + arg0 - The `int` value required to map with the Java method parameter.
    # + return - The `int` value returning from the Java mapping.
    function testMethod(int arg0) returns int {
        return org_ballerinalang_bindgen_FieldsTestResource_testMethod(self.jObj, arg0);
    }

    # The function that maps to the `wait` method of `org.ballerinalang.bindgen.FieldsTestResource`.
    #
    # + return - The `InterruptedException` value returning from the Java mapping.
    function 'wait() returns InterruptedException? {
        error|() externalObj = org_ballerinalang_bindgen_FieldsTestResource_wait(self.jObj);
        if (externalObj is error) {
            InterruptedException e = error InterruptedException(INTERRUPTEDEXCEPTION, externalObj, message = externalObj.message());
            return e;
        }
    }

    # The function that maps to the `wait` method of `org.ballerinalang.bindgen.FieldsTestResource`.
    #
    # + arg0 - The `int` value required to map with the Java method parameter.
    # + return - The `InterruptedException` value returning from the Java mapping.
    function wait2(int arg0) returns InterruptedException? {
        error|() externalObj = org_ballerinalang_bindgen_FieldsTestResource_wait2(self.jObj, arg0);
        if (externalObj is error) {
            InterruptedException e = error InterruptedException(INTERRUPTEDEXCEPTION, externalObj, message = externalObj.message());
            return e;
        }
    }

    # The function that maps to the `wait` method of `org.ballerinalang.bindgen.FieldsTestResource`.
    #
    # + arg0 - The `int` value required to map with the Java method parameter.
    # + arg1 - The `int` value required to map with the Java method parameter.
    # + return - The `InterruptedException` value returning from the Java mapping.
    function wait3(int arg0, int arg1) returns InterruptedException? {
        error|() externalObj = org_ballerinalang_bindgen_FieldsTestResource_wait3(self.jObj, arg0, arg1);
        if (externalObj is error) {
            InterruptedException e = error InterruptedException(INTERRUPTEDEXCEPTION, externalObj, message = externalObj.message());
            return e;
        }
    }

    # The function that retrieves the value of the public field `getInstanceByte`.
    #
    # + return - The `byte` value of the field.
    function getGetInstanceByte() returns byte {
        return org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceByte(self.jObj);
    }

    # The function to set the value of the public field `getInstanceByte`.
    #
    # + arg - The `byte` value that is to be set for the field.
    function setGetInstanceByte(byte arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceByte(self.jObj, arg);
    }

    # The function that retrieves the value of the public field `getInstanceChar`.
    #
    # + return - The `int` value of the field.
    function getGetInstanceChar() returns int {
        return org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceChar(self.jObj);
    }

    # The function to set the value of the public field `getInstanceChar`.
    #
    # + arg - The `int` value that is to be set for the field.
    function setGetInstanceChar(int arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceChar(self.jObj, arg);
    }

    # The function that retrieves the value of the public field `getInstanceShort`.
    #
    # + return - The `int` value of the field.
    function getGetInstanceShort() returns int {
        return org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceShort(self.jObj);
    }

    # The function to set the value of the public field `getInstanceShort`.
    #
    # + arg - The `int` value that is to be set for the field.
    function setGetInstanceShort(int arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceShort(self.jObj, arg);
    }

    # The function that retrieves the value of the public field `getInstanceInt`.
    #
    # + return - The `int` value of the field.
    function getGetInstanceInt() returns int {
        return org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceInt(self.jObj);
    }

    # The function to set the value of the public field `getInstanceInt`.
    #
    # + arg - The `int` value that is to be set for the field.
    function setGetInstanceInt(int arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceInt(self.jObj, arg);
    }

    # The function that retrieves the value of the public field `getInstanceLong`.
    #
    # + return - The `int` value of the field.
    function getGetInstanceLong() returns int {
        return org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceLong(self.jObj);
    }

    # The function to set the value of the public field `getInstanceLong`.
    #
    # + arg - The `int` value that is to be set for the field.
    function setGetInstanceLong(int arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceLong(self.jObj, arg);
    }

    # The function that retrieves the value of the public field `getInstanceFloat`.
    #
    # + return - The `float` value of the field.
    function getGetInstanceFloat() returns float {
        return org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceFloat(self.jObj);
    }

    # The function to set the value of the public field `getInstanceFloat`.
    #
    # + arg - The `float` value that is to be set for the field.
    function setGetInstanceFloat(float arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceFloat(self.jObj, arg);
    }

    # The function that retrieves the value of the public field `getInstanceDouble`.
    #
    # + return - The `float` value of the field.
    function getGetInstanceDouble() returns float {
        return org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceDouble(self.jObj);
    }

    # The function to set the value of the public field `getInstanceDouble`.
    #
    # + arg - The `float` value that is to be set for the field.
    function setGetInstanceDouble(float arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceDouble(self.jObj, arg);
    }

    # The function that retrieves the value of the public field `getInstanceBoolean`.
    #
    # + return - The `boolean` value of the field.
    function getGetInstanceBoolean() returns boolean {
        return org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceBoolean(self.jObj);
    }

    # The function to set the value of the public field `getInstanceBoolean`.
    #
    # + arg - The `boolean` value that is to be set for the field.
    function setGetInstanceBoolean(boolean arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceBoolean(self.jObj, arg);
    }

    # The function that retrieves the value of the public field `getInstanceString`.
    #
    # + return - The `string` value of the field.
    function getGetInstanceString() returns string? {
        return java:toString(org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceString(self.jObj));
    }

    # The function to set the value of the public field `getInstanceString`.
    #
    # + arg - The `string` value that is to be set for the field.
    function setGetInstanceString(string arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceString(self.jObj, java:fromString(arg));
    }

    # The function that retrieves the value of the public field `getInstanceByteArray`.
    #
    # + return - The `byte[]` value of the field.
    function getGetInstanceByteArray() returns byte[]|error {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceByteArray(self.jObj);
        return <byte[]>check jarrays:fromHandle(externalObj, "byte[]");
    }

    # The function to set the value of the public field `getInstanceByteArray`.
    #
    # + arg - The `byte[]` value that is to be set for the field.
    function setGetInstanceByteArray(byte[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceByteArray(self.jObj, check jarrays:toHandle(arg, "byte"));
    }

    # The function that retrieves the value of the public field `getInstanceCharArray`.
    #
    # + return - The `int[]` value of the field.
    function getGetInstanceCharArray() returns int[]|error {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceCharArray(self.jObj);
        return <int[]>check jarrays:fromHandle(externalObj, "int[]");
    }

    # The function to set the value of the public field `getInstanceCharArray`.
    #
    # + arg - The `int[]` value that is to be set for the field.
    function setGetInstanceCharArray(int[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceCharArray(self.jObj, check jarrays:toHandle(arg, "char"));
    }

    # The function that retrieves the value of the public field `getInstanceShortArray`.
    #
    # + return - The `int[]` value of the field.
    function getGetInstanceShortArray() returns int[]|error {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceShortArray(self.jObj);
        return <int[]>check jarrays:fromHandle(externalObj, "int[]");
    }

    # The function to set the value of the public field `getInstanceShortArray`.
    #
    # + arg - The `int[]` value that is to be set for the field.
    function setGetInstanceShortArray(int[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceShortArray(self.jObj, check jarrays:toHandle(arg, "short"));
    }

    # The function that retrieves the value of the public field `getInstanceIntArray`.
    #
    # + return - The `int[]` value of the field.
    function getGetInstanceIntArray() returns int[]|error {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceIntArray(self.jObj);
        return <int[]>check jarrays:fromHandle(externalObj, "int[]");
    }

    # The function to set the value of the public field `getInstanceIntArray`.
    #
    # + arg - The `int[]` value that is to be set for the field.
    function setGetInstanceIntArray(int[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceIntArray(self.jObj, check jarrays:toHandle(arg, "int"));
    }

    # The function that retrieves the value of the public field `getInstanceLongArray`.
    #
    # + return - The `int[]` value of the field.
    function getGetInstanceLongArray() returns int[]|error {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceLongArray(self.jObj);
        return <int[]>check jarrays:fromHandle(externalObj, "int[]");
    }

    # The function to set the value of the public field `getInstanceLongArray`.
    #
    # + arg - The `int[]` value that is to be set for the field.
    function setGetInstanceLongArray(int[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceLongArray(self.jObj, check jarrays:toHandle(arg, "long"));
    }

    # The function that retrieves the value of the public field `getInstanceFloatArray`.
    #
    # + return - The `float[]` value of the field.
    function getGetInstanceFloatArray() returns float[]|error {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceFloatArray(self.jObj);
        return <float[]>check jarrays:fromHandle(externalObj, "float[]");
    }

    # The function to set the value of the public field `getInstanceFloatArray`.
    #
    # + arg - The `float[]` value that is to be set for the field.
    function setGetInstanceFloatArray(float[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceFloatArray(self.jObj, check jarrays:toHandle(arg, "float"));
    }

    # The function that retrieves the value of the public field `getInstanceDoubleArray`.
    #
    # + return - The `float[]` value of the field.
    function getGetInstanceDoubleArray() returns float[]|error {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceDoubleArray(self.jObj);
        return <float[]>check jarrays:fromHandle(externalObj, "float[]");
    }

    # The function to set the value of the public field `getInstanceDoubleArray`.
    #
    # + arg - The `float[]` value that is to be set for the field.
    function setGetInstanceDoubleArray(float[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceDoubleArray(self.jObj, check jarrays:toHandle(arg, "double"));
    }

    # The function that retrieves the value of the public field `getInstanceBooleanArray`.
    #
    # + return - The `boolean[]` value of the field.
    function getGetInstanceBooleanArray() returns boolean[]|error {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceBooleanArray(self.jObj);
        return <boolean[]>check jarrays:fromHandle(externalObj, "boolean[]");
    }

    # The function to set the value of the public field `getInstanceBooleanArray`.
    #
    # + arg - The `boolean[]` value that is to be set for the field.
    function setGetInstanceBooleanArray(boolean[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceBooleanArray(self.jObj, check jarrays:toHandle(arg, "boolean"));
    }

    # The function that retrieves the value of the public field `getInstanceStringArray`.
    #
    # + return - The `string[]` value of the field.
    function getGetInstanceStringArray() returns string[]|error {
        return <string[]>check jarrays:fromHandle(org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceStringArray(self.jObj), "string");
    }

    # The function to set the value of the public field `getInstanceStringArray`.
    #
    # + arg - The `string[]` value that is to be set for the field.
    function setGetInstanceStringArray(string[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceStringArray(self.jObj, check jarrays:toHandle(arg, "java.lang.String"));
    }

    # The function that retrieves the value of the public field `getInstanceObjectArray`.
    #
    # + return - The `StringBuilder[]` value of the field.
    function getGetInstanceObjectArray() returns StringBuilder[]|error {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceObjectArray(self.jObj);
        StringBuilder[] newObj = [];
        handle[] anyObj = <handle[]>check jarrays:fromHandle(externalObj, "handle");
        int count = anyObj.length();
        foreach int i in 0 ... count - 1 {
            StringBuilder[] element = new (anyObj[i]);
            newObj[i] = element;
        }
        return newObj;
    }

    # The function to set the value of the public field `getInstanceObjectArray`.
    #
    # + arg - The `StringBuilder[]` value that is to be set for the field.
    function setGetInstanceObjectArray(StringBuilder[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceObjectArray(self.jObj, check jarrays:toHandle(arg, "java.lang.StringBuilder"));
    }

    # The function that retrieves the value of the public field `getInstanceObjectMultiArray1`.
    #
    # + return - The `Integer[]` value of the field.
    function getGetInstanceObjectMultiArray1() returns Integer[]|error {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceObjectMultiArray1(self.jObj);
        Integer[] newObj = [];
        handle[] anyObj = <handle[]>check jarrays:fromHandle(externalObj, "handle");
        int count = anyObj.length();
        foreach int i in 0 ... count - 1 {
            Integer[] element = new (anyObj[i]);
            newObj[i] = element;
        }
        return newObj;
    }

    # The function to set the value of the public field `getInstanceObjectMultiArray1`.
    #
    # + arg - The `Integer[]` value that is to be set for the field.
    function setGetInstanceObjectMultiArray1(Integer[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceObjectMultiArray1(self.jObj, check jarrays:toHandle(arg, "java.lang.Integer"));
    }

    # The function that retrieves the value of the public field `getInstanceObjectMultiArray2`.
    #
    # + return - The `Object[]` value of the field.
    function getGetInstanceObjectMultiArray2() returns Object[]|error {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceObjectMultiArray2(self.jObj);
        Object[] newObj = [];
        handle[] anyObj = <handle[]>check jarrays:fromHandle(externalObj, "handle");
        int count = anyObj.length();
        foreach int i in 0 ... count - 1 {
            Object[] element = new (anyObj[i]);
            newObj[i] = element;
        }
        return newObj;
    }

    # The function to set the value of the public field `getInstanceObjectMultiArray2`.
    #
    # + arg - The `Object[]` value that is to be set for the field.
    function setGetInstanceObjectMultiArray2(Object[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceObjectMultiArray2(self.jObj, check jarrays:toHandle(arg, "java.lang.Object"));
    }

    # The function that retrieves the value of the public field `getInstanceInterface`.
    #
    # + return - The `List` value of the field.
    function getGetInstanceInterface() returns List {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceInterface(self.jObj);
        List newObj = new (externalObj);
        return newObj;
    }

    # The function to set the value of the public field `getInstanceInterface`.
    #
    # + arg - The `List` value that is to be set for the field.
    function setGetInstanceInterface(List arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceInterface(self.jObj, arg.jObj);
    }

    # The function that retrieves the value of the public field `getInstanceAbstractClass`.
    #
    # + return - The `AbstractList` value of the field.
    function getGetInstanceAbstractClass() returns AbstractList {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceAbstractClass(self.jObj);
        AbstractList newObj = new (externalObj);
        return newObj;
    }

    # The function to set the value of the public field `getInstanceAbstractClass`.
    #
    # + arg - The `AbstractList` value that is to be set for the field.
    function setGetInstanceAbstractClass(AbstractList arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceAbstractClass(self.jObj, arg.jObj);
    }

    # The function that retrieves the value of the public field `getInstanceObject`.
    #
    # + return - The `Path` value of the field.
    function getGetInstanceObject() returns Path {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceObject(self.jObj);
        Path newObj = new (externalObj);
        return newObj;
    }

    # The function to set the value of the public field `getInstanceObject`.
    #
    # + arg - The `Path` value that is to be set for the field.
    function setGetInstanceObject(Path arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceObject(self.jObj, arg.jObj);
    }

    # The function that retrieves the value of the public field `getInstanceGenericObject`.
    #
    # + return - The `Set` value of the field.
    function getGetInstanceGenericObject() returns Set {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceGenericObject(self.jObj);
        Set newObj = new (externalObj);
        return newObj;
    }

    # The function to set the value of the public field `getInstanceGenericObject`.
    #
    # + arg - The `Set` value that is to be set for the field.
    function setGetInstanceGenericObject(Set arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceGenericObject(self.jObj, arg.jObj);
    }

    # The function that retrieves the value of the public field `getInstanceEnumeration`.
    #
    # + return - The `Level` value of the field.
    function getGetInstanceEnumeration() returns Level {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceEnumeration(self.jObj);
        Level newObj = new (externalObj);
        return newObj;
    }

    # The function to set the value of the public field `getInstanceEnumeration`.
    #
    # + arg - The `Level` value that is to be set for the field.
    function setGetInstanceEnumeration(Level arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceEnumeration(self.jObj, arg.jObj);
    }

    # The function that retrieves the value of the public field `getInstanceThrowable`.
    #
    # + return - The `ArithmeticException` value of the field.
    function getGetInstanceThrowable() returns JArithmeticException {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceThrowable(self.jObj);
        JArithmeticException newObj = new (externalObj);
        return newObj;
    }

    # The function to set the value of the public field `getInstanceThrowable`.
    #
    # + arg - The `JArithmeticException` value that is to be set for the field.
    function setGetInstanceThrowable(JArithmeticException arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceThrowable(self.jObj, arg.jObj);
    }

    # The function that retrieves the value of the public field `getStaticObjectArray`.
    #
    # + return - The `StringBuilder[]` value of the field.
    function getGetStaticObjectArray() returns StringBuilder[]|error {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticObjectArray(self.jObj);
        StringBuilder[] newObj = [];
        handle[] anyObj = <handle[]>check jarrays:fromHandle(externalObj, "handle");
        int count = anyObj.length();
        foreach int i in 0 ... count - 1 {
            StringBuilder[] element = new (anyObj[i]);
            newObj[i] = element;
        }
        return newObj;
    }

    # The function to set the value of the public field `getStaticObjectArray`.
    #
    # + arg - The `StringBuilder[]` value that is to be set for the field.
    function setGetStaticObjectArray(StringBuilder[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetStaticObjectArray(self.jObj, check jarrays:toHandle(arg, "java.lang.StringBuilder"));
    }

    # The function that retrieves the value of the public field `getStaticObjectMultiArray1`.
    #
    # + return - The `Integer[]` value of the field.
    function getGetStaticObjectMultiArray1() returns Integer[]|error {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticObjectMultiArray1(self.jObj);
        Integer[] newObj = [];
        handle[] anyObj = <handle[]>check jarrays:fromHandle(externalObj, "handle");
        int count = anyObj.length();
        foreach int i in 0 ... count - 1 {
            Integer[] element = new (anyObj[i]);
            newObj[i] = element;
        }
        return newObj;
    }

    # The function to set the value of the public field `getStaticObjectMultiArray1`.
    #
    # + arg - The `Integer[]` value that is to be set for the field.
    function setGetStaticObjectMultiArray1(Integer[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetStaticObjectMultiArray1(self.jObj, check jarrays:toHandle(arg, "java.lang.Integer"));
    }

    # The function that retrieves the value of the public field `getStaticObjectMultiArray2`.
    #
    # + return - The `Object[]` value of the field.
    function getGetStaticObjectMultiArray2() returns Object[]|error {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticObjectMultiArray2(self.jObj);
        Object[] newObj = [];
        handle[] anyObj = <handle[]>check jarrays:fromHandle(externalObj, "handle");
        int count = anyObj.length();
        foreach int i in 0 ... count - 1 {
            Object[] element = new (anyObj[i]);
            newObj[i] = element;
        }
        return newObj;
    }

    # The function to set the value of the public field `getStaticObjectMultiArray2`.
    #
    # + arg - The `Object[]` value that is to be set for the field.
    function setGetStaticObjectMultiArray2(Object[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetStaticObjectMultiArray2(self.jObj, check jarrays:toHandle(arg, "java.lang.Object"));
    }

}

# The constructor function to generate an object of `org.ballerinalang.bindgen.FieldsTestResource`.
#
# + return - The new `FieldsTestResource` class generated.
function newFieldsTestResource1() returns FieldsTestResource {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_newFieldsTestResource1();
    FieldsTestResource newObj = new (externalObj);
    return newObj;
}

# The function that retrieves the value of the public field `getStaticByte`.
#
# + return - The `byte` value of the field.
function FieldsTestResource_getGetStaticByte() returns byte {
    return org_ballerinalang_bindgen_FieldsTestResource_getGetStaticByte();
}

# The function to set the value of the public field `getStaticByte`.
#
# + arg - The `byte` value that is to be set for the field.
function FieldsTestResource_setGetStaticByte(byte arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticByte(arg);
}

# The function that retrieves the value of the public field `getStaticChar`.
#
# + return - The `int` value of the field.
function FieldsTestResource_getGetStaticChar() returns int {
    return org_ballerinalang_bindgen_FieldsTestResource_getGetStaticChar();
}

# The function to set the value of the public field `getStaticChar`.
#
# + arg - The `int` value that is to be set for the field.
function FieldsTestResource_setGetStaticChar(int arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticChar(arg);
}

# The function that retrieves the value of the public field `getStaticShort`.
#
# + return - The `int` value of the field.
function FieldsTestResource_getGetStaticShort() returns int {
    return org_ballerinalang_bindgen_FieldsTestResource_getGetStaticShort();
}

# The function to set the value of the public field `getStaticShort`.
#
# + arg - The `int` value that is to be set for the field.
function FieldsTestResource_setGetStaticShort(int arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticShort(arg);
}

# The function that retrieves the value of the public field `getStaticInt`.
#
# + return - The `int` value of the field.
function FieldsTestResource_getGetStaticInt() returns int {
    return org_ballerinalang_bindgen_FieldsTestResource_getGetStaticInt();
}

# The function to set the value of the public field `getStaticInt`.
#
# + arg - The `int` value that is to be set for the field.
function FieldsTestResource_setGetStaticInt(int arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticInt(arg);
}

# The function that retrieves the value of the public field `getStaticLong`.
#
# + return - The `int` value of the field.
function FieldsTestResource_getGetStaticLong() returns int {
    return org_ballerinalang_bindgen_FieldsTestResource_getGetStaticLong();
}

# The function to set the value of the public field `getStaticLong`.
#
# + arg - The `int` value that is to be set for the field.
function FieldsTestResource_setGetStaticLong(int arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticLong(arg);
}

# The function that retrieves the value of the public field `getStaticFloat`.
#
# + return - The `float` value of the field.
function FieldsTestResource_getGetStaticFloat() returns float {
    return org_ballerinalang_bindgen_FieldsTestResource_getGetStaticFloat();
}

# The function to set the value of the public field `getStaticFloat`.
#
# + arg - The `float` value that is to be set for the field.
function FieldsTestResource_setGetStaticFloat(float arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticFloat(arg);
}

# The function that retrieves the value of the public field `getStaticDouble`.
#
# + return - The `float` value of the field.
function FieldsTestResource_getGetStaticDouble() returns float {
    return org_ballerinalang_bindgen_FieldsTestResource_getGetStaticDouble();
}

# The function to set the value of the public field `getStaticDouble`.
#
# + arg - The `float` value that is to be set for the field.
function FieldsTestResource_setGetStaticDouble(float arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticDouble(arg);
}

# The function that retrieves the value of the public field `getStaticBoolean`.
#
# + return - The `boolean` value of the field.
function FieldsTestResource_getGetStaticBoolean() returns boolean {
    return org_ballerinalang_bindgen_FieldsTestResource_getGetStaticBoolean();
}

# The function to set the value of the public field `getStaticBoolean`.
#
# + arg - The `boolean` value that is to be set for the field.
function FieldsTestResource_setGetStaticBoolean(boolean arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticBoolean(arg);
}

# The function that retrieves the value of the public field `getStaticString`.
#
# + return - The `string` value of the field.
function FieldsTestResource_getGetStaticString() returns string? {
    return java:toString(org_ballerinalang_bindgen_FieldsTestResource_getGetStaticString());
}

# The function to set the value of the public field `getStaticString`.
#
# + arg - The `string` value that is to be set for the field.
function FieldsTestResource_setGetStaticString(string arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticString(java:fromString(arg));
}

# The function that retrieves the value of the public field `GET_STATIC_FINAL_BYTE`.
#
# + return - The `byte` value of the field.
function FieldsTestResource_getGET_STATIC_FINAL_BYTE() returns byte {
    return org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_BYTE();
}

# The function that retrieves the value of the public field `GET_STATIC_FINAL_CHAR`.
#
# + return - The `int` value of the field.
function FieldsTestResource_getGET_STATIC_FINAL_CHAR() returns int {
    return org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_CHAR();
}

# The function that retrieves the value of the public field `GET_STATIC_FINAL_SHORT`.
#
# + return - The `int` value of the field.
function FieldsTestResource_getGET_STATIC_FINAL_SHORT() returns int {
    return org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_SHORT();
}

# The function that retrieves the value of the public field `GET_STATIC_FINAL_INT`.
#
# + return - The `int` value of the field.
function FieldsTestResource_getGET_STATIC_FINAL_INT() returns int {
    return org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_INT();
}

# The function that retrieves the value of the public field `GET_STATIC_FINAL_LONG`.
#
# + return - The `int` value of the field.
function FieldsTestResource_getGET_STATIC_FINAL_LONG() returns int {
    return org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_LONG();
}

# The function that retrieves the value of the public field `GET_STATIC_FINAL_FLOAT`.
#
# + return - The `float` value of the field.
function FieldsTestResource_getGET_STATIC_FINAL_FLOAT() returns float {
    return org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_FLOAT();
}

# The function that retrieves the value of the public field `GET_STATIC_FINAL_DOUBLE`.
#
# + return - The `float` value of the field.
function FieldsTestResource_getGET_STATIC_FINAL_DOUBLE() returns float {
    return org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_DOUBLE();
}

# The function that retrieves the value of the public field `GET_STATIC_FINAL_BOOLEAN`.
#
# + return - The `boolean` value of the field.
function FieldsTestResource_getGET_STATIC_FINAL_BOOLEAN() returns boolean {
    return org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_BOOLEAN();
}

# The function that retrieves the value of the public field `GET_STATIC_FINAL_STRING`.
#
# + return - The `string` value of the field.
function FieldsTestResource_getGET_STATIC_FINAL_STRING() returns string? {
    return java:toString(org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_STRING());
}

# The function that retrieves the value of the public field `getStaticByteArray`.
#
# + return - The `byte[]` value of the field.
function FieldsTestResource_getGetStaticByteArray() returns byte[]|error {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticByteArray();
    return <byte[]>check jarrays:fromHandle(externalObj, "byte[]");
}

# The function to set the value of the public field `getStaticByteArray`.
#
# + arg - The `byte[]` value that is to be set for the field.
function FieldsTestResource_setGetStaticByteArray(byte[] arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticByteArray(check jarrays:toHandle(arg, "byte"));
}

# The function that retrieves the value of the public field `getStaticCharArray`.
#
# + return - The `int[]` value of the field.
function FieldsTestResource_getGetStaticCharArray() returns int[]|error {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticCharArray();
    return <int[]>check jarrays:fromHandle(externalObj, "int[]");
}

# The function to set the value of the public field `getStaticCharArray`.
#
# + arg - The `int[]` value that is to be set for the field.
function FieldsTestResource_setGetStaticCharArray(int[] arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticCharArray(check jarrays:toHandle(arg, "char"));
}

# The function that retrieves the value of the public field `getStaticShortArray`.
#
# + return - The `int[]` value of the field.
function FieldsTestResource_getGetStaticShortArray() returns int[]|error {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticShortArray();
    return <int[]>check jarrays:fromHandle(externalObj, "int[]");
}

# The function to set the value of the public field `getStaticShortArray`.
#
# + arg - The `int[]` value that is to be set for the field.
function FieldsTestResource_setGetStaticShortArray(int[] arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticShortArray(check jarrays:toHandle(arg, "short"));
}

# The function that retrieves the value of the public field `getStaticIntArray`.
#
# + return - The `int[]` value of the field.
function FieldsTestResource_getGetStaticIntArray() returns int[]|error {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticIntArray();
    return <int[]>check jarrays:fromHandle(externalObj, "int[]");
}

# The function to set the value of the public field `getStaticIntArray`.
#
# + arg - The `int[]` value that is to be set for the field.
function FieldsTestResource_setGetStaticIntArray(int[] arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticIntArray(check jarrays:toHandle(arg, "int"));
}

# The function that retrieves the value of the public field `getStaticLongArray`.
#
# + return - The `int[]` value of the field.
function FieldsTestResource_getGetStaticLongArray() returns int[]|error {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticLongArray();
    return <int[]>check jarrays:fromHandle(externalObj, "int[]");
}

# The function to set the value of the public field `getStaticLongArray`.
#
# + arg - The `int[]` value that is to be set for the field.
function FieldsTestResource_setGetStaticLongArray(int[] arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticLongArray(check jarrays:toHandle(arg, "long"));
}

# The function that retrieves the value of the public field `getStaticFloatArray`.
#
# + return - The `float[]` value of the field.
function FieldsTestResource_getGetStaticFloatArray() returns float[]|error {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticFloatArray();
    return <float[]>check jarrays:fromHandle(externalObj, "float[]");
}

# The function to set the value of the public field `getStaticFloatArray`.
#
# + arg - The `float[]` value that is to be set for the field.
function FieldsTestResource_setGetStaticFloatArray(float[] arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticFloatArray(check jarrays:toHandle(arg, "float"));
}

# The function that retrieves the value of the public field `getStaticDoubleArray`.
#
# + return - The `float[]` value of the field.
function FieldsTestResource_getGetStaticDoubleArray() returns float[]|error {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticDoubleArray();
    return <float[]>check jarrays:fromHandle(externalObj, "float[]");
}

# The function to set the value of the public field `getStaticDoubleArray`.
#
# + arg - The `float[]` value that is to be set for the field.
function FieldsTestResource_setGetStaticDoubleArray(float[] arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticDoubleArray(check jarrays:toHandle(arg, "double"));
}

# The function that retrieves the value of the public field `getStaticBooleanArray`.
#
# + return - The `boolean[]` value of the field.
function FieldsTestResource_getGetStaticBooleanArray() returns boolean[]|error {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticBooleanArray();
    return <boolean[]>check jarrays:fromHandle(externalObj, "boolean[]");
}

# The function to set the value of the public field `getStaticBooleanArray`.
#
# + arg - The `boolean[]` value that is to be set for the field.
function FieldsTestResource_setGetStaticBooleanArray(boolean[] arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticBooleanArray(check jarrays:toHandle(arg, "boolean"));
}

# The function that retrieves the value of the public field `getStaticStringArray`.
#
# + return - The `string[]` value of the field.
function FieldsTestResource_getGetStaticStringArray() returns string[]|error {
    return <string[]>check jarrays:fromHandle(org_ballerinalang_bindgen_FieldsTestResource_getGetStaticStringArray(), "string");
}

# The function to set the value of the public field `getStaticStringArray`.
#
# + arg - The `string[]` value that is to be set for the field.
function FieldsTestResource_setGetStaticStringArray(string[] arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticStringArray(check jarrays:toHandle(arg, "java.lang.String"));
}

# The function that retrieves the value of the public field `getStaticInterface`.
#
# + return - The `List` value of the field.
function FieldsTestResource_getGetStaticInterface() returns List {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticInterface();
    List newObj = new (externalObj);
    return newObj;
}

# The function to set the value of the public field `getStaticInterface`.
#
# + arg - The `List` value that is to be set for the field.
function FieldsTestResource_setGetStaticInterface(List arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticInterface(arg.jObj);
}

# The function that retrieves the value of the public field `getStaticAbstractClass`.
#
# + return - The `AbstractList` value of the field.
function FieldsTestResource_getGetStaticAbstractClass() returns AbstractList {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticAbstractClass();
    AbstractList newObj = new (externalObj);
    return newObj;
}

# The function to set the value of the public field `getStaticAbstractClass`.
#
# + arg - The `AbstractList` value that is to be set for the field.
function FieldsTestResource_setGetStaticAbstractClass(AbstractList arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticAbstractClass(arg.jObj);
}

# The function that retrieves the value of the public field `getStaticObject`.
#
# + return - The `Path` value of the field.
function FieldsTestResource_getGetStaticObject() returns Path {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticObject();
    Path newObj = new (externalObj);
    return newObj;
}

# The function to set the value of the public field `getStaticObject`.
#
# + arg - The `Path` value that is to be set for the field.
function FieldsTestResource_setGetStaticObject(Path arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticObject(arg.jObj);
}

# The function that retrieves the value of the public field `getStaticGenericObject`.
#
# + return - The `Set` value of the field.
function FieldsTestResource_getGetStaticGenericObject() returns Set {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticGenericObject();
    Set newObj = new (externalObj);
    return newObj;
}

# The function to set the value of the public field `getStaticGenericObject`.
#
# + arg - The `Set` value that is to be set for the field.
function FieldsTestResource_setGetStaticGenericObject(Set arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticGenericObject(arg.jObj);
}

# The function that retrieves the value of the public field `getStaticEnumeration`.
#
# + return - The `Level` value of the field.
function FieldsTestResource_getGetStaticEnumeration() returns Level {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticEnumeration();
    Level newObj = new (externalObj);
    return newObj;
}

# The function to set the value of the public field `getStaticEnumeration`.
#
# + arg - The `Level` value that is to be set for the field.
function FieldsTestResource_setGetStaticEnumeration(Level arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticEnumeration(arg.jObj);
}

# The function that retrieves the value of the public field `getStaticThrowable`.
#
# + return - The `ArithmeticException` value of the field.
function FieldsTestResource_getGetStaticThrowable() returns JArithmeticException {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticThrowable();
    JArithmeticException newObj = new (externalObj);
    return newObj;
}

# The function to set the value of the public field `getStaticThrowable`.
#
# + arg - The `JArithmeticException` value that is to be set for the field.
function FieldsTestResource_setGetStaticThrowable(JArithmeticException arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticThrowable(arg.jObj);
}

# The function that retrieves the value of the public field `TEST_FIELD`.
#
# + return - The `int` value of the field.
function FieldsTestResource_getTEST_FIELD() returns int {
    return org_ballerinalang_bindgen_FieldsTestResource_getTEST_FIELD();
}

function org_ballerinalang_bindgen_FieldsTestResource_equals(handle receiver, handle arg0) returns boolean = @java:Method {
    name: "equals",
    'class: "org.ballerinalang.bindgen.FieldsTestResource",
    paramTypes: ["java.lang.Object"]
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getClass(handle receiver) returns handle = @java:Method {
    name: "getClass",
    'class: "org.ballerinalang.bindgen.FieldsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_FieldsTestResource_hashCode(handle receiver) returns int = @java:Method {
    name: "hashCode",
    'class: "org.ballerinalang.bindgen.FieldsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_FieldsTestResource_notify(handle receiver) = @java:Method {
    name: "notify",
    'class: "org.ballerinalang.bindgen.FieldsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_FieldsTestResource_notifyAll(handle receiver) = @java:Method {
    name: "notifyAll",
    'class: "org.ballerinalang.bindgen.FieldsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_FieldsTestResource_returnStringArray(handle receiver) returns handle = @java:Method {
    name: "returnStringArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_FieldsTestResource_testMethod(handle receiver, int arg0) returns int = @java:Method {
    name: "testMethod",
    'class: "org.ballerinalang.bindgen.FieldsTestResource",
    paramTypes: ["int"]
} external;

function org_ballerinalang_bindgen_FieldsTestResource_wait(handle receiver) returns error? = @java:Method {
    name: "wait",
    'class: "org.ballerinalang.bindgen.FieldsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_FieldsTestResource_wait2(handle receiver, int arg0) returns error? = @java:Method {
    name: "wait",
    'class: "org.ballerinalang.bindgen.FieldsTestResource",
    paramTypes: ["long"]
} external;

function org_ballerinalang_bindgen_FieldsTestResource_wait3(handle receiver, int arg0, int arg1) returns error? = @java:Method {
    name: "wait",
    'class: "org.ballerinalang.bindgen.FieldsTestResource",
    paramTypes: ["long", "int"]
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceByte(handle receiver) returns byte = @java:FieldGet {
    name: "getInstanceByte",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceByte(handle receiver, byte arg) = @java:FieldSet {
    name: "getInstanceByte",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceChar(handle receiver) returns int = @java:FieldGet {
    name: "getInstanceChar",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceChar(handle receiver, int arg) = @java:FieldSet {
    name: "getInstanceChar",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceShort(handle receiver) returns int = @java:FieldGet {
    name: "getInstanceShort",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceShort(handle receiver, int arg) = @java:FieldSet {
    name: "getInstanceShort",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceInt(handle receiver) returns int = @java:FieldGet {
    name: "getInstanceInt",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceInt(handle receiver, int arg) = @java:FieldSet {
    name: "getInstanceInt",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceLong(handle receiver) returns int = @java:FieldGet {
    name: "getInstanceLong",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceLong(handle receiver, int arg) = @java:FieldSet {
    name: "getInstanceLong",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceFloat(handle receiver) returns float = @java:FieldGet {
    name: "getInstanceFloat",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceFloat(handle receiver, float arg) = @java:FieldSet {
    name: "getInstanceFloat",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceDouble(handle receiver) returns float = @java:FieldGet {
    name: "getInstanceDouble",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceDouble(handle receiver, float arg) = @java:FieldSet {
    name: "getInstanceDouble",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceBoolean(handle receiver) returns boolean = @java:FieldGet {
    name: "getInstanceBoolean",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceBoolean(handle receiver, boolean arg) = @java:FieldSet {
    name: "getInstanceBoolean",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceString(handle receiver) returns handle = @java:FieldGet {
    name: "getInstanceString",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceString(handle receiver, handle arg) = @java:FieldSet {
    name: "getInstanceString",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getTestUnsupportedArray(handle receiver) returns handle = @java:FieldGet {
    name: "testUnsupportedArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceByteArray(handle receiver) returns handle = @java:FieldGet {
    name: "getInstanceByteArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceByteArray(handle receiver, handle arg) = @java:FieldSet {
    name: "getInstanceByteArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceCharArray(handle receiver) returns handle = @java:FieldGet {
    name: "getInstanceCharArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceCharArray(handle receiver, handle arg) = @java:FieldSet {
    name: "getInstanceCharArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceShortArray(handle receiver) returns handle = @java:FieldGet {
    name: "getInstanceShortArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceShortArray(handle receiver, handle arg) = @java:FieldSet {
    name: "getInstanceShortArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceIntArray(handle receiver) returns handle = @java:FieldGet {
    name: "getInstanceIntArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceIntArray(handle receiver, handle arg) = @java:FieldSet {
    name: "getInstanceIntArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceLongArray(handle receiver) returns handle = @java:FieldGet {
    name: "getInstanceLongArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceLongArray(handle receiver, handle arg) = @java:FieldSet {
    name: "getInstanceLongArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceFloatArray(handle receiver) returns handle = @java:FieldGet {
    name: "getInstanceFloatArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceFloatArray(handle receiver, handle arg) = @java:FieldSet {
    name: "getInstanceFloatArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceDoubleArray(handle receiver) returns handle = @java:FieldGet {
    name: "getInstanceDoubleArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceDoubleArray(handle receiver, handle arg) = @java:FieldSet {
    name: "getInstanceDoubleArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceBooleanArray(handle receiver) returns handle = @java:FieldGet {
    name: "getInstanceBooleanArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceBooleanArray(handle receiver, handle arg) = @java:FieldSet {
    name: "getInstanceBooleanArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceStringArray(handle receiver) returns handle = @java:FieldGet {
    name: "getInstanceStringArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceStringArray(handle receiver, handle arg) = @java:FieldSet {
    name: "getInstanceStringArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceObjectArray(handle receiver) returns handle = @java:FieldGet {
    name: "getInstanceObjectArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceObjectArray(handle receiver, handle arg) = @java:FieldSet {
    name: "getInstanceObjectArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceObjectMultiArray1(handle receiver) returns handle = @java:FieldGet {
    name: "getInstanceObjectMultiArray1",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceObjectMultiArray1(handle receiver, handle arg) = @java:FieldSet {
    name: "getInstanceObjectMultiArray1",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceObjectMultiArray2(handle receiver) returns handle = @java:FieldGet {
    name: "getInstanceObjectMultiArray2",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceObjectMultiArray2(handle receiver, handle arg) = @java:FieldSet {
    name: "getInstanceObjectMultiArray2",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceInterface(handle receiver) returns handle = @java:FieldGet {
    name: "getInstanceInterface",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceInterface(handle receiver, handle arg) = @java:FieldSet {
    name: "getInstanceInterface",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceAbstractClass(handle receiver) returns handle = @java:FieldGet {
    name: "getInstanceAbstractClass",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceAbstractClass(handle receiver, handle arg) = @java:FieldSet {
    name: "getInstanceAbstractClass",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceObject(handle receiver) returns handle = @java:FieldGet {
    name: "getInstanceObject",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceObject(handle receiver, handle arg) = @java:FieldSet {
    name: "getInstanceObject",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceGenericObject(handle receiver) returns handle = @java:FieldGet {
    name: "getInstanceGenericObject",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceGenericObject(handle receiver, handle arg) = @java:FieldSet {
    name: "getInstanceGenericObject",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceEnumeration(handle receiver) returns handle = @java:FieldGet {
    name: "getInstanceEnumeration",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceEnumeration(handle receiver, handle arg) = @java:FieldSet {
    name: "getInstanceEnumeration",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceThrowable(handle receiver) returns handle = @java:FieldGet {
    name: "getInstanceThrowable",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceThrowable(handle receiver, handle arg) = @java:FieldSet {
    name: "getInstanceThrowable",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticByte() returns byte = @java:FieldGet {
    name: "getStaticByte",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticByte(byte arg) = @java:FieldSet {
    name: "getStaticByte",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticChar() returns int = @java:FieldGet {
    name: "getStaticChar",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticChar(int arg) = @java:FieldSet {
    name: "getStaticChar",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticShort() returns int = @java:FieldGet {
    name: "getStaticShort",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticShort(int arg) = @java:FieldSet {
    name: "getStaticShort",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticInt() returns int = @java:FieldGet {
    name: "getStaticInt",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticInt(int arg) = @java:FieldSet {
    name: "getStaticInt",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticLong() returns int = @java:FieldGet {
    name: "getStaticLong",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticLong(int arg) = @java:FieldSet {
    name: "getStaticLong",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticFloat() returns float = @java:FieldGet {
    name: "getStaticFloat",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticFloat(float arg) = @java:FieldSet {
    name: "getStaticFloat",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticDouble() returns float = @java:FieldGet {
    name: "getStaticDouble",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticDouble(float arg) = @java:FieldSet {
    name: "getStaticDouble",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticBoolean() returns boolean = @java:FieldGet {
    name: "getStaticBoolean",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticBoolean(boolean arg) = @java:FieldSet {
    name: "getStaticBoolean",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticString() returns handle = @java:FieldGet {
    name: "getStaticString",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticString(handle arg) = @java:FieldSet {
    name: "getStaticString",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_BYTE() returns byte = @java:FieldGet {
    name: "GET_STATIC_FINAL_BYTE",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_CHAR() returns int = @java:FieldGet {
    name: "GET_STATIC_FINAL_CHAR",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_SHORT() returns int = @java:FieldGet {
    name: "GET_STATIC_FINAL_SHORT",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_INT() returns int = @java:FieldGet {
    name: "GET_STATIC_FINAL_INT",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_LONG() returns int = @java:FieldGet {
    name: "GET_STATIC_FINAL_LONG",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_FLOAT() returns float = @java:FieldGet {
    name: "GET_STATIC_FINAL_FLOAT",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_DOUBLE() returns float = @java:FieldGet {
    name: "GET_STATIC_FINAL_DOUBLE",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_BOOLEAN() returns boolean = @java:FieldGet {
    name: "GET_STATIC_FINAL_BOOLEAN",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_STRING() returns handle = @java:FieldGet {
    name: "GET_STATIC_FINAL_STRING",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticByteArray() returns handle = @java:FieldGet {
    name: "getStaticByteArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticByteArray(handle arg) = @java:FieldSet {
    name: "getStaticByteArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticCharArray() returns handle = @java:FieldGet {
    name: "getStaticCharArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticCharArray(handle arg) = @java:FieldSet {
    name: "getStaticCharArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticShortArray() returns handle = @java:FieldGet {
    name: "getStaticShortArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticShortArray(handle arg) = @java:FieldSet {
    name: "getStaticShortArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticIntArray() returns handle = @java:FieldGet {
    name: "getStaticIntArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticIntArray(handle arg) = @java:FieldSet {
    name: "getStaticIntArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticLongArray() returns handle = @java:FieldGet {
    name: "getStaticLongArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticLongArray(handle arg) = @java:FieldSet {
    name: "getStaticLongArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticFloatArray() returns handle = @java:FieldGet {
    name: "getStaticFloatArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticFloatArray(handle arg) = @java:FieldSet {
    name: "getStaticFloatArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticDoubleArray() returns handle = @java:FieldGet {
    name: "getStaticDoubleArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticDoubleArray(handle arg) = @java:FieldSet {
    name: "getStaticDoubleArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticBooleanArray() returns handle = @java:FieldGet {
    name: "getStaticBooleanArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticBooleanArray(handle arg) = @java:FieldSet {
    name: "getStaticBooleanArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticStringArray() returns handle = @java:FieldGet {
    name: "getStaticStringArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticStringArray(handle arg) = @java:FieldSet {
    name: "getStaticStringArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticObjectArray(handle receiver) returns handle = @java:FieldGet {
    name: "getStaticObjectArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticObjectArray(handle receiver, handle arg) = @java:FieldSet {
    name: "getStaticObjectArray",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticObjectMultiArray1(handle receiver) returns handle = @java:FieldGet {
    name: "getStaticObjectMultiArray1",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticObjectMultiArray1(handle receiver, handle arg) = @java:FieldSet {
    name: "getStaticObjectMultiArray1",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticObjectMultiArray2(handle receiver) returns handle = @java:FieldGet {
    name: "getStaticObjectMultiArray2",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticObjectMultiArray2(handle receiver, handle arg) = @java:FieldSet {
    name: "getStaticObjectMultiArray2",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticInterface() returns handle = @java:FieldGet {
    name: "getStaticInterface",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticInterface(handle arg) = @java:FieldSet {
    name: "getStaticInterface",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticAbstractClass() returns handle = @java:FieldGet {
    name: "getStaticAbstractClass",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticAbstractClass(handle arg) = @java:FieldSet {
    name: "getStaticAbstractClass",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticObject() returns handle = @java:FieldGet {
    name: "getStaticObject",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticObject(handle arg) = @java:FieldSet {
    name: "getStaticObject",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticGenericObject() returns handle = @java:FieldGet {
    name: "getStaticGenericObject",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticGenericObject(handle arg) = @java:FieldSet {
    name: "getStaticGenericObject",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticEnumeration() returns handle = @java:FieldGet {
    name: "getStaticEnumeration",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticEnumeration(handle arg) = @java:FieldSet {
    name: "getStaticEnumeration",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getGetStaticThrowable() returns handle = @java:FieldGet {
    name: "getStaticThrowable",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_setGetStaticThrowable(handle arg) = @java:FieldSet {
    name: "getStaticThrowable",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_getTEST_FIELD() returns int = @java:FieldGet {
    name: "TEST_FIELD",
    'class: "org.ballerinalang.bindgen.FieldsTestResource"
} external;

function org_ballerinalang_bindgen_FieldsTestResource_newFieldsTestResource1() returns handle = @java:Constructor {
    'class: "org.ballerinalang.bindgen.FieldsTestResource",
    paramTypes: []
} external;
