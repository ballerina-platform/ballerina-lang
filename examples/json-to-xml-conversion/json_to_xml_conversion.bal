import ballerina/io;

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
    var x1 = j1.toXML({});
    io:println(x1);

    // Converts the JSON object to XML using a custom `attributePrefix` (i.e., the `#` character)
    // and the custom `arrayEntryTag` (i.e., `wrapper`).
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
    var x2 = j2.toXML({ attributePrefix: "#", arrayEntryTag: "wrapper" });
    io:println(x2);
}
