function name1() {
map<string> words = {
        a: "apple",
        b: "banana",
        c: "cherry"
    };

    while(true){
        while(false) {
    break;
        }break;
    }

    foreach k, v in words {
        io:println("letter: ", k, ", word: ", v);
        foreach j, l in words {
            io:println("letter: ", j, ", word: ", l);break;
        }
                 break;
    }
}