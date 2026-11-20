import cachedbir/package_b;

public type Holder record {|
    package_b:Config config;
|};

public function name(Holder holder) returns string => holder.config.name;
