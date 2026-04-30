<!--
 ~ Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 ~
 ~ This software is the property of WSO2 LLC. and its suppliers, if any.
 ~ Dissemination of any information or reproduction of any material contained
 ~ herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 ~ You may not alter or remove any copyright or other notice from copies of this content.
-->

# CDS Toolkit Setup Guide

This guide covers deploying and configuring the three main components of the CDS Reference Implementation:

1. **Toolkit Webapp** — core CDS extension service that runs on WSO2 IS
2. **Account Metadata Webapp** — manages DOMS, secondary accounts, business-stakeholder permissions, and legal-entity blocking
3. **CDS Account Validation Policy** — WSO2 APIM Synapse mediator that enforces account-level restrictions on API calls

---

## Prerequisites

| Requirement                   | Version                    |
|-------------------------------|----------------------------|
| Java (OpenJDK)                 | 11 or above                |
| Apache Maven                  | 3.0.5 or above             |
| MySQL                         | 8.0                        |
| WSO2 Identity Server          | 7.1.0                      |
| WSO2 API Manager              | Compatible release for IS 7.1.0 (4.5.0) |
| WSO2 Open Banking Accelerator | 4.0.0                      |

> **Port reference used in this guide** — IS is assumed to run with `offset = 3`, giving a management HTTPS port of **9446** (9443 + 3). APIM management port is **9443** (default, no offset). Adjust URLs throughout if your setup differs.

---

## Part 1 — Toolkit Webapp

The toolkit webapp implements the CDS-specific OpenAPI extension endpoints called by the WSO2 Open Banking Accelerator running on IS. It handles the full consent lifecycle, DCR, event notifications, and authorization validation for the Australian CDS specification.

### 1.1 Build

```bash
cd components/reference-implementation-openbanking-consumerdatastandards-au
mvn clean install
```

Artifact produced:
```
components/reference-implementation-openbanking-consumerdatastandards-au/target/
└── api#reference-implementation#ob#consumerdatastandards#au.war
```

### 1.2 Deploy on WSO2 IS

```bash
cp components/reference-implementation-openbanking-consumerdatastandards-au/target/api#reference-implementation#ob#consumerdatastandards#au.war \
   <IS_HOME>/repository/deployment/server/webapps/
```

Deployed context path:
```
https://<IS_HOST>:9446/api/reference-implementation/ob/consumerdatastandards/au/
```

### 1.3 Internal Configuration — `ConfigurableProperties.java`

The file [components/reference-implementation-openbanking-consumerdatastandards-au/src/main/java/org/wso2/openbanking/consumerdatastandards/au/extensions/configurations/ConfigurableProperties.java](components/reference-implementation-openbanking-consumerdatastandards-au/src/main/java/org/wso2/openbanking/consumerdatastandards/au/extensions/configurations/ConfigurableProperties.java) holds all hard-coded configuration values. Update these before building if your environment differs from the defaults.

| Constant | Example value                                                                                                  | Purpose |
|---|----------------------------------------------------------------------------------------------------------------|---|
| `SHARABLE_ENDPOINT` | `http://<IS_HOST>:9766/api/openbanking/cds/backend/services/bankaccounts/bankaccountservice/sharable-accounts` | Demo backend URL for fetching shareable accounts |
| `CDS_HEADER_HOLDER_IDENTIFIER` | `HID`                                                                                                          | Header identifier key for the holder |
| `ENABLE_ACCOUNT_ID_VALIDATION_ON_RETRIEVAL` | `"true"`                                                                                                       | Toggles account ID validation during consent retrieval |
| `ACCOUNT_METADATA_WEBAPP_BASE_URL` | `http://<IS_HOST>:9766/ob/cds/account-metadata`                                                                | Base URL of the Account Metadata Webapp (Part 2) |
| `ACCOUNT_METADATA_WEBAPP_USERNAME` | `is_admin@wso2.com`                                                                                            | Basic Auth username for calling the Account Metadata Webapp |
| `ACCOUNT_METADATA_WEBAPP_PASSWORD` | `wso2123`                                                                                                      | Basic Auth password for calling the Account Metadata Webapp |
| `ACCOUNT_METADATA_WEBAPP_CONNECT_TIMEOUT_MILLIS` | `5000`                                                                                                         | HTTP connection timeout (ms) |
| `ACCOUNT_METADATA_WEBAPP_SOCKET_TIMEOUT_MILLIS` | `10000`                                                                                                        | HTTP socket/read timeout (ms) |
| `PROFILE_SELECTION_PAGE_ENABLED` | `true`                                                                                                         | Enables the CDS profile-selection page during authorization |

