package ballerina.builtin;

@Description { value: "Options struct for JSON to XML conversion "}
@Field {value:"attributePrefix: Prefix to identify XML attribute,default value is '@'."}
@Field {value:"arrayEntryTag: The name of the tag to be added to each entry of JSON array."}
public struct jsonOptions {
    string attributePrefix = "@";
    string arrayEntryTag = "item";
}

@Description { value:"Removes each element that matches the given key."}
@Param { value:"j: A JSON object" }
@Param { value:"key: Key of the field to remove" }
documentation {
Removes each element that matches the given key.
- #j A JSON object
- #key Key of the field to remove
}
public native function <json j> remove (string key);

@Description { value:"Converts a JSON object to a string representation"}
@Param { value:"j: A JSON object" }
@Return { value:"String value of the converted JSON" }
documentation {
Converts a JSON object to a string representation.
- #j A JSON object
- #s String value of the converted JSON                             
}
public native function <json j> toString () (string s);

@Description { value:"Returns an array of keys contained in the specified JSON."}
@Param { value:"j: A JSON object" }
@Return { value:"A string array of keys contained in the specified JSON" }
documentation {
Returns an array of keys contained in the specified JSON.
- #j A JSON object
- #sArray A string array of keys contained in the specified JSON  
}
public native function <json j> getKeys() (string[] sArray);

@Description { value:"Converts a JSON object to a XML representation"}
@Param { value:"j: A JSON object" }
@Param { value:"options: jsonOptions struct for JSON to XML conversion properties" }
@Return { value:"The XML representation of the JSON" }
documentation {
Converts a JSON object to a XML representation.
- #j A JSON object
- #options jsonOptions struct for JSON to XML conversion properties
- #payload The XML representation of the JSON
- #cError JSON to XML conversion error
}
public native function <json j> toXML (jsonOptions options) (xml payload, TypeConversionError cError);
