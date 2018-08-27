function main (string... args) {
    string data = "static";

    xmlns "http://ballerina.com/updated" as ns0;

    xml x1 = xml `<book>The Lost World{{data}}</book>`;
    secureFunction(x1, x1);

    xml x2 = xml `<{{data}}>The Lost World</{{data}}>`;
    secureFunction(x2, x2);

    xml x3 = xml `<ns0:book ns0:status="{{data}}" count="5"/>`;
    secureFunction(x3@[ns0:status], x3@[ns0:status]);
    secureFunction(x3@[ns0:count], x3@[ns0:count]);
}

public function secureFunction (@sensitive any secureIn, any insecureIn) {

}
