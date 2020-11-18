import ballerina/java;
import ballerina/java.arrays as jarrays;

# Returns a Ballerina array for a handle that holds a Java array.
# ```ballerina
# int[] array = <int[]> check jarrays:fromHandle(arrayHandle, "int");
# ```
#
# + array - The `handle`, which refers to the Java array
# + jType - The `string` parameter provided to specify the Java array element type
# + bType - The optional `string` parameter provided to specify the Ballerina array element type
# + return - Ballerina `any[]|error` array for the provided handle
public function fromHandle(handle array, string jType, string bType = "default") returns any[]|error {
    int count = jarrays:getLength(array);
    any[] returnArray = [];
    if (!java:isNull(array)) {
        if (jType == "string") {
            string[] returnStringArray = [];
            foreach int i in 0 ... count-1 {
                string? element = java:toString(<handle>jarrays:get(array, i));
                if (element is string) {
                    returnStringArray[i] = element;
                }
            }
            return returnStringArray;
        } else if (jType == "handle") {
            handle[] returnHandleArray = [];
            foreach int i in 0 ... count-1 {
                returnHandleArray[i] = jarrays:get(array, i);
            }
            return returnHandleArray;
        } else if (jType == "boolean") {
            boolean[] returnBooleanArray = [];
            foreach int i in 0 ... count-1 {
                boolean element = getBBooleanFromJBoolean(jarrays:get(array, i));
                returnBooleanArray[i] = element;
            }
            return returnBooleanArray;
        } else if (jType == "float") {
            float[] returnFloatArray = [];
            foreach int i in 0 ... count-1 {
                float element = getBFloatFromJFloat(jarrays:get(array, i));
                returnFloatArray[i] = element;
            }
            return returnFloatArray;
        } else if (jType == "double") {
            float[] returnFloatArray = [];
            foreach int i in 0 ... count-1 {
                float element = getBFloatFromJDouble(jarrays:get(array, i));
                returnFloatArray[i] = element;
            }
            return returnFloatArray;
        } else if (jType == "int" && (bType == "default" || bType == "int")) {
            int[] returnIntArray = [];
            foreach int i in 0 ... count-1 {
                int element = getBIntFromJInt(jarrays:get(array, i));
                returnIntArray[i] = element;
            }
            return returnIntArray;
        } else if (jType == "int" && bType == "float") {
            float[] returnFloatArray = [];
            foreach int i in 0 ... count-1 {
                float element = getBFloatFromJInt(jarrays:get(array, i));
                returnFloatArray[i] = element;
            }
            return returnFloatArray;
        } else if (jType == "byte" && (bType == "default" || bType == "byte")) {
            byte[] returnByteArray = [];
            foreach int i in 0 ... count-1 {
                byte element = getBByteFromJByte(jarrays:get(array, i));
                returnByteArray[i] = element;
            }
            return returnByteArray;
        } else if (jType == "byte" && bType == "float") {
            float[] returnFloatArray = [];
            foreach int i in 0 ... count-1 {
                float element = getBFloatFromJByte(jarrays:get(array, i));
                returnFloatArray[i] = element;
            }
            return returnFloatArray;
        } else if (jType == "byte" && bType == "int") {
            int[] returnIntArray = [];
            foreach int i in 0 ... count-1 {
                int element = getBIntFromJByte(jarrays:get(array, i));
                returnIntArray[i] = element;
            }
            return returnIntArray;
        } else if (jType == "short" && (bType == "default" || bType == "int")) {
            int[] returnIntArray = [];
            foreach int i in 0 ... count-1 {
                int element = getBIntFromJShort(jarrays:get(array, i));
                returnIntArray[i] = element;
            }
            return returnIntArray;
        } else if (jType == "short" && bType == "float") {
            float[] returnFloatArray = [];
            foreach int i in 0 ... count-1 {
                float element = getBFloatFromJShort(jarrays:get(array, i));
                returnFloatArray[i] = element;
            }
            return returnFloatArray;
        } else if (jType == "long" && (bType == "default" || bType == "int")) {
            int[] returnIntArray = [];
            foreach int i in 0 ... count-1 {
                int element = getBIntFromJLong(jarrays:get(array, i));
                returnIntArray[i] = element;
            }
            return returnIntArray;
        } else if (jType == "long" && bType == "float") {
            float[] returnFloatArray = [];
            foreach int i in 0 ... count-1 {
                float element = getBFloatFromJLong(jarrays:get(array, i));
                returnFloatArray[i] = element;
            }
            return returnFloatArray;
        } else if (jType == "char" && (bType == "default" || bType == "int")) {
            int[] returnIntArray = [];
            foreach int i in 0 ... count-1 {
                int element = getBIntFromJChar(jarrays:get(array, i));
                returnIntArray[i] = element;
            }
            return returnIntArray;
        } else if (jType == "char" && bType == "float") {
            float[] returnFloatArray = [];
            foreach int i in 0 ... count-1 {
                float element = getBFloatFromJChar(jarrays:get(array, i));
                returnFloatArray[i] = element;
            }
            return returnFloatArray;
        } else {
            return error("{ballerina/java.arrays} Handle to array conversion cannot be applied on the provided types");
        }
    }
    return returnArray;
}

