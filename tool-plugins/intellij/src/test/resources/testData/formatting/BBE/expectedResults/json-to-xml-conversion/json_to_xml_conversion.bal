import ballerina/io;

function main(string... args) {
    // Create a JSON object.
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
    // Convert the JSON object to XML using the default `attributePrefix`
    // and the default `arrayEntryTag`.
    var x1 = j1.toXML({});
    io:println(x1);

    // Convert the JSON object to XML using a custom `attributePrefix` (i.e., the `#` character),
    // and custom `arrayEntryTag` (i.e., `wrapper`).
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
