import object_mocking.TestHttpClient;

TestHttpClient:AttributeDAO mockAttributeDAO = {
    id: "aaa-bbb-ccc",
    name: "mock organization name",
    description: "This is mock organization",
    created_at: 1627639797657
};

function returnMockedAttributeDAOStream() returns stream<TestHttpClient:AttributeDAO, TestHttpClient:Error> {
    stream<TestHttpClient:AttributeDAO, TestHttpClient:Error> attributeDAOStream = new (new AttributeDAOStreamImplementor());
    return attributeDAOStream;
}

function returnDAOStreamWithGenericError() returns stream<TestHttpClient:AttributeDAO, error> {
    stream<TestHttpClient:AttributeDAO, error> attributeDAOStream = new (new AttributeDAOStreamImplementor());
    return attributeDAOStream;
}

class AttributeDAOStreamImplementor {
    private int index = 0;
    private TestHttpClient:AttributeDAO[] currentEntries = [
        {
            id: "aaa-bbb-ccc",
            name: "mock organization name",
            description: "This is mock organization",
            created_at: 1627639797657
        }
    ];

    isolated function init() {
    }

    public isolated function next() returns record {|TestHttpClient:AttributeDAO value;|}|TestHttpClient:Error {
        if (self.index < self.currentEntries.length()) {
            record {|TestHttpClient:AttributeDAO value;|} attributeDAO = {value: self.currentEntries[self.index]};
            self.index += 1;
            return attributeDAO;
        }
        return error("this is error");
    }
}

class InvalidAttributeDAOStreamImplementor {
    private int index = 0;
    private TestHttpClient:AttributeDAO[] currentEntries = [
        {
            id: "aaa-bbb-ccc",
            name: "mock organization name",
            description: "This is mock organization",
            created_at: 1627639797657
        }
    ];

    isolated function init() {
    }

    public isolated function next() returns record {|TestHttpClient:AttributeDAO value;|}|error {
        if (self.index < self.currentEntries.length()) {
            record {|TestHttpClient:AttributeDAO value;|} attributeDAO = {value: self.currentEntries[self.index]};
            self.index += 1;
            return attributeDAO;
        }
        return error("this is error");
    }
}
