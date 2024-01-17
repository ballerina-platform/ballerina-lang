type Asset record {
    int Type;
    string Id;
    boolean Confirmed;
};

type Input record {
    Asset[] Assets;
};

type Output record {
    record {
        int Type;
        string Id;
        boolean Confirmed;
        record {
            int Type;
            string Id;
            boolean Confirmed;
        }[] AssetsInner;
    }[] Assets;
    string 'type;
};

function transform(Input input) returns Output => {
    Assets: input.Assets,
    'type: ""
};
