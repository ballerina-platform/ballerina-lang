type ValueTypeMapper object {
    map<ValueKind> valueKindMap = {};

    public function __init() {
        self.fillValueMapper();
    }

    function fillValueMapper() {
        self.valueKindMap = {
            INT: INT_TYPE,
            STRING: STRING_TYPE,
            CONTINUE: CONTINUE_TYPE
        };
    }

};
