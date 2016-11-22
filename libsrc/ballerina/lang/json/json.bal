package ballerina.lang.json;

native function get(json j, string jsonPath) (json);
native function set(json j, string jsonPath, json value);

native function get(json j, string jsonPath) (string);
native function set(json j, string jsonPath, string value);

native function remove(json j, string jsonPath);