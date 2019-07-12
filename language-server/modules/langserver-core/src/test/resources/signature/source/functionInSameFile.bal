function testSignatureHelp () {
    getGreeting(
}

# Returns a sample greeting with the given content
#
# + year - Year to print 
# + message - Greeting message to print 
# + return - Return the combined greeting
function getGreeting (int year, string message) returns string {
    return message + year;
}