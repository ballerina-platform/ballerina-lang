type ObjectName object {
    int i;

    function name();
} & readonly;

isolated class IsolatedClass {
    ObjectName myObj;

    function init(ObjectName myObj) {
        self.myObj = myObj;
    }
}
