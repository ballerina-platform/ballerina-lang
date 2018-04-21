
import ballerina/doc;

@doc:Description { value:"Removes the specified element from the map"}
@doc:Param { value:"m: The map object" }
@doc:Param { value:"key: The key to be removed" }
native function remove (map m, string key);

@doc:Description { value:"Returns the length of the specified map"}
@doc:Param { value:"m: The map object" }
@doc:Return { value:"int: The number of elements in the map object" }
native function length (map m) (int);

@doc:Description { value:"Returns an array of keys contained in the specified map"}
@doc:Param { value:"m: The map object" }
@doc:Return { value:"string[]: A string array of keys contained in the specified map " }
native function keys (map m) (string[]);

