function testIfStmtWithIncompatibleType() {
	while ("foo") {
		system:log(3, true);
	}
}