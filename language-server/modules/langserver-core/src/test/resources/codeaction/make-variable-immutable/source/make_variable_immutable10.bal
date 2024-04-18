type ObjectName object {
    int i;

    function name();
};

isolated class IsolatedClass {
    ObjectName myObj;

    function init(ObjectName & readonly myObj) {
        self.myObj = myObj;
    }
}
