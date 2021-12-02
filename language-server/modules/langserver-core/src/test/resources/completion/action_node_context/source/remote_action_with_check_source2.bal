public type Product record {
    string id?;
    string name;
    string description?;
};

public type ProductError distinct error;

client class ProductClient {

    private string url;

    public function init(string url) returns error? {
        self.url = url;
    }

    remote function findByName(string name) returns Product|ProductError {
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

    # Count the products by given criteria.
    # Criteria can be optional
    #
    # + name - Name to search
    # + id - id to be searched
    # + page - Page number  
    # + offset - Offset number
    # + return - Count 
    remote function countBy(string name, string id, int page, int offset) returns int {
        return 1;
    }

    remote function geVersion() returns string {
        return "v1.0.0";
    }
}

public function main() returns error? {
    ProductClient prodClient = check new ("http://example,com");
    Product prod = prodClient->;

    int val = 10;
}
