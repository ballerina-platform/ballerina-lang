function getClient() returns jdbc:Client|error {
    if isTestEnabled {
        return new ("jdbc:h2:mem:mockTestDB");
    } else if (true) {
        return new (database.url,
            database.user,
            database.password,
            connectionPool = connPool,
            options = options
        );
    }
    return new (
        database.url,
        database.user
    );
}
