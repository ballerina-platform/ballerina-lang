import ballerina.lang.errors;
import ballerina.lang.system;

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


function main (string[] args) {
    XManagementConnector conn = create XManagementConnector();
    conn.addRecord("tom", 100);
    conn.delete("tom");
    conn.addRecord("bob", 100);
    system:println("Data adding done.");
}