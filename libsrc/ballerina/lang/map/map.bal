package ballerina.lang.map;

// getting the value as m["key"]
// putting a value as m["key"] = value

native function keys(map m) (string[]);

native function length(map m) (int);

native function remove(map m, string key);