# Returns a handle value representation for a Ballerina array.
# ```ballerina
# handle handleValue = check java.arrays:toHandle(array, "char");
# ```
#
# + array - Ballerina array, which is to be converted to a handle reference
# + jType - Java class name or the primitive type of the array elements referenced by the handle
# + return - The `handle|error` after the conversion
function toHandle(any[] array, string jType) returns handle|error {
    handle returnHandle = jarrays:newInstance(check java:getClass(jType), array.length());
    int count=0;
    while (count < array.length()) {
        if (array is byte[] && jType == "char") {
            jarrays:set(returnHandle, count, wrapByteToChar(array[count]));
            count+=1;
        } else if (array is byte[] && jType == "short") {
            jarrays:set(returnHandle, count, wrapByteToShort(array[count]));
            count+=1;
        } else if (array is byte[] && jType == "long") {
            jarrays:set(returnHandle, count, wrapByteToLong(array[count]));
            count+=1;
        } else if (array is byte[] && jType == "int") {
            jarrays:set(returnHandle, count, wrapByteToInt(array[count]));
            count+=1;
        } else if (array is byte[] && jType == "float") {
            jarrays:set(returnHandle, count, wrapByteToFloat(array[count]));
            count+=1;
        } else if (array is byte[] && jType == "double") {
                jarrays:set(returnHandle, count, wrapByteToDouble(array[count]));
            count+=1;
        } else if (array is byte[] && jType == "byte") {
                jarrays:set(returnHandle, count, wrapByteToByte(array[count]));
            count+=1;
        } else if (array is int[] && jType == "byte") {
                jarrays:set(returnHandle, count, wrapIntToByte(array[count]));
            count+=1;
        } else if (array is int[] && jType == "char") {
                jarrays:set(returnHandle, count, wrapIntToChar(array[count]));
            count+=1;
        } else if (array is int[] && jType == "short") {
                jarrays:set(returnHandle, count, wrapIntToShort(array[count]));
            count+=1;
        } else if (array is int[] && jType == "long") {
                jarrays:set(returnHandle, count, wrapIntToLong(array[count]));
            count+=1;
        } else if (array is int[] && jType == "int") {
                jarrays:set(returnHandle, count, wrapIntToInt(array[count]));
            count+=1;
        } else if (array is float[] && jType == "char") {
                jarrays:set(returnHandle, count, wrapFloatToChar(array[count]));
            count+=1;
        } else if (array is float[] && jType == "short") {
                jarrays:set(returnHandle, count, wrapFloatToShort(array[count]));
            count+=1;
        } else if (array is float[] && jType == "long") {
                jarrays:set(returnHandle, count, wrapFloatToLong(array[count]));
            count+=1;
        } else if (array is float[] && jType == "double") {
                jarrays:set(returnHandle, count, wrapFloatToDouble(array[count]));
            count+=1;
        } else if (array is float[] && jType == "byte") {
                jarrays:set(returnHandle, count, wrapFloatToByte(array[count]));
            count+=1;
        } else if (array is float[] && jType == "int") {
                jarrays:set(returnHandle, count, wrapFloatToInt(array[count]));
            count+=1;
        } else if (array is float[] && jType == "float") {
                jarrays:set(returnHandle, count, wrapFloatToFloat(array[count]));
            count+=1;
        } else if (array is boolean[]) {
                jarrays:set(returnHandle, count, wrapBooleanToBoolean(array[count]));
            count+=1;
        } else if (array is string[]) {
                jarrays:set(returnHandle, count, java:fromString(array[count]));
            count+=1;
        } else if (array is JObject[]) {
            JObject jObject = array[count];
                jarrays:set(returnHandle, count, jObject.jObj);
            count+=1;
        } else {
            return error("{ballerina/java.arrays} Array to handle conversion cannot be applied on the provided types");
        }
    }
    return returnHandle;
}

