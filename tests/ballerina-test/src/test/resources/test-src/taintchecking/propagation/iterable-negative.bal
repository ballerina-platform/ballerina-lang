public function main (string[] args) {
    string[] words = [ "ant", args[0], "cat", "dear", "elephant" ];
    secureFunction(words.count(), words.count());

    words.foreach((string word) => { secureFunction(word, word);});
}

public function secureFunction (@sensitive any secureIn, any insecureIn) {

}
