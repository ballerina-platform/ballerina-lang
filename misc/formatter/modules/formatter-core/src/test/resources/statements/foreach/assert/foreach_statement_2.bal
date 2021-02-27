function foo() {
    xml books = xml `<books>
                          <book><name>Sherlock Holmes</name><author>Sir Arthur Conan Doyle</author></book>
                     </books>`;

    foreach var book in books/<*> {
    }
}
