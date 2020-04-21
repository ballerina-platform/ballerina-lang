import ballerina/twilio;

public function main() {
twilio:Client twilioClient = new ("ACb2e9f049adcb98c7c31b913f8be70733", "34b2e025b2db33da04cc53ead8ce09bf", "");
twilio:WhatsAppResponse twilioResult = check twilioClient->sendWhatsAppMessage("+14155238886", "+94773898282", dataMapperResult);
}