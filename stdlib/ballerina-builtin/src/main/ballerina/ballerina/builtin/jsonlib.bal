package ballerina.builtin;

@Description { value:"Removes each element that matches the given key."}
@Param { value:"j: A JSON object" }
@Param { value:"key: Key of the field to remove" }
public native function <json j> remove (string key);

@Description { value:"Converts a JSON object to a string representation"}
@Param { value:"j: A JSON object" }
@Return { value:"String value of the converted JSON" }
public native function <json j> toString () (string);

@Description { value:"Returns an array of keys contained in the specified JSON."}
@Param { value:"j: A JSON object" }
@Return { value:"A string array of keys contained in the specified JSON" }
public native function <json j> getKeys() (string[]);

@Description { value:"Converts a JSON object to a XML representation"}
@Param { value:"j: A JSON object" }
@Param { value:"options: jsonOptions struct for JSON to XML conversion properties" }
@Return { value:"The XML representation of the JSON" }
public native function <json j> toXML (struct {
                                           string attributePrefix = "@";
                                           string arrayEntryTag = "item";
                                       } options) (xml, error);
