package ballerina.net.grpc;

struct StockQuote {
    string symbol;
    string name;
    float last;
    float low;
    float high;
}

struct StockRequest {
    string name;
}

struct StockQuotes {
    StockQuote stock;
}

struct Void {

}

public connector photoStub(){
    action getQuotes(Void req) (StockQuotes) {
        endpoint<grpc:GRPCConnector> ep {
            create grpc:GRPCConnector("localhost",9090);
        }
        map mapValue = <map>req;
        map responseMap = ep.execute(mapValue,"getQuotes","Void","StockQuotes",0,"StockQuoteService");
        var response = <StockQuotes> responseMap;
        return response;
    }
    action addStock(StockQuote req) (StockQuotes) {
        endpoint<grpc:GRPCConnector> ep {
            create grpc:GRPCConnector("localhost",9090);
        }
        map mapValue = <map>req;
        map responseMap = ep.execute(mapValue,"addStock","StockQuote","StockQuotes",1,"StockQuoteService");
        var response= <StockQuotes> responseMap;
        return response;
    }
    action getQuote(StockRequest req) (StockQuotes) {
        endpoint<grpc:GRPCConnector> ep {
            create grpc:GRPCConnector("localhost",9090);
        }
        map mapValue = <map>req;
        map responseMap = ep.execute(mapValue,"getQuote","StockRequest","StockQuotes",2,"StockQuoteService");
        var response = <StockQuotes> responseMap;
        return response;
    }
}