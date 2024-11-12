public function func() {
    xml book = xml `<book>
                           <name>Sherlock Holmes</name>
                           <author>Sir Arthur Conan Doyle</author>
                     </book>`;

    from var x in book/<name> select x;
}
