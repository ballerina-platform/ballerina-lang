import org.test;

service<http> test {

    resource test (message m) {
        test:/*ref*/fun();
    }
}
