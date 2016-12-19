package ballerina.lang.json;

// Get the value of an existing element
native function getJson(json j, string jsonPath) (json);
native function getString(json j, string jsonPath) (string);
native function getInt(json j, string jsonPath) (int);
native function getDouble(json j, string jsonPath) (double);
native function getBoolean(json j, string jsonPath) (boolean);

// Set the value of an existing element
native function set(json j, string jsonPath, json value);
native function set(json j, string jsonPath, string value);
native function set(json j, string jsonPath, int value);
native function set(json j, string jsonPath, double value);
native function set(json j, string jsonPath, boolean value);

// Add a new element to a json array
native function add(json j, string jsonPath, json value);
native function add(json j, string jsonPath, string value);
native function add(json j, string jsonPath, int value);
native function add(json j, string jsonPath, double value);
native function add(json j, string jsonPath, boolean value);

// Add a new element to a json object
native function add(json j, string jsonPath, string key, json value);
native function add(json j, string jsonPath, string key, string value);
native function add(json j, string jsonPath, string key, int value);
native function add(json j, string jsonPath, string key, double value);
native function add(json j, string jsonPath, string key, boolean value);

native function remove(json j, string jsonPath);

native function rename(json j, string jsonPath, string oldKey, string newKey);

native function toString(json j) (string);