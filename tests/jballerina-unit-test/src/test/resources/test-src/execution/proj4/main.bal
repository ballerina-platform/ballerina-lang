import unit_tests/proj4.a as a;
import unit_tests/proj4.b as b;

function init() {
	a:println("Initializing module c");
}

public function main() {
    b:sample();
    a:println("Module c main function invoked");
}

listener a:ABC ep = new a:ABC("ModC");
