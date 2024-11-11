function foo() {
    transaction {
    } on fail error _ {
    }

    transaction {
        int a = 5;
    } on fail error _ {
    }

    transaction {
        transaction {
            transaction {
                string b;
            }
        }
    } on fail error _ {
    }

    transaction {
        transaction {
            transaction {
                string b;
            } on fail error _ {
            }
        }
    } on fail error _ {
    }
}
