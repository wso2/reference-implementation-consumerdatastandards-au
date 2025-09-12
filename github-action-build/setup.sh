#!/bin/bash
# Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

set -e
RUNNER_HOME=`pwd`
echo "RUNNER_HOME: $RUNNER_HOME"
ARTIFACTS_DIR="$RUNNER_HOME/github-action-build/product-artifacts"
OBIE_CERTS_DIR="$RUNNER_HOME/integration-test-suite/test-artifacts/obie-info/sandbox-certs"

echo '======================= SetUp base Products ======================='
# Create the test home directory if it doesn't exist
if [ ! -d "$TEST_HOME" ]; then
    mkdir -p $TEST_HOME
fi

echo '======================= Extracting Base Products to test home directory======================='
wget "https://atuwa.private.wso2.com/WSO2-Products/identity-server/6.1.0/wso2is-6.1.0.zip" -O $TEST_HOME/wso2is-6.1.0.zip
unzip $TEST_HOME/wso2is-6.1.0.zip -d $TEST_HOME

wget "https://atuwa.private.wso2.com/WSO2-Products/api-manager/4.2.0/APIM/wso2am-4.2.0.zip" -O $TEST_HOME/wso2am-4.2.0.zip
unzip $TEST_HOME/wso2am-4.2.0.zip -d $TEST_HOME

cp $ARTIFACTS_DIR/wso2si-4.2.0.zip $TEST_HOME
unzip $TEST_HOME/wso2si-4.2.0.zip -d $TEST_HOME

echo '======================= Extracting Accelerator Artifacts to product home directory======================='
wget "https://atuwa.private.wso2.com/WSO2-Products/open-banking-accelerators/3.0.0/wso2-obiam-accelerator-3.0.0.zip" -O $TEST_HOME/wso2-obiam-accelerator-3.0.0.zip
unzip $TEST_HOME/wso2-obiam-accelerator-3.0.0.zip -d $TEST_HOME/wso2is-6.1.0

wget "https://atuwa.private.wso2.com/WSO2-Products/open-banking-accelerators/3.0.0/wso2-obam-accelerator-3.0.0.zip" -O $TEST_HOME/wso2-obam-accelerator-3.0.0.zip
unzip $TEST_HOME/wso2-obam-accelerator-3.0.0.zip -d $TEST_HOME/wso2am-4.2.0

wget "https://atuwa.private.wso2.com/WSO2-Products/open-banking-accelerators/3.0.0/wso2-obbi-accelerator-3.0.0.zip" -O $TEST_HOME/wso2-obbi-accelerator-3.0.0.zip
unzip $TEST_HOME/wso2-obbi-accelerator-3.0.0.zip -d $TEST_HOMER/wso2si-4.2.0

# Extract the WSO2 IS Connector to the TEST_HOME directory
cp $ARTIFACTS_DIR/wso2is-extensions-1.6.8.zip $TEST_HOME
unzip $TEST_HOME/wso2is-extensions-1.6.8.zip -d $TEST_HOME


echo '======================= Installing WSO2 Updates ======================='
echo '======================= Installing WSO2 IS Updates ===================='
name=$(echo "$WSO2_USERNAME" | cut -d'@' -f1)
WSO2_UPDATES_HOME=home/$name/.wso2updates
sudo mkdir -p /home/$name/.wso2-updates/docker && sudo chmod -R 777 /home/$name/.wso2-updates
cp ${RUNNER_HOME}/github-action-build/wso2update_linux $TEST_HOME/wso2is-6.1.0/bin/
chmod +x $TEST_HOME/wso2is-6.1.0/bin/wso2update_linux
$TEST_HOME/wso2is-6.1.0/bin/wso2update_linux -v --username $WSO2_USERNAME --password $WSO2_U2_PASSWORD ||  ($TEST_HOME/wso2is-6.1.0/bin/wso2update_linux -v --username $WSO2_USERNAME --password $WSO2_U2_PASSWORD )

