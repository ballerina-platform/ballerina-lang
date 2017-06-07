import org.test;

service test {

    resource test (message m) {
        test:Name name = {firstName:""};
        test:Person person = {/*ref*/name:name};
    }
}
