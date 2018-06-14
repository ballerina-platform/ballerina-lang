import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function, which replaces the real function.
@test:Mock {
    packageName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    outputs[counter] = s[0];
    counter++;
}

@test:Config
function testFunc() {
    // Invoking the main function.
    main();

    json js1 = {
        "h:Store": {
            "@xmlns:h": "http://www.test.com",
            "@id": "AST",
            "h:name": {
                "@xmlns:h": "http://www.test.com",
                "#text": "Anne"
            },
            "h:address": {
                "@xmlns:h": "http://www.test.com",
                "h:street": {
                    "@xmlns:h": "http://www.test.com",
                    "#text": "Main"
                },
                "h:city": {
                    "@xmlns:h": "http://www.test.com",
                    "#text": "94"
                }
            },
            "h:code": {
                "@xmlns:h": "http://www.test.com",
                "h:item": [
                    {
                        "@xmlns:h": "http://www.test.com",
                        "#text": "4"
                    },
                    {
                        "@xmlns:h": "http://www.test.com",
                        "#text": "8"
                    }
                ]
            }
        }
    };

    json js2 = {
        "Store": {
            "#id": "AST",
            "name": "Anne",
            "address": {
                "street": "Main",
                "city": "94"
            },
            "code": {
                "item": [
                    "4",
                    "8"
                ]
            }
        }
    };

    test:assertEquals(js1, outputs[0]);
    test:assertEquals(js2, outputs[1]);
}
