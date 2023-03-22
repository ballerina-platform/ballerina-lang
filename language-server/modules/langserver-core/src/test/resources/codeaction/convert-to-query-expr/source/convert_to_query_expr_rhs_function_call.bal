type Asset record {
    int 'type;
    string id;
    boolean confirmed;
};

type Output record {
    record {
        int 'type;
        string id;
        boolean confirmed;
        record {
            int 'type;
            string id;
            boolean confirmed;
        }[] AssetsInner;
    }[] assets;
    string 'type;
};

function testFieldAccess()  {
    Output out;
    out.assets = getAssets();
};

function getAssets() returns Asset[] {
    return [];
}
