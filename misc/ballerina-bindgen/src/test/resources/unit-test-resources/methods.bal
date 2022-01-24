import ballerina/jballerina.java;
import ballerina/jballerina.java.arrays as jarrays;

# Ballerina class mapping for the Java `org.ballerinalang.bindgen.MethodsTestResource` class.
@java:Binding {'class: "org.ballerinalang.bindgen.MethodsTestResource"}
distinct class MethodsTestResource {

    *java:JObject;
    *Object;

    # The `handle` field that stores the reference to the `org.ballerinalang.bindgen.MethodsTestResource` object.
    public handle jObj;

    # The init function of the Ballerina class mapping the `org.ballerinalang.bindgen.MethodsTestResource` Java class.
    #
    # + obj - The `handle` value containing the Java reference of the object.
    function init(handle obj) {
        self.jObj = obj;
    }

    # The function to retrieve the string representation of the Ballerina class mapping the `org.ballerinalang.bindgen.MethodsTestResource` Java class.
    #
    # + return - The `string` form of the Java object instance.
    function toString() returns string {
        return java:toString(self.jObj) ?: "null";
    }
    # The function that maps to the `abstractObjectParam` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + arg0 - The `AbstractSet` value required to map with the Java method parameter.
    # + arg1 - The `Object` value required to map with the Java method parameter.
    # + arg2 - The `string` value required to map with the Java method parameter.
    function abstractObjectParam(AbstractSet arg0, Object arg1, string arg2) {
        org_ballerinalang_bindgen_MethodsTestResource_abstractObjectParam(self.jObj, arg0.jObj, arg1.jObj, java:fromString(arg2));
    }

    # The function that maps to the `enumParam` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + arg0 - The `Level` value required to map with the Java method parameter.
    # + return - The `Level` value returning from the Java mapping.
    function enumParam(Level arg0) returns Level {
        handle externalObj = org_ballerinalang_bindgen_MethodsTestResource_enumParam(self.jObj, arg0.jObj);
        Level newObj = new (externalObj);
        return newObj;
    }

    # The function that maps to the `equals` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + arg0 - The `Object` value required to map with the Java method parameter.
    # + return - The `boolean` value returning from the Java mapping.
    function 'equals(Object arg0) returns boolean {
        return org_ballerinalang_bindgen_MethodsTestResource_equals(self.jObj, arg0.jObj);
    }

    # The function that maps to the `errorParam` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + arg0 - The `JIOException` value required to map with the Java method parameter.
    # + arg1 - The `string[]` value required to map with the Java method parameter.
    # + return - The `error?` value returning from the Java mapping.
    function errorParam(JIOException arg0, string[] arg1) returns error? {
        org_ballerinalang_bindgen_MethodsTestResource_errorParam(self.jObj, arg0.jObj, check jarrays:toHandle(arg1, "java.lang.String"));
    }

    # The function that maps to the `genericObjectParam` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + arg0 - The `Set` value required to map with the Java method parameter.
    # + arg1 - The `int` value required to map with the Java method parameter.
    # + arg2 - The `string[]` value required to map with the Java method parameter.
    # + return - The `Set` value returning from the Java mapping.
    function genericObjectParam(Set arg0, int arg1, string[] arg2) returns Set|error {
        handle externalObj = org_ballerinalang_bindgen_MethodsTestResource_genericObjectParam(self.jObj, arg0.jObj, arg1, check jarrays:toHandle(arg2, "java.lang.String"));
        Set newObj = new (externalObj);
        return newObj;
    }

    # The function that maps to the `getClass` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + return - The `Class` value returning from the Java mapping.
    function getClass() returns Class {
        handle externalObj = org_ballerinalang_bindgen_MethodsTestResource_getClass(self.jObj);
        Class newObj = new (externalObj);
        return newObj;
    }

    # The function that maps to the `hashCode` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + return - The `int` value returning from the Java mapping.
    function hashCode() returns int {
        return org_ballerinalang_bindgen_MethodsTestResource_hashCode(self.jObj);
    }

    # The function that maps to the `interfaceParam` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + arg0 - The `Map` value required to map with the Java method parameter.
    # + arg1 - The `Object[]` value required to map with the Java method parameter.
    # + arg2 - The `Object[]` value required to map with the Java method parameter.
    # + return - The `error?` value returning from the Java mapping.
    function interfaceParam(Map arg0, Object[] arg1, Object[] arg2) returns error? {
        org_ballerinalang_bindgen_MethodsTestResource_interfaceParam(self.jObj, arg0.jObj, check jarrays:toHandle(arg1, "java.lang.Object"), check jarrays:toHandle(arg2, "java.lang.Object"));
    }

    # The function that maps to the `notify` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    function notify() {
        org_ballerinalang_bindgen_MethodsTestResource_notify(self.jObj);
    }

    # The function that maps to the `notifyAll` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    function notifyAll() {
        org_ballerinalang_bindgen_MethodsTestResource_notifyAll(self.jObj);
    }

    # The function that maps to the `returnAbstractObject` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + return - The `AbstractSet` value returning from the Java mapping.
    function returnAbstractObject() returns AbstractSet {
        handle externalObj = org_ballerinalang_bindgen_MethodsTestResource_returnAbstractObject(self.jObj);
        AbstractSet newObj = new (externalObj);
        return newObj;
    }

    # The function that maps to the `returnBoolean` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + arg0 - The `boolean` value required to map with the Java method parameter.
    # + return - The `boolean` value returning from the Java mapping.
    function returnBoolean(boolean arg0) returns boolean {
        return org_ballerinalang_bindgen_MethodsTestResource_returnBoolean(self.jObj, arg0);
    }

    # The function that maps to the `returnBoolean` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + arg0 - The `boolean` value required to map with the Java method parameter.
    # + arg1 - The `Object[]` value required to map with the Java method parameter.
    # + return - The `boolean` value returning from the Java mapping.
    function returnBoolean2(boolean arg0, Object[] arg1) returns boolean|error {
        return org_ballerinalang_bindgen_MethodsTestResource_returnBoolean2(self.jObj, arg0, check jarrays:toHandle(arg1, "java.lang.Object"));
    }

    # The function that maps to the `returnBooleanArray` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + return - The `boolean[]` value returning from the Java mapping.
    function returnBooleanArray() returns boolean[]|error {
        handle externalObj = org_ballerinalang_bindgen_MethodsTestResource_returnBooleanArray(self.jObj);
        return <boolean[]>check jarrays:fromHandle(externalObj, "boolean");
    }

    # The function that maps to the `returnBooleanMultiArray` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + return - The `boolean[]` value returning from the Java mapping.
    function returnBooleanMultiArray() returns boolean[]|error {
        handle externalObj = org_ballerinalang_bindgen_MethodsTestResource_returnBooleanMultiArray(self.jObj);
        return <boolean[]>check jarrays:fromHandle(externalObj, "boolean");
    }

    # The function that maps to the `returnByte` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + arg0 - The `byte` value required to map with the Java method parameter.
    # + return - The `byte` value returning from the Java mapping.
    function returnByte(byte arg0) returns byte {
        return org_ballerinalang_bindgen_MethodsTestResource_returnByte(self.jObj, arg0);
    }

    # The function that maps to the `returnByteArray` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + arg0 - The `byte[]` value required to map with the Java method parameter.
    # + return - The `byte[]` value returning from the Java mapping.
    function returnByteArray(byte[] arg0) returns byte[]|error {
        handle externalObj = org_ballerinalang_bindgen_MethodsTestResource_returnByteArray(self.jObj, check jarrays:toHandle(arg0, "byte"));
        return <byte[]>check jarrays:fromHandle(externalObj, "byte");
    }

    # The function that maps to the `returnByteMultiArray` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + arg0 - The `byte[]` value required to map with the Java method parameter.
    # + return - The `byte[]` value returning from the Java mapping.
    function returnByteMultiArray(byte[] arg0) returns byte[]|error {
        handle externalObj = org_ballerinalang_bindgen_MethodsTestResource_returnByteMultiArray(self.jObj, check jarrays:toHandle(arg0, "byte"));
        return <byte[]>check jarrays:fromHandle(externalObj, "byte");
    }

    # The function that maps to the `returnChar` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + arg0 - The `int` value required to map with the Java method parameter.
    # + arg1 - The `int[]` value required to map with the Java method parameter.
    # + return - The `int` value returning from the Java mapping.
    function returnChar(int arg0, int[] arg1) returns int|error {
        return org_ballerinalang_bindgen_MethodsTestResource_returnChar(self.jObj, arg0, check jarrays:toHandle(arg1, "int"));
    }

    # The function that maps to the `returnCharArray` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + arg0 - The `int[]` value required to map with the Java method parameter.
    # + arg1 - The `int[]` value required to map with the Java method parameter.
    # + arg2 - The `int[]` value required to map with the Java method parameter.
    # + return - The `int[]` value returning from the Java mapping.
    function returnCharArray(int[] arg0, int[] arg1, int[] arg2) returns int[]|error {
        handle externalObj = org_ballerinalang_bindgen_MethodsTestResource_returnCharArray(self.jObj, check jarrays:toHandle(arg0, "char"), check jarrays:toHandle(arg1, "int"), check jarrays:toHandle(arg2, "short"));
        return <int[]>check jarrays:fromHandle(externalObj, "char");
    }

    # The function that maps to the `returnCharMultiArray` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + arg0 - The `int[]` value required to map with the Java method parameter.
    # + arg1 - The `int[]` value required to map with the Java method parameter.
    # + arg2 - The `Object[]` value required to map with the Java method parameter.
    # + return - The `int[]` value returning from the Java mapping.
    function returnCharMultiArray(int[] arg0, int[] arg1, Object[] arg2) returns int[]|error {
        handle externalObj = org_ballerinalang_bindgen_MethodsTestResource_returnCharMultiArray(self.jObj, check jarrays:toHandle(arg0, "char"), check jarrays:toHandle(arg1, "int"), check jarrays:toHandle(arg2, "java.lang.Object"));
        return <int[]>check jarrays:fromHandle(externalObj, "char");
    }

    # The function that maps to the `returnDouble` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + arg0 - The `float` value required to map with the Java method parameter.
    # + return - The `float` value returning from the Java mapping.
    function returnDouble(float arg0) returns float {
        return org_ballerinalang_bindgen_MethodsTestResource_returnDouble(self.jObj, arg0);
    }

    # The function that maps to the `returnDouble` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + arg0 - The `float` value required to map with the Java method parameter.
    # + arg1 - The `Object` value required to map with the Java method parameter.
    # + return - The `float` value returning from the Java mapping.
    function returnDouble2(float arg0, Object arg1) returns float {
        return org_ballerinalang_bindgen_MethodsTestResource_returnDouble2(self.jObj, arg0, arg1.jObj);
    }

    # The function that maps to the `returnDoubleArray` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + arg0 - The `float[]` value required to map with the Java method parameter.
    # + arg1 - The `float[]` value required to map with the Java method parameter.
    # + return - The `float[]` value returning from the Java mapping.
    function returnDoubleArray(float[] arg0, float[] arg1) returns float[]|error {
        handle externalObj = org_ballerinalang_bindgen_MethodsTestResource_returnDoubleArray(self.jObj, check jarrays:toHandle(arg0, "double"), check jarrays:toHandle(arg1, "double"));
        return <float[]>check jarrays:fromHandle(externalObj, "double");
    }

    # The function that maps to the `returnDoubleMultiArray` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + arg0 - The `float[]` value required to map with the Java method parameter.
    # + arg1 - The `float[]` value required to map with the Java method parameter.
    # + return - The `float[]` value returning from the Java mapping.
    function returnDoubleMultiArray(float[] arg0, float[] arg1) returns float[]|error {
        handle externalObj = org_ballerinalang_bindgen_MethodsTestResource_returnDoubleMultiArray(self.jObj, check jarrays:toHandle(arg0, "double"), check jarrays:toHandle(arg1, "double"));
        return <float[]>check jarrays:fromHandle(externalObj, "double");
    }

    # The function that maps to the `returnEnum` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + return - The `Level` value returning from the Java mapping.
    function returnEnum() returns Level {
        handle externalObj = org_ballerinalang_bindgen_MethodsTestResource_returnEnum(self.jObj);
        Level newObj = new (externalObj);
        return newObj;
    }

    # The function that maps to the `returnError` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + return - The `JIOException` value returning from the Java mapping.
    function returnError() returns JIOException {
        handle externalObj = org_ballerinalang_bindgen_MethodsTestResource_returnError(self.jObj);
        JIOException newObj = new (externalObj);
        return newObj;
    }

    # The function that maps to the `returnFloat` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + arg0 - The `float` value required to map with the Java method parameter.
    # + return - The `float` value returning from the Java mapping.
    function returnFloat(float arg0) returns float {
        return org_ballerinalang_bindgen_MethodsTestResource_returnFloat(self.jObj, arg0);
    }

    # The function that maps to the `returnFloat` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + arg0 - The `float` value required to map with the Java method parameter.
    # + arg1 - The `string` value required to map with the Java method parameter.
    # + return - The `float` value returning from the Java mapping.
    function returnFloat2(float arg0, string arg1) returns float {
        return org_ballerinalang_bindgen_MethodsTestResource_returnFloat2(self.jObj, arg0, java:fromString(arg1));
    }

    # The function that maps to the `returnFloatArray` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + arg0 - The `float[]` value required to map with the Java method parameter.
    # + arg1 - The `string` value required to map with the Java method parameter.
    # + return - The `float[]` value returning from the Java mapping.
    function returnFloatArray(float[] arg0, string arg1) returns float[]|error {
        handle externalObj = org_ballerinalang_bindgen_MethodsTestResource_returnFloatArray(self.jObj, check jarrays:toHandle(arg0, "float"), java:fromString(arg1));
        return <float[]>check jarrays:fromHandle(externalObj, "float");
    }

    # The function that maps to the `returnGenericObject` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + return - The `Set` value returning from the Java mapping.
    function returnGenericObject() returns Set {
        handle externalObj = org_ballerinalang_bindgen_MethodsTestResource_returnGenericObject(self.jObj);
        Set newObj = new (externalObj);
        return newObj;
    }

    # The function that maps to the `returnInt` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + return - The `int` value returning from the Java mapping.
    function returnInt() returns int {
        return org_ballerinalang_bindgen_MethodsTestResource_returnInt(self.jObj);
    }

    # The function that maps to the `returnInt` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + arg0 - The `int` value required to map with the Java method parameter.
    # + return - The `int` value returning from the Java mapping.
    function returnInt2(int arg0) returns int {
        return org_ballerinalang_bindgen_MethodsTestResource_returnInt2(self.jObj, arg0);
    }

    # The function that maps to the `returnInt` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + arg0 - The `int` value required to map with the Java method parameter.
    # + arg1 - The `int` value required to map with the Java method parameter.
    # + return - The `int` value returning from the Java mapping.
    function returnInt3(int arg0, int arg1) returns int {
        return org_ballerinalang_bindgen_MethodsTestResource_returnInt3(self.jObj, arg0, arg1);
    }

    # The function that maps to the `returnIntArray` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + arg0 - The `int[]` value required to map with the Java method parameter.
    # + arg1 - The `boolean` value required to map with the Java method parameter.
    # + arg2 - The `boolean[]` value required to map with the Java method parameter.
    # + return - The `int[]` value returning from the Java mapping.
    function returnIntArray(int[] arg0, boolean arg1, boolean[] arg2) returns int[]|error {
        handle externalObj = org_ballerinalang_bindgen_MethodsTestResource_returnIntArray(self.jObj, check jarrays:toHandle(arg0, "int"), arg1, check jarrays:toHandle(arg2, "boolean"));
        return <int[]>check jarrays:fromHandle(externalObj, "int");
    }

    # The function that maps to the `returnIntMultiArray` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + arg0 - The `int[]` value required to map with the Java method parameter.
    # + arg1 - The `boolean` value required to map with the Java method parameter.
    # + return - The `int[]` value returning from the Java mapping.
    function returnIntMultiArray(int[] arg0, boolean arg1) returns int[]|error {
        handle externalObj = org_ballerinalang_bindgen_MethodsTestResource_returnIntMultiArray(self.jObj, check jarrays:toHandle(arg0, "int"), arg1);
        return <int[]>check jarrays:fromHandle(externalObj, "int");
    }

    # The function that maps to the `returnInterface` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + return - The `Map` value returning from the Java mapping.
    function returnInterface() returns Map {
        handle externalObj = org_ballerinalang_bindgen_MethodsTestResource_returnInterface(self.jObj);
        Map newObj = new (externalObj);
        return newObj;
    }

    # The function that maps to the `returnLongArray` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + arg0 - The `int[]` value required to map with the Java method parameter.
    # + arg1 - The `Object` value required to map with the Java method parameter.
    # + return - The `int[]` value returning from the Java mapping.
    function returnLongArray(int[] arg0, Object arg1) returns int[]|error {
        handle externalObj = org_ballerinalang_bindgen_MethodsTestResource_returnLongArray(self.jObj, check jarrays:toHandle(arg0, "long"), arg1.jObj);
        return <int[]>check jarrays:fromHandle(externalObj, "long");
    }

    # The function that maps to the `returnObject` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + return - The `File` value returning from the Java mapping.
    function returnObject() returns File {
        handle externalObj = org_ballerinalang_bindgen_MethodsTestResource_returnObject(self.jObj);
        File newObj = new (externalObj);
        return newObj;
    }

    # The function that maps to the `returnObjectError1` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + return - The `File[]` value returning from the Java mapping.
    function returnObjectError1() returns File[]|error {
        handle externalObj = org_ballerinalang_bindgen_MethodsTestResource_returnObjectError1(self.jObj);
        File[] newObj = [];
        handle[] anyObj = <handle[]>check jarrays:fromHandle(externalObj, "handle");
        int count = anyObj.length();
        foreach int i in 0 ... count - 1 {
            File element = new (anyObj[i]);
            newObj[i] = element;
        }
        return newObj;
    }

    # The function that maps to the `returnObjectError2` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + arg0 - The `int[]` value required to map with the Java method parameter.
    # + return - The `File` value returning from the Java mapping.
    function returnObjectError2(int[] arg0) returns File|error {
        handle externalObj = org_ballerinalang_bindgen_MethodsTestResource_returnObjectError2(self.jObj, check jarrays:toHandle(arg0, "int"));
        File newObj = new (externalObj);
        return newObj;
    }

    # The function that maps to the `returnObjectThrowable` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + return - The `File` or the `FileNotFoundException` value returning from the Java mapping.
    function returnObjectThrowable() returns File|FileNotFoundException {
        handle|error externalObj = org_ballerinalang_bindgen_MethodsTestResource_returnObjectThrowable(self.jObj);
        if (externalObj is error) {
            FileNotFoundException e = error FileNotFoundException(FILENOTFOUNDEXCEPTION, externalObj, message = externalObj.message());
            return e;
        } else {
            File newObj = new (externalObj);
            return newObj;
        }
    }

    # The function that maps to the `returnObjectThrowableError` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + return - The `File[]` or the `FileNotFoundException` value returning from the Java mapping.
    function returnObjectThrowableError() returns File[]|FileNotFoundException|error {
        handle|error externalObj = org_ballerinalang_bindgen_MethodsTestResource_returnObjectThrowableError(self.jObj);
        if (externalObj is error) {
            FileNotFoundException e = error FileNotFoundException(FILENOTFOUNDEXCEPTION, externalObj, message = externalObj.message());
            return e;
        } else {
            File[] newObj = [];
            handle[] anyObj = <handle[]>check jarrays:fromHandle(externalObj, "handle");
            int count = anyObj.length();
            foreach int i in 0 ... count - 1 {
                File element = new (anyObj[i]);
                newObj[i] = element;
            }
        }
        return newObj;
    }

    # The function that maps to the `returnObjectThrowableError` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + arg0 - The `int[]` value required to map with the Java method parameter.
    # + return - The `File` or the `FileNotFoundException` value returning from the Java mapping.
    function returnObjectThrowableError2(int[] arg0) returns File|FileNotFoundException|error {
        handle|error externalObj = org_ballerinalang_bindgen_MethodsTestResource_returnObjectThrowableError2(self.jObj, check jarrays:toHandle(arg0, "int"));
        if (externalObj is error) {
            FileNotFoundException e = error FileNotFoundException(FILENOTFOUNDEXCEPTION, externalObj, message = externalObj.message());
            return e;
        } else {
            File newObj = new (externalObj);
            return newObj;
        }
    }

    # The function that maps to the `returnOptionalError` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + return - The `Exception` value returning from the Java mapping.
    function returnOptionalError() returns Exception? {
        error|() externalObj = org_ballerinalang_bindgen_MethodsTestResource_returnOptionalError(self.jObj);
        if (externalObj is error) {
            Exception e = error Exception(EXCEPTION, externalObj, message = externalObj.message());
            return e;
        }
    }

    # The function that maps to the `returnShortArray` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + arg0 - The `int` value required to map with the Java method parameter.
    # + arg1 - The `int` value required to map with the Java method parameter.
    # + return - The `int[]` value returning from the Java mapping.
    function returnShortArray(int arg0, int arg1) returns int[]|error {
        handle externalObj = org_ballerinalang_bindgen_MethodsTestResource_returnShortArray(self.jObj, arg0, arg1);
        return <int[]>check jarrays:fromHandle(externalObj, "short");
    }

    # The function that maps to the `returnShortMultiArray` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + arg0 - The `int` value required to map with the Java method parameter.
    # + arg1 - The `int` value required to map with the Java method parameter.
    # + arg2 - The `int[]` value required to map with the Java method parameter.
    # + return - The `int[]` value returning from the Java mapping.
    function returnShortMultiArray(int arg0, int arg1, int[] arg2) returns int[]|error {
        handle externalObj = org_ballerinalang_bindgen_MethodsTestResource_returnShortMultiArray(self.jObj, arg0, arg1, check jarrays:toHandle(arg2, "short"));
        return <int[]>check jarrays:fromHandle(externalObj, "short");
    }

    # The function that maps to the `returnString` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + arg0 - The `string` value required to map with the Java method parameter.
    # + return - The `string` value returning from the Java mapping.
    function returnString(string arg0) returns string? {
        return java:toString(org_ballerinalang_bindgen_MethodsTestResource_returnString(self.jObj, java:fromString(arg0)));
    }

    # The function that maps to the `returnStringArray` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + return - The `string[]` value returning from the Java mapping.
    function returnStringArray() returns string[]?|error {
        handle externalObj = org_ballerinalang_bindgen_MethodsTestResource_returnStringArray(self.jObj);
        if java:isNull(externalObj) {
            return null;
        }
        return <string[]>check jarrays:fromHandle(externalObj, "string");
    }

    # The function that maps to the `returnStringArray1` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + arg0 - The `string[]` value required to map with the Java method parameter.
    # + arg1 - The `StringBuffer` value required to map with the Java method parameter.
    # + arg2 - The `int` value required to map with the Java method parameter.
    # + return - The `string[]` value returning from the Java mapping.
    function returnStringArray1(string[] arg0, StringBuffer arg1, int arg2) returns string[]?|error {
        handle externalObj = org_ballerinalang_bindgen_MethodsTestResource_returnStringArray1(self.jObj, check jarrays:toHandle(arg0, "java.lang.String"), arg1.jObj, arg2);
        if java:isNull(externalObj) {
            return null;
        }
        return <string[]>check jarrays:fromHandle(externalObj, "string");
    }

    # The function that maps to the `returnStringArray2` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + arg0 - The `string[]` value required to map with the Java method parameter.
    # + arg1 - The `StringBuffer` value required to map with the Java method parameter.
    # + arg2 - The `int` value required to map with the Java method parameter.
    # + return - The `string[]` or the `InterruptedException` value returning from the Java mapping.
    function returnStringArray2(string[] arg0, StringBuffer arg1, int arg2) returns string[]?|InterruptedException|error {
        handle|error externalObj = org_ballerinalang_bindgen_MethodsTestResource_returnStringArray2(self.jObj, check jarrays:toHandle(arg0, "java.lang.String"), arg1.jObj, arg2);
        if java:isNull(externalObj) {
            return null;
        }
        if (externalObj is error) {
            InterruptedException e = error InterruptedException(INTERRUPTEDEXCEPTION, externalObj, message = externalObj.message());
            return e;
        } else {
            return <string[]>check jarrays:fromHandle(externalObj, "string");
        }
    }

    # The function that maps to the `returnVoid` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    function returnVoid() {
        org_ballerinalang_bindgen_MethodsTestResource_returnVoid(self.jObj);
    }

    # The function that maps to the `testMethod` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + arg0 - The `int` value required to map with the Java method parameter.
    # + return - The `int` value returning from the Java mapping.
    function testMethod(int arg0) returns int {
        return org_ballerinalang_bindgen_MethodsTestResource_testMethod(self.jObj, arg0);
    }

    # The function that maps to the `wait` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + return - The `InterruptedException` value returning from the Java mapping.
    function 'wait() returns InterruptedException? {
        error|() externalObj = org_ballerinalang_bindgen_MethodsTestResource_wait(self.jObj);
        if (externalObj is error) {
            InterruptedException e = error InterruptedException(INTERRUPTEDEXCEPTION, externalObj, message = externalObj.message());
            return e;
        }
    }

    # The function that maps to the `wait` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + arg0 - The `int` value required to map with the Java method parameter.
    # + return - The `InterruptedException` value returning from the Java mapping.
    function wait2(int arg0) returns InterruptedException? {
        error|() externalObj = org_ballerinalang_bindgen_MethodsTestResource_wait2(self.jObj, arg0);
        if (externalObj is error) {
            InterruptedException e = error InterruptedException(INTERRUPTEDEXCEPTION, externalObj, message = externalObj.message());
            return e;
        }
    }

    # The function that maps to the `wait` method of `org.ballerinalang.bindgen.MethodsTestResource`.
    #
    # + arg0 - The `int` value required to map with the Java method parameter.
    # + arg1 - The `int` value required to map with the Java method parameter.
    # + return - The `InterruptedException` value returning from the Java mapping.
    function wait3(int arg0, int arg1) returns InterruptedException? {
        error|() externalObj = org_ballerinalang_bindgen_MethodsTestResource_wait3(self.jObj, arg0, arg1);
        if (externalObj is error) {
            InterruptedException e = error InterruptedException(INTERRUPTEDEXCEPTION, externalObj, message = externalObj.message());
            return e;
        }
    }

}

