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
    # + user - User account
    # + message - Message to send
    # + return -  Message status or an error
    @display {label: "Send Message"}
    remote isolated function sendMessage(User user, @display {label: "Message"} string message) returns string|error? {
        return "success";
    }

    # Test get message.
    #
    # + uid - User id
    # + name - User's name
    # + age - User's age
    # + return - Test message or an error
    remote isolated function viewMessage(string uid, string name="john", int? age=20)  returns string|error? {
        return "Test Message";
    }

    remote isolated function DraftFunc(string uid)  returns string{
        return "Draft function without doc comment";
    }
}

# User account.
# + uid - Unique identifier of the account
# + name - The name of the account
# + address - User address
# + parent - Parent info
@display{label: "User Account"} 
public type User record {
    string uid = "1";
    string name;

    record {|
        string city;
        string country;
    |} address;

    record {|
        User father;
        User mother;
    |} parent;
};