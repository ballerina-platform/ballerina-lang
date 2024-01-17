import samjs/package_comp_plugin_with_func_node_analyzer as bar;
import samjs/package_comp_plugin_with_two_java_dependencies as _;
import samjs/package_comp_plugin_with_one_java_dependency as _;

public function main() {
   bar:doSomething();
   bar:doSomething();
   int _ = 5;
   string _ = foo();
   boolean _ = true;
}

int a = 5;
string k = foo();

function foo() returns string {
    return "ss";
}