### 1.4 IS `deployment.toml` Changes

#### Access control for the toolkit webapp

```toml
[[resource.access_control]]
allowed_auth_handlers = ["BasicAuthentication"]
context = "(.*)/api/reference-implementation/ob/consumerdatastandards/au/(.*)"
http_method = "all"
secure = "true"
```

#### Enable and configure the extensions endpoint

Tells the Accelerator where the toolkit webapp is running and which extension hooks are active:

```toml
[financial_services.extensions.endpoint]
enabled = true
allowed_extensions = [
    "pre_process_client_creation", "pre_process_client_update", "pre_process_client_retrieval",
    "pre_process_consent_creation", "enrich_consent_creation_response",
    "pre_process_consent_file_upload", "enrich_consent_file_response",
    "pre_process_consent_retrieval", "validate_consent_file_retrieval",
    "pre_process_consent_revoke", "enrich_consent_search_response",
    "populate_consent_authorize_screen", "persist_authorized_consent",
    "validate_consent_access", "issue_refresh_token", "validate_authorization_request",
    "validate_event_subscription", "enrich_event_subscription_response",
    "validate_event_creation", "validate_event_polling", "enrich_event_polling_response",
    "map_accelerator_error_response"
]
base_url = "http://<IS_HOST>:9766/api/reference-implementation/ob/consumerdatastandards/au"
retry_count = 5
connect_timeout = 5
read_timeout = 5

[financial_services.extensions.endpoint.security]
type = "Basic-Auth"
username = "is_admin@wso2.com"
password = "wso2123"
```

> `base_url` uses the IS HTTP port (9766 = 9763 + 3 with offset 3). Use `https` and the management port if calling over TLS.

#### Consent authorize JSP (profile selection page)

```toml
[financial_services.consent.authorize_jsp]
path = "fs_cds_profile_selection.jsp"
```

Place the profile-selection files in the WSO2 IS authentication endpoint webapp:

| File | Source path in repository | Deployment path |
|---|---|---|
| `fs_cds_profile_selection.jsp` | `components/reference-implementation-openbanking-consumerdatastandards-au/src/main/resources/fs_cds_profile_selection.jsp` | `<IS_HOME>\repository\deployment\server\webapps\fs#authenticationendpoint\` |
| `profile-selection.js` | `components/reference-implementation-openbanking-consumerdatastandards-au/src/main/resources/profile-selection.js` | `<IS_HOME>\repository\deployment\server\webapps\fs#authenticationendpoint\includes\` |

#### Consent payload signing — prevent double-signing

The CDS Account Validation Policy (Part 3) re-signs the `Account-Request-Information` JWT in the APIM gateway. To prevent the consent enforcement policy from also signing the same payload, disable response payload signing here:

```toml
[financial_services.consent.validation]
response_payload_signing.enabled = false
```

#### Consent scope and claim configuration

```toml
[financial_services.consent]
is_pre_initiated_consent = false
auth_flow_consent_id_source = "requestObject"
consent_id_claim_name = "cdr_arrangement_id"
scope_based.scopes = [
    "accounts", "bank:accounts.basic:read", "bank:accounts.detail:read",
    "bank:payees:read", "bank:transactions:read", "common:customer.detail:read",
    "common:customer.basic:read", "bank:regular_payments:read", "common", "openid"
]

[financial_services.consent.consent_id_extraction]
json_path = "/cdr_arrangement_id"

[financial_services.identity]
consent_id_claim_name = "cdr_arrangement_id"
append_consent_id_to_token_id_token = false
append_consent_id_to_authz_id_token = true
append_consent_id_to_access_token = true
append_consent_id_to_token_introspect_response = false
```

---

## Part 2 — Account Metadata Webapp

The account metadata webapp provides REST APIs consumed by both the toolkit webapp and the APIM policy to make per-account decisions about data sharing.

| API path | Purpose |
|---|---|
| `/ob/cds/account-metadata/disclosure-options` | DOMS — joint account disclosure options |
| `/ob/cds/account-metadata/secondary-accounts` | Secondary account sharing instructions |
| `/ob/cds/account-metadata/business-stakeholders` | Business-account stakeholder permissions |
| `/ob/cds/account-metadata/legal-entity` | Legal-entity sharing block status |

### 2.1 Database Setup

Create a dedicated MySQL schema. For a schema-only bootstrap script that creates the database and the required tables, use [components/reference-implementation-openbanking-consumerdatastandards-au/src/main/resources/create_fs_account_metadatadb.sql](components/reference-implementation-openbanking-consumerdatastandards-au/src/main/resources/create_fs_account_metadatadb.sql).

