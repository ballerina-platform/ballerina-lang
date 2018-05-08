import ballerina/lang.system;
import ballerina/lang.jsons;

function main (string... args) {
    //Create json.
    json j1 = {"Store":{"@id":"AST","name":"Anne","address":
                   {"street":"Main","city": "94"},"codes":["4","8"]}};
    //Convert to XML with default attribute prefix and arrayEntryTag.
    jsons:Options options1 = {};
    xml x1 = jsons:toXML(j1, options1);
    system:println(x1);

    //Convert to XML with custom attribute prefix and with custom array tag.
    json j2 = {"Store":{"#id":"AST","name":"Anne","address":
                   {"street":"Main","city": "94"},"codes":["4","8"]}};
    jsons:Options options2 = {attributePrefix : "#", arrayEntryTag : "wrapper"};
    xml x2 = jsons:toXML(j2, options2);
    system:println(x2);
}
