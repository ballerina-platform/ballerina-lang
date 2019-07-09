public function main (string... args) {
    string data = "static";

    xmlns "http://ballerina.com/updated" as ns0;

    xml x1 = xml `<book>The Lost World${data}</book>`;
    secureFunction(x1, x1);

    xml x2 = xml `<static>The Lost World</static>`;
    secureFunction(x2, x2);

    xml x3 = xml `<ns0:book ns0:status="${data}" count="5"/>`;
    secureFunction(x3@[ns0:status], x3@[ns0:status]);
    secureFunction(x3@[ns0:count], x3@[ns0:count]);

    var x4 = xml `<root xmlns:ns4="http://sample.com/wso2/f"></root>`;
    x4@["foo1"] = "foo";
    secureFunction(x4, x4);

    if ("tests" == "test") {
        secureFunction(ns0:book, ns0:book);
    }
}

public function secureFunction (@untainted any secureIn, any insecureIn) {

}
