package ballerina.lang.arrays;

import ballerina.doc;

@doc:Description { value:"Returns the length of the specified message object array"}
@doc:Param { value:"messageArray: The message object array" }
@doc:Return { value:"int: The length of the specified array" }
native function length (message[] messageArray) (int);

@doc:Description { value:"Returns the length of the specified JSON array"}
@doc:Param { value:"jsonArray: The JSON array" }
@doc:Return { value:"int: The length of the specified array" }
native function length (json[] jsonArray) (int);

@doc:Description { value:"Copies the specified string array"}
@doc:Param { value:"stringArray: The array to be copied" }
@doc:Return { value:"string[]: A copy of the specified array" }
native function copyOf (string[] stringArray) (string[]);

@doc:Description { value:"Copies the specified XML array"}
@doc:Param { value:"xmlArray: The XML array to be copied" }
@doc:Return { value:"xml[]: A copy of the specified array" }
native function copyOf (xml[] xmlArray) (xml[]);

@doc:Description { value:"Copies the specified range of the specified string array "}
@doc:Param { value:"stringArray: The string array from which the range will be copied" }
@doc:Param { value:"from: The initial index of the range" }
@doc:Param { value:"to: The final index of the range" }
@doc:Return { value:"string[]: A new array with the specified range from the original array" }
native function copyOfRange (string[] stringArray, int from, int to) (string[]);

@doc:Description { value:"Sorts the specified string array "}
@doc:Param { value:"arr: The string array to be sorted" }
@doc:Return { value:"string[]): The sorted array" }
native function sort (string[] arr) (string[]);

@doc:Description { value:"Returns the length of the specified XML array"}
@doc:Param { value:"xmlArray: The XML array" }
@doc:Return { value:"int: The length of the specified array" }
native function length (xml[] xmlArray) (int);

@doc:Description { value:"Returns the length of the specified string array"}
@doc:Param { value:"stringArray: The string array" }
@doc:Return { value:"int: The length of the specified array" }
native function length (string[] stringArray) (int);

@doc:Description { value:"Returns the length of the specified float array"}
@doc:Param { value:"floatArray: The float array" }
@doc:Return { value:"int: The length of the specified array" }
native function length (float[] floatArray) (int);

@doc:Description { value:"Copies the specified integer array"}
@doc:Param { value:"intArray: The array to be copied" }
@doc:Return { value:"int[]: A copy of the specified array" }
native function copyOf (int[] intArray) (int[]);

@doc:Description { value:"Returns the length of the specified integer array"}
@doc:Param { value:"intArray: The integer array" }
@doc:Return { value:"int: The length of the specified array" }
native function length (int[] intArray) (int);

@doc:Description { value:"Copies the specified range of the specified XML array "}
@doc:Param { value:"xmlArray: The XML array from which the range will be copied" }
@doc:Param { value:"from: The initial index of the range" }
@doc:Param { value:"to: The final index of the range" }
@doc:Return { value:"xml[]: A new array with the specified range from the original array" }
native function copyOfRange (xml[] xmlArray, int from, int to) (xml[]);

@doc:Description { value:"Copies the specified range of the specified message object array "}
@doc:Param { value:"messageArray: The message object array from which the range will be copied" }
@doc:Param { value:"from: The initial index of the range" }
@doc:Param { value:"to: The final index of the range" }
@doc:Return { value:"message[]: A new array with the specified range from the original array" }
native function copyOfRange (message[] messageArray, int from, int to) (message[]);

@doc:Description { value:"Copies the specified message object array"}
@doc:Param { value:"messageArray: The array to be copied" }
@doc:Return { value:"message[]: A copy of the specified array" }
native function copyOf (message[] messageArray) (message[]);

@doc:Description { value:"Copies the specified range of the specified integer array "}
@doc:Param { value:"intArray: The integer array from which the range will be copied" }
@doc:Param { value:"from: The initial index of the range" }
@doc:Param { value:"to: The final index of the range" }
@doc:Return { value:"int[]: A new array with the specified range from the original array" }
native function copyOfRange (int[] intArray, int from, int to) (int[]);

@doc:Description { value:"Copies the specified JSON array"}
@doc:Param { value:"jsonArray: The array to be copied" }
@doc:Return { value:"json[]: A copy of the specified array" }
native function copyOf (json[] jsonArray) (json[]);

@doc:Description { value:"Copies the specified float array"}
@doc:Param { value:"floatArray: The array to be copied" }
@doc:Return { value:"float[]: A copy of the specified array" }
native function copyOf (float[] floatArray) (float[]);

@doc:Description { value:"Copies the specified range of the specified JSON array "}
@doc:Param { value:"jsonArray: The JSON array from which the range will be copied" }
@doc:Param { value:"from: The initial index of the range" }
@doc:Param { value:"to: The final index of the range" }
@doc:Return { value:"json[]: A new array with the specified range from the original array" }
native function copyOfRange (json[] jsonArray, int from, int to) (json[]);

@doc:Description { value:"Copies the specified range of the specified float array "}
@doc:Param { value:"floatArray: The float array from which the range will be copied" }
@doc:Param { value:"from: The initial index of the range" }
@doc:Param { value:"to: The final index of the range" }
@doc:Return { value:"float[]: A new array with the specified range from the original array" }
native function copyOfRange (float[] floatArray, int from, int to) (float[]);