If you prefer to run the SQL inline, the schema definition is:

```sql
CREATE DATABASE fs_account_metadatadb
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

The four tables used by the webapp are:

| Table | Purpose |
|---|---|
| `fs_account_doms_status` | Disclosure option status per account |
| `fs_account_bnr_permission` | Business-stakeholder permissions per account |
| `fs_account_secondary_user_legal_entity` | Legal-entity blocking records |
| `fs_account_secondary_user` | Secondary account sharing instructions |

### 2.2 Migrating from Accelerator 3.0 to 4.0

If migrating from an Accelerator 3.0 deployment to Accelerator 4.0 deployment , apply the migration script:

```bash
mysql -u root -p fs_account_metadatadb < \
  components/reference-implementation-openbanking-consumerdatastandards-au/src/main/resources/ob3_to_ob4_migration_mysql.sql
```

### 2.3 Build

```bash
cd components/account-metadata-service
mvn clean install
```

Artifact produced:
```
components/account-metadata-service/target/
└── ob#cds#account-metadata.war
```

### 2.4 Deploy on WSO2 IS

```bash
cp components/account-metadata-service/target/ob#cds#account-metadata.war \
   <IS_HOME>/repository/deployment/server/webapps/
```

Deployed context path:
```
https://<IS_HOST>:9446/ob/cds/account-metadata/
```

### 2.5 IS `deployment.toml` Changes

#### Add the `ACCOUNT_METADATA_DB` datasource

The webapp uses the JNDI name `jdbc/ACCOUNT_METADATA_DB`. Add this datasource block so IS registers it — it does **not** exist in a default IS configuration:

```toml
[[datasource]]
id = "ACCOUNT_METADATA_DB"
url = "jdbc:mysql://<DB_HOST>:3306/fs_account_metadatadb?allowPublicKeyRetrieval=true&amp;autoReconnect=true&amp;useSSL=false"
username = "root"
password = "<DB_PASSWORD>"
driver = "com.mysql.jdbc.Driver"
jmx_enable = false
```

> Place this alongside the other `[[datasource]]` entries such as `WSO2FS_DB`.

#### Access control for the account metadata webapp

```toml
[[resource.access_control]]
allowed_auth_handlers = ["BasicAuthentication"]
context = "(.*)/ob/cds/account-metadata/(.*)"
http_method = "all"
secure = "true"
```

### 2.6 Internal Configuration — `ConfigurableProperties.java`

The file [components/account-metadata-service/src/main/java/org/wso2/openbanking/consumerdatastandards/account/metadata/configurations/ConfigurableProperties.java](components/account-metadata-service/src/main/java/org/wso2/openbanking/consumerdatastandards/account/metadata/configurations/ConfigurableProperties.java) holds all hard-coded configuration values. Update these before building if your environment differs.

| Constant | Default value                                                          | Purpose |
|---|------------------------------------------------------------------------|---|
| `ACCOUNT_METADATA_DATASOURCE_JNDI_NAME` | `jdbc/ACCOUNT_METADATA_DB`                                             | JNDI name used to look up the datasource from IS |
| `IS_USERNAME` | `is_admin@wso2.com`                                                    | IS admin user for resolving client ID → legal entity ID |
| `IS_PASSWORD` | `wso2123`                                                              | IS admin password |
| `ACCELERATOR_TOKEN_ENDPOINT_URL` | `https://<IS_HOST>:9446/oauth2/token`                                  | IS OAuth2 token endpoint |
| `ACCELERATOR_CONSENT_SEARCH_URL` | `https://<IS_HOST>:9446/api/fs/consent/admin/search`                   | IS consent search API |
| `ACCELERATOR_CONSENT_UPDATE_BASE_URL` | `https://<IS_HOST>:9446/api/fs/consent/manage/account-access-consents` | IS consent expire (update) API |
| `IS_ADMIN_USERNAME` | `is_admin@wso2.com`                                                    | Admin credentials for Basic Auth on the consent update endpoint |
| `IS_ADMIN_PASSWORD` | `wso2123`                                                              | Admin password |
| `ACCOUNT_METADATA_CLIENT_APP_ID` | `<CLIENT_APP_ID>`                                                      | OAuth2 client ID of a valid registered app in IS with consent-manager functionality (used for consent search operations) |
| `ACCOUNT_METADATA_CLIENT_APP_SECRET` | `<SECRET_KEY>`                                                         | OAuth2 client secret of the same registered app |
| `CUSTOMER_CARE_OFFICER_USERNAME` | `<CUSTOMER_CARE_OFFICER_USERNAME>`                                     | Resource-owner user for password-grant consent queries |
| `CUSTOMER_CARE_OFFICER_PASSWORD` | `<CUSTOMER_CARE_OFFICER_PASSWORD>`                                     | Resource-owner password |
| `CUSTOMER_CARE_OFFICER_TOKEN_SCOPE` | `consents:read_all`                                                    | Scope for resource-owner consent queries |
| `ACCELERATOR_CONNECT_TIMEOUT_MILLIS` | `5000`                                                                 | HTTP connection timeout (ms) for IS calls |
| `ACCELERATOR_SOCKET_TIMEOUT_MILLIS` | `10000`                                                                | HTTP socket/read timeout (ms) for IS calls |

