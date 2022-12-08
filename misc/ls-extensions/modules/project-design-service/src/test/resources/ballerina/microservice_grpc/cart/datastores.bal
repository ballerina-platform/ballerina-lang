import ballerinax/redis;

public type DataStore object {
    isolated function add(string userId, string productId, int quantity) returns error?;

    isolated function emptyCart(string userId) returns error?;

    isolated function getCart(string userId) returns Cart|error;
};

# Description
public isolated class InMemoryStore {
    *DataStore;
    private map<Cart> store = {};

    isolated function add(string userId, string productId, int quantity) {
        lock {
            if self.store.hasKey(userId) {
                Cart existingCart = self.store.get(userId);
                CartItem[] existingItems = existingCart.items;
                CartItem[] matchedItem = from CartItem item in existingItems
                    where item.product_id == productId
                    limit 1
                    select item;
                if matchedItem.length() == 1 {
                    CartItem item = matchedItem[0];
                    item.quantity = item.quantity + quantity;
                } else {
                    CartItem newItem = {product_id: productId, quantity: quantity};
                    existingItems.push(newItem);
                }
            } else {
                Cart newItem = {
                    user_id: userId,
                    items: [{product_id: productId, quantity: quantity}]
                };
                self.store[userId] = newItem;
            }
        }
    }

    isolated function emptyCart(string userId) {
        lock {
            _ = self.store.remove(userId);
        }
    }

    isolated function getCart(string userId) returns Cart {
        lock {
            if self.store.hasKey(userId) {
                return self.store.get(userId).cloneReadOnly();
            }
            Cart newItem = {
                user_id: userId,
                items: []
            };
            self.store[userId] = newItem;
            return newItem.cloneReadOnly();
        }
    }
}

public isolated class RedisStore {
    *DataStore;
    private final redis:Client redisClient;

    function init() returns error? {
        self.redisClient = check new ({
            host: redisHost,
            password: redisPassword
        });
    }

    isolated function add(string userId, string productId, int quantity) returns error? {
        map<any> existingItems = check self.redisClient->hMGet(userId, [productId]);
        if existingItems.get(productId) == null {
            map<int> itemsMap = {productId: quantity};
            _ = check self.redisClient->hMSet(userId, itemsMap);
        } else {
            int existingQuantity = check int:fromString(existingItems.get(productId).toString());
            existingItems[productId] = existingQuantity + quantity;
            _ = check self.redisClient->hMSet(userId, existingItems);
        }
    }

    isolated function emptyCart(string userId) returns error? {
        _ = check self.redisClient->del([userId]);
    }

    isolated function getCart(string userId) returns Cart|error {
        map<any> userItems = check self.redisClient->hGetAll(userId);

        CartItem[] items = [];
        foreach [string, any] [productId, quantity] in userItems.entries() {
            CartItem item = {
                product_id: productId,
                quantity: check int:fromString(quantity.toString())
            };
            items.push(item);
        }
        return {user_id: userId, items: items};
    }
}
