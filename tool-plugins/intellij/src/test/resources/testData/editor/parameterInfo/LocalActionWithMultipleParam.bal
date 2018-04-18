function main (string... args) {
    Con c = create Con();
    c.test(<caret>)
}

connector Con () {

    action test(string a, string b){

    }
}
