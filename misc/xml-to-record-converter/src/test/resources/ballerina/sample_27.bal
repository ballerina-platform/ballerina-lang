type Customer record {
    string firstName;
    string lastName;
    string email;
    int id;
    @xmldata:Attribute
    string loyalty;
    @xmldata:Attribute
    string optedInNewsLetter;
    @xmldata:Attribute
    string 'xmlns;
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
    @xmldata:Attribute
    string 'xmlns;
};

type Order record {
    int id;
    Customer customer;
    Items items;
    decimal total;
    @xmldata:Attribute
    string 'xmlns;
};

type Orders record {
    Order[] 'order;
    @xmldata:Attribute
    string 'xmlns;
};