# The constructor function to generate an object of `org.ballerinalang.bindgen.MethodsTestResource`.
#
# + return - The new `MethodsTestResource` class generated.
function newMethodsTestResource1() returns MethodsTestResource {
    handle externalObj = org_ballerinalang_bindgen_MethodsTestResource_newMethodsTestResource1();
    MethodsTestResource newObj = new (externalObj);
    return newObj;
}

# The function that maps to the `abstractObjectStaticParam` method of `org.ballerinalang.bindgen.MethodsTestResource`.
#
# + arg0 - The `AbstractSet` value required to map with the Java method parameter.
# + arg1 - The `int` value required to map with the Java method parameter.
# + arg2 - The `Object` value required to map with the Java method parameter.
function MethodsTestResource_abstractObjectStaticParam(AbstractSet arg0, int arg1, Object arg2) {
    org_ballerinalang_bindgen_MethodsTestResource_abstractObjectStaticParam(arg0.jObj, arg1, arg2.jObj);
}

# The function that maps to the `enumStaticParam` method of `org.ballerinalang.bindgen.MethodsTestResource`.
#
# + arg0 - The `Level` value required to map with the Java method parameter.
# + arg1 - The `Object[]` value required to map with the Java method parameter.
# + return - The `Level` value returning from the Java mapping.
function MethodsTestResource_enumStaticParam(Level arg0, Object[] arg1) returns Level|error {
    handle externalObj = org_ballerinalang_bindgen_MethodsTestResource_enumStaticParam(arg0.jObj, check jarrays:toHandle(arg1, "java.lang.Object"));
    Level newObj = new (externalObj);
    return newObj;
}

