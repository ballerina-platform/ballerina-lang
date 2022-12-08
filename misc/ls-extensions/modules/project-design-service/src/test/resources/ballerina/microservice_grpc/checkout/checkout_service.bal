import ballerina/grpc;
import ballerina/uuid;
import ballerina/log;

listener grpc:Listener ep = new (9094);

configurable string cartHost = "localhost";
configurable string catalogHost = "localhost";
configurable string currencyHost = "localhost";
configurable string shippingHost = "localhost";
configurable string paymentHost = "localhost";
configurable string emailHost = "localhost";

@display {
    label: "",
    id: "checkout"
}
@grpc:ServiceDescriptor {descriptor: ROOT_DESCRIPTOR_DEMO, descMap: getDescriptorMapDemo()}
service "CheckoutService" on ep {
    @display {
        label: "",
        id: "cart"
    }
    final CartServiceClient cartClient;
    @display {
        label: "",
        id: "catalog"
    }
    final ProductCatalogServiceClient catalogClient;
    @display {
        label: "",
        id: "currency"
    }
    final CurrencyServiceClient currencyClient;
    @display {
        label: "",
        id: "shipping"
    }
    final ShippingServiceClient shippingClient;
    @display {
        label: "",
        id: "payment"
    }
    final PaymentServiceClient paymentClient;
    @display {
        label: "",
        id: "email"
    }
    final EmailServiceClient emailClient;

    function init() returns error? {
        self.cartClient = check new ("http://" + cartHost + ":9092");
        self.catalogClient = check new ("http://" + catalogHost + ":9091");
        self.currencyClient = check new ("http://" + currencyHost + ":9093");
        self.shippingClient = check new ("http://" + shippingHost + ":9095");
        self.paymentClient = check new ("http://" + paymentHost + ":9096");
        self.emailClient = check new ("http://" + emailHost + ":9097");
    }

    remote function PlaceOrder(PlaceOrderRequest value) returns PlaceOrderResponse|error {
        string orderId = uuid:createType1AsString();
        CartItem[] userCartItems = check self.getUserCart(value.user_id, value.user_currency);
        OrderItem[] orderItems = check self.prepOrderItems(userCartItems, value.user_currency);
        Money shippingPrice = check self.convertCurrency(check self.quoteShipping(value.address, userCartItems), value.user_currency);

        Money totalCost = {
            currency_code: value.user_currency,
            units: 0,
            nanos: 0
        };
        totalCost = sum(totalCost, shippingPrice);
        foreach OrderItem item in orderItems {
            Money multPrice = multiplySlow(item.cost, item.item.quantity);
            totalCost = sum(totalCost, multPrice);
        }

        string transactionId = check self.chargeCard(totalCost, value.credit_card);
        log:printInfo("payment went through " + transactionId);
        string shippingTrackingId = check self.shipOrder(value.address, userCartItems);
        check self.emptyUserCart(value.user_id);

        OrderResult orderRes = {
            order_id: orderId,
            shipping_tracking_id: shippingTrackingId,
            shipping_cost: shippingPrice,
            shipping_address: value.address,
            items: orderItems
        };

        check self.confirmationMail(value.email, orderRes);

        return {
            'order: orderRes
        };
    }

    function getUserCart(string userId, string userCurrency) returns CartItem[]|error {
        GetCartRequest req = {user_id: userId};
        Cart|grpc:Error cart = self.cartClient->GetCart(req);
        if cart is grpc:Error {
            log:printError("failed to call getCart of cart service", 'error = cart);
            return cart;
        }
        return cart.items;
    }

    function prepOrderItems(CartItem[] items, string userCurrency) returns OrderItem[]|error {
        OrderItem[] orderItems = [];
        foreach CartItem item in items {
            GetProductRequest req = {id: item.product_id};
            Product|grpc:Error product = self.catalogClient->GetProduct(req);
            if product is grpc:Error {
                log:printError("failed to call getProduct from catalog service", 'error = product);
                return product;
            }

            CurrencyConversionRequest req1 = {
                'from: product.price_usd,
                to_code: userCurrency
            };

            Money|grpc:Error money = self.currencyClient->Convert(req1);
            if money is grpc:Error {
                log:printError("failed to call convert from currency service", 'error = money);
                return money;
            }
            orderItems.push({
                item: item,
                cost: money
            });
        }
        return orderItems;
    }

    function quoteShipping(Address address, CartItem[] items) returns Money|error {
        GetQuoteRequest req = {
            address: address,
            items: items
        };
        GetQuoteResponse|grpc:Error getQuoteResponse = self.shippingClient->GetQuote(req);
        if getQuoteResponse is grpc:Error {
            log:printError("failed to call getQuote from shipping service", 'error = getQuoteResponse);
            return getQuoteResponse;
        }
        return getQuoteResponse.cost_usd;
    }

    function convertCurrency(Money usd, string userCurrency) returns Money|error {
        CurrencyConversionRequest req1 = {
            'from: usd,
            to_code: userCurrency
        };
        Money|grpc:Error convert = self.currencyClient->Convert(req1);
        if convert is grpc:Error {
            log:printError("failed to call convert from currency service", 'error = convert);
            return convert;
        }
        return self.currencyClient->Convert(req1);
    }

    function chargeCard(Money total, CreditCardInfo card) returns string|error {
        ChargeRequest req = {
            amount: total,
            credit_card: card
        };
        ChargeResponse|grpc:Error chargeResponse = self.paymentClient->Charge(req);
        if chargeResponse is grpc:Error {
            log:printError("failed to call charge from payment service", 'error = chargeResponse);
            return chargeResponse;
        }
        return chargeResponse.transaction_id;
    }

    function shipOrder(Address address, CartItem[] items) returns string|error {
        ShipOrderRequest req = {};
        ShipOrderResponse|grpc:Error getSupportedCurrenciesResponse = self.shippingClient->ShipOrder(req);
        if getSupportedCurrenciesResponse is grpc:Error {
            log:printError("failed to call shipOrder from shipping service", 'error = getSupportedCurrenciesResponse);
            return getSupportedCurrenciesResponse;
        }
        return getSupportedCurrenciesResponse.tracking_id;
    }

    function emptyUserCart(string userId) returns error? {
        EmptyCartRequest req = {
            user_id: userId
        };
        Empty|grpc:Error emptyCart = self.cartClient->EmptyCart(req);
        if emptyCart is grpc:Error {
            log:printError("failed to call emptyCart from cart service", 'error = emptyCart);
            return emptyCart;
        }
    }

    function confirmationMail(string email, OrderResult orderRes) returns error? {
        SendOrderConfirmationRequest req = {
            email: email,
            'order: orderRes
        };
        Empty|grpc:Error sendOrderConfirmation = self.emailClient->SendOrderConfirmation(req);
        if sendOrderConfirmation is grpc:Error {
            log:printError("failed to call sendOrderConfirmation from email service", 'error = sendOrderConfirmation);
            return sendOrderConfirmation;
        }
    }
}
