public type HSC record {|
    string host = "b7a.default";
    boolean treatNilableAsOptional = true;
|};

public annotation HSC HSCfa on field;
public annotation HSC HSCsa on service;

function createService(string hosty, decimal maxAgeMy, boolean allowCredentials) returns service object {

} {

    var httpService =
    @HSCsa {
        host : hosty
    }
    isolated service object {

        @HSCfa {
            host : hosty
        }
        private final string xField = hosty;
    };

    return httpService;
}

public function main() {
   var ser = createService("hostKRv", 200, true);
}