# The function that maps to the `genericObjectStaticParam` method of `org.ballerinalang.bindgen.MethodsTestResource`.
#
# + arg0 - The `Set` value required to map with the Java method parameter.
# + return - The `Set` value returning from the Java mapping.
function MethodsTestResource_genericObjectStaticParam(Set arg0) returns Set {
    handle externalObj = org_ballerinalang_bindgen_MethodsTestResource_genericObjectStaticParam(arg0.jObj);
    Set newObj = new (externalObj);
    return newObj;
}

# The function that maps to the `interfaceStaticParam` method of `org.ballerinalang.bindgen.MethodsTestResource`.
#
# + arg0 - The `Map` value required to map with the Java method parameter.
# + arg1 - The `boolean[]` value required to map with the Java method parameter.
# + return - The `error?` value returning from the Java mapping.
function MethodsTestResource_interfaceStaticParam(Map arg0, boolean[] arg1) returns error? {
    org_ballerinalang_bindgen_MethodsTestResource_interfaceStaticParam(arg0.jObj, check jarrays:toHandle(arg1, "boolean"));
}

# The function that maps to the `join` method of `org.ballerinalang.bindgen.MethodsTestResource`.
function MethodsTestResource_join() {
    org_ballerinalang_bindgen_MethodsTestResource_join();
}

