annotation W on type, class, service;

annotation map<int> X on record field;

annotation map<string> Y on object field;

annotation Z on field;

@W
class Bar {
    @Y {
        q: "hello",
        r: "world"
    }
    @Z
    int j = 1;
}

@W
service class Ser {
    @Z
    int j = 1;
}

public type HSC record {|

    string hostRecField = "default_host_name";
    boolean boolRecField = true;
|};

public annotation HSC HSCfa on field;

public annotation HSC HSCsa on service;

int glob = 2;

function getServiceTypeDesc(string hosty) returns typedesc<service object {}> {
    var httpService =
    @HSCsa {
        hostRecField: hosty
    }
    isolated service object {

        @HSCfa {
            hostRecField: hosty
        }
        final string xField = hosty;

        @HSCfa {
            hostRecField: hosty
        }
        final string yField = hosty;
    };

    return typeof httpService;
}

public function testStructureAnnots() returns [typedesc<object {}>, typedesc<service object {}>, typedesc<service object {}>] {
    glob = 123;

    Bar b = @W object {
        @Y {
            q: "hello",
            r: "world"
        }
        @Z
        int j = 1;
    };

    var bar = @W service object {
        @Y {
            q: "hello",
            r: "world"
        }
        @Z
        int j = 1;
    };

    var serTypeDesc = getServiceTypeDesc("closure key");

    return [typeof b, typeof bar, serTypeDesc];
}

