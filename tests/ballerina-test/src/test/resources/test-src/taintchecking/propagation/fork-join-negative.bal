function main (string[] args) {
    fork {
        worker w1 {
            args[0] -> fork;
        }
        worker w2 {
            "staticData" -> fork;
        }
    } join (all) (map results) {
        secureFunction(results, results);
    }
}

function secureFunction (@sensitive any secureIn, any insecureIn) {

}
