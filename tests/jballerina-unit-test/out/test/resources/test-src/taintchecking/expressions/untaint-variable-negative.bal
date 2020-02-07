public function main (string... args) {
    string data = args[0];
    string untaintedData = <@untainted string> data;
    secureFunction(untaintedData);
    secureFunction(data);
}

function secureFunction (@untainted string secureIn) {

}
