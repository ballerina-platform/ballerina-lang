import ballerina/httpx;
import ballerina/udpx;
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

service /abc/users on new httpx:Listener(8080) {

    resource function post .(@httpx:Payload json payload) returns httpx:Response {
        return payload;
    }
    
    resource function post [string id](@httpx:Payload json payload) returns httpx:Response {
        return payload;
    }
    
    resource function post register/[string id](@httpx:Payload json payload) returns httpx:Response {
        return payload;
    }

    resource function get user/[string id]() returns httpx:Response {
        return users[id]
    }
}

service /abc/items on new httpx:Listener(8090) {
    resource function post add(@httpx:Payload json payload) returns httpx:Response {
        return payload;
    }

    resource function get item[string... props](@httpx:Payload json payload) returns httpx:Response {
        return payload;
    }

    resource function () {
        
    }
}


service on new udpx:Listener(8080) {
    remote function onDatagram(readonly & udpx:Datagram datagram) {       
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

public type MyType record {|
    int arg1;
    string arg2;
|};

public type ObjectType object {
    public function attach();
};

type OtherType string;

public MyType modVar1 = {arg1:10, arg2:""};

public int _ = 10;

public const string myConst = "CONSTANT";

public enum KIND {
    KIND1,
    KIND2
}

string ballerina = "http://ballerina.io";

xmlns ballerina as bal;

xmlns "http://ballerina.io";

public listener http:ListenerType myListener = new(8080);

type AnnotationType1 record {|
    string prop1;
    string prop2;
    string ...;
|};

type AnnotationType2 record {|
    boolean prop1;
|};

public const annotation AnnotationType1 MyAnnotation1 on function;

public const annotation AnnotationType2 MyAnnotation2 on service;

type MyObjectType object {
    public function function1();
    public function function2();
}

