function main (string... args) {
    worker w1 {
        string data = "string";
        data -> w2;
        data <- w2;
        secureFunction(data, data);
    }
    worker w2 {
        string data1 = "string";
        data1 <- w1;
        secureFunction(data1, data1);
        data1 -> w1;
    }
}
function secureFunction (@sensitive string secureIn, string insecureIn) {

}
