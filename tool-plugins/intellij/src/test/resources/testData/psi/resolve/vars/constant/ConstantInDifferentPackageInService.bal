import org.test;

service<http> test {

    resource test (message m) {
        int value = test:/*ref*/a;
    }
}
