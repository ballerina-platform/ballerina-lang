function testIfStmtWithIncompatibleType() (boolean) {
	if (false) {
		return false;
	} else if ("foo") {
		return true;
	}
}