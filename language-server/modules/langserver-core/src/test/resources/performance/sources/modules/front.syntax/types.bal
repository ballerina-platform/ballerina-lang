import wso2/nballerina.types as t;
import wso2/nballerina.err;

public function resolveInlineTypeDesc(InlineTypeDesc|"()" td) returns t:UniformTypeBitSet {
    match td {
        "any" => { return t:ANY; }
        "boolean" => { return t:BOOLEAN; }
        "int" => { return t:INT; }
        "float" => { return t:FLOAT; }
        "string" => { return t:STRING; }
        "()" => { return t:NIL; }
    }
    if td is InlineUnionTypeDesc {
        t:UniformTypeBitSet left = resolveInlineTypeDesc(td.left);
        t:UniformTypeBitSet right = resolveInlineTypeDesc(td.right);
        return left|right;
    }
    if td is InlineArrayTypeDesc {
        return t:LIST;
    }
    if td is InlineMapTypeDesc {
        return t:MAPPING;
    }
    panic err:impossible("unreachable in resolveInlineTypeDesc");
}
