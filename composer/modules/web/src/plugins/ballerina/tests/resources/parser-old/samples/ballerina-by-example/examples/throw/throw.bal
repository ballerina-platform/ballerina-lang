import ballerina/lang.errors;
import ballerina/lang.system;

connector XManagementConnector () {

    map data = {};

    action addRecord (XManagementConnector conn, string name, int amount) {
        if (data == null) {
            // Program can't add record, construct an error and throw it to the caller.
            errors:Error err = {msg:"Internal error, database not found."};
            throw err;
        }
        data[name] = amount;
        system:println("Record added for " + name);
    }

    action delete (XManagementConnector conn, string name) {
        system:println("deleteing record for " + name);
        //Executing Malicious code.
        data = null;
    }

}

@doc:Description {value:"Here's how you can throw an error. Next example shows you how to catch thrown errors."}
function toUpperCase (string value) (string) {
    if (value == "") {
        errors:Error err =
        {msg:"Value the arg 'value' is invalid"};
        throw err;
    } else {
        return strings:toUpperCase(value);
    }
}


function main (string... args) {
    XManagementConnector conn = create XManagementConnector();
    conn.addRecord("tom", 100);
    conn.delete("tom");
    conn.addRecord("bob", 100);
    system:println("Data adding done.");
}