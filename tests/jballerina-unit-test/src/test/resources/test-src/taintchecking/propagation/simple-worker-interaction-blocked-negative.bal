public function main (string... args) {
    worker w1 {
        string data1 = "string";
        data1 = <- w2;
        secureFunction(data1, data1);
        data1 -> w2;
    }
    worker w2 {
        string data = "string";
        args[0] -> w1;
        data = <- w1;
        secureFunction(data, data);
    }
}
function secureFunction (@untainted string secureIn, string insecureIn) {

}
