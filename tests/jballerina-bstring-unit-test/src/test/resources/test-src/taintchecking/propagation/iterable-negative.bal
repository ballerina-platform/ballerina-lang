public function main (string... args) {
    string[] words = [ "ant", args[0], "cat", "dear", "elephant" ];
    secureFunction(words.length(), words.length());

    words.forEach(function (string word) { secureFunction(word, word);});
}

public function secureFunction (@untainted any secureIn, any insecureIn) {

}
