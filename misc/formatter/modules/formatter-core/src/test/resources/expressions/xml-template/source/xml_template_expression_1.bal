public function foo() {
   string title = "(Sir)";
   xml x3 = xml `<book>
                   <name>Sherlock Holmes</name>
                   <author>${  title
                   } Arthur Conan Doyle</author>
                 </book>`;
}
