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
    jsonOptions options1 = {};
    xml x1 = j1.toXML(options1);
    println(x1);

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
    jsonOptions options2 = {attributePrefix:"#", arrayEntryTag:"wrapper"};
    xml x2 = j2.toXML(options2);
    println(x2);
}
