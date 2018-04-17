import ballerina/io;

@Description {value:"Workers in Ballerina allow users to delegate tasks to a new worker thread."}
function main (string... args) {
    worker w1 {
        int iw = 200;
        float kw = 5.44;
        io:println("[w1] iw: " + iw + " kw: " + kw);
    }
}
