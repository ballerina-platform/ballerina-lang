int /*def*/a;

service test {

    resource test (message m) {
        /*ref*/a = 10;
    }
}
