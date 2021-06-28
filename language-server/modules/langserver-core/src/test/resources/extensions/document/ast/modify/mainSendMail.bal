import wso2/choreo.sendemail;

public function main() returns error? {

    sendemail:Client sendemailEndpoint = check new ();
    string sendEmailResponse = check sendemailEndpoint->sendEmail("", "", "");
}
