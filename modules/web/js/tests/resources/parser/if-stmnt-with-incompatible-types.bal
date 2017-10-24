function testIfStmtWithIncompatibleType() {
	if ("foo") {
		int a = 5;
	}
}