import ballerina/io;

public function main() {
    // An `xml` element with nested children.
    xml school = xml `<school>
                          <student>
                              <firstName>Michelle</firstName>
                              <lastName>Sadler</lastName>
                              <intakeYear>1990</intakeYear>
                              <gpa>3.5</gpa>
                          </student>
                          <student>
                              <firstName>Ranjan</firstName>
                              <lastName>Fonseka</lastName>
                              <intakeYear>2001</intakeYear>
                              <gpa>1.9</gpa>
                          </student>
                          <student>
                              <firstName>Martin</firstName>
                              <lastName>Guthrie</lastName>
                              <intakeYear>2002</intakeYear>
                              <gpa>3.7</gpa>
                          </student>
                      </school>`;

    // The `from` clause works similar to a `foreach` statement.
    // The `students` is the concatenated `xml` of the `query expression` results.
    xml students = from var studentName in school/<student>/<firstName>
                   // The `limit` clause limits the number of output items.
                   limit 2
                   // The `select` clause is evaluated for each iteration.
                   // The emitted values are concatenated to form the `xml` result.
                   select <xml>studentName;

    io:println(students);
}