// Returns a Java Boolean handle for a Ballerina boolean
function wrapBooleanToBoolean(boolean b) returns handle = @java:Constructor {
    'class: "java.lang.Boolean",
    paramTypes: ["boolean"]
} external;

// Returns a Java Integer handle for a Ballerina integer
function wrapIntToInt(int i) returns handle = @java:Constructor {
    'class: "java.lang.Integer",
    paramTypes: ["int"]
} external;

// Returns a Java Character handle for a Ballerina integer
function wrapIntToChar(int b) returns handle = @java:Constructor {
    'class: "java.lang.Character",
    paramTypes: ["char"]
} external;

// Returns a Java Short handle for a Ballerina integer
function wrapIntToShort(int b) returns handle = @java:Constructor {
    'class: "java.lang.Short",
    paramTypes: ["short"]
} external;

// Returns a Java Long handle for a Ballerina integer
function wrapIntToLong(int b) returns handle = @java:Constructor {
    'class: "java.lang.Long",
    paramTypes: ["long"]
} external;

// Returns a Java Byte handle for a Ballerina integer
function wrapIntToByte(int b) returns handle = @java:Constructor {
    'class: "java.lang.Byte",
    paramTypes: ["byte"]
} external;

// Returns a Java Float handle for a Ballerina float
function wrapFloatToFloat(float b) returns handle = @java:Constructor {
    'class: "java.lang.Float",
    paramTypes: ["float"]
} external;

// Returns a Java Character handle for a Ballerina float
function wrapFloatToChar(float b) returns handle = @java:Constructor {
    'class: "java.lang.Character",
    paramTypes: ["char"]
} external;

// Returns a Java Short handle for a Ballerina float
function wrapFloatToShort(float b) returns handle = @java:Constructor {
    'class: "java.lang.Short",
    paramTypes: ["short"]
} external;

// Returns a Java Long handle for a Ballerina float
function wrapFloatToLong(float b) returns handle = @java:Constructor {
    'class: "java.lang.Long",
    paramTypes: ["long"]
} external;

// Returns a Java Double handle for a Ballerina float
function wrapFloatToDouble(float b) returns handle = @java:Constructor {
    'class: "java.lang.Double",
    paramTypes: ["double"]
} external;

// Returns a Java Byte handle for a Ballerina float
function wrapFloatToByte(float b) returns handle = @java:Constructor {
    'class: "java.lang.Byte",
    paramTypes: ["byte"]
} external;

// Returns a Java Integer handle for a Ballerina float
function wrapFloatToInt(float b) returns handle = @java:Constructor {
    'class: "java.lang.Integer",
    paramTypes: ["int"]
} external;

// Returns a Java Byte handle for a Ballerina byte
function wrapByteToByte(byte b) returns handle = @java:Constructor {
    'class: "java.lang.Byte",
    paramTypes: ["byte"]
} external;

