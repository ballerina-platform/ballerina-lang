public function testListBindingPatternWithInvalidMember() {
    Arr [x, a:b] = [];
    Arr [a:b, y] = [];
    Arr2 [a:b, z, a:c] = [];
    Arr2 [a:b, a:b, a:c] = [];
}
