import reftest/module2;

public function testFunctionInDifferentFile() {
    functionInDifferentFile();
    int testOther = module2:functionInOtherPkg();
}
