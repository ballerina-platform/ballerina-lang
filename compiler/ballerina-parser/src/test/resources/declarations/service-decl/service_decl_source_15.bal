service Foo on bar {
    resource final transactional function() a = b;
    final resource isolated function() a = b;
    resource Person person;
    resource final Animal animal;
    final resource Vehicle vehicle;
}
