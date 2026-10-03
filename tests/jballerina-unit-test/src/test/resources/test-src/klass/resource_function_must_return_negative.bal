// Negative test: reachability error for resource function must use readable kind in message (issue #42746)
service class Person {
    resource function get age() returns int {
        do {
        } on fail error err {
        }
    }
}
