function testIfStmtWithIncompatibleType() {
	if ("foo") {
		system:log(3, true);
	}
}