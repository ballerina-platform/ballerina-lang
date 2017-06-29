import org.test;

service<http> test {

    resource test (message m) {
        test:Name name = {firstName:""};
        test:Person person = {/*ref*/name:name};
    }
}
