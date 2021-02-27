function foo() {
    transaction {
    }

    transaction {
        int a = 5;
    }

    transaction {
        transaction {
            transaction {
                string b;
            }
        }
    }
}
