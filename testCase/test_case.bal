service class Person {
    resource function get age() returns int {
        do {
            return 25;
        } on fail error err {
        return 0;
        }
    }
}