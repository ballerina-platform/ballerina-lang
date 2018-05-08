
import ballerina/doc;

@doc:Description { value:"Copies the specified range of the specified string array "}
@doc:Param { value:"anyArrayFrom: The any array from which the range will be copied" }
@doc:Param { value:"anyArrayTo: The any array to which the range will be copied" }
@doc:Param { value:"from: The initial index of the range" }
@doc:Param { value:"to: The final index of the range" }
@doc:Return { value:"int: Number of elements copied" }
native function copyOfRange (any anyArrayFrom, any anyArrayTo, int from, int to) (int);

@doc:Description { value:"Sorts the specified string array "}
@doc:Param { value:"arr: The string array to be sorted" }
@doc:Return { value:"string[]): The sorted array" }
native function sort (string[] arr) (string[]);

@doc:Description { value:"Copies the specified any array"}
@doc:Param { value:"anyArrayFrom: The from array to be copied" }
@doc:Param { value:"anyArrayTo: The to array to which to copy to" }
@doc:Return { value:"int: Number of elements copied" }
native function copyOf (any anyArrayFrom, any anyArrayTo) (int);