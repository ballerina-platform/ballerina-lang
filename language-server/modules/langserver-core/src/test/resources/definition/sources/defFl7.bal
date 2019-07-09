import ballerina/runtime;

int[] data = [1, -3, 5, -30, 4, 11, 25, 10];
int sum = 0;

function add(int i) {
    sum = sum + i;
}

function testForeachFunc1() returns int {
    sum = 0;
    // cover goto def of data
    foreach var itr in data {
        // cover goto def of add in the sense statement visit within the foreach body
        // cover goto def of variable itr
        add(itr);
    }
    return sum;
}

function testNestedWithBreakContinue() {
    string output = "";
    string[] sArray = ["d0", "d1", "d2", "d3"];
    int rangeEnd = 0;
    int rangeStart = 1;
    foreach var outerV in sArray {
        // goto def of range variables
        foreach var j in rangeStart ... rangeEnd {
            if (j == 4) {
                // nested foreach access parent foreach's variable
                output = outerV;
                break;
            } else if (j == 2) {
                continue;
            }
        }
    }
}
