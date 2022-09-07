class TestPositionalArg {

    private string str1;
    private xml xml1;
    
    public function init(string str1, xml xml1) {
        self.str1 = str1;
        self.xml1 = xml1;
    }
    
    public function setString1(string str1) {
        self.str1 = str1;
    }

    public function setXml1(xml xml1) {
        self.xml1 = xml1;
    }
    
}

function myFunction(string expr) {

}

function testPositionalArgs() {
    
    myFunction(`sample test`);
    
    TestPositionalArg explicitNewExpr = new TestPositionalArg(`sample string`, `sample xml`);
    
    TestPositionalArg implicitNewExpr = new (`sample string`, `sample xml`);
    
    explicitNewExpr.setString1(`sample str`);

}
