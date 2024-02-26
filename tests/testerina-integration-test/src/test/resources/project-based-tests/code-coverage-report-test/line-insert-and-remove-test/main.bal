import samjs/package_comp_plugin_code_modify_add_remove_function as _;

public function main() {
    _ = intAdd(1,2);
}

function baz () {

}

function qux () {


}

function intAdd(int a, int b) returns (int) {
    return a + b;
}
