import ballerina/test;
import ballerina/http;

http:Client clientEndpoint = new("http://postman-echo.com");

class Person {
    public int age;
    public string firstName;
    public string lastName;

    function init(int age, string firstName, string lastName) {
        self.age = age;
        self.firstName = firstName;
        self.lastName = lastName;
    }

    function getFullName() returns string {
        return self.firstName + " " + self.lastName;
    }

}

function performGet() returns http:Response {
    http:Response|error result = clientEndpoint->get("/headers");
    http:Response response = <http:Response>result;
    return response;
}

public client class MockHttpClient {
    public string url = "";
    public remote function get(@untainted string path,
        public http:RequestMessage message = ()) returns
            http:Response|http:ClientError {

        http:Response res = new;
        res.statusCode = 500;
        return res;
    }
}

public function main() {
    Person v01_person = new Person(5, "John", "Doe");
    string v02_fullName = v01_person.getFullName();

    clientEndpoint = test:mock(http:Client, new MockHttpClient());
    http:Response v03_res = performGet();
    int v04_statusCode = v03_res.statusCode;
}
