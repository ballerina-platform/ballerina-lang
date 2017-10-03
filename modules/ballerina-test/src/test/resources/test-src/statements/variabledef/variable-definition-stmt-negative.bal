function testDuplicateVariables (string[] i) {

    // Following line is invalid.
    boolean b;
    float b;

    return;
    }

    function testUndeclaredVariables () (int) {
        return a;
    }

    function testUnsupportedTypeVariable (string[] i) {
        Foo bar;
    }

    const int b = 10;
    const float b = 10;

    function testDuplicateConstantVariables (string[] i) {
        return;
    }

