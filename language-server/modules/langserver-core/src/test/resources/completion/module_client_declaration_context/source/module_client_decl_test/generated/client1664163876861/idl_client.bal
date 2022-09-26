public type ClientConfiguration record {
    string specVersion;
};

public isolated client class 'client {
    public final string specVersion;

    public function init(*ClientConfiguration config) {
        self.specVersion = config.specVersion;
    }

    remote function getSpecVersion() returns string {
        lock {
            return self.specVersion;
        }
    }

}
