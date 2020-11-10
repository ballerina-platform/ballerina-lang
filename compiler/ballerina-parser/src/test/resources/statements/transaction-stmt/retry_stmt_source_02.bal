function foo() {
    retry transaction {
    }

    retry<T> transaction {
    }

    retry(a, b) transaction {
    }

    retry<T> (a, b) transaction {
    }
}