echo '======================= Installing WSO2 API-M Updates ===================='
name=$(echo "$WSO2_USERNAME" | cut -d'@' -f1)
WSO2_UPDATES_HOME=home/$name/.wso2updates
sudo mkdir -p /home/$name/.wso2-updates/docker && sudo chmod -R 777 /home/$name/.wso2-updates
cp ${RUNNER_HOME}/github-action-build/wso2update_linux $TEST_HOME/wso2am-4.2.0/bin/
chmod +x $TEST_HOME/wso2am-4.2.0/bin/wso2update_linux
$TEST_HOME/wso2am-4.2.0/bin/wso2update_linux -v --username $WSO2_USERNAME --password $WSO2_U2_PASSWORD ||  ($TEST_HOME/wso2am-4.2.0/bin/wso2update_linux -v --username $WSO2_USERNAME --password $WSO2_U2_PASSWORD )

echo '======================= Installing WSO2 SI Updates ===================='
name=$(echo "$WSO2_USERNAME" | cut -d'@' -f1)
WSO2_UPDATES_HOME=home/$name/.wso2updates
sudo mkdir -p /home/$name/.wso2-updates/docker && sudo chmod -R 777 /home/$name/.wso2-updates
cp ${RUNNER_HOME}/github-action-build/wso2update_linux $TEST_HOME/wso2si-4.2.0/bin/
chmod +x $TEST_HOME/wso2si-4.2.0/bin/wso2update_linux
$TEST_HOME/wso2si-4.2.0/bin/wso2update_linux -v --username $WSO2_USERNAME --password $WSO2_U2_PASSWORD ||  ($TEST_HOME/wso2si-4.2.0/bin/wso2update_linux -v --username $WSO2_USERNAME --password $WSO2_U2_PASSWORD )

# Updating Accelerator Packs
echo '======================= Installing WSO2 IS Accelerator Updates ===================='
name=$(echo "$WSO2_USERNAME" | cut -d'@' -f1)
WSO2_UPDATES_HOME=home/$name/.wso2updates
sudo mkdir -p /home/$name/.wso2-updates/docker && sudo chmod -R 777 /home/$name/.wso2-updates
cp ${RUNNER_HOME}/github-action-build/wso2update_linux $TEST_HOME/wso2is-6.1.0/wso2-obiam-accelerator-3.0.0/bin/
chmod +x $TEST_HOME/wso2is-6.1.0/wso2-obiam-accelerator-3.0.0/bin/wso2update_linux
$TEST_HOME/wso2-obiam-accelerator-3.0.0/wso2is-6.1.0/bin/wso2update_linux -v --username $WSO2_USERNAME --password $WSO2_U2_PASSWORD ||  ($TEST_HOME/wso2-obiam-accelerator-3.0.0/wso2is-6.1.0/bin/wso2update_linux -v --username $WSO2_USERNAME --password $WSO2_U2_PASSWORD )

echo '======================= Installing WSO2 API-M Accelerator Updates ===================='
name=$(echo "$WSO2_USERNAME" | cut -d'@' -f1)
WSO2_UPDATES_HOME=home/$name/.wso2updates
sudo mkdir -p /home/$name/.wso2-updates/docker && sudo chmod -R 777 /home/$name/.wso2-updates
cp ${RUNNER_HOME}/github-action-build/wso2update_linux $TEST_HOME/wso2am-4.2.0/wso2-obam-accelerator-3.0.0/bin/
chmod +x $TEST_HOME/wso2am-4.2.0/wso2-obam-accelerator-3.0.0/bin/wso2update_linux
$TEST_HOME/wso2am-4.2.0/wso2-obam-accelerator-3.0.0/bin/wso2update_linux -v --username $WSO2_USERNAME --password $WSO2_U2_PASSWORD ||  ($TEST_HOME/wso2am-4.2.0/wso2-obam-accelerator-3.0.0/bin/wso2update_linux -v --username $WSO2_USERNAME --password $WSO2_U2_PASSWORD )

echo '======================= Installing WSO2 BI Accelerator Updates ===================='
name=$(echo "$WSO2_USERNAME" | cut -d'@' -f1)
WSO2_UPDATES_HOME=home/$name/.wso2updates
sudo mkdir -p /home/$name/.wso2-updates/docker && sudo chmod -R 777 /home/$name/.wso2-updates
cp ${RUNNER_HOME}/github-action-build/wso2update_linux $TEST_HOME/wso2si-4.2.0/wso2-obbi-accelerator-3.0.0/bin/
chmod +x $TEST_HOME/wso2si-4.2.0/wso2-obbi-accelerator-3.0.0/bin/wso2update_linux
$TEST_HOME/wso2si-4.2.0/wso2-obbi-accelerator-3.0.0/bin/wso2update_linux -v --username $WSO2_USERNAME --password $WSO2_U2_PASSWORD ||  ($TEST_HOME/wso2si-4.2.0/wso2-obbi-accelerator-3.0.0/bin/wso2update_linux -v --username $WSO2_USERNAME --password $WSO2_U2_PASSWORD )

