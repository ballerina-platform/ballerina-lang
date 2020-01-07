# Security Policy

Ballerina project maintainers take security issues very seriously and all the vulnerability reports are treated with the highest priority and confidentiality.

- [Reporting a vulnerability](#reporting-a-vulnerability)
- [Handling a vulnerability](#handling-a-vulnerability)

## Reporting a vulnerability

Ensure you are using the latest Ballerina version before you test a security issue, run an automated security scan or perform a penetration test.

If you have any concerns regarding the security aspects of the source code or any other resource in this repo or have uncovered a security vulnerability, we strongly encourage you to report that to our private and highly confidential security mailing list: **[security@ballerina.io](mailto:security@ballerina.io)** first using the below key without disclosing them in any forums, sites, or other groups - public or private. 

security@ballerina.io: 0168 DA26 2989 0DB9 4ACD 8367 E683 061E 2F85 C381 [pgp.mit.edu](https://pgp.surfnet.nl/pks/lookup?op=vindex&fingerprint=on&search=0xE683061E2F85C381)

We will keep you informed of the progress towards a fix and disclosure of the vulnerability if reported issue is identified as a true positive. To protect the end-user security, these issues could be disclosed in other places only after it’s mitigation actions and disclosure process are completed.

**Warning:** Please do not create GitHub issues for security vulnerabilities. Further, kindly refrain from sharing the vulnerability details you come across with other individuals. 

Also, use the following template when reporting vulnerabilities so that it contains all the required information and helps expedite the analysis and mitigation process.

- Vulnerable Ballerina artifacts(s) and version(s)
- Overview: High-level overview of the issue and self-assessed severity
- Description: Include the steps to reproduce
- Impact: Self-assessed impact
- Solution: Any proposed solution

## Handling a vulnerability

The below is an overview of the vulnerability handling process.

1. The user privately reports the vulnerability to security@ballerina.io. (The initial response time will be less than 24 hours).
2. The WSO2 security team works privately with the user to fix the vulnerability and QA verifies the solution.
3. Apply the fix to the master branch and release a new version of the distribution if required.
4. The reported user is kept updated on the progress of the process. 
