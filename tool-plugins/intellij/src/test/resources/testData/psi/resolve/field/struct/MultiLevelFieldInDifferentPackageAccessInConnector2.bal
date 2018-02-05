import org.test;

connector test() {

    action test () {
        test:Name name = {firstName:""};
        test:Person person = {};
        person./*ref*/name=name;
    }
}
