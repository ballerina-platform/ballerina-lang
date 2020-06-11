public function main (string... args) {
    string data = args[0];
    string untaintedData = <@untainted string> data;
    secureFunction(untaintedData);
    secureFunction(data);

    string fakeUntaintedStr = <@fakeUntaint string> data;
    secureFunction(fakeUntaintedStr);
}

function secureFunction (@untainted string secureIn) {

}

public const annotation fakeUntaint on source type;
