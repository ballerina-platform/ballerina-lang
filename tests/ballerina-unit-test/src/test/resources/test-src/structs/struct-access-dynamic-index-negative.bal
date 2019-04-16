type Person record {
    string name = "default first name";
    string lname = "";
    map<any> adrs = {};
    int age = 999;
};

type StructField record {
    string key = "";
};

function testExpressionAsStructIndex () returns string {
    StructField nameField = {key:"name"};
    Person emp = {name:"Jack", adrs:{"country":"USA", "state":"CA"}, age:25};
    string|map<any>|int|anydata|error result = emp[nameField.key];
    if result is string {
        return result;
    } else {
        return "fail";
    }
}
