import bar/Z;

# Prints `Hello World`.

public function main() {
    string s = sayFromX();
}

public function sayFromX() returns string {
    return Z:sayFromZ() + "\nHello world from module X!";
}
