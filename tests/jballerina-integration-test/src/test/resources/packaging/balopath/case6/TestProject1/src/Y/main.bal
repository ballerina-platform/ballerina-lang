import bar/Z;
  
# Prints `Hello World`.

public function main() {
    string s = sayFromY();
}

public function sayFromY() returns string {
    return Z:sayFromZ() + "\nHello world from module Y!";
}
