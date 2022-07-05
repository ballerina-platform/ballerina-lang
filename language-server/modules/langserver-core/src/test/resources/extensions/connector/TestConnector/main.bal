# Test Client documentation
#
# + url - Test url to check documentation
@display {label: "Test Client", iconPath: "logo.svg"}
public client class TestClient {
    public string url;

    public isolated function init() returns error? {
        self.url = "http://example.com/";
    }

    # Test get message.
    #
    # + return - Test message or an error
    @display {label: "Get Message"}
    remote isolated function getMessage()  returns string|error? {
        return "Test Message";
    }

    # Test send message.
    #
    # + message - Message to send
    # + return -  Message status or an error
    @display {label: "Send Message"}
    remote isolated function sendMessage(@display {label: "Message"} string message) returns string|error? {
        return "success";
    }
}
