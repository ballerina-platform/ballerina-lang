import org.test;

connector test() {

    action test () {
        test:Name name = {firstName:""};
        test:Person person = {/*ref*/name:name};
    }
}
