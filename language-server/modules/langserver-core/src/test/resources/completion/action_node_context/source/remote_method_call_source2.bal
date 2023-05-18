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

    remote function countByNameOrId(string name, int id, int[] ids) returns int {
        return 1;
    }

}

public function testArgContext() {
    ProductClient prodClient = check new("http://example,com");
    int count = prodClient -> countByNameOrId(ids = [],);
}
