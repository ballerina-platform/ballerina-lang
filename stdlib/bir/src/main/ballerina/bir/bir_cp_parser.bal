public type ConstPool record {
    ModuleID[] packages = [];
    string[] strings = [];
    int[] ints = [];
    float[] floats = [];
};

public type ConstPoolParser object {
    ChannelReader reader;
    ConstPool cp = {};
    int i;

    public function __init(ChannelReader reader) {
        self.reader = reader;
        self.i = 0;
    }

    public function parse() returns ConstPool {
        var cpCount = self.reader.readInt32();
        while (self.i < cpCount) {
            self.parseConstPoolEntry();
            self.i += 1;
        }
        return self.cp;
    }

    public function parseConstPoolEntry() {
        var cpType = self.reader.readInt8();

        if (cpType == 1){
            self.parseInt();
        } else if (cpType == 2){
            self.parseFloat();
        } else if (cpType == 4){
            self.parseString();
        } else if (cpType == 5){
            self.parseModuleID();
        } else {
            error err = error("cp type " + cpType + " not supported.:");
            panic err;
        }
    }

    function parseInt() {
        self.cp.ints[self.i] = self.reader.readInt64();
    }

    function parseFloat() {
        self.cp.floats[self.i] = self.reader.readFloat64();
    }

    function parseString() {
        self.cp.strings[self.i] = self.reader.readString();
    }

    function parseModuleID() {
        ModuleID id = { org: self.cp.strings[self.reader.readInt32()],
            name: self.cp.strings[self.reader.readInt32()],
            modVersion: self.cp.strings[self.reader.readInt32()] };
        self.cp.packages[self.i] = id;
    }

};

