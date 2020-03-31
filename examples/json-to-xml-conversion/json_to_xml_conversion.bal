import ballerina/io;
import ballerina/xmlutils;

public function main() {
    // Creates a JSON object.
    json j1 = {
        "Store": {
            "@id": "AST",
            "name": "Anne",
            "address": {
                "street": "Main",
                "city": "94"
            },
            "codes": ["4", "8"]
        }
    };
    // Converts the JSON object to XML using the default `attributePrefix`
    // and the default `arrayEntryTag`.
    var x1 = xmlutils:fromJSON(j1);
    io:println(x1);

    json j2 = {
        "Store": {
            "#id": "AST",
            "name": "Anne",
            "address": {
                "street": "Main",
                "city": "94"
            },
            "codes": ["4", "8"]
        }
    };
    // Converts the JSON object to XML using a custom `attributePrefix` (i.e., the `#` character)
    // and the custom `arrayEntryTag` (i.e., `wrapper`).
    var x2 = xmlutils:fromJSON(j2, {attributePrefix: "#", arrayEntryTag: "wrapper"});
    io:println(x2);
}
