function foo() {
    transaction {
    }
on fail error e{
        io: println("Exception thrown...")
    ;
}
}
