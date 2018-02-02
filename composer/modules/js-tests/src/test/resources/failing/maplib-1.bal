package ballerina.builtin;

@Description { value:"Removes the specified element from the map"}
@Param { value:"m: The map object" }
@Param { value:"key: The key to be removed" }
public native function <map m>  remove (string key);

@Description { value:"Returns the length of the specified map"}
@Param { value:"m: The map object" }
@Return { value:"int: The number of elements in the map object" }
public native function <map m> length () (int);

@Description { value:"Returns an array of keys contained in the specified map"}
@Param { value:"m: The map object" }
@Return { value:"string[]: A string array of keys contained in the specified map " }
public native function <map m> keys () (string[]);
