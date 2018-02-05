connector test() {

    action test () {
        Name name = {firstName:""};
        Person person = {};
        person./*ref*/name=name;
    }
}
