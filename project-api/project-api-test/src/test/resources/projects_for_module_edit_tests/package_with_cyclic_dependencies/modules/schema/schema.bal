import myproject.util;
import myproject.storage; // this is to produce a cyclic dependency with the schema module

function run() {
    storage:initDatabase();
}

public type Employee record {|
    util:PersonalDetails personalDetails?;
    string designation?;
|};
