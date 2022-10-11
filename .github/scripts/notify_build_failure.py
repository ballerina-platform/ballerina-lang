from json import dumps
import sys
from cryptography.fernet import Fernet
from httplib2 import Http
import os
import csv

def main():
    message = "*ballerina-lang* daily build failure\n" +\
                "Please visit <https://github.com/ballerina-platform/ballerina-lang" +\
                "/actions?query=workflow%3A%22Daily+build%22|the daily build page> for more information" +"\n"
    try:
        owners = ['gimantha', 'azinneera']
        encryption_key = os.environ['ENV_USER_ENCRYPTION_KEY']

        fernet = Fernet(encryption_key)
        with open('.github/scripts/resources/github_users_encrypted.csv', 'rb') as enc_file:
            encrypted_csv = enc_file.read()

        decrypted = fernet.decrypt(encrypted_csv)
        with open('.github/scripts/resources/github_users_decrypted.csv', 'wb') as dec_file:
            dec_file.write(decrypted)

        for owner in owners:
            with open('.github/scripts/resources/github_users_decrypted.csv', 'r') as read_obj:
                user_file = csv.DictReader(read_obj)
                owner = owner.strip()
                for row in user_file:
                    if row['gh-username'] == owner:
                        message += "<users/" + row['user-id'] + ">" + "\n"
                        break

        send_message(message)
    except:
        send_message(message)

def send_message(message):
    build_chat_id = os.environ['ENV_CHAT_ID']
    build_chat_key = os.environ['ENV_CHAT_KEY']
    build_chat_token = os.environ['ENV_CHAT_TOKEN']

    url = 'https://chat.googleapis.com/v1/spaces/' + build_chat_id + \
          '/messages?key=' + build_chat_key + '&token=' + build_chat_token

    bot_message = {'text': message}
    message_headers = {'Content-Type': 'application/json; charset=UTF-8'}

    http_obj = Http()

    resp = http_obj.request(
        uri=url,
        method='POST',
        headers=message_headers,
        body=dumps(bot_message),
    )[0]

    if resp.status == 200:
        print("Successfully send notification")
    else:
        print("Failed to send notification, status code: " + str(resp.status))
        sys.exit(1)

main()