// Returns a Java Float handle for a Ballerina byte
function wrapByteToFloat(byte b) returns handle = @java:Constructor {
    'class: "java.lang.Float",
    paramTypes: ["float"]
} external;

// Returns a Java Character handle for a Ballerina byte
function wrapByteToChar(byte b) returns handle = @java:Constructor {
    'class: "java.lang.Character",
    paramTypes: ["char"]
} external;

// Returns a Java Short handle for a Ballerina byte
function wrapByteToShort(byte b) returns handle = @java:Constructor {
    'class: "java.lang.Short",
    paramTypes: ["short"]
} external;

// Returns a Java Long handle for a Ballerina byte
function wrapByteToLong(byte b) returns handle = @java:Constructor {
    'class: "java.lang.Long",
    paramTypes: ["long"]
} external;

// Returns a Java Double handle for a Ballerina byte
function wrapByteToDouble(byte b) returns handle = @java:Constructor {
    'class: "java.lang.Double",
    paramTypes: ["double"]
} external;

// Returns a Java Integer handle for a Ballerina byte
function wrapByteToInt(byte b) returns handle = @java:Constructor {
    'class: "java.lang.Integer",
    paramTypes: ["int"]
} external;


// Returns a Ballerina boolean from a Java Boolean handle
function getBBooleanFromJBoolean(handle receiver) returns boolean = @java:Method {
    name:"booleanValue",
    'class:"java.lang.Boolean"
} external;

// Returns a Ballerina integer from a Java Integer handle
function getBIntFromJInt(handle receiver) returns int = @java:Method {
    name:"longValue",
    'class:"java.lang.Integer"
} external;

// Returns a Ballerina integer from a Java Byte handle
function getBIntFromJByte(handle receiver) returns int = @java:Method {
    name:"longValue",
    'class:"java.lang.Byte"
} external;

// Returns a Ballerina integer from a Java Short handle
function getBIntFromJShort(handle receiver) returns int = @java:Method {
    name:"longValue",
    'class:"java.lang.Short"
} external;

// Returns a Ballerina integer from a Java Long handle
function getBIntFromJLong(handle receiver) returns int = @java:Method {
    name:"longValue",
    'class:"java.lang.Long"
} external;

// Returns a Ballerina integer from a Java Character handle
function getBIntFromJChar(handle receiver) returns int = @java:Method {
    name:"charValue",
    'class:"java.lang.Character"
} external;

// Returns a Ballerina float from a Java Float handle
function getBFloatFromJFloat(handle receiver) returns float = @java:Method {
    name:"doubleValue",
    'class:"java.lang.Float"
} external;

// Returns a Ballerina float from a Java Short handle
function getBFloatFromJShort(handle receiver) returns float = @java:Method {
    name:"doubleValue",
    'class:"java.lang.Short"
} external;

// Returns a Ballerina float from a Java Byte handle
function getBFloatFromJByte(handle receiver) returns float = @java:Method {
    name:"doubleValue",
    'class:"java.lang.Byte"
} external;

// Returns a Ballerina float from a Java Double handle
function getBFloatFromJDouble(handle receiver) returns float = @java:Method {
    name:"doubleValue",
    'class:"java.lang.Double"
} external;

// Returns a Ballerina float from a Java Character handle
function getBFloatFromJChar(handle receiver) returns float = @java:Method {
    name:"charValue",
    'class:"java.lang.Character"
} external;

// Returns a Ballerina float from a Java Long handle
function getBFloatFromJLong(handle receiver) returns float = @java:Method {
    name:"doubleValue",
    'class:"java.lang.Long"
} external;

// Returns a Ballerina float from a Java Integer handle
function getBFloatFromJInt(handle receiver) returns float = @java:Method {
    name:"doubleValue",
    'class:"java.lang.Integer"
} external;

// Returns a Ballerina byte from a Java Byte handle
function getBByteFromJByte(handle receiver) returns byte = @java:Method {
    name:"byteValue",
    'class:"java.lang.Byte"
} external;

