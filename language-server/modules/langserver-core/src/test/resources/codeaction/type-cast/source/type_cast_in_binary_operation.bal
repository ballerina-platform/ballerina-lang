float f1 = 10.1;
decimal d1 = 10.1;
int i1 = 10;

float f2 = f1 / d1;
decimal d2 = f1 / d1;
int i2 = f1 / d1;
int i3 = i1 / d1;
decimal d3 = i1 / d1;

function testTypeCastInBinaryOp() {
    int a = i1 / f1;
}

function testFunction(float x) returns float =>
    let decimal x1 = f1 / d1;