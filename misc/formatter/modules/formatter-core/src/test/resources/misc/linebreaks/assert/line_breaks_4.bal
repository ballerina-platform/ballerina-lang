public function foo() {
    foreach string animal in animals {
        match
        animal
            {
            "Mouse" =>
            {
            }
            "Dog"|"Canine" => {
            }

            "Cat"|"Feline" =>
            {
            }
            _ => {
            }
        }
    }

    int i = <
                @qwe
                @abc>test();

    Person person = <Person>
    employee;

    if (true
    )
    {
    }

    error? flushResult =
    flush
    w2
    ;

    xml x3 = xml `<book>
                           <name>Sherlock Holmes</name>
                           <author>${title
                            } Arthur Conan Doyle</author>
                         </book>`;

    fork
    {
        worker w1 returns int {
            return 20;
        }
    }

    return
        20
    ;
}

function foo(string
|int id) returns int|error
{
}

public function foo() {
    xml<never> xmlValue = <xml<never>>'xml:concat();
    map<
    never> someMap = {};
    table<Person> key
<never> personTable = table [
        {name: "John"}
    ];

    Person p2 = new (
    );
}

type Person record {
    Address
    ? addr;
};

final http:Client emailClientEndpoint = check new (emailServiceUrl,
    secureSocket = {
        enable: false
    }
);
