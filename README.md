# ScanIt

## Overview

ScanIt is a cloud-native receipt scanning and budget management platform that helps users digitise paper receipts, automatically extract transaction details, and gain insights into their spending habits through a web-based dashboard.


## Repository Structure

```text
scanit-platform/
│
├── app/
│   ├── api/
│   │   ├── src/
│   │   ├── docs/
│   │   ├── Dockerfile
│   │   └── README.md
│   │
│   └── web/
│       ├── src/
│       ├── public/
│       ├── Dockerfile
│       └── README.md
│
├── infrastructure/
│   ├── aws/
│   ├── docker/
│   ├── jenkins/
│   └── README.md
│
├── docs/
│   ├── architecture/
│   ├── diagrams/
│   ├── presentation/
│   └── report/
│
├── .github/
│
└── README.md
```

---

## Getting Started

Application-specific setup instructions can be found in:

* `api/README.md`
* `web/README.md`
* `infrastructure/README.md`

---

# Git Workflow

The project follows a simplified Git Flow workflow.

## Branch Structure

```text
main
│
develop
│
├── feature/*
├── bugfix/*
└── hotfix/*
```

### Main Branch

The `main` branch contains stable and release-ready code only.

### Develop Branch

The `develop` branch serves as the primary integration branch for ongoing development.

### Feature Branches

Used for implementing new functionality.

Examples:

```text
feature/user-authentication
feature/receipt-upload
feature/ocr-processing
feature/budget-dashboard
```

### Bugfix Branches

Used for fixing non-critical defects.

Examples:

```text
bugfix/login-validation
bugfix/receipt-parser
```

### Hotfix Branches

Used for urgent fixes that affect production-ready code.

Examples:

```text
hotfix/security-patch
hotfix/build-failure
```

---

## Development Process

1. Pull the latest changes from `develop`
2. Create a new feature branch
3. Implement changes
4. Commit changes using the commit convention
5. Push the branch
6. Create a Pull Request to `develop`
7. Request review
8. Merge after approval

---

## Common Git Commands

### Create a New Feature Branch

```bash
git checkout develop
git pull origin develop
git checkout -b feature/receipt-upload
```

### Push a Branch

```bash
git push -u origin feature/receipt-upload
```

### Update Local Develop Branch

```bash
git checkout develop
git pull origin develop
```

### Sync Feature Branch with Develop

```bash
git checkout feature/receipt-upload
git merge develop
```

---

# Commit Message Convention

Use Conventional Commits whenever possible.

### Examples

```text
feat: add receipt upload endpoint

feat: implement AWS Textract integration

fix: resolve duplicate transaction issue

docs: update project documentation

refactor: simplify OCR service implementation

test: add receipt parser unit tests

ci: update Jenkins pipeline configuration
```

### Commit Types

| Type     | Description                                 |
| -------- | ------------------------------------------- |
| feat     | New feature                                 |
| fix      | Bug fix                                     |
| docs     | Documentation updates                       |
| refactor | Code improvements without behaviour changes |
| test     | Testing changes                             |
| ci       | CI/CD changes                               |
| chore    | Maintenance tasks                           |

---

# Pull Request Guidelines

Before opening a Pull Request:

* Ensure the application builds successfully
* Ensure tests pass
* Keep changes focused on a single feature or fix
* Update documentation when required
* Resolve merge conflicts

### Pull Request Checklist

* [ ] Code compiles successfully
* [ ] Tests pass
* [ ] Documentation updated
* [ ] No unnecessary files included
* [ ] Ready for review

---

# Definition of Done

A task is considered complete when:

* Feature requirements are implemented
* Code is reviewed
* Tests pass successfully
* Documentation is updated
* Pull Request is approved
* Changes are merged into `develop`

---

# Team Collaboration Guidelines

* Commit small, focused changes frequently
* Avoid direct commits to `main`
* Avoid direct commits to `develop` whenever possible
* Use Pull Requests for all feature work
* Review teammates' code before merging
* Keep documentation updated

---

# Project Documentation

Additional project documentation should be stored under the `/docs` directory.

Suggested contents:

```text
docs/
├── architecture/
├── diagrams/
├── meeting-notes/
├── presentation/
└── report/
```


## Authors

PDip in Software Development, Cloud Computing & DevOps

Capstone Project – ScanIt
