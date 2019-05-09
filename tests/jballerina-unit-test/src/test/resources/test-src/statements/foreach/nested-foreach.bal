int[] idata = [1, 2, 3];
string[] sdata = ["A", "B", "C"];
float[] fdata = [10.0, 11.0, 12.0];

string output = "";

function concatIntString(int i, string s) {
    output = output + i + ":" + s + " ";
}

function concatIntStringFloat(int i, string s, float f) {
    output = output + i + ":" + s + ":" + f + " ";
}

// ---------------------------------------------------------------------------------------------------------------------

function test2LevelNestedForeachWithoutType() returns string {
    output = "";
    foreach var i in idata {
        foreach var s in sdata {
            concatIntString(i, s);
        }
    }
    return output;
}

function test2LevelNestedForeachWithType() returns string {
    output = "";
    foreach int i in idata {
        foreach string s in sdata {
            concatIntString(i, s);
        }
    }
    return output;
}

// ---------------------------------------------------------------------------------------------------------------------

function test3LevelNestedForeachWithoutType() returns string {
    output = "";
    foreach var i in idata {
        foreach var s in sdata {
            foreach var f in fdata {
                concatIntStringFloat(i, s, f);
            }
        }
    }
    return output;
}

function test3LevelNestedForeachWithType() returns string {
    output = "";
    foreach int i in idata {
        foreach string s in sdata {
            foreach float f in fdata {
                concatIntStringFloat(i, s, f);
            }
        }
    }
    return output;
}