echo '=================== Setup Firefox ==================='
if command -v firefox &> /dev/null
then
    echo "Firefox is installed"
else
    sudo install -d -m 0755 /etc/apt/keyrings
    wget -q https://packages.mozilla.org/apt/repo-signing-key.gpg -O- | sudo tee /etc/apt/keyrings/packages.mozilla.org.asc > /dev/null
    gpg -n -q --import --import-options import-show /etc/apt/keyrings/packages.mozilla.org.asc | awk '/pub/{getline; gsub(/^ +| +$/,""); if($0 == "35BAA0B33E9EB396F59CA838C0BA5CE6DC6315A3") print "\nThe key fingerprint matches ("$0").\n"; else print "\nVerification failed: the fingerprint ("$0") does not match the expected one.\n"}'
    echo "deb [signed-by=/etc/apt/keyrings/packages.mozilla.org.asc] https://packages.mozilla.org/apt mozilla main" | sudo tee -a /etc/apt/sources.list.d/mozilla.list > /dev/null
    echo '
        Package: *
        Pin: origin packages.mozilla.org
        Pin-Priority: 1000
          ' | sudo tee /etc/apt/preferences.d/mozilla
    sudo apt-get update && sudo apt-get install firefox
    firefox -version
fi


echo '======================= Building packs ======================='
#
mvn clean install -P solution --file ${RUNNER_HOME}/pom.xml
MVNSTATE=$?
#
echo '======================= Moving Packs to RUNNER_HOME ======================='
echo '======================= Moving IS Toolkit to IS HOME ======================'
is_zip_file_name=$(find toolkits/ob-is/target -maxdepth 1 -name "*.zip" -exec basename {} .zip \;)
echo "$is_zip_file_name"
unzip "toolkits/ob-is/target/$is_zip_file_name.zip" -d $TEST_HOME/wso2is-6.1.0/

echo '======================= Moving API-M Toolkit to API-M HOME ======================'
am_zip_file_name=$(find toolkits/ob-apim/target -maxdepth 1 -name "*.zip" -exec basename {} .zip \;)
echo "$am_zip_file_name"
unzip "toolkits/ob-apim/target/$am_zip_file_name.zip" -d $TEST_HOME/wso2am-4.2.0/

echo '======================= Moving BI Toolkit to SI HOME ======================'
bi_zip_file_name=$(find toolkits/ob-bi/target -maxdepth 1 -name "*.zip" -exec basename {} .zip \;)
echo "$bi_zip_file_name"
unzip "toolkits/ob-bi/target/$bi_zip_file_name.zip" -d $TEST_HOME/wso2si-4.2.0/


echo '======================= Setup MYSQL ======================='
sudo apt-get update
sudo apt-get install -y mysql-server
sudo systemctl start mysql
mysql --version

echo '======================= Increase Maximum Connections ======================='
# Log into MySQL as root (or sudo)
sudo mysql -u root -p

# Inside MySQL prompt, run:
SET GLOBAL max_connections = 500;


echo '======================= Run Accelerator merge and Config scripts ======================='
cd $TEST_HOME/wso2is-6.1.0/wso2-obiam-accelerator-3.0.0/bin
bash merge.sh
bash configure.sh

cd $TEST_HOME/wso2am-4.2.0/wso2-obam-accelerator-3.0.0/bin
bash merge.sh
bash configure.sh

cd $TEST_HOME/wso2si-4.2.0/wso2-obbi-accelerator-3.0.0/bin
bash merge.sh
bash configure.sh

echo '======================= Run Toolkit merge and Config scripts ======================='
cd $TEST_HOME/wso2is-6.1.0/$is_zip_file_name/bin
chmod +x merge.sh configure.sh
bash merge.sh
bash configure.sh

cd $TEST_HOME/wso2am-4.2.0/$am_zip_file_name/bin
chmod +x merge.sh configure.sh
bash merge.sh
bash configure.sh

cd $TEST_HOME/wso2si-4.2.0/$bi_zip_file_name/bin
chmod +x merge.sh configure.sh
bash merge.sh
bash configure.sh