# The function that maps to the `returnStaticAbstractObject` method of `org.ballerinalang.bindgen.MethodsTestResource`.
#
# + return - The `AbstractSet` value returning from the Java mapping.
function MethodsTestResource_returnStaticAbstractObject() returns AbstractSet {
    handle externalObj = org_ballerinalang_bindgen_MethodsTestResource_returnStaticAbstractObject();
    AbstractSet newObj = new (externalObj);
    return newObj;
}

# The function that maps to the `returnStaticEnum` method of `org.ballerinalang.bindgen.MethodsTestResource`.
#
# + return - The `Level` value returning from the Java mapping.
function MethodsTestResource_returnStaticEnum() returns Level {
    handle externalObj = org_ballerinalang_bindgen_MethodsTestResource_returnStaticEnum();
    Level newObj = new (externalObj);
    return newObj;
}

# The function that maps to the `returnStaticError` method of `org.ballerinalang.bindgen.MethodsTestResource`.
#
# + return - The `JIOException` value returning from the Java mapping.
function MethodsTestResource_returnStaticError() returns JIOException {
    handle externalObj = org_ballerinalang_bindgen_MethodsTestResource_returnStaticError();
    JIOException newObj = new (externalObj);
    return newObj;
}

# The function that maps to the `returnStaticGenericObject` method of `org.ballerinalang.bindgen.MethodsTestResource`.
#
# + return - The `Set` value returning from the Java mapping.
function MethodsTestResource_returnStaticGenericObject() returns Set {
    handle externalObj = org_ballerinalang_bindgen_MethodsTestResource_returnStaticGenericObject();
    Set newObj = new (externalObj);
    return newObj;
}

