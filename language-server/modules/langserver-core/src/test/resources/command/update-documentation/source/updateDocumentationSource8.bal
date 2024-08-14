type Args record {|
    decimal x;
    decimal y;
|};

type Response record {|
    decimal result;
|};

listener http:Listener ln = new (9090);

service /calc on ln {
   # Description.
   #
   # + args - parameter description
   # + return - return value description
   resource function post add(int val, *Args args) returns Response {
      return {result: args.x + args.y};
   }
}
