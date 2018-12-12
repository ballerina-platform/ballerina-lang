import ballerina/io;
public function main(string... args) {
    int a = 10;

    transaction {
        string testString = "Hello World!!";
        a = 1000;
    } onretry {
        io:println("Within On-Retry");
    } committed {
        s += " committed";
         
    } aborted {
        s += " aborted";
         
    }
}
