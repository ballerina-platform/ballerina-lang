import unit_tests/proj6.a as a;
import unit_tests/proj6.b as b;

function init() {
   a:incrementCount();
   a:assertCount(3);
}

public function main() {
    b:sample();
   a:incrementCount();
   a:assertCount(4);
}

listener a:ABC ep = new a:ABC("ModC");
