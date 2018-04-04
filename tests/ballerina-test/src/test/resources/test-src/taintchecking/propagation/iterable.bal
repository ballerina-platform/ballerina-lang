public function main (string[] args) {
    string[] words = [ "ant", "bear", "cat", "dear", "elephant" ];
    secureFunction(words.count(), words.count());

    words.foreach((string word) => { secureFunction(word, word);});
}

public function secureFunction (@sensitive any secureIn, any insecureIn) {

}
