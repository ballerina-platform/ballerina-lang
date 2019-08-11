import ballerina/io;
import balTest/A;

public function main() {
	json j = {"a":25};
	A:Bar f = j;

	io:println(f);
}