type Customer record {
    string firstName;
    string lastName;
    string email;
    int id;
    @xmldata:Attribute
    string loyalty;
    @xmldata:Attribute
    string optedInNewsLetter;
};

type Item record {
    string name;
    int quantity;
    decimal price;
    @xmldata:Attribute
    string id;
};

type Items record {
    Item[] item;
};

type Order record {
    int id;
    Customer customer;
    Items items;
    decimal total;
};

type Orders record {
    Order[] 'order;
};
