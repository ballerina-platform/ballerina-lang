int[] data = [1, -3, 5, -30, 4, 11, -25, 10];

function testIntArrayWithArityOne() returns string {
    string dataStr = "";
    int negativeCount = 0;
    foreach var i in data {
        if(i > 0) {
            dataStr = dataStr + " (Positive:" + i.toString() + "),";
        } else {
             dataStr += " (Negative:" + i.toString() + ")";
             negativeCount += 1;
             if(negativeCount > 2) {
                 error err = error("Throttle reached");
                  fail err;
             }
             dataStr += " within grace,";
        }
    } on fail error e {
        dataStr += " " + e.message();
    }
    return dataStr;
}

function testForeachStmtWithCheck() returns string {
    string str = "";

     foreach var i in data {
         str = str.concat("Value: ", i.toString(), " ");
         if(i < 0) {
             int val = check getError();
             str = str.concat(" Value: ", val.toString());
         }
     } on fail error e {
         str += "-> error caught. Hence value returning";
         return str;
     }
     str += "-> reached end";
     return str;
}

function getError()  returns int|error {
    error err = error("Custom Error");
    return err;
}

function testNestedWhileStmtWithFail() returns string {
    int[] data = [1];
    string result = "";
    error err = error("Custom Error");
    foreach var i in data {
        foreach var j in data {
            foreach var k in data {
                result = result + "level3";
                fail err;
            } on fail error e3 {
                result = result + "-> error caught at level 3, ";
            }
            result = result + "level2";
            fail err;
        } on fail error e2 {
            result = result + "-> error caught at level 2, ";
        }
        result = result + "level1";
        fail err;
    } on fail error e1 {
         result = result + "-> error caught at level 1.";
    }
    return result;
}

function testNestedForeachLoopBreak() returns string {
    int[] data1 = [1, 2, 3];
    int[] data2 = [1];
    string result = "";
    error err = error("Custom Error");
    foreach var i in data1 {
        foreach var j in data2 {
            foreach var k in data2 {
                result = result + "level3";
                fail err;
            } on fail error e3 {
                result = result + "-> error caught at level 3, ";
            }
            result = result + "level2";
            fail err;
        } on fail error e2 {
            result = result + "-> error caught at level 2, ";
        }
        result = result + "level1";
        fail err;
    } on fail error e1 {
         result = result + "-> error caught at level 1.";
    }
    return result;
}
