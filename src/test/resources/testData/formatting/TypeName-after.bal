function test () {

    any a = getParameter();
    any b = "foo";
    any i = 5;
    any j = 10;

    int k = (int)i + (int)j;

    int[][] a = [];

    a[0] = [1, 2, 3, 5, 6];
    a[1] = [10, 54];
    a[2] = [4, 6, 1];

    int i = a[0][0]; // Value of i is 1
    i = a[2][1];  // value of i is 6 now.

    message VarName = {};

    map m = {"a":1, "b":2};
    int i = (int)m["a"] + (int)m["b"];
    any k = m["a"];

    xml VariableName;
    xml<{"SchemaNamespaceName"} SchemaTypeOrElementName> VariableName;

    xmlDocument VariableName;
    xmlDocument<{"SchemaNamespaceName"} DocumentElementTypeOrElementName> VariableName;

    xml VariableName = `<xml-element-name [namespace declarations] [attributes]>element-content</xml-element-name>`;

    json VariableName;
    json VariableName = {"PropertyName":"Value", "PropertyName":"Value"};

    address a1 = {city:"Colombo", "country":"Sri Lanka"};
    json j = {city:"Colombo", "country":"Sri Lanka"};
    map m = {city:"Colombo", "country":"Sri Lanka"};

    system:println(a1.city, a1["country"]);
    system:println(j.city, j["country"]);
    system:println(m.city, m["country"]);
}

struct address {
    string city;
    string country;
}
