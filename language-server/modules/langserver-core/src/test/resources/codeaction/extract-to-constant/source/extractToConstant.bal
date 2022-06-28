function testFunction() {
    int intLiteral = 10;
    int hexIntLiteral = 0x12;
    float floatingPointLiteral = 3.14;
    float hexFloatingPointLiteral = 0xAp1;
    boolean booleanLiteral = true;
    string stringLiteral = "abc";
}

function getArea(int r) returns float {
	return 3.14 * r * r;
}

class Square {
	function getArea() {
	    int r = 3;
	    float area = 3.14 * r * r;
	}
}