# The function that maps to the `returnStaticInterface` method of `org.ballerinalang.bindgen.MethodsTestResource`.
#
# + return - The `Map` value returning from the Java mapping.
function MethodsTestResource_returnStaticInterface() returns Map {
    handle externalObj = org_ballerinalang_bindgen_MethodsTestResource_returnStaticInterface();
    Map newObj = new (externalObj);
    return newObj;
}

# The function that maps to the `returnStaticObject` method of `org.ballerinalang.bindgen.MethodsTestResource`.
#
# + return - The `File` value returning from the Java mapping.
function MethodsTestResource_returnStaticObject() returns File {
    handle externalObj = org_ballerinalang_bindgen_MethodsTestResource_returnStaticObject();
    File newObj = new (externalObj);
    return newObj;
}

# The function that maps to the `returnStaticObjectThrowableError1` method of `org.ballerinalang.bindgen.MethodsTestResource`.
#
# + return - The `File[]` or the `FileNotFoundException` value returning from the Java mapping.
function MethodsTestResource_returnStaticObjectThrowableError1() returns File[]|FileNotFoundException|error {
    handle|error externalObj = org_ballerinalang_bindgen_MethodsTestResource_returnStaticObjectThrowableError1();
    if (externalObj is error) {
        FileNotFoundException e = error FileNotFoundException(FILENOTFOUNDEXCEPTION, externalObj, message = externalObj.message());
        return e;
    } else {
        File[] newObj = [];
        handle[] anyObj = <handle[]>check jarrays:fromHandle(externalObj, "handle");
        int count = anyObj.length();
        foreach int i in 0 ... count - 1 {
            File element = new (anyObj[i]);
            newObj[i] = element;
        }
    }
    return newObj;
}

# The function that maps to the `returnStaticOptionalError` method of `org.ballerinalang.bindgen.MethodsTestResource`.
#
# + return - The `Exception` value returning from the Java mapping.
function MethodsTestResource_returnStaticOptionalError() returns Exception? {
    error|() externalObj = org_ballerinalang_bindgen_MethodsTestResource_returnStaticOptionalError();
    if (externalObj is error) {
        Exception e = error Exception(EXCEPTION, externalObj, message = externalObj.message());
        return e;
    }
}

# The function that maps to the `returnStaticVoid` method of `org.ballerinalang.bindgen.MethodsTestResource`.
function MethodsTestResource_returnStaticVoid() {
    org_ballerinalang_bindgen_MethodsTestResource_returnStaticVoid();
}

# The function that retrieves the value of the public field `TEST_FIELD`.
#
# + return - The `int` value of the field.
function MethodsTestResource_getTEST_FIELD() returns int {
    return org_ballerinalang_bindgen_MethodsTestResource_getTEST_FIELD();
}

function org_ballerinalang_bindgen_MethodsTestResource_abstractObjectParam(handle receiver, handle arg0, handle arg1, handle arg2) = @java:Method {
    name: "abstractObjectParam",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: ["java.util.AbstractSet", "java.lang.Object", "java.lang.String"]
} external;

function org_ballerinalang_bindgen_MethodsTestResource_abstractObjectStaticParam(handle arg0, int arg1, handle arg2) = @java:Method {
    name: "abstractObjectStaticParam",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: ["java.util.AbstractSet", "int", "java.lang.Object"]
} external;

function org_ballerinalang_bindgen_MethodsTestResource_enumParam(handle receiver, handle arg0) returns handle = @java:Method {
    name: "enumParam",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: ["java.lang.System$Logger$Level"]
} external;

function org_ballerinalang_bindgen_MethodsTestResource_enumStaticParam(handle arg0, handle arg1) returns handle = @java:Method {
    name: "enumStaticParam",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: ["java.lang.System$Logger$Level", "[Ljava.lang.Object;"]
} external;

function org_ballerinalang_bindgen_MethodsTestResource_equals(handle receiver, handle arg0) returns boolean = @java:Method {
    name: "equals",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: ["java.lang.Object"]
} external;

function org_ballerinalang_bindgen_MethodsTestResource_errorParam(handle receiver, handle arg0, handle arg1) = @java:Method {
    name: "errorParam",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: ["java.io.IOException", "[Ljava.lang.String;"]
} external;

function org_ballerinalang_bindgen_MethodsTestResource_genericObjectParam(handle receiver, handle arg0, int arg1, handle arg2) returns handle = @java:Method {
    name: "genericObjectParam",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: ["java.util.Set", "int", "[Ljava.lang.String;"]
} external;

function org_ballerinalang_bindgen_MethodsTestResource_genericObjectStaticParam(handle arg0) returns handle = @java:Method {
    name: "genericObjectStaticParam",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: ["java.util.Set"]
} external;