> **Client app requirement for consent retrieval**
> Use a real application registered in IS (not a placeholder client) that supports Consent Manager flows/capabilities, because this client is used by the account metadata service for consent retrieval operations.
> Reference setup: https://ob.docs.wso2.com/en/latest/learn/consent-manager/

---

## Part 3 — CDS Account Validation Policy (APIM Mediator)

This is a custom Synapse mediator deployed on WSO2 API Manager. It intercepts requests to CDS banking resource APIs, reads the `Account-Request-Information` JWT header injected by the consent enforcement policy, removes any blocked accounts (based on DOMS, secondary accounts, business-stakeholder permissions, and legal-entity blocks), and re-signs the JWT before passing the request downstream.

### 3.1 Build

```bash
cd components/policies/cds-account-validation-policy
mvn clean install
```

Artifact produced:
```
components/policies/cds-account-validation-policy/target/
└── cds-account-validation-payload-mediator-<version>.jar
```

### 3.2 Deploy on WSO2 APIM

Copy the mediator JAR to the APIM lib folder and restart APIM:

```bash
cp components/policies/cds-account-validation-policy/target/cds-account-validation-payload-mediator-*.jar \
   <APIM_HOME>/repository/components/lib/
```

Restart APIM after copying for the class to be picked up by the OSGi runtime.

### 3.3 Policy Placement in the API Mediation Flow

The CDS Account Validation Policy must be placed **between** the **Consent Enforcement Policy** and the **Dynamic Endpoint Policy** in the API's request mediation sequence. This ordering ensures:

1. Consent Enforcement Policy runs first — validates the consent, injects the `Account-Request-Information` JWT header with the authorized account list.
2. **CDS Account Validation Policy** runs next — reads the JWT, removes accounts blocked by DOMS / secondary-account / business-stakeholder / legal-entity rules, and re-signs the JWT.
3. Dynamic Endpoint Policy runs last — routes the cleaned-up request to the backend.

```
Request flow:
  [ Consent Enforcement Policy ]
           ↓
  [ CDS Account Validation Policy ]   ← insert here
           ↓
  [ Dynamic Endpoint Policy ]
           ↓
  [ Backend ]
```

To configure this in the APIM Publisher:

1. Open the API → **Policies** tab.
2. In the **Request** flow, drag **Consent Enforcement Policy** to its position.
3. Drag **CDS Account Validation Policy** immediately after it, and before **Dynamic Endpoint Policy**.
4. Configure the policy properties as described below.

### 3.4 Policy Properties

When attaching the policy via the APIM Publisher UI, two properties must be set:

| Property | Description | Example value |
|---|---|---|
| `webappBaseURL` | Base URL of the Account Metadata Webapp (no trailing slash) | `https://<IS_HOST>:9446/ob/cds/account-metadata` |
| `basicAuthCredentials` | Base64-encoded `username:password` for calling the Account Metadata Webapp | `aXNfYWRtaW5Ad3NvMi5jb206d3NvMjEyMw==` |

> **Note:** In the current implementation, this policy supports only Basic Auth when calling the Account Metadata Webapp. If you need to support other authentication mechanisms, the policy implementation must be changed.

To generate the Base64 credential:
```bash
echo -n "is_admin@wso2.com:wso2123" | base64
```

In a custom Synapse sequence XML the mediator is wired as:

```xml
<class name="org.wso2.openbanking.consumerdatastandards.au.policy.CDSAccountValidationMediator">
    <property name="webappBaseURL" value="https://<IS_HOST>:9446/ob/cds/account-metadata"/>
    <property name="basicAuthCredentials" value="<BASE64(username:password)>"/>
</class>
```

## Summary of `deployment.toml` Changes

