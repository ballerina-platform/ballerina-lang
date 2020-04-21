public function main (string... args) {
    worker w1 {
        [string, string] data = ["string", "string"];
        data -> w2;
        data = <- w2;
        secureFunction(data[0], data[1]);
    }
    worker w2 {
        string data3 = "string";
        string data4 = "string";
        [string, string] val = <- w1;
        secureFunction(data3, data4);
        [string, string] dataW2 = [data3, data4];
        dataW2 -> w1;
    }
}

function secureFunction (@untainted string secureIn, string insecureIn) {

}
