
public type kind "PLUS" | "MINUS" | "AND" | "OR" | "NOT" | "GREATER_THAN";

public kind not = "NOT";
public kind plus = "PLUS";

public function getEnumeratorInPackage() returns (kind) {
    return not;
}

