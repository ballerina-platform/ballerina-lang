public function testMiddle() {
        int adult = 20;
        int age = 30; 
        int i = age < 18 ? 2.5 : adult;
}

public function testEnd() {
        int adult = 20;
        int age = 30; 
        int i = age < 18 ? adult : 2.5;
}

public function testLHS() {
        int adult = 20;
        int age = 30; 
        string | boolean condition = true;
        int i = condition ? adult : 2.5;
}
