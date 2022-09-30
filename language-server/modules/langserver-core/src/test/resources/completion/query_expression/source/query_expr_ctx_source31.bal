Customer[] customerList = [];

function testInClauseSuggestion() {
    Customer[] customers = [];
    string customerStr = "Customer 1";
    map<string> customerMap = {};
    string[] customerStrArray = [];
    int i = 0;

    Customer[] filteredCustomers = from  in customerList
}

function getCustomers() returns Customer[] {
    return [];
}

type Customer record {|
    readonly int id;
    readonly string name;
    int noOfItems;
|};
