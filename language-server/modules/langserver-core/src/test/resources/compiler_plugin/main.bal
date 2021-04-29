import lstest/package_comp_plugin_with_codeactions as foo;
import ballerina/module1;

public function doNothing() {
    // do nothing
    foo:doSomething();
}

service / on module1:listener1  {
    
}