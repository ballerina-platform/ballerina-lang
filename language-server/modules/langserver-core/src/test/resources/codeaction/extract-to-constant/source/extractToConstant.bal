function testFunction() {
    int intLiteral = 10;
    int hexIntLiteral = 0x12;
    float floatingPointLiteral = 3.14;
    float hexFloatingPointLiteral = 0xAp1;
    boolean booleanLiteral = true;
    string stringLiteral = "abc";
    int intExpr = 10 + 20;
}

function getPI() returns float {
	return 3.14;
}

class Square {
	function getArea() returns float {
	    float area = 3.14 * 3 * 3;
	    return area;
	}
}

const int CONST1 = 10 + 20;

int unaryNumericExpr = -100;
boolean unaryLogicalExpr = !true;

string str = "abc"
           + "def";
