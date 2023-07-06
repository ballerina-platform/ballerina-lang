function testSignatureHelp () {
    boolean bool = true;
    getGreeting(getMessage());
    int foo = 10;
}

# Returns a sample greeting with the given content
#
# + year - Year to print 
# + message - Greeting message to print 
# + return - Return the combined greeting
function getGreeting(int year, string message) returns string {
    return message + year;
}

 # Returns a sample greeting message
 #
 # + name - Name
 # + return - Return message
function getMessage(string name) returns string {
    return "Hi" + name;
}
