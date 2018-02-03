package ballerina.util.arrays;

@Description { value:"Copies the specified range of the specified string array "}
@Param { value:"anyArrayFrom: The any array from which the range will be copied" }
@Param { value:"anyArrayTo: The any array to which the range will be copied" }
@Param { value:"from: The initial index of the range" }
@Param { value:"to: The final index of the range" }
@Return { value:"int: Number of elements copied" }
public native function copyOfRange (any anyArrayFrom, any anyArrayTo, int from, int to) (int);

@Description { value:"Sorts the specified string array "}
@Param { value:"arr: The string array to be sorted" }
@Return { value:"string[]): The sorted array" }
public native function sort (string[] arr) (string[]);

@Description { value:"Copies the specified any array"}
@Param { value:"anyArrayFrom: The from array to be copied" }
@Param { value:"anyArrayTo: The to array to which to copy to" }
@Return { value:"int: Number of elements copied" }
public native function copyOf (any anyArrayFrom, any anyArrayTo) (int);