import ballerina/module1;

public type Product record {
    string id?;
    string name;
    string description?;
};

client class ProductClient {

    private string url;

    public function init(string url) returns error? {
        self.url = url;
    }

    remote function findByName(string name) returns Product {
        return {
            id: "123",
            name: "Test"
        };
    }

    remote function listAll() returns Product[] {
        return [];
    }

    remote function countByName(string name) returns int {
        return 1;
    }

    remote function getVersion() returns string {
        return "v1.0.0";
    }
}

public function getData(string firstName, string lastName, int page, int pageSize) returns error? {
    module1:Client modClient = new("http://example2.com");
    string name = firstName.concat(lastName);
    ProductClient prodClient = check new("http://example,com");
    int count = pr->countByName(name);
}
