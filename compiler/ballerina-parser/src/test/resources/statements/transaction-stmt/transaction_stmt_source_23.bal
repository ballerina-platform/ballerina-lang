function foo() {
    transaction {
    } on fail = {
    }

    transaction {
        int a = 5;
    } on fail = {
    }

    transaction {
        transaction {
            transaction {
                string b;
            }
        }
    } on fail = {
    }

    transaction {
        transaction {
            transaction {
                string b;
            } on fail = {
            }
        }
    } on fail = {
    }
}
