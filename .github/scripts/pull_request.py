import os
import sys
import time

from github import Github
from httplib2 import Http

BALLERINA_ORG_NAME = 'ballerina-platform'
ENV_BALLERINA_BOT_TOKEN = 'BALLERINA_BOT_TOKEN'
ENV_CHAT_ID = 'CHAT_ID'
ENV_CHAT_KEY = 'CHAT_KEY'
ENV_CHAT_TOKEN = 'CHAT_TOKEN'
PULL_REQUEST_TITLE = '[Automated] Merge patch branch with the master'

SLEEP_INTERVAL = 30 # 30s
MAX_WAIT_CYCLES = 180 # script timeout is 90 minutes

ballerina_bot_token = os.environ[ENV_BALLERINA_BOT_TOKEN]
github = Github(ballerina_bot_token)

def main():
    repo = github.get_repo(BALLERINA_ORG_NAME + '/' + 'ballerina-lang')
    branches = repo.get_branches()

    for branch in branches:
        if ('2201.0.' in branch.name):
            patch_branch = branch.name
            break

    # Check whether master branch already has an unmerged PR from patch branch
    pr_exists = False
    pulls = repo.get_pulls(state='open')
    for pull in pulls:
        if (pull.head.ref == patch_branch):
            pr_exists = True
            unmerged_pr = pull

    if (pr_exists):
        print ("Master branch already has an open pull request from " + patch_branch)
        unmerged_pr.closed_at(time.localtime())

    pr = create_pull_request(repo, patch_branch)
    pending = True
    wait_cycles = 0
    while pending:
        if wait_cycles < MAX_WAIT_CYCLES:
            time.sleep(30)
            pending, passing, failed_checks = check_pending_pr_checks(repo, pr) # Check_for_pr_check()
            if not pending:
                if passing:
                    try:
                        pr.merge()
                        log_message = "[Info] Automated master update PR merged. PR: " + pr.html_url
                        print(log_message)
                    except Exception as e:
                        print("[Error] Error occurred while merging master update PR " , e)
                else:
                    send_message("[Info] Automated ballerina-lang master update PR has failed checks." + "\n" +\
                     "Please visit <" + pr.html_url + "|the build page> for more information"
            else:
                wait_cycles+=1
        else:
            send_message("[Info] Automated ballerina-lang master update PR is unmerged due to pr checks timeout." + "\n" +\
             "Please visit <" + pr.html_url + "|the build page> for more information"
            break

def create_pull_request(repo, patch_branch):
    try:
        pull_request_title = PULL_REQUEST_TITLE
        created_pr = repo.create_pull(
            title=pull_request_title,
            body='Daily syncing of patch branch content with the master'
            head=patch_branch,
            base=repo.default_branch
        )
        log_message = "[Info] Automated PR created for ballerina-lang repo at "
                       + created_pr.html_url
        print(log_message)
    except Exception as e:
        print("[Error] Error occurred while creating pull request ", e)
        sys.exit(1)

    try:
        approve_pr(repo, created_pr.number)
    except Exception as e:
        print("[Error] Error occurred while approving the PR ", e)
    return created_pr

def approve_pr(repo, pr_number):
    time.sleep(5)
    pr = repo.get_pull(pr_number)
    try:
        pr.create_review(event='APPROVE')
        print(
            "[Info] Automated master update PR approved. PR: " + pr.html_url)
    except Exception as e:
        raise e

def check_pending_pr_checks(repo, pr):
    print("[Info] Checking the status of the dependency master syncing PR ")
    passing = True
    pending = False
    pull_request = pr.number
    sha = pull_request.sha
    for pr_check in repo.get_commit(sha=sha).get_check_runs():
        # Ignore codecov checks temporarily due to bug
        if not pr_check.name.startswith('codecov'):
            if pr_check.status != 'completed':
                pending = True
                break
            elif pr_check.conclusion == 'success':
                continue
            else:
                failed_pr_check = {
                    'name': pr_check.name,
                    'html_url': pr_check.html_url
                }
                failed_pr_checks.append(failed_pr_check)
                passing = False
    return pending, passing, failed_pr_checks

def send_message(message):
    build_chat_id = os.environ[ENV_CHAT_ID]
    build_chat_key = os.environ[ENV_CHAT_KEY]
    build_chat_token = os.environ[ENV_CHAT_TOKEN]

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
