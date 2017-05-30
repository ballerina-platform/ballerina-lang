import org.test;

service test {

    resource test (message m) {
        int value = test:/*ref*/a;
    }
}
