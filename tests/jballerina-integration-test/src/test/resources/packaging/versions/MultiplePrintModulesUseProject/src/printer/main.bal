import bcintegrationtest/print version 1.0.0 as p1;
import bcintegrationtest/print version 2.0.0 as p2;

public function getString1() returns string {
    return p1:printText("Hello World!");
}

public function getString2() returns string {
    return p2:printText("Hello World!");
}
