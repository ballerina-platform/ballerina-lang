type Person record {
    string name = "";
    int age = 0;
    Person | () parent = ();
    json info = null;
    map<any> address = {};
    int[] marks = [];
};

function testMultiValuedStructInlineInit () returns (Person) {
    Person p1 = {name:"aaa", age:25,
                    parent:{name:"bbb", age:50},
                    address:{"city":"Colombo", "country":"SriLanka"},
                    info:{status:"single"}
                };
    return p1;
}

function testAccessJsonInStruct () returns [string, string, string]| error {
    Person p1 = {name:"aaa",
                    age:25,
                    parent:{name:"bbb",
                               age:50,
                               address:{"city":"Colombo", "country":"SriLanka"},
                               info:{status:"married"}
                           },
                    info:{status:"single"}
                };
    string statusKey = "status";
    string status1 = "";
    string status2 = "";
    string status3 = "";

    var result1 = p1.parent;
    if (result1 is Person) {
        status1 = check result1.info.status;
    }

    var result2 = p1["parent"];
    if (result2 is Person) {
        status2 = check result2["info"].status;
        status3 = check result2.info.status;
    }

    return [status1, status2, status3];
}

function testAccessMapInStruct () returns [any, any, any, string] {
    Person p1 = {name:"aaa",
                    age:25,
                    parent:{name:"bbb",
                               age:50,
                               address:{"city":"Colombo", "country":"SriLanka"},
                               info:{status:"married"}
                           },
                    info:{status:"single"}
                };
    string cityKey = "city";
    string city = "";

    var result = p1["parent"];
    if (result is Person) {
        city = checkpanic result.address[cityKey].ensureType();
        return [result.address["city"], result["address"]["city"], result.address["city"], city];
    } else {
        return [(), (), (), city];
    }
}

function testSetValueToJsonInStruct () returns (json) {
    Person p1 = {name:"aaa",
                    age:25,
                    parent:{name:"bbb",
                               age:50,
                               address:{"city":"Colombo", "country":"SriLanka"},
                               info:{status:"married"}
                           },
                    info:{status:"single"}
                };

    var result = p1.parent;
    if (result is Person) {
         map<json> info = <map<json>> result.info;
         info["status"] = "widowed";
         info["retired"] = true;
         return result.info;
    } else {
         return null;
    }
}

function testAccessArrayInStruct () returns [int, int] {
    Person p1 = {marks:[87, 94, 72]};
    string statusKey = "status";
    return [p1.marks[1], p1["marks"][2]];
}
