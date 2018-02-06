int /*def*/a;

service<http> test {

    resource test (message m) {
        /*ref*/a = 10;
    }
}
