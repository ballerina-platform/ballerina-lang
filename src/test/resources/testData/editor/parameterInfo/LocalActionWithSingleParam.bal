function main (string[] args) {
    Con c = create Con();
    Con.test(<caret>)
}

connector Con () {

    action test(string a){

    }
}
