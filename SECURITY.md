---
layout: ballerina-inner-page
title: Reporting a Security Vulnerability
permalink: /security/
---

# Security Policy

Ballerina project maintainers take security issues very seriously and all the vulnerability reports are treated with the highest priority and confidentiality.

- [Reporting a vulnerability](#reporting-a-vulnerability)
- [Handling a vulnerability](#handling-a-vulnerability)

## Reporting a vulnerability

Ensure you are using the latest Ballerina version before you run an automated security scan or perform a penetration test against it.

Based on the ethics of responsible disclosure, you must only use the **[security@ballerina.io](mailto:security@ballerina.io)** mailing list to report security vulnerabilities and any other concerns regarding the security aspects of the source code or any other resource in this repo.

**WARNING:** To protect the end-user security, please do not use any other medium to report security vulnerabilities. Also, kindly refrain from disclosing the vulnerability details you come across with other individuals, in any forums, sites, or other groups - public or private before it’s mitigation actions and disclosure process are completed.

Use the following key to send secure messages to security@ballerina.io:

> security@ballerina.io: 0168 DA26 2989 0DB9 4ACD 8367 E683 061E 2F85 C381 [pgp.mit.edu](https://pgp.surfnet.nl/pks/lookup?op=vindex&fingerprint=on&search=0xE683061E2F85C381)

Also, use the following template when reporting vulnerabilities so that it contains all the required information and helps expedite the analysis and mitigation process.

- Vulnerable Ballerina artifact(s) and version(s)
- Overview: High-level overview of the issue and self-assessed severity
- Description: Include the steps to reproduce
- Impact: Self-assessed impact
- Solution: Any proposed solution

We will keep you informed of the progress towards a fix and disclosure of the vulnerability if the reported issue is identified as a true positive. 

## Handling a vulnerability

The below is an overview of the vulnerability handling process.

1. The vulnerability will be reported privately to security@ballerina.io. (The initial response time will be less than 24 hours).
2. The reported vulnerability gets fixed and the solution gets verified by the relevant teams at WSO2.
3. The fix gets applied to the master branch and a new version of the distribution gets released if required.
4. The reported user is kept updated on the progress of the process. 

