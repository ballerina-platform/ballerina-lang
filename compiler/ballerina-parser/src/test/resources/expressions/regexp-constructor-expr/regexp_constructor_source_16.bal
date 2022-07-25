function foo() {
    x = re `(?m:AAD*)`;
    x = re `(?i:[^\-d-hM-N\tQ-T])`;
    x = re `(?ms:a{1,3})`;
    x = re `(?msix:A.*?)`;
    x = re `(?ms-ix:[^ABC]{1245,3212})`;
    x = re `(?m-i:[\u{1234}])`;
    x = re `(?si-m:[\t])`;
    x = re `(?x-sm:[\-d-hM-N\tQ-T])`;
    x = re `(?msix-msix:[\-])`;
    x = re `(?ms-i:x*|y|z+)`;
}
