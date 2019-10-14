
public function main() {
	xml bookXML = xml `<book>
                    <name>Sherlock Holmes</name>
                    <author>
                        <fname title="Sir">Arthur</fname>
                        <mname>Conan</mname>
                            <lname>Doyle</lname>
                    </author>
                    <!--Price: $10-->
                    </book>`;
    xml name = bookXML["name"].
}