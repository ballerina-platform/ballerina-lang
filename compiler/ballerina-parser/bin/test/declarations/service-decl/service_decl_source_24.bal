service / on new http:Listener(8080) {
    resource function get limit/[int id]() {
    }

    resource function get /limit/[int id]() {
    }

    resource function get foo/limit/[int id]() {
    }
}
