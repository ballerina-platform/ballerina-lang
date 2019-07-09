public function main (string... args) {
    var returnData = unionReturn(args[0]);
    if (returnData is string) {
        string sample = "static";
        sample = sample + returnData;
        secureFunction(sample);
    } else {
        int sample = 100;
        sample = sample + returnData;
        secureFunction(sample);
    }
}

function unionReturn(string input1) returns string|int {
    return input1;
}

function secureFunction(@untainted any secureIn) {

}
