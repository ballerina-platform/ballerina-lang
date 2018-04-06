public function main (string[] args) {
    var returnData = unionReturn("static");
    match returnData {
        string stringData => {
            string sample = "static";
            sample = sample + stringData;
            secureFunction(sample);
        }
        int intData => {
            int sample = 100;
            sample = sample + intData;
            secureFunction(intData);
        }
    }
}

function unionReturn(string input1) returns string|int {
    return input1;
}

function secureFunction(@sensitive any secureIn) {

}