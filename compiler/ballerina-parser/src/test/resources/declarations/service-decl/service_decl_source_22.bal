service / on new http:Listener(8080) {
    // inserting "isolated"
    resource iso function get getResource(http:Caller caller, http:Request req) returns string {
        lock {
            count += 1;
        }
        return "hello world";
    }

    // inserting "isolated"
iso resource function get getResource(http:Caller caller, http:Request req) returns string {
        lock {
            count += 1;
        }
        return "hello world";
    }
    
    // inserting "resource"
res function get getResource(http:Caller caller, http:Request req) returns string {
        lock {
            count += 1;
        }
        return "hello world";
    }

    // inserting "remote"
rem function getResource(http:Caller caller, http:Request req) returns string {
        lock {
            count += 1;
        }
        return "hello world";
    }
}