function org_ballerinalang_bindgen_MethodsTestResource_getClass(handle receiver) returns handle = @java:Method {
    name: "getClass",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_MethodsTestResource_hashCode(handle receiver) returns int = @java:Method {
    name: "hashCode",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_MethodsTestResource_interfaceParam(handle receiver, handle arg0, handle arg1, handle arg2) = @java:Method {
    name: "interfaceParam",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: ["java.util.Map", "[Ljava.lang.Object;", "[Ljava.lang.Object;"]
} external;

function org_ballerinalang_bindgen_MethodsTestResource_interfaceStaticParam(handle arg0, handle arg1) = @java:Method {
    name: "interfaceStaticParam",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: ["java.util.Map", "[Z"]
} external;

function org_ballerinalang_bindgen_MethodsTestResource_join() = @java:Method {
    name: "join",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_MethodsTestResource_notify(handle receiver) = @java:Method {
    name: "notify",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_MethodsTestResource_notifyAll(handle receiver) = @java:Method {
    name: "notifyAll",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnAbstractObject(handle receiver) returns handle = @java:Method {
    name: "returnAbstractObject",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnBoolean(handle receiver, boolean arg0) returns boolean = @java:Method {
    name: "returnBoolean",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: ["boolean"]
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnBoolean2(handle receiver, boolean arg0, handle arg1) returns boolean = @java:Method {
    name: "returnBoolean",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: ["boolean", "[Ljava.lang.Object;"]
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnBooleanArray(handle receiver) returns handle = @java:Method {
    name: "returnBooleanArray",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnBooleanMultiArray(handle receiver) returns handle = @java:Method {
    name: "returnBooleanMultiArray",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnByte(handle receiver, byte arg0) returns byte = @java:Method {
    name: "returnByte",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: ["byte"]
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnByteArray(handle receiver, handle arg0) returns handle = @java:Method {
    name: "returnByteArray",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: ["[B"]
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnByteMultiArray(handle receiver, handle arg0) returns handle = @java:Method {
    name: "returnByteMultiArray",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: ["[B"]
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnChar(handle receiver, int arg0, handle arg1) returns int = @java:Method {
    name: "returnChar",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: ["char", "[I"]
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnCharArray(handle receiver, handle arg0, handle arg1, handle arg2) returns handle = @java:Method {
    name: "returnCharArray",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: ["[C", "[I", "[S"]
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnCharMultiArray(handle receiver, handle arg0, handle arg1, handle arg2) returns handle = @java:Method {
    name: "returnCharMultiArray",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: ["[C", "[I", "[Ljava.lang.Object;"]
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnDouble(handle receiver, float arg0) returns float = @java:Method {
    name: "returnDouble",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: ["double"]
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnDouble2(handle receiver, float arg0, handle arg1) returns float = @java:Method {
    name: "returnDouble",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: ["double", "java.lang.Object"]
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnDoubleArray(handle receiver, handle arg0, handle arg1) returns handle = @java:Method {
    name: "returnDoubleArray",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: ["[D", "[D"]
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnDoubleMultiArray(handle receiver, handle arg0, handle arg1) returns handle = @java:Method {
    name: "returnDoubleMultiArray",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: ["[D", "[D"]
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnEnum(handle receiver) returns handle = @java:Method {
    name: "returnEnum",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnError(handle receiver) returns handle = @java:Method {
    name: "returnError",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnFloat(handle receiver, float arg0) returns float = @java:Method {
    name: "returnFloat",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: ["float"]
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnFloat2(handle receiver, float arg0, handle arg1) returns float = @java:Method {
    name: "returnFloat",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: ["float", "java.lang.String"]
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnFloatArray(handle receiver, handle arg0, handle arg1) returns handle = @java:Method {
    name: "returnFloatArray",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: ["[F", "java.lang.String"]
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnGenericObject(handle receiver) returns handle = @java:Method {
    name: "returnGenericObject",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnInt(handle receiver) returns int = @java:Method {
    name: "returnInt",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnInt2(handle receiver, int arg0) returns int = @java:Method {
    name: "returnInt",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: ["long"]
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnInt3(handle receiver, int arg0, int arg1) returns int = @java:Method {
    name: "returnInt",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: ["short", "int"]
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnIntArray(handle receiver, handle arg0, boolean arg1, handle arg2) returns handle = @java:Method {
    name: "returnIntArray",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: ["[I", "boolean", "[Z"]
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnIntMultiArray(handle receiver, handle arg0, boolean arg1) returns handle = @java:Method {
    name: "returnIntMultiArray",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: ["[I", "boolean"]
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnInterface(handle receiver) returns handle = @java:Method {
    name: "returnInterface",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnLongArray(handle receiver, handle arg0, handle arg1) returns handle = @java:Method {
    name: "returnLongArray",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: ["[J", "java.lang.Object"]
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnObject(handle receiver) returns handle = @java:Method {
    name: "returnObject",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnObjectError1(handle receiver) returns handle = @java:Method {
    name: "returnObjectError1",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnObjectError2(handle receiver, handle arg0) returns handle = @java:Method {
    name: "returnObjectError2",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: ["[I"]
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnObjectThrowable(handle receiver) returns handle|error = @java:Method {
    name: "returnObjectThrowable",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnObjectThrowableError(handle receiver) returns handle|error = @java:Method {
    name: "returnObjectThrowableError",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnObjectThrowableError2(handle receiver, handle arg0) returns handle|error = @java:Method {
    name: "returnObjectThrowableError",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: ["[I"]
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnOptionalError(handle receiver) returns error? = @java:Method {
    name: "returnOptionalError",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnShortArray(handle receiver, int arg0, int arg1) returns handle = @java:Method {
    name: "returnShortArray",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: ["short", "int"]
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnShortMultiArray(handle receiver, int arg0, int arg1, handle arg2) returns handle = @java:Method {
    name: "returnShortMultiArray",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: ["short", "int", "[S"]
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnStaticAbstractObject() returns handle = @java:Method {
    name: "returnStaticAbstractObject",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnStaticEnum() returns handle = @java:Method {
    name: "returnStaticEnum",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnStaticError() returns handle = @java:Method {
    name: "returnStaticError",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnStaticGenericObject() returns handle = @java:Method {
    name: "returnStaticGenericObject",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnStaticInterface() returns handle = @java:Method {
    name: "returnStaticInterface",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnStaticObject() returns handle = @java:Method {
    name: "returnStaticObject",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnStaticObjectThrowableError1() returns handle|error = @java:Method {
    name: "returnStaticObjectThrowableError1",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnStaticOptionalError() returns error? = @java:Method {
    name: "returnStaticOptionalError",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnStaticVoid() = @java:Method {
    name: "returnStaticVoid",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnString(handle receiver, handle arg0) returns handle = @java:Method {
    name: "returnString",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: ["java.lang.String"]
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnStringArray(handle receiver) returns handle = @java:Method {
    name: "returnStringArray",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnStringArray1(handle receiver, handle arg0, handle arg1, int arg2) returns handle = @java:Method {
    name: "returnStringArray1",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: ["[Ljava.lang.String;", "java.lang.StringBuffer", "int"]
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnStringArray2(handle receiver, handle arg0, handle arg1, int arg2) returns handle|error = @java:Method {
    name: "returnStringArray2",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: ["[Ljava.lang.String;", "java.lang.StringBuffer", "int"]
} external;

function org_ballerinalang_bindgen_MethodsTestResource_returnVoid(handle receiver) = @java:Method {
    name: "returnVoid",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_MethodsTestResource_testMethod(handle receiver, int arg0) returns int = @java:Method {
    name: "testMethod",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: ["int"]
} external;

function org_ballerinalang_bindgen_MethodsTestResource_unsupportedReturnType(handle receiver) returns handle = @java:Method {
    name: "unsupportedReturnType",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_MethodsTestResource_wait(handle receiver) returns error? = @java:Method {
    name: "wait",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: []
} external;

function org_ballerinalang_bindgen_MethodsTestResource_wait2(handle receiver, int arg0) returns error? = @java:Method {
    name: "wait",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: ["long"]
} external;

function org_ballerinalang_bindgen_MethodsTestResource_wait3(handle receiver, int arg0, int arg1) returns error? = @java:Method {
    name: "wait",
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: ["long", "int"]
} external;

function org_ballerinalang_bindgen_MethodsTestResource_getTEST_FIELD() returns int = @java:FieldGet {
    name: "TEST_FIELD",
    'class: "org.ballerinalang.bindgen.MethodsTestResource"
} external;

function org_ballerinalang_bindgen_MethodsTestResource_newMethodsTestResource1() returns handle = @java:Constructor {
    'class: "org.ballerinalang.bindgen.MethodsTestResource",
    paramTypes: []
} external;
