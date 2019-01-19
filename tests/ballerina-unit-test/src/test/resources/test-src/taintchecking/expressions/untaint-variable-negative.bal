import ballerina/crypto;

public function main (string... args) {
    string data = args[0];
    string untaintedData = <string>crypto:unsafeMarkUntainted(data);
    secureFunction(untaintedData);
    secureFunction(data);
}

function secureFunction (@sensitive string secureIn) {

}
