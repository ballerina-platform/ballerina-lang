import foo/package_with_warning;
import test_warning.modWithWarning;

public function main() {
    package_with_warning:hello();
    modWithWarning:helloInternal();
}
