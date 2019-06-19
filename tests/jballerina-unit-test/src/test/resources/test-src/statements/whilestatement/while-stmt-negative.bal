function testIfStmtWithIncompatibleType() {
	while ("foo") {
		int a = 5;
	}

	while "bar" {
	    int a = 5;
	}

	while 5 {
	    int a = 5;
	}

	while [5, "foo"] {
	    int a = 5;
	}
}