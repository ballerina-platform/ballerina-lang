import ballerina/jballerina.java;
import ballerinax/java.arrays as jarrays;

# Ballerina class mapping for the Java `org.ballerinalang.bindgen.FieldsTestResource` class.
@java:Binding {'class: "org.ballerinalang.bindgen.FieldsTestResource"}
distinct class FieldsTestResource {

    *java:JObject;
    *Object;

    # The `handle` field that stores the reference to the `org.ballerinalang.bindgen.FieldsTestResource` object.
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

    # The function that maps to the `wait` method of `org.ballerinalang.bindgen.FieldsTestResource`.
    #
    # + return - The `InterruptedException` value returning from the Java mapping.
    function wait1() returns InterruptedException? {
        error|() externalObj = org_ballerinalang_bindgen_FieldsTestResource_wait1(self.jObj);
        if (externalObj is error) {
            InterruptedException e = error InterruptedException(INTERRUPTEDEXCEPTION, externalObj, message = externalObj.
            message());
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
            InterruptedException e = error InterruptedException(INTERRUPTEDEXCEPTION, externalObj, message = externalObj.
            message());
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
            InterruptedException e = error InterruptedException(INTERRUPTEDEXCEPTION, externalObj, message = externalObj.
            message());
            return e;
        }
    }

    # The function that retrieves the value of the public field `getInstanceByte`.
    #
    # + return - The `byte` value of the field.
    function getGetInstanceByte(byte arg) returns byte {
        return org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceByte(self.jObj, arg);
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
    function getGetInstanceChar(int arg) returns int {
        return org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceChar(self.jObj, arg);
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
    function getGetInstanceShort(int arg) returns int {
        return org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceShort(self.jObj, arg);
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
    function getGetInstanceInt(int arg) returns int {
        return org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceInt(self.jObj, arg);
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
    function getGetInstanceLong(int arg) returns int {
        return org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceLong(self.jObj, arg);
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
    function getGetInstanceFloat(float arg) returns float {
        return org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceFloat(self.jObj, arg);
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
    function getGetInstanceDouble(float arg) returns float {
        return org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceDouble(self.jObj, arg);
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
    function getGetInstanceBoolean(boolean arg) returns boolean {
        return org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceBoolean(self.jObj, arg);
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
    function getGetInstanceString(string arg) returns string? {
        return java:toString(org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceString(self.jObj,
        java:fromString(arg)));
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
    function getGetInstanceByteArray(byte[] arg) returns byte[]|error {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceByteArray(self.jObj, check
        jarrays:toHandle(arg, "byte"));
        return <byte[]>check jarrays:fromHandle(externalObj, "byte[]");
    }

    # The function to set the value of the public field `getInstanceByteArray`.
    #
    # + arg - The `byte[]` value that is to be set for the field.
    function setGetInstanceByteArray(byte[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceByteArray(self.jObj, check jarrays:toHandle(arg,
        "byte"));
    }

    # The function that retrieves the value of the public field `getInstanceCharArray`.
    #
    # + return - The `int[]` value of the field.
    function getGetInstanceCharArray(int[] arg) returns int[]|error {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceCharArray(self.jObj, check
        jarrays:toHandle(arg, "char"));
        return <int[]>check jarrays:fromHandle(externalObj, "int[]");
    }

    # The function to set the value of the public field `getInstanceCharArray`.
    #
    # + arg - The `int[]` value that is to be set for the field.
    function setGetInstanceCharArray(int[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceCharArray(self.jObj, check jarrays:toHandle(arg,
        "char"));
    }

    # The function that retrieves the value of the public field `getInstanceShortArray`.
    #
    # + return - The `int[]` value of the field.
    function getGetInstanceShortArray(int[] arg) returns int[]|error {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceShortArray(self.jObj, check
        jarrays:toHandle(arg, "short"));
        return <int[]>check jarrays:fromHandle(externalObj, "int[]");
    }

    # The function to set the value of the public field `getInstanceShortArray`.
    #
    # + arg - The `int[]` value that is to be set for the field.
    function setGetInstanceShortArray(int[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceShortArray(self.jObj, check jarrays:toHandle(arg,
        "short"));
    }

    # The function that retrieves the value of the public field `getInstanceIntArray`.
    #
    # + return - The `int[]` value of the field.
    function getGetInstanceIntArray(int[] arg) returns int[]|error {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceIntArray(self.jObj, check
        jarrays:toHandle(arg, "int"));
        return <int[]>check jarrays:fromHandle(externalObj, "int[]");
    }

    # The function to set the value of the public field `getInstanceIntArray`.
    #
    # + arg - The `int[]` value that is to be set for the field.
    function setGetInstanceIntArray(int[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceIntArray(self.jObj, check
        jarrays:toHandle(arg, "int"));
    }

    # The function that retrieves the value of the public field `getInstanceLongArray`.
    #
    # + return - The `int[]` value of the field.
    function getGetInstanceLongArray(int[] arg) returns int[]|error {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceLongArray(self.jObj, check
        jarrays:toHandle(arg, "long"));
        return <int[]>check jarrays:fromHandle(externalObj, "int[]");
    }

    # The function to set the value of the public field `getInstanceLongArray`.
    #
    # + arg - The `int[]` value that is to be set for the field.
    function setGetInstanceLongArray(int[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceLongArray(self.jObj, check jarrays:toHandle(arg,
        "long"));
    }

    # The function that retrieves the value of the public field `getInstanceFloatArray`.
    #
    # + return - The `float[]` value of the field.
    function getGetInstanceFloatArray(float[] arg) returns float[]|error {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceFloatArray(self.jObj, check
        jarrays:toHandle(arg, "float"));
        return <float[]>check jarrays:fromHandle(externalObj, "float[]");
    }

    # The function to set the value of the public field `getInstanceFloatArray`.
    #
    # + arg - The `float[]` value that is to be set for the field.
    function setGetInstanceFloatArray(float[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceFloatArray(self.jObj, check jarrays:toHandle(arg,
        "float"));
    }

    # The function that retrieves the value of the public field `getInstanceDoubleArray`.
    #
    # + return - The `float[]` value of the field.
    function getGetInstanceDoubleArray(float[] arg) returns float[]|error {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceDoubleArray(self.jObj, check
        jarrays:toHandle(arg, "double"));
        return <float[]>check jarrays:fromHandle(externalObj, "float[]");
    }

    # The function to set the value of the public field `getInstanceDoubleArray`.
    #
    # + arg - The `float[]` value that is to be set for the field.
    function setGetInstanceDoubleArray(float[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceDoubleArray(self.jObj, check jarrays:toHandle(arg,
        "double"));
    }

    # The function that retrieves the value of the public field `getInstanceBooleanArray`.
    #
    # + return - The `boolean[]` value of the field.
    function getGetInstanceBooleanArray(boolean[] arg) returns boolean[]|error {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceBooleanArray(self.jObj, check
        jarrays:toHandle(arg, "boolean"));
        return <boolean[]>check jarrays:fromHandle(externalObj, "boolean[]");
    }

    # The function to set the value of the public field `getInstanceBooleanArray`.
    #
    # + arg - The `boolean[]` value that is to be set for the field.
    function setGetInstanceBooleanArray(boolean[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceBooleanArray(self.jObj, check jarrays:toHandle(arg,
        "boolean"));
    }

    # The function that retrieves the value of the public field `getInstanceStringArray`.
    #
    # + return - The `string[]` value of the field.
    function getGetInstanceStringArray(string[] arg) returns string[]|error {
        return <string[]>check jarrays:fromHandle(org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceStringArray(
        self.jObj, check jarrays:toHandle(arg, "java.lang.String")), "string");
    }

    # The function to set the value of the public field `getInstanceStringArray`.
    #
    # + arg - The `string[]` value that is to be set for the field.
    function setGetInstanceStringArray(string[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceStringArray(self.jObj, check jarrays:toHandle(arg,
        "java.lang.String"));
    }

    # The function that retrieves the value of the public field `getInstanceObjectArray`.
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

    # The function to set the value of the public field `getInstanceObjectArray`.
    #
    # + arg - The `StringBuilder[]` value that is to be set for the field.
    function setGetInstanceObjectArray(StringBuilder[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceObjectArray(self.jObj, check jarrays:toHandle(arg,
        "java.lang.StringBuilder"));
    }

    # The function that retrieves the value of the public field `getInstanceObjectMultiArray1`.
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

    # The function to set the value of the public field `getInstanceObjectMultiArray1`.
    #
    # + arg - The `Integer[]` value that is to be set for the field.
    function setGetInstanceObjectMultiArray1(Integer[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceObjectMultiArray1(self.jObj, check jarrays:toHandle(
        arg, "java.lang.Integer"));
    }

    # The function that retrieves the value of the public field `getInstanceObjectMultiArray2`.
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

    # The function to set the value of the public field `getInstanceObjectMultiArray2`.
    #
    # + arg - The `Object[]` value that is to be set for the field.
    function setGetInstanceObjectMultiArray2(Object[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetInstanceObjectMultiArray2(self.jObj, check jarrays:toHandle(
        arg, "java.lang.Object"));
    }

    # The function that retrieves the value of the public field `getInstanceInterface`.
    #
    # + return - The `List` value of the field.
    function getGetInstanceInterface(List arg) returns List {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceInterface(self.jObj, arg.jObj);
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
    function getGetInstanceAbstractClass(AbstractList arg) returns AbstractList {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceAbstractClass(self.jObj, arg.
        jObj);
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
    function getGetInstanceObject(Path arg) returns Path {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceObject(self.jObj, arg.jObj);
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
    function getGetInstanceGenericObject(Set arg) returns Set {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceGenericObject(self.jObj, arg.
        jObj);
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
    function getGetInstanceEnumeration(Level arg) returns Level {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceEnumeration(self.jObj, arg.jObj);
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
    function getGetInstanceThrowable(JArithmeticException arg) returns JArithmeticException {
        handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetInstanceThrowable(self.jObj, arg.jObj);
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

    # The function to set the value of the public field `getStaticObjectArray`.
    #
    # + arg - The `StringBuilder[]` value that is to be set for the field.
    function setGetStaticObjectArray(StringBuilder[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetStaticObjectArray(self.jObj, check jarrays:toHandle(arg,
        "java.lang.StringBuilder"));
    }

    # The function that retrieves the value of the public field `getStaticObjectMultiArray1`.
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

    # The function to set the value of the public field `getStaticObjectMultiArray1`.
    #
    # + arg - The `Integer[]` value that is to be set for the field.
    function setGetStaticObjectMultiArray1(Integer[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetStaticObjectMultiArray1(self.jObj, check jarrays:toHandle(arg,
        "java.lang.Integer"));
    }

    # The function that retrieves the value of the public field `getStaticObjectMultiArray2`.
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

    # The function to set the value of the public field `getStaticObjectMultiArray2`.
    #
    # + arg - The `Object[]` value that is to be set for the field.
    function setGetStaticObjectMultiArray2(Object[] arg) {
        org_ballerinalang_bindgen_FieldsTestResource_setGetStaticObjectMultiArray2(self.jObj, check jarrays:toHandle(arg,
        "java.lang.Object"));
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
function FieldsTestResource_getGetStaticByte(byte arg) returns byte {
    return org_ballerinalang_bindgen_FieldsTestResource_getGetStaticByte(arg);
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
function FieldsTestResource_getGetStaticChar(int arg) returns int {
    return org_ballerinalang_bindgen_FieldsTestResource_getGetStaticChar(arg);
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
function FieldsTestResource_getGetStaticShort(int arg) returns int {
    return org_ballerinalang_bindgen_FieldsTestResource_getGetStaticShort(arg);
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
function FieldsTestResource_getGetStaticInt(int arg) returns int {
    return org_ballerinalang_bindgen_FieldsTestResource_getGetStaticInt(arg);
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
function FieldsTestResource_getGetStaticLong(int arg) returns int {
    return org_ballerinalang_bindgen_FieldsTestResource_getGetStaticLong(arg);
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
function FieldsTestResource_getGetStaticFloat(float arg) returns float {
    return org_ballerinalang_bindgen_FieldsTestResource_getGetStaticFloat(arg);
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
function FieldsTestResource_getGetStaticDouble(float arg) returns float {
    return org_ballerinalang_bindgen_FieldsTestResource_getGetStaticDouble(arg);
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
function FieldsTestResource_getGetStaticBoolean(boolean arg) returns boolean {
    return org_ballerinalang_bindgen_FieldsTestResource_getGetStaticBoolean(arg);
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
function FieldsTestResource_getGetStaticString(string arg) returns string? {
    return java:toString(org_ballerinalang_bindgen_FieldsTestResource_getGetStaticString(java:fromString(arg)));
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
function FieldsTestResource_getGET_STATIC_FINAL_BYTE(byte arg) returns byte {
    return org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_BYTE(arg);
}

# The function that retrieves the value of the public field `GET_STATIC_FINAL_CHAR`.
#
# + return - The `int` value of the field.
function FieldsTestResource_getGET_STATIC_FINAL_CHAR(int arg) returns int {
    return org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_CHAR(arg);
}

# The function that retrieves the value of the public field `GET_STATIC_FINAL_SHORT`.
#
# + return - The `int` value of the field.
function FieldsTestResource_getGET_STATIC_FINAL_SHORT(int arg) returns int {
    return org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_SHORT(arg);
}

# The function that retrieves the value of the public field `GET_STATIC_FINAL_INT`.
#
# + return - The `int` value of the field.
function FieldsTestResource_getGET_STATIC_FINAL_INT(int arg) returns int {
    return org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_INT(arg);
}

# The function that retrieves the value of the public field `GET_STATIC_FINAL_LONG`.
#
# + return - The `int` value of the field.
function FieldsTestResource_getGET_STATIC_FINAL_LONG(int arg) returns int {
    return org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_LONG(arg);
}

# The function that retrieves the value of the public field `GET_STATIC_FINAL_FLOAT`.
#
# + return - The `float` value of the field.
function FieldsTestResource_getGET_STATIC_FINAL_FLOAT(float arg) returns float {
    return org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_FLOAT(arg);
}

# The function that retrieves the value of the public field `GET_STATIC_FINAL_DOUBLE`.
#
# + return - The `float` value of the field.
function FieldsTestResource_getGET_STATIC_FINAL_DOUBLE(float arg) returns float {
    return org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_DOUBLE(arg);
}

# The function that retrieves the value of the public field `GET_STATIC_FINAL_BOOLEAN`.
#
# + return - The `boolean` value of the field.
function FieldsTestResource_getGET_STATIC_FINAL_BOOLEAN(boolean arg) returns boolean {
    return org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_BOOLEAN(arg);
}

# The function that retrieves the value of the public field `GET_STATIC_FINAL_STRING`.
#
# + return - The `string` value of the field.
function FieldsTestResource_getGET_STATIC_FINAL_STRING(string arg) returns string? {
    return java:toString(org_ballerinalang_bindgen_FieldsTestResource_getGET_STATIC_FINAL_STRING(java:fromString(arg)));
}

# The function that retrieves the value of the public field `getStaticByteArray`.
#
# + return - The `byte[]` value of the field.
function FieldsTestResource_getGetStaticByteArray(byte[] arg) returns byte[]|error {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticByteArray(check jarrays:toHandle(arg,
    "byte"));
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
function FieldsTestResource_getGetStaticCharArray(int[] arg) returns int[]|error {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticCharArray(check jarrays:toHandle(arg,
    "char"));
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
function FieldsTestResource_getGetStaticShortArray(int[] arg) returns int[]|error {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticShortArray(check jarrays:toHandle(arg,
    "short"));
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
function FieldsTestResource_getGetStaticIntArray(int[] arg) returns int[]|error {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticIntArray(check jarrays:toHandle(arg,
    "int"));
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
function FieldsTestResource_getGetStaticLongArray(int[] arg) returns int[]|error {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticLongArray(check jarrays:toHandle(arg,
    "long"));
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
function FieldsTestResource_getGetStaticFloatArray(float[] arg) returns float[]|error {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticFloatArray(check jarrays:toHandle(arg,
    "float"));
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
function FieldsTestResource_getGetStaticDoubleArray(float[] arg) returns float[]|error {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticDoubleArray(check jarrays:toHandle(arg,
    "double"));
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
function FieldsTestResource_getGetStaticBooleanArray(boolean[] arg) returns boolean[]|error {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticBooleanArray(check jarrays:toHandle(
    arg, "boolean"));
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
function FieldsTestResource_getGetStaticStringArray(string[] arg) returns string[]|error {
    return <string[]>check jarrays:fromHandle(org_ballerinalang_bindgen_FieldsTestResource_getGetStaticStringArray(check
    jarrays:toHandle(arg, "java.lang.String")), "string");
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
function FieldsTestResource_getGetStaticInterface(List arg) returns List {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticInterface(arg.jObj);
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
function FieldsTestResource_getGetStaticAbstractClass(AbstractList arg) returns AbstractList {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticAbstractClass(arg.jObj);
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
function FieldsTestResource_getGetStaticObject(Path arg) returns Path {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticObject(arg.jObj);
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
function FieldsTestResource_getGetStaticGenericObject(Set arg) returns Set {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticGenericObject(arg.jObj);
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
function FieldsTestResource_getGetStaticEnumeration(Level arg) returns Level {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticEnumeration(arg.jObj);
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
function FieldsTestResource_getGetStaticThrowable(JArithmeticException arg) returns JArithmeticException {
    handle externalObj = org_ballerinalang_bindgen_FieldsTestResource_getGetStaticThrowable(arg.jObj);
    JArithmeticException newObj = new (externalObj);
    return newObj;
}

# The function to set the value of the public field `getStaticThrowable`.
#
# + arg - The `JArithmeticException` value that is to be set for the field.
function FieldsTestResource_setGetStaticThrowable(JArithmeticException arg) {
    org_ballerinalang_bindgen_FieldsTestResource_setGetStaticThrowable(arg.jObj);
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

function org_ballerinalang_bindgen_FieldsTestResource_wait1(handle receiver) returns error? = @java:Method {
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
