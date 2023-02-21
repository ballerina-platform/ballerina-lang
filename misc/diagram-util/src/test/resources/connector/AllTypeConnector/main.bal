# All types Connector documentation
# This connector will cover (basic) structured type kinds like,
# array, tuple, map, record, inline record, closed record
#
# + credentials - Connector credentials
# + user - Test user info
@display {label: "All Type Connector"}
public client class Client {
    public Auth credentials;
    public User user;

    public isolated function init(Auth credentials, User user) returns error? {
        self.credentials = credentials;
        self.user = user;
    }

    # Get test message array.
    # without any display annotations
    #
    # + return - Test message array
    remote isolated function getMessages() returns string[] {
        return ["msg1", "msg2", "msg3"];
    }

    # Read test message.
    # with function level display annotation
    # this function will cover array, tuple input types
    #
    # + ids - Id list to read messages
    # + token - Credentials
    # + return - Tuple with message id and message or an error
    @display {label: "Read Message"}
    remote isolated function readMessage(int[] ids, [int, string] token = [0, ""]) returns [int, string]|error {
        return [10, "No message"];
    }

    # Send messages
    # with function level and parameter level display annotations
    # this will cover record and map input types
    #
    # + msgList - Message list with id and message
    # + auth - Credentials to authenticate broker
    # + return - Error or Map with message status
    @display {label: "Send Message"}
    remote isolated function sendMessage(
            @display {label: "Message List"} map<string> msgList,
            @display {label: "Credentials"} Auth auth) returns map<boolean|error>|error {

        map<boolean|error> m = {
            "x": true,
            "y": false,
            "z": error("Error")
        };
        return m;
    }

    # View messages
    #
    # + msg - Parameter Description
    # + return - retunrn message map or nill
    remote isolated function viewMessage(table<Message> msg) returns map<string>|() {
        return ();
    }

    # Update message with inline closed record parameter
    #
    # + path - Parameter Description  
    # + id - Message id  
    # + message - Message contents  
    # + color - Parameter Description
    # + return - Updated message
    resource isolated function get [string... path](int id,
            record {|string body; User receiver; User sender?;|} message, Color color) returns Message|error {
        Message newMsg = {
            body: message.body,
            sender: {
                id: 0,
                name: ""
            },
            receiver: message.receiver
        };

        return newMsg;
    }

    # Forward messages
    # Remote function with optional error return type
    #
    # + user - User data to forward messages
    # + message - Message info
    # + return - retunrn message map or nill
    resource isolated function forward .(User user, *Message message) returns Message|error? {
        Message newMsg = {
            body: message.body,
            sender: {
                id: 0,
                name: ""
            },
            receiver: message.receiver
        };

        return newMsg;
    }

    # Delete messages
    # Isolated function without remote access modifier
    #
    # + name - User data to get messages
    # + return - retunrn message map or nill
    resource function delete users/[string name]() returns map<string>|() {
        return ();
    }

    # Get student list 
    # this function will handle stream return type
    #
    # + return - Student stream
    resource isolated function get .() returns stream<Student>|error {
        Student s1 = {id: 11, name: "George", score: 1.5};
        Student s2 = {id: 22, name: "Fonseka", score: 0.9};
        Student s3 = {id: 33, name: "David", score: 1.2};

        Student[] studentList = [s1, s2, s3];
        stream<Student> studentStream = studentList.toStream();
        return studentStream;
    }

    # Send message to user
    # this function will handle typedesc and union type
    #
    # + message - Message 
    # + receiver - Reciever  
    # + paylodType - Payload type
    # + return - Error or Map with message status
    resource isolated function get msg(string|xml|json message, UserObj receiver, TargetType paylodType) returns TargetType|error {
        return TargetType;
    }

    # Search messages
    #
    # + msg - Any content to search message  
    # + position - Position points (defaultable)
    # + return - retunrn message map or nill
    resource isolated function post msg/[string index](anydata msg, byte[] position = base16 `aeeecdefabcd12345567888822`) returns string|xml|json|() {
        return ();
    }

}

public class UserObj {

    private int n;

    public function init(int n = 0) {
        self.n = n;
    }

    public function inc() {
        self.n += 1;
    }

    public function get() returns int {
        return self.n;
    }
}

# Auth Type
#
# + username - Username
# + password - Password  
public type Auth record {
    string username;
    string password;
};

# User Type with inline address record type
#
# + id - User id  
# + name - Username  
# + owner - Owner of the user  
# + address - Address 
public type User record {
    int id;
    string name;
    User owner?;

    record {|
        string homeNo;
        string street?;
        string city;
        string postalcode;
    |} address?;
};

# Student type
# inclusion with User type
#
# + score - Field Description
public type Student record {
    *User;
    float score;
};

# Payload Type
public type PayloadType string|xml|json|map<string>|map<json>|byte[]|record {|anydata...;|}|record {|anydata...;|}[];

# Tartget Type
public type TargetType typedesc<PayloadType>;

# Message type
#
# + body - Mesage content 
# + receiver - Receiver
# + sender - Sender  
public type Message record {
    string body;
    User receiver;
    User sender?;
};

# Color type
# enum type template - ```enum {value1, value2, value3}```
# For more information, see [ValueRenderOption](https://developers.google.com/sheets/api/reference/rest/v4/ValueRenderOption)
# + RED - Red color
# + GREEN - Green color
# + BLUE - Blue color 
public enum Color {
    RED,
    GREEN,
    BLUE
}
