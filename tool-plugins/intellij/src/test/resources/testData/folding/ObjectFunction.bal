type Person object <fold text='{...}'>{
    public int age;
    public string firstName;
    public string lastName;

    function getFullName() returns string <fold text='{...}'>{
        return firstName + lastName;
    }</fold>

    function checkAndModifyAge(int condition, int a);
}</fold>;