function testIfStmtWithIncompatibleType() {
	while ("foo") {
		int a = 5;
	}
}