
public type Cart record {|
    string user_id = "";
    CartItem[] items = [];
|};

public type AddItemRequest record {|
    string user_id = "";
    CartItem item = {};
|};
 
public type CartItem record {|
    string product_id = "";
    int quantity = 0;
|};

const string ROOT_DESCRIPTOR_DEMO = "<roo-t-descriptor-demo>";

public isolated function getDescriptorMapDemo() returns map<string> {
    return {"demo.proto": "<demo.proto>"};
}

public type Empty record {|
|};

public type GetCartRequest record {|
    string user_id = "";
|};

public type EmptyCartRequest record {|
    string user_id = "";
|};