echo '======================= Copy IS Extensions =========================='
cp -r $TEST_HOME/wso2is-extensions-1.6.8/dropins/* $TEST_HOME/wso2is-6.1.0/repository/components/dropins
cp -r $TEST_HOME/wso2is-extensions-1.6.8/webapps/* $TEST_HOME/wso2is-6.1.0/repository/deployment/server/webapps

echo '======================= Mock SMS Authenticator for Testing Purpose =========================='
DROPINS_DIR="$TEST_HOME/wso2is-6.1.0/repository/components/dropins"
NEW_JAR_SOURCE="$TEST_HOME/org.wso2.carbon.extension.identity.authenticator.smsotp.connector-3.0.4.jar"

# Remove any existing smsotp connector JARs
rm -f "$DROPINS_DIR"/org.wso2.carbon.extension.identity.authenticator.smsotp.connector-*.jar

cp $ARTIFACTS_DIR/org.wso2.carbon.extension.identity.authenticator.smsotp.connector-3.0.4.jar $DROPINS_DIR

# Check if new JAR exists (supports wildcards)
NEW_JAR_FOUND=( $NEW_JAR_SOURCE )
if [ -e "${NEW_JAR_FOUND[0]}" ]; then
    echo "Adding new JAR: ${NEW_JAR_FOUND[0]}"
    cp $NEW_JAR_SOURCE "$DROPINS_DIR/"
else
    echo "No new JAR found matching: $NEW_JAR_SOURCE"
    exit 1
fi


echo '======================= Download and install Drivers ======================='
wget -q https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/9.2.0/mysql-connector-j-9.2.0.jar
mv mysql-connector-j-9.2.0.jar $TEST_HOME/wso2is-6.1.0/repository/components/lib
mv mysql-connector-j-9.2.0.jar $TEST_HOME/wso2am-4.2.0/repository/components/lib
mv mysql-connector-j-9.2.0.jar $TEST_HOME/wso2si-4.2.0/bin

# Convert to OSGI bundle and move to lib folder
sh $TEST_HOME/wso2si-4.2.0/bin/jartobundle.sh mysql-connector-j-9.2.0.jar $TEST_HOME/wso2si-4.2.0/lib

echo '======================= Update DB Column Size ======================='
SQL_SCRIPT=$ARTIFACTS_DIR/update_columns.sql
mysql -h "$DB_HOST" -u "$DB_USER" -p"$DB_PASS" "openbank_apimgtdb" < "$SQL_SCRIPT"


echo '======================= Update deployment.toml ======================='
cp $ARTIFACTS_DIR/configuration-files/is/deployment.toml $TEST_HOME/wso2is-6.1.0/repository/conf/
cp $ARTIFACTS_DIR/configuration-files/am/deployment.toml $TEST_HOME/wso2am-4.2.0/repository/conf/


echo '======================= Copy Modified Certs to Base Packs ======================='
# Certs inside the product-artifacts/security_certs folder are updated with OBIE sandbox certs and the host mapping
cp $ARTIFACTS_DIR/security_certs/is_certs/wso2carbon.jks $TEST_HOME/wso2is-6.1.0/repository/resources/security/
cp $ARTIFACTS_DIR/security_certs/is_certs/client-truststore.jks $TEST_HOME/wso2is-6.1.0/repository/resources/security/

cp $ARTIFACTS_DIR/security_certs/am_certs/wso2carbon.jks $TEST_HOME/wso2am-4.2.0/repository/resources/security/
cp $ARTIFACTS_DIR/security_certs/am_certs/client-truststore.jks $TEST_HOME/wso2am-4.2.0/repository/resources/security/

cp $ARTIFACTS_DIR/security_certs/si_certs/wso2carbon.jks $TEST_HOME/wso2si-4.2.0/resources/security/
cp $ARTIFACTS_DIR/security_certs/si_certs/client-truststore.jks $TEST_HOME/wso2si-4.2.0/resources/security/


echo '======================= Starting SI server ======================='
cd $TEST_HOME/wso2si-4.2.0/bin
./wso2server.sh  start
sleep 60

echo '======================= Starting IS server ======================='
cd $TEST_HOME/wso2is-6.1.0/bin
./wso2server.sh  start
sleep 120

echo '======================= Starting API-M server ======================='
cd $TEST_HOME/wso2am-4.2.0/bin
./wso2server.sh  start
sleep 120
