### **WSO2 Open Banking APIM CDS Reference Toolkit 1.0.0**

**Try Locally:**

Prerequisites:
1. Download WSO2 API Manager product 
2. Download the WSO2 Open Banking API Management Accelerator 
3. Download the WSO2 Open Banking API Management CDS Toolkit
4. Setup MySQL database server
5. Install Java on your local machine

Steps:
1. Extract WSO2 APIM <WSO2_APIM_HOME>
2. Extract WSO2 OB APIM Accelerator to WSO2_APIM_HOME 
3. Run <WSO2_OB_APIM_ACC_HOME>/bin/merge.sh. This will copy the artifacts to the WSO2 APIM
4. Run <WSO2_OB_APIM_ACC_HOME>/bin/configure.sh. This will configure the server and create databases and  tables.

NOTE: Above step is not recommended in the production environments. We recommend creating databases and configure the products manually in production environment.

5. Extract WSO2 OB APIM CDS Toolkit to WSO2_APIM_HOME
6. Run <WSO2_OB_APIM_CDS_TOOLKIT_HOME>/bin/merge.sh. This will copy the artifacts to the WSO2 APIM
7. Run <WSO2_OB_APIM_CDS_TOOLKIT_HOME>/bin/configure.sh. This will configure the server.
8. Run <WSO2_APIM_HOME>/bin/api-manager.sh to start the server

