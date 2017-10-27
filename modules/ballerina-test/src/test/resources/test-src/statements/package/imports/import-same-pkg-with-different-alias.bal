import ballerina.lang.time;
import ballerina.lang.time as x;
import ballerina.lang.time as y;

function testFunc() {
    _ = time:currentTime();
    _ = x:currentTime();
    _ = y:currentTime();
}
