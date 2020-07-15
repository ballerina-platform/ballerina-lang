type Person <fold text='{...}'>object {
    public int age;
    public string firstName;
    public string lastName;

    function getFullName() returns string;

    function checkAndModifyAge(int condition, int a);
};</fold>
