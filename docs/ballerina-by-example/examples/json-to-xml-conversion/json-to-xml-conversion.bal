import ballerina/io;

function main (string[] args) {
    //Create JSON.
    json j1 = {"Store":{
                           "@id":"AST",
                           "name":"Anne",
                           "address":{
                                         "street":"Main",
                                         "city":"94"
                                     },
                           "codes":["4", "8"]
                       }
              };
    //Convert to XML with default attribute prefix and arrayEntryTag.
    var x1 = j1.toXML({});
    io:println(x1);

    //Convert to XML with custom attribute prefix and custom array tag.
    json j2 = {"Store":{
                           "#id":"AST",
                           "name":"Anne",
                           "address":{
                                         "street":"Main",
                                         "city":"94"
                                     },
                           "codes":["4", "8"]
                       }
              };
    var x2 = j2.toXML({attributePrefix:"#", arrayEntryTag:"wrapper"});
    io:println(x2);
}
