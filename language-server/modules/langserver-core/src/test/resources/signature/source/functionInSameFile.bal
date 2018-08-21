function testSignatureHelp () {
    getGreeting(
}

documentation {
	Returns a sample greeting with the given content
	P{{year}}       Year to print
	P{{message}}    Greeting message to print
    R{{}}           Return the combined greeting
}
function getGreeting (int year, string message) returns string {
    return message + year;
}