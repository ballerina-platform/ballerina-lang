import simpleconfigs.mod1;

configurable string itemCode = "item12393";
configurable float discount = 4.5;
configurable boolean testMode = ?;

public function main() {
    _ = mod1:hello("user");
}
