import lstest/package_comp_plugin_with_completions as foo;

public listener listener1 = new foo:Listener(9090);

service on listener1 {
    r
}

service on listener1 {

    r

    resource function get foo() returns string {
        return "foo";
    }
}
