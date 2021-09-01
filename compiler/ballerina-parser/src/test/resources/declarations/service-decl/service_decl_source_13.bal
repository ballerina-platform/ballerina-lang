
service someListner2 on listener {
}

service someListner2 on listener {
    resource function get greeting() returns string {
        return "Hello, World!";
    }
}

service someListner2 on int, ppp {
}