### WSO2 IS (`<IS_HOME>/repository/conf/deployment.toml`)

| Config section | Change | Notes |
|---|---|---|
| `[[datasource]] id = "ACCOUNT_METADATA_DB"` | **Add new block** | MySQL datasource for `fs_account_metadatadb` |
| `[[resource.access_control]] /ob/cds/account-metadata/` | **Add new block** | Secures the Account Metadata Webapp |
| `[[resource.access_control]] /api/reference-implementation/ob/consumerdatastandards/au/` | **Add new block** | Secures the Toolkit Webapp |
| `[financial_services.extensions.endpoint] enabled` | **Change to `true`** | Activates the extension endpoint |
| `[financial_services.extensions.endpoint] base_url` | **Set URL** | Point to the toolkit webapp host/port/context |
| `[financial_services.extensions.endpoint] allowed_extensions` | **Expand list** | Include all CDS extension hooks |
| `[financial_services.extensions.endpoint.security]` | **Set credentials** | Username/password for calling the toolkit webapp |
| `[financial_services.consent.validation] response_payload_signing.enabled` | **Set to `false`** | Prevents double-signing; the APIM mediator re-signs instead |
| `[financial_services.consent.authorize_jsp] path` | **Set value** | Points to `fs_cds_profile_selection.jsp` |
| `[financial_services.consent]` / `[financial_services.identity]` | **Update values** | CDS consent ID claim name, scope list, token attachment flags |

### WSO2 APIM (`<APIM_HOME>/repository/conf/deployment.toml`)

| Config section | Change | Notes |
|---|---|---|
| `[apim.key_manager] service_url` | **Update** | Set to IS 9446 management port |
| `[apim.key_manager] username / password` | **Update** | IS admin credentials |
| `[apim.oauth_config] white_listed_scopes` | **Update** | Add `^FS_.*` and `^TIME_.*` |
| `[[financial_services.keymanager.application.type.attributes]]` | **Add two new blocks** | `regulatory` and `sp_certificate` custom attributes |
| `[synapse_handlers] JwsResponseHeaderHandler.enabled` | **Add, set `false`** | Keeps JWS handler registered but inactive |
| `[financial_services.extensions.endpoint] enabled` | **Set `false`** | APIM-side extension hooks disabled (not used for CDS) |

---

## Component Interaction Diagram

```
[Data Consumer / TPP]
        |
        v (API call with access token)
[WSO2 APIM Gateway]
  Policy flow (request):
    1. Consent Enforcement Policy
       → validates consent, injects Account-Request-Information JWT
    2. CDS Account Validation Policy  (mediator JAR)
       → calls Account Metadata Webapp → fetches blocked accounts
       → removes blocked accounts, re-signs JWT
    3. Dynamic Endpoint Policy
       → routes to backend

[Account Metadata Webapp]  ← called by APIM mediator and Toolkit Webapp
  /ob/cds/account-metadata/*
  Reads from: fs_account_metadatadb (MySQL)
  Calls IS for: consent search, consent expiry

[WSO2 IS]
  Toolkit Webapp  (extension endpoints consumed by Accelerator)
  Consent lifecycle, DCR, authorization, token issuance
  Calls Account Metadata Webapp for DOMS status during consent authorization
```

---

## Troubleshooting

| Symptom | Likely cause | Fix |
|---|---|---|
| Account metadata webapp returns `401` | Access control not configured or wrong credentials | Add `[[resource.access_control]]` for `/ob/cds/account-metadata/` in IS `deployment.toml`; verify Basic Auth credentials |
| Toolkit webapp extension calls fail | `enabled = false` or wrong `base_url` | Set `enabled = true` and correct `base_url` in `[financial_services.extensions.endpoint]` |
| Mediator JAR not loaded | JAR not in lib or APIM not restarted | Copy JAR to `<APIM_HOME>/repository/components/lib/` and restart APIM |
| `ACCOUNT_METADATA_DB` datasource not found at startup | Missing `[[datasource]]` block or DB unreachable | Add the `id = "ACCOUNT_METADATA_DB"` datasource block in IS `deployment.toml`; verify MySQL is running and `fs_account_metadatadb` exists |
| Response payload double-signed | `response_payload_signing.enabled` is `true` | Set `response_payload_signing.enabled = false` in `[financial_services.consent.validation]` in IS `deployment.toml` |
| Policy order wrong — accounts not filtered | Mediator placed after Dynamic Endpoint Policy | Re-order: Consent Enforcement → CDS Account Validation → Dynamic Endpoint |
