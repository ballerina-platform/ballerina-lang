public function main (string... args) {
    worker w1 {
        string data1 = "string";
        string data2 = "string";
        (data1, data2) -> w2;
        (data1, data2) <- w2;
        secureFunction(data1, data2);
    }
    worker w2 {
        string data3 = "string";
        string data4 = "string";
        (data3, data4) <- w1;
        secureFunction(data3, data4);
        (data3, data4) -> w1;
    }
}
function secureFunction (@sensitive string secureIn, string insecureIn) {

}
