function main (string[] args) {
    Con c = create Con();
    c.test(c,<caret>)
}

connector Con () {

    action test(string a, string b){

    }
}
