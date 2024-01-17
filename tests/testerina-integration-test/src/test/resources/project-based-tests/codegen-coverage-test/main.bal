import samjs/package_comp_plugin_code_modify_add_function as _;

public function main() {
    _ = intAdd(1,2);
}

function intAdd(int a, int b) returns (int) {
    return a + b;
}
