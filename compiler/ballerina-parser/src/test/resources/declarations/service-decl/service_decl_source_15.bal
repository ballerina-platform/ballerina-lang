service Foo on bar {

    // Test duplicate qualifiers

    public transactional isolated transactional function foo() {

    }

    isolated isolated resource function get x/y/z() {

    }

    // Test visibility qualifier with remote and resource

    public remote function foo() {

    }

    public resource function get x/y/z() {

    }
}
