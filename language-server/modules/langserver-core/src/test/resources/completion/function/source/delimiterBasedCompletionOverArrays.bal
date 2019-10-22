import ballerina/io;

type Fruit object {
    public int weight = 12;
};

public function main() {
	Fruit[] fruits = [];
	fruits[0].
	io:println("Array Test");
}