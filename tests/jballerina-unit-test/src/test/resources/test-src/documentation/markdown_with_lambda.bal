# Documentation for TimeOrderWindow.
#
# + f - documentation
public type TimeOrderWindow object {

    public function (int i) f;

    public function __init() {
        // Test lambda function in object init.
        self.f = function (int i) {

        };
    }
};
