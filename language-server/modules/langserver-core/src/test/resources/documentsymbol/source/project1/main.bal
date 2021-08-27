import ballerina/http;
import ballerina/udp;
import project1.types;

# Description  
public class User {
    *types:User;
    string email;
    string userName;
    types:Address address;

    # Init method
    #
    # + userName - username   
    # + email - email  
    # + address - address
    public function init(string userName, string email, types:Address address) {
        self.userName =  userName;
        self.address = address;
        self.email = email;
    }

    # Returns the address of the user.
    # + return - address.
    public function getAddress() returns types:Address {
        return self.address;
    }

    public function getEmail() returns string {
        return self.email;
    }

    public function getType() returns types:UserType {
        return types:SELLER;
    }

    public function getUserName() returns string {
        return self.userName;
    }
}


# Description  
public class Item {
    string code;

    # Init method
    #
    # + code - Item code.
    public function init(string code) {
        self.code = code;
    }

    # Returns the code of the item.
    # + return - address.
    public function getCode() returns string {
        return self.code;
    }
}

public function main() {

    User user1 = new("jhond","jhond@abc.com",{
        addressLine1: "No:405",
        addressLine2: "First avenue",
        postalCode: 23098,
        state: types:EAST
    });

    User user2 = new("janed","janed@abc.com",{
        addressLine1: "No:406",
        addressLine2: "Second avenue",
        postalCode: 23000,
        state: types:NORTH
    });
}

service /abc/users on new http:Listener(8080) {

    resource function post .(@http:Payload json payload) returns http:Response {
        return payload;
    }
    
    resource function post [string id](@http:Payload json payload) returns http:Response {
        return payload;
    }
    
    resource function post register/[string id](@http:Payload json payload) returns http:Response {
        return payload;
    }

    resource function get user/[string id]() returns http:Response {
        return users[id]
    }
}

service /abc/items on new http:Listener(8090) {
    resource function post add(@http:Payload json payload) returns http:Response {
        return payload;
    }

    resource function get item[string... props](@http:Payload json payload) returns http:Response {
        return payload;
    }

    resource function () {
        
    }
}


service on new udp:Listener(8080) {
    remote function onDatagram(readonly & udp:Datagram datagram) {       
        string|error data = string:fromBytes(datagram.data);
        return datagram;
    }

    function processDataAndGetToken(string data) returns string{
        return "token";
    }
}


# Another function.
# # Deprecated
# This is a deprecated function.
@deprecated 
public function deprecatedFunction() {
    
}
