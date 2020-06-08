import ballerina/java;
import ballerina/java.arrays as jarrays;

public function testToHandleWithString() returns handle|error {
    string[] array = ["Five", "Two", "Nine", "Three", "Seven"];
    return jarrays:toHandle(array, "java.lang.String");
}

public function testToHandleWithByte() returns handle|error {
    byte[] array = [80, 65, 78, 32, 65];
    return jarrays:toHandle(array, "byte");
}

public function testToHandleWithByteChar() returns handle|error {
    byte[] array = [80, 65, 78, 32, 65];
    return jarrays:toHandle(array, "char");
}

public function testToHandleWithByteShort() returns handle|error {
    byte[] array = [80, 65, 78, 32, 65];
    return jarrays:toHandle(array, "short");
}

public function testToHandleWithByteLong() returns handle|error {
    byte[] array = [80, 65, 78, 32, 65];
    return jarrays:toHandle(array, "long");
}

public function testToHandleWithByteInt() returns handle|error {
    byte[] array = [80, 65, 78, 32, 65];
    return jarrays:toHandle(array, "int");
}

public function testToHandleWithByteFloat() returns handle|error {
    byte[] array = [80, 65, 78, 32, 65];
    return jarrays:toHandle(array, "float");
}

public function testToHandleWithByteDouble() returns handle|error {
    byte[] array = [80, 65, 78, 32, 65];
    return jarrays:toHandle(array, "double");
}

public function testToHandleWithInt() returns handle|error {
    int[] array = [8, 25, 79, 34, 2];
    return jarrays:toHandle(array, "int");
}

public function testToHandleWithIntByte() returns handle|error {
    int[] array = [8, 25, 79, 34, 2];
    return jarrays:toHandle(array, "byte");
}

public function testToHandleWithIntChar() returns handle|error {
    int[] array = [8, 25, 79, 34, 2];
    return jarrays:toHandle(array, "char");
}

public function testToHandleWithIntLong() returns handle|error {
    int[] array = [8, 25, 79, 34, 2];
    return jarrays:toHandle(array, "long");
}

public function testToHandleWithIntShort() returns handle|error {
    int[] array = [8, 25, 79, 34, 2];
    return jarrays:toHandle(array, "short");
}

public function testToHandleWithFloat() returns handle|error {
    float[] array = [8.7, 25.2, 79.1, 34.6, 2];
    return jarrays:toHandle(array, "float");
}

public function testToHandleWithFloatChar() returns handle|error {
    float[] array = [8.7, 25.2, 79.1, 34.6, 2];
    return jarrays:toHandle(array, "char");
}

public function testToHandleWithFloatByte() returns handle|error {
    float[] array = [8.7, 25.2, 79.1, 34.6, 2];
    return jarrays:toHandle(array, "byte");
}

public function testToHandleWithFloatShort() returns handle|error {
    float[] array = [8.7, 25.2, 79.1, 34.6, 2];
    return jarrays:toHandle(array, "short");
}

public function testToHandleWithFloatLong() returns handle|error {
    float[] array = [8.7, 25.2, 79.1, 34.6, 2];
    return jarrays:toHandle(array, "long");
}

public function testToHandleWithFloatDouble() returns handle|error {
    float[] array = [8.7, 25.2, 79.1, 34.6, 2];
    return jarrays:toHandle(array, "double");
}

public function testToHandleWithFloatInt() returns handle|error {
    float[] array = [8.7, 25.2, 79.1, 34.6, 2];
    return jarrays:toHandle(array, "int");
}

public function testToHandleWithBoolean() returns handle|error {
    boolean[] array = [true, true, false, true];
    return jarrays:toHandle(array, "boolean");
}

public function testToHandleWithJObject() returns handle|error {
    String str1 = newString("Welcome");
    String str2 = newString("To");
    String str3 = newString("Ballerina");
    String str4 = newString("Language");
    String[] array = [str1, str2, str3, str4];
    return jarrays:toHandle(array, "java.lang.String");
}

public function testFromHandleWithString(handle arrayHandle) returns boolean {
    string[] arrayIntended = ["Five", "Two", "Nine", "Three", "Seven"];
    any[]|error value = jarrays:fromHandle(arrayHandle, "string");
    if (value is error) {
        return false;
    }
    string[] array = <string[]>value;
    if (arrayIntended == array) {
        return true;
    } else {
        return false;
    }
}

public function testFromHandleWithBoolean(handle arrayHandle) returns boolean {
    boolean[] arrayIntended = [true, true, false, true];
    any[]|error value = jarrays:fromHandle(arrayHandle, "boolean");
    if (value is error) {
        return false;
    }
    boolean[] array = <boolean[]>value;
    if (arrayIntended == array) {
        return true;
    } else {
        return false;
    }
}

public function testFromHandleWithFloat(handle arrayHandle) returns boolean {
    float[] arrayIntended = [8.699999809265137, 25.200000762939453, 79.0999984741211, 34.599998474121094, 2.0];
    any[]|error value = jarrays:fromHandle(arrayHandle, "float");
    if (value is error) {
        return false;
    }
    float[] array = <float[]>value;
    if (arrayIntended == array) {
        return true;
    } else {
        return false;
    }
}

public function testFromHandleWithDouble(handle arrayHandle) returns boolean {
    float[] arrayIntended = [8.7, 25.2, 79.1, 34.6, 2.0];
    any[]|error value = jarrays:fromHandle(arrayHandle, "double");
    if (value is error) {
        return false;
    }
    float[] array = <float[]>value;
    if (arrayIntended == array) {
        return true;
    } else {
        return false;
    }
}

