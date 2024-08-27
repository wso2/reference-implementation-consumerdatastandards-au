#!/bin/bash
# ------------------------------------------------------------------------
#
# Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
#
# WSO2 LLC. licenses this file to you under the Apache License,
# Version 2.0 (the "License"); you may not use this file except
# in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.
#
# ------------------------------------------------------------------------

# merge.sh script copy the WSO2 OB IS CDS Toolkit artifacts on top of WSO2 IS base product
#
# merge.sh <WSO2_OB_IS_HOME>

WSO2_OB_IS_HOME=$1

# set toolkit home
cd ../
TOOLKIT_HOME=$(pwd)
echo "Toolkit home is: ${TOOLKIT_HOME}"

# set product home
if [ "${WSO2_OB_IS_HOME}" == "" ];
  then
    cd ../
    WSO2_OB_IS_HOME=$(pwd)
    echo "Product home is: ${WSO2_OB_IS_HOME}"
fi

# validate product home
if [ ! -d "${WSO2_OB_IS_HOME}/repository/components" ]; then
  echo -e "\n\aERROR:specified product path is not a valid carbon product path\n";
  exit 2;
else
  echo -e "\nValid carbon product path.\n";
fi

echo -e "\nRemoving old open banking artifacts from the base product\n"
echo -e "================================================\n"
find "${WSO2_OB_IS_HOME}"/repository/components/dropins -name "org.wso2.openbanking.cds.*" -exec rm -rf {} \;
find "${WSO2_OB_IS_HOME}"/repository/components/lib -name "org.wso2.openbanking.cds.*" -exec rm -rf {} \;
find "${WSO2_OB_IS_HOME}"/repository/deployment/server/webapps -name "api#openbanking#account-type-mgt.war" -exec rm -rf {} \;
find "${WSO2_OB_IS_HOME}"/repository/deployment/server/webapps -name "consentmgr.war" -exec rm -rf {} \;

echo -e "\nCopying open banking artifacts\n"
echo -e "================================================\n"
cp -r ${TOOLKIT_HOME}/carbon-home/* "${WSO2_OB_IS_HOME}"/
echo -e "\nComplete!\n"

echo -e "\nWARNING: This will replace the current consentmgr dist with the updated one.\n"
echo -e "\nExtracting consentmgr_cds.zip\n"
echo -e "================================================\n"
unzip -q "${WSO2_OB_IS_HOME}/repository/deployment/server/webapps/consentmgr/consentmgr_cds.zip" -d "${WSO2_OB_IS_HOME}/repository/deployment/server/webapps/consentmgr"
rm -f "${WSO2_OB_IS_HOME}/repository/deployment/server/webapps/consentmgr/consentmgr_cds.zip"
echo -e "\nComplete! \n"

echo -e "\nRemoving current consentmgr dist directory\n"
echo -e "================================================\n"
rm -rf  "${WSO2_OB_IS_HOME}/repository/deployment/server/webapps/consentmgr/dist"
echo -e "\nComplete! \n"

echo -e "\nCopying files from consentmgr_cds to consentmgr\n"
echo -e "================================================\n"
cp -r "${WSO2_OB_IS_HOME}/repository/deployment/server/webapps/consentmgr/consentmgr_cds/dist/." "${WSO2_OB_IS_HOME}/repository/deployment/server/webapps/consentmgr/dist"
cp -r "${WSO2_OB_IS_HOME}/repository/deployment/server/webapps/consentmgr/consentmgr_cds/self-care-portal-frontend/." "${WSO2_OB_IS_HOME}/repository/deployment/server/webapps/consentmgr/self-care-portal-frontend"
echo -e "\nComplete! \n"

echo -e "\nRemoving consentmgr_cds directory\n"
echo -e "================================================\n"
rm -rf  "${WSO2_OB_IS_HOME}/repository/deployment/server/webapps/consentmgr/consentmgr_cds"
echo -e "\nComplete! \n"
