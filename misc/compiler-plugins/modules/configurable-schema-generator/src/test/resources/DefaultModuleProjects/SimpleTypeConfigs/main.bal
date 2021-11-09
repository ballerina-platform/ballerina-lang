@display {
    label: "fixedDiscount",
    description: "Fixed dicount value in LKR"
}
configurable int fixedDiscount = -200;
@display {
    label: "discountPercentage",
    description: "Discount as a percentage"
}
configurable decimal discountPercentage = ?;
@display {
    label: "testMode",
    description: "Flag to enable test mode"
}
configurable boolean testMode = true;

public function main() {
}
