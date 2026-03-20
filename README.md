<!--
 ~ Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 ~
 ~ This software is the property of WSO2 LLC. and its suppliers, if any.
 ~ Dissemination of any information or reproduction of any material contained
 ~ herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 ~ You may not alter or remove any copyright or other notice from copies of this content.
-->

# WSO2 Open Banking CDS Compliance Reference Implementation

This project provides the Reference Implementation for the Australian Consumer Data Standards (CDS) Specification, enabling financial institutions to achieve compliance with Australian Consumer Data Right (CDR) standards.

## Overview

With the release of Open Banking 4.0.0, it has introduced OpenAPI based extensions such that the toolkit developer
can implement Open Banking specification requirements in their preferred programming language.
The custom developed extensions can be deployed externally and tested separately without restarting the WSO2 servers.

The OpenAPI extension can be found from [here](https://ob.docs.wso2.com/en/latest/references/accelerator-extensions-api/).

This reference implementation provides a comprehensive solution for implementing Open Banking Australian CDS specifications, including:

- **Reference Implementation**: Core implementation of Australian Consumer Data Standards
- **Demo Backend**: Sample backend service for testing and demonstration purposes
- **React Applications**: Frontend applications including the self-care portal
- **Integration Test Suite**: Comprehensive test frameworks for validation and compliance testing

## Prerequisites

Before building the toolkit, ensure you have the following installed:

- **Java**: JDK 11 or above
- **Apache Maven**: Version 3.0.5 or above
- **Database**: MySQL 5.7 or above
- **Node.js & npm**: Node.js 18.16.0 and above and npm 9.7.0 and above. Required for building React applications (when using solution profile)

## Project Structure

The toolkit consists of the following main modules:

### Core Modules

- **components/reference-implementation-openbanking-consumerdatastandards-au**: Core implementation of Australian Consumer Data Standards (version 2.0.0)
- **components/demo-backend**: Sample backend service demonstrating CDS integration patterns
- **react-apps**: Frontend applications including the self-care portal

### Test Modules

- **integration-test-suite**: Comprehensive test suite including:
   - CDS Toolkit Test Framework
   - CDS Toolkit Integration Test
   - CDS Toolkit Preconfiguration Test
   - BFSI Test Framework
   - Test artifacts and configurations
   - End-to-end test suite execution

## Building from Source

### Clone the Repository

Get the source code from the GitHub repository:

```bash
git clone https://github.com/wso2/reference-implementation-consumerdatastandards-au
git checkout 2.0.0
cd components/reference-implementation-openbanking-consumerdatastandards-au
```

### Build Commands

Navigate to the project root directory and execute one of the following Maven commands:

| Command | Description |
|:--------|:------------|
| `mvn install` | Builds the project without cleaning existing artifacts |
| `mvn clean install` | Cleans all build artifacts and builds from scratch |

### Build Artifacts

After a successful build, the following artifacts will be available:

```
components/reference-implementation-openbanking-consumerdatastandards-au/target/
├── api#reference-implementation#ob#consumerdatastandards#au.war

components/demo-backend/target/
├── api#openbanking#cds#backend.war
```

## Installation and Setup

1. Navigate to the `components/reference-implementation-openbanking-consumerdatastandards-au/target/` folder and host the `reference-implementation-openbanking-consumerdatastandards-au-2.0.0.war` in a preferred location and get the base URL.

NOTE:
- If you are hosting this in WSO2 Identity Server, copy the `api#reference-implementation#ob#consumerdatastandards#au.war` to the `<IS_HOME>/repository/deployment/server/webapps` folder.
- Add the following configurations to the deployment.toml file inside the `<IS_HOME>/repository/conf` folder:
```
[[resource.access_control]]
allowed_auth_handlers = ["BasicAuthentication"]
context = "(.*)/api/reference-implementation/ob/consumerdatastandards/au/(.*)"
http_method = "all"
secure = "true"
```

2. Navigate to the `components/demo-backend/target/` folder and host the `cds-demo-backend-2.0.0.war` in a preferred location and get the base URL.

NOTE:
- If you are hosting this in WSO2 Identity Server, copy the `api#openbanking#cds#backend.war` to the `<IS_HOME>/repository/deployment/server/webapps` folder.
- Add the following configurations to the deployment.toml file inside the `<IS_HOME>/repository/conf` folder:
```
[[resource.access_control]]
context = "(.*)/api/openbanking/cds/backend/(.*)"
http_method = "all"
secure = "false"
```

## Configuring WSO2 Open Banking Accelerator 4.0.0

1. Update the following configurations in the deployment.toml file inside the `<IS_HOME>/repository/conf` folder.

```
[financial_services.extensions.endpoint]
enabled = true
# allowed extensions: "pre_process_client_creation", "pre_process_consent_creation"
allowed_extensions = ["pre_process_client_creation", "pre_process_client_update", "pre_process_client_retrieval",
    "pre_process_consent_creation", "enrich_consent_creation_response", "pre_process_consent_file_upload",
    "enrich_consent_file_response", "pre_process_consent_retrieval", "validate_consent_file_retrieval",
    "pre_process_consent_revoke", "enrich_consent_search_response", "populate_consent_authorize_screen",
    "persist_authorized_consent", "validate_consent_access", "issue_refresh_token", "validate_authorization_request",
    "validate_event_subscription", "enrich_event_subscription_response", "validate_event_creation",
    "validate_event_polling", "enrich_event_polling_response", "map_accelerator_error_response"]
base_url = "https://<HOSTNAME>:<PORT>/api/reference-implementation/ob/consumerdatastandards/au"
retry_count = 5
connect_timeout = 5
read_timeout = 5

[financial_services.extensions.endpoint.security]
# supported types : Basic-Auth or OAuth2
type = "Basic-Auth"
username = "<USERNAME>"
password = "<PASSWORD>"
```

2. Start the IS server.

## Running Integration Tests

The project includes a comprehensive integration test suite for CDS compliance validation. For detailed instructions on running the integration tests, refer to the [Integration Test Suite README](integration-test-suite/README.md).

## Contributing

We encourage contributions to improve the toolkit. Please ensure:

- Code follows the project's coding standards
- All tests pass before submitting pull requests
- New features include appropriate test coverage

## Reporting Issues

Report issues, bugs, or feature requests through the project's issue tracking system.

## License

WSO2 LLC. licenses this source under the WSO2 Software License. See [LICENSE](LICENSE) for details.

