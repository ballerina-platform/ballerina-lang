type OperatorMapper object {
    map<OperatorKind> operatorTypeMap = {};

    public function __init() {
        self.fillOperatorMapper();
    }

    function fillOperatorMapper() {
        self.operatorTypeMap = {
            ADD: PLUS_OP,
            SUB: MINUS_OP,
            DIV: DIVISION_OP,
            MUL: MULTIPLICATION_OP,
            COLON: COLON_OP,
            MOD: MOD_OP,
            LT_EQUAL: LT_EQUAL_OP,
            GT_EQUAL: GT_EQUAL_OP,
            GT: GT_OP,
            LT: LT_OP,
            EQUAL: EQUAL_OP,
            NOT_EQUAL: NOT_EQUAL_OP,
            REF_EQUAL: REF_EQUAL_OP,
            REF_NOT_EQUAL: REF_NOT_EQUAL_OP,
            UNARY_MINUS: MINUS_OP,
            UNARY_PLUS: PLUS_OP,
            NOT: NOT_OP,
            BIT_COMPLEMENT: BIT_COMPLEMENT_OP,
            UNTAINT: UNTAINT_TYPE
        };
    }
};
