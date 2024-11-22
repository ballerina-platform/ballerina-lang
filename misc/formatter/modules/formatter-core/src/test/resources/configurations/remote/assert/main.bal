type Item record {
    string name;
    string status;
    int price;
};

function checkOrderStatus(Item[] items,
                         string status) returns boolean {
    foreach Item item in items {
        if item.status != status { return false; }
    }
    return true;
}

public function main() {
    Item[] items = [
        { name: "Laptop", status: "A", price: 1000 },
        { name: "Smartphone", status: "NA", price: 500 },
        { name: "Tablet", status: "A", price: 300 },
        { name: "Headphones", status: "NA", price: 100 }
    ];

    Item[] affordableProducts = from Item item in items
                                where item.price < 500
                                select item;

}
