package ballerina.builtin;

@Description { value:"Removes the specified element from the map"}
@Param { value:"m: The map object" }
@Param { value:"key: The key to be removed" }
public native function <map m>  remove (string key);

@Description { value:"Returns the length of the specified map"}
@Param { value:"m: The map object" }
@Return { value:"The number of elements in the map object" }
public native function <map m> size () (int);

@Description { value:"Returns an array of keys contained in the specified map"}
@Param { value:"m: The map object" }
@Return { value:"A string array of keys contained in the specified map " }
public native function <map m> keys () (string[]);

@Description { value:"Check whether specific key exists from the given map"}
@Param { value:"m: The map object" }
@Param { value:"key: The key to be find existence" }
public native function <map m>  hasKey (string key) (boolean);

@Description { value:"Clear the items from given map"}
@Param { value:"m: The map object" }
public native function <map m>  clear ();
