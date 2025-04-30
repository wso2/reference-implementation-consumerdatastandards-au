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

# merge.sh script copy the WSO2 OB APIM CDS Toolkit artifacts on top of WSO2 APIM base product
#
# merge.sh <WSO2_OB_APIM_HOME>

WSO2_OB_APIM_HOME=$1

# set toolkit home
cd ../
TOOLKIT_HOME=$(pwd)
echo "Toolkit home is: ${TOOLKIT_HOME}"

# set product home
if [ "${WSO2_OB_APIM_HOME}" == "" ];
  then
    cd ../
    WSO2_OB_APIM_HOME=$(pwd)
    echo "Product home is: ${WSO2_OB_APIM_HOME}"
fi

# validate product home
if [ ! -d "${WSO2_OB_APIM_HOME}/repository/components" ]; then
  echo -e "\n\aERROR:specified product path is not a valid carbon product path\n";
  exit 2;
else
  echo -e "\nValid carbon product path.\n";
fi

echo -e "\nRemoving default backend war\n"
echo -e "================================================\n"
rm -f ${WSO2_OB_APIM_HOME}/repository/deployment/server/webapps/api#openbanking#backend.war

echo -e "\nRemoving old open banking artifacts from the base product\n"
echo -e "================================================\n"
find "${WSO2_OB_APIM_HOME}"/repository/components/dropins -name "org.wso2.openbanking.cds.*" -exec rm -rf {} \;
find "${WSO2_OB_APIM_HOME}"/repository/components/lib -name "org.wso2.openbanking.cds.*" -exec rm -rf {} \;
find "${WSO2_OB_APIM_HOME}"/repository/deployment/server/webapps -name "api#openbanking#cds#backend.war" -exec rm -rf {} \;

echo -e "\nCopying open banking artifacts\n"
echo -e "================================================\n"
cp -r ${TOOLKIT_HOME}/carbon-home/* "${WSO2_OB_APIM_HOME}"/
echo -e "\nCopying .swagger-validator file\n"
cp ${TOOLKIT_HOME}/carbon-home/.swagger-validator "${WSO2_OB_APIM_HOME}"/
echo -e "\nComplete!\n"
