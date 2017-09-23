package ballerina.lang.jsons;

import ballerina.doc;

@doc:Description { value: "Options struct for JSON to XML conversion "}
struct Options {
    string attributePrefix = "@";
    string arrayEntryTag = "item";
}

@doc:Description { value:"Removes each element that matches the given key."}
@doc:Param { value:"j: A JSON object" }
@doc:Param { value:"key: Key of the field to remove" }
native function remove (json j, string key);

@doc:Description { value:"Converts a JSON object to a string representation"}
@doc:Param { value:"j: A JSON object" }
@doc:Return { value:"string: String value of the converted JSON" }
native function toString (json j) (string);

@doc:Description { value:"Parses the string argument as a JSON value."}
@doc:Param { value:"jsonStr: String representation of JSON" }
@doc:Return { value:"json: Parsed JSON value" }
native function parse(string jsonStr) (json);

@doc:Description { value:"Returns an array of keys contained in the specified JSON."}
@doc:Param { value:"j: A JSON object" }
@doc:Return { value:"string[]: A string array of keys contained in the specified JSON" }
native function getKeys(json j) (string[]);

@doc:Description { value:"Converts a JSON object to a XML representation"}
@doc:Param { value:"j: A JSON object" }
@doc:Return { value:"xml: XML value of the converted JSON" }
native function toXML (json j, Options options) (xml);
