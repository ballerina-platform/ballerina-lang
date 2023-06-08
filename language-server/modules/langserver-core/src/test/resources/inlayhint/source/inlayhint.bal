function testFunction() {
    string fullName = getFullName("John", "Doe");
}

function getFullName(string firstName, string lastName) {
    return firstName + " " + lastName.substring(0, 1);
}
