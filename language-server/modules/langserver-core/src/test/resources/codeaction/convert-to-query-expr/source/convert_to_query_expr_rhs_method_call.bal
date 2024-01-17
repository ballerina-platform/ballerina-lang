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
    MyAssets asset = new;
    Output out;
    out.assets = asset.getAssets();
};

class MyAssets {
    function getAssets() returns Asset[] {
        return [];
    }
}
