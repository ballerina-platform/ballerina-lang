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

public function main() returns error? { 
    ProductClient prodClient = check new("http://example,com");
    string v = prodClient->listAll();
}
