import ballerina/module1;
import projectls.lsmod3 as mod3;

public function typeTest() {
    module1:TestMap2 | module1:TestMap3 | int | mod3:PositionRec myVar = 10;
    if myVar is 
}
