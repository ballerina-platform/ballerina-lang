import unit_tests/proj4.a as a;


function init() {
	a:println("Initializing module b");
	error sampleErr = error("panicked while initializing module B");
	panic sampleErr;
}

public function sample() {

}

listener a:ABC ep = new a:ABC("ModB");
