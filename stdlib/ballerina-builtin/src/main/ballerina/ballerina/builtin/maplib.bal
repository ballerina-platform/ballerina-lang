package ballerina.builtin;

@Description { value:"Removes the specified element from the map"}
@Param { value:"m: The map object" }
@Param { value:"key: The key to be removed" }
documentation {
Removes the specified element from the map.
- #m The map object
- #key The key to be removed
}
public native function <map m>  remove (string key);

@Description { value:"Returns an array of keys contained in the specified map"}
@Param { value:"m: The map object" }
@Return { value:"A string array of keys contained in the specified map " }
documentation {
Returns an array of keys contained in the specified map.
- #m The map object
- #sArray A string array of keys contained in the specified map
}
public native function <map m> keys () (string[] sArray);

@Description { value:"Check whether specific key exists from the given map"}
@Param { value:"m: The map object" }
@Param { value:"key: The key to be find existence" }
documentation {
Check whether specific key exists from the given map.
- #m The map object
- #key The key to be find existence
}
public native function <map m>  hasKey (string key) (boolean);

@Description { value:"Clear the items from given map"}
@Param { value:"m: The map object" }
documentation {
Clear the items from given map.
- #m The map object
}
public native function <map m>  clear ();

@Description { value:"Returns an array of values contained in the specified map"}
@Param { value:"m: The map object" }
@Return { value:"An any array of values contained in the specified map " }
documentation {
Returns an array of values contained in the specified map.
- #m The map object
- #anyArray An any array of values contained in the specified map
}
public native function <map m> values () (any[] anyArray);