public function testFromHandleWithInt(handle arrayHandle) returns boolean {
    int[] arrayIntended = [34, 76, 12, 90, 45];
    any[]|error value = jarrays:fromHandle(arrayHandle, "int");
    if (value is error) {
        return false;
    }
    int[] array = <int[]>value;
    if (arrayIntended == array) {
        return true;
    } else {
        return false;
    }
}

public function testFromHandleWithIntFloat(handle arrayHandle) returns boolean {
    float[] arrayIntended = [34, 76, 12, 90, 45];
    any[]|error value = jarrays:fromHandle(arrayHandle, "int", "float");
    if (value is error) {
        return false;
    }
    float[] array = <float[]>value;
    if (arrayIntended == array) {
        return true;
    } else {
        return false;
    }
}

public function testFromHandleWithByte(handle arrayHandle) returns boolean {
    byte[] arrayIntended = [80, 65, 78, 32, 65];
    any[]|error value = jarrays:fromHandle(arrayHandle, "byte");
    if (value is error) {
        return false;
    }
    byte[] array = <byte[]>value;
    if (arrayIntended == array) {
        return true;
    } else {
        return false;
    }
}

public function testFromHandleWithByteFloat(handle arrayHandle) returns boolean {
    float[] arrayIntended = [80.0, 65.0, 78, 32, 65];
    any[]|error value = jarrays:fromHandle(arrayHandle, "byte", "float");
    if (value is error) {
        return false;
    }
    float[] array = <float[]>value;
    if (arrayIntended == array) {
        return true;
    } else {
        return false;
    }
}

public function testFromHandleWithByteInt(handle arrayHandle) returns boolean {
    int[] arrayIntended = [80, 65, 78, 32, 65];
    any[]|error value = jarrays:fromHandle(arrayHandle, "byte", "int");
    if (value is error) {
        return false;
    }
    int[] array = <int[]>value;
    if (arrayIntended == array) {
        return true;
    } else {
        return false;
    }
}

public function testFromHandleWithShort(handle arrayHandle) returns boolean {
    int[] arrayIntended = [34, 76, 12, 90, 45];
    any[]|error value = jarrays:fromHandle(arrayHandle, "short", "int");
    if (value is error) {
        return false;
    }
    int[] array = <int[]>value;
    if (arrayIntended == array) {
        return true;
    } else {
        return false;
    }
}

public function testFromHandleWithShortFloat(handle arrayHandle) returns boolean {
    float[] arrayIntended = [34, 76, 12, 90, 45];
    any[]|error value = jarrays:fromHandle(arrayHandle, "short", "float");
    if (value is error) {
        return false;
    }
    float[] array = <float[]>value;
    if (arrayIntended == array) {
        return true;
    } else {
        return false;
    }
}

public function testFromHandleWithLong(handle arrayHandle) returns boolean {
    int[] arrayIntended = [34, 76, 12, 90, 45];
    any[]|error value = jarrays:fromHandle(arrayHandle, "long");
    if (value is error) {
        return false;
    }
    int[] array = <int[]>value;
    if (arrayIntended == array) {
        return true;
    } else {
        return false;
    }
}

public function testFromHandleWithLongFloat(handle arrayHandle) returns boolean {
    float[] arrayIntended = [34, 76, 12, 90, 45];
    any[]|error value = jarrays:fromHandle(arrayHandle, "long", "float");
    if (value is error) {
        return false;
    }
    float[] array = <float[]>value;
    if (arrayIntended == array) {
        return true;
    } else {
        return false;
    }
}

public function testFromHandleWithCharacter(handle arrayHandle) returns boolean {
    int[] arrayIntended = [115, 107, 112, 119, 105];
    any[]|error value = jarrays:fromHandle(arrayHandle, "char");
        if (value is error) {
            return false;
        }
    int[] array = <int[]>value;
    if (arrayIntended == array) {
        return true;
    } else {
        return false;
    }
}

public function testFromHandleWithCharacterFloat(handle arrayHandle) returns boolean {
    float[] arrayIntended = [115, 107, 112, 119, 105];
    any[]|error value = jarrays:fromHandle(arrayHandle, "char", "float");
        if (value is error) {
            return false;
        }
    float[] array = <float[]>value;
    if (arrayIntended == array) {
        return true;
    } else {
        return false;
    }
}

public function testFromHandleWithHandle(handle arrayHandle) returns boolean {
    String str1 = newString("Welcome");
    String str2 = newString("To");
    String str3 = newString("Ballerina");
    String[] arrayIntended = [str1, str2, str3];
    any[]|error value = jarrays:fromHandle(arrayHandle, "handle");
    if (value is error) {
        return false;
    }
    handle[] array = <handle[]>value;
    int count = array.length();
    String[] obj = [];
    foreach int i in 0 ... count-1 {
        String element = new(array[i]);
        obj[i] = element;
    }
    if (obj[0].toString() == "Welcome" || obj[1].toString() == "To" || obj[2].toString() == "Ballerina") {
        return true;
    } else {
        return false;
    }
}

@java:Binding {
  class: "java.lang.String"
}
public type String object {

    *java:JObject;

    public function __init(handle obj) {
        self.jObj = obj;
    }

    public function toString() returns string {
        return java:jObjToString(self.jObj);
    }
};

public function newString(string arg0) returns String {
    handle obj = java_lang_String_newString(java:fromString(arg0));
    String _string = new(obj);
    return _string;
}

function java_lang_String_newString(handle arg0) returns handle = @java:Constructor {
    class: "java.lang.String",
    paramTypes: ["java.lang.String"]
} external;
