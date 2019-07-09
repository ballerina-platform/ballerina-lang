public function main (string... args) {
    string data = args[0];

    xmlns "http://ballerina.com/updated" as ns0;

    xml x1 = xml `<book>The Lost World${data}</book>`;
    secureFunction(x1, x1);

    xml x2 = xml `<elem>The Lost World - ${data}</elem>`;
    secureFunction(x2, x2);

    xml x3 = xml `<ns0:book ns0:status="${data}" count="5"/>`;
    secureFunction(x3@[ns0:status], x3@[ns0:status]);
    secureFunction(x3@[ns0:count], x3@[ns0:count]);

    var x4 = xml `<root xmlns:ns4="http://sample.com/wso2/f"></root>`;
    x4@["foo1"] = args[0];
    secureFunction(x4, x4);
}

public function secureFunction (@untainted any secureIn, any insecureIn) {

}
