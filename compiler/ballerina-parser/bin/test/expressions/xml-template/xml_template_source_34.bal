function foo() {
    xml x1 = xml `</>`;
    xml x2 = xml `   </>   `;
    xml x3 = xml `<z ab="hello"> </>  `;
    xml x4 = xml `</book> </>  `;
    xml x5 = xml `</></book>   `;
    xml x6 = xml `</><book></book>   `;
}
