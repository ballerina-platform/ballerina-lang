service Foo on bar {

    // resource qual should come first
    transactional resource function () a = b;


    // Test incomplete resource field
    resource function
}
