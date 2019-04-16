import ballerina/http;

public type Person record {
    string name;
    int age;
    Person? parent = ();
    json info;
    map<anydata> address = {

    };
    int[] marks = [];
};

http:ServiceEndpointConfiguration helloWorldEPConfig = {
  secureSocket: {
                    keyStore: {
               path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
          password: "ballerina"
           }
       }
};

function testCastToStructInDifferentPkg() {
    Person p1 = {
        name: "aaa",
        age: 25,
        parent:{ name: "bbb",
            age: 50,
            address:{"city": "Colombo", "country": "SriLanka"},
            info:{ status: "married"}
        },
        info:{ status: "single"}
    };
}