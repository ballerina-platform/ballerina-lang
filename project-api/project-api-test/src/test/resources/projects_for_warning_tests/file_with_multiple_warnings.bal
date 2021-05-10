import foo/package_with_warning;

@deprecated
public function helloInternal() {
    string s = "foo";
}

public function main() {
    package_with_warning:hello();
    helloInternal();
}
