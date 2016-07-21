
#!/bin/bash
################################################################################################
## This is and deployment script for a aurion gateway. This script will perform      ##
## either an initial deployment or a redeployment of aurion gateway. This sript works with   ##
## the version of Aurion that is bundled with Aurion 5.1.                            ##
##                                                                                            ##
## The deploy.properties file must be updated prior to running this deploy script.            ##
################################################################################################

# Source the properties file
. deploy.properties

function displayAbort()
{
    echo "Deployment aborted!"
}

function validateProperty()
{
    if [ 'a' == 'a'$2 ]; then
        echo "The $1 property must be set in the deploy.properties file"
        displayAbort
        exit
    fi
}

# Usage: updatePropertiesFile fileName property propertyValue
function updatePropertiesFile()
{
    sed -i '/'$2'/c '$2'='$3 $1
}

# Usage: updateDatabaseConfig hibernateConfigFile connectionString username password
function updateDatabaseConfig()
{
    sed -i '/name="connection.driver_class"/c <property name="connection.driver_class">'$databaseDriver'</property>' $1
    sed -i '/name="dialect"/c <property name="dialect">'$databaseDialect'</property>' $1
    sed -i '/name="connection.url"/c <property name="connection.url">'$2'</property>' $1
    sed -i '/name="connection.username"/c <property name="connection.username">'$3'</property>' $1
    sed -i '/name="connection.password"/c <property name="connection.password">'$4'</property>' $1
}

# Usage: updateDatabaseConfigDialectOnly hibernateConfigFile
function updateDatabaseConfigDialectOnly()
{
    sed -i '/name="dialect"/c <property name="dialect">'$databaseDialect'</property>' $1
}

# Verify that the properties file has been updated
validateProperty "domainLocation" $domainLocation
validateProperty "domainName" $domainName
validateProperty "localHomeCommunityId" $localHomeCommunityId
validateProperty "localDeviceId" $localDeviceId
validateProperty "assigningAuthorityId" $assigningAuthorityId
validateProperty "XDSbHomeCommunityId" $XDSbHomeCommunityId
validateProperty "override_mime_message_id" $override_mime_message_id
validateProperty "org_apache_ws_security_saml_issuer_key_name" $org_apache_ws_security_saml_issuer_key_name
validateProperty "databaseDriver" $databaseDriver
validateProperty "databaseDialect" $databaseDialect
validateProperty "nhincConnectionString" $nhincConnectionString 
validateProperty "nhincUsername" $nhincUsername
validateProperty "nhincPassword" $nhincPassword

# Create backup directory if necessary
if [ ! -d ../backup ]; then
    echo "Creating backup directory"
    mkdir ../backup
fi

# All files to be deployed
DEPLOY_FILES="../CONNECT-GF/*.ear"

# Start server
asadmin start-domain $domainName

# Undeploy EARs
for f in $DEPLOY_FILES
do
    filewithext=$(basename $f)
    filename="${filewithext%.*}"
    echo "Undeploying $filename"
    asadmin undeploy $filename
done

# Stop server
asadmin stop-domain $domainName

# Create domain/config/nhin directory if necessary
if [ ! -d "$domainLocation/config/nhin" ]; then
    echo "Creating directory: $domainLocation/config/nhin"
    mkdir $domainLocation/config/nhin
fi

# Backup configuration files if they exist
if [ -f "$domainLocation/config/nhin/internalConnectionInfo.xml" ]; then
    echo "Backing up internalConnectionInfo.xml"
    cp -f "$domainLocation/config/nhin/internalConnectionInfo.xml" ../backup
fi
if [ -f "$domainLocation/config/nhin/uddiConnectionInfo.xml" ]; then
    echo "Backing up uddiConnectionInfo.xml"
    cp -f "$domainLocation/config/nhin/uddiConnectionInfo.xml" ../backup
fi
if [ -f "$domainLocation/config/nhin/saml.properties" ]; then
    echo "Backing up saml.properties"
    cp -f "$domainLocation/config/nhin/saml.properties" ../backup
fi

# Copy to config
echo "Copying configuration files to $domainLocation/config/nhin"
cp -rf ../Properties/nhin/* $domainLocation/config/nhin

# Copy log4j.properties if it does not exist
if [ ! -f $domainLocation/config/log4j.properties ]; then
    echo "Copying the standard log4j.properties file for use as a default"
    cp -f ../Properties/log4j.standard "$domainLocation/config/log4j.properties"
fi

# Restore connection files if they exist
if [ -f ../backup/internalConnectionInfo.xml ]; then
    echo "Restoring internalConnectionInfo.xml"
    cp -f ../backup/internalConnectionInfo.xml "$domainLocation/config/nhin"
fi
if [ -f ../backup/uddiConnectionInfo.xml ]; then
    echo "Restoring uddiConnectionInfo.xml"
    cp -f ../backup/uddiConnectionInfo.xml "$domainLocation/config/nhin"
fi

# Update gateway.properties
echo "Updating gateway.properties"
updatePropertiesFile $domainLocation/config/nhin/gateway.properties localHomeCommunityId $localHomeCommunityId
updatePropertiesFile $domainLocation/config/nhin/gateway.properties localDeviceId $localDeviceId
updatePropertiesFile $domainLocation/config/nhin/gateway.properties override_mime_message_id $override_mime_message_id

# Update adapter.properties
echo "Updating adapter.properties"
updatePropertiesFile $domainLocation/config/nhin/adapter.properties assigningAuthorityId $assigningAuthorityId
updatePropertiesFile $domainLocation/config/nhin/adapter.properties XDSbHomeCommunityId $XDSbHomeCommunityId

# Update saml.properties
echo "Updating saml.properties"
updatePropertiesFile $domainLocation/config/nhin/saml.properties org.apache.ws.security.saml.issuer.key.name $org_apache_ws_security_saml_issuer_key_name



# Update Admin Gui Hibernate Configuration
echo "Updating hibernate configuration file for the admingui database"
updateDatabaseConfigDialectOnly $domainLocation/config/nhin/hibernate/admingui.hibernate.cfg.xml

# Update Assigning Authority Hibernate Configuration
echo "Updating hibernate configuration file for the assigning authority database"
updateDatabaseConfigDialectOnly $domainLocation/config/nhin/hibernate/assignauthority.hibernate.cfg.xml

# Update Async Messages Hibernate Configuration
echo "Updating hibernate configuration file for the async messages database"
updateDatabaseConfigDialectOnly $domainLocation/config/nhin/hibernate/AsyncMsgs.hibernate.cfg.xml

# Update Audit Repository Hibernate Configuration
echo "Updating hibernate configuration file for the audit repository database"
updateDatabaseConfigDialectOnly $domainLocation/config/nhin/hibernate/auditrepo.hibernate.cfg.xml

# Update Config Build Hibernate Configuration
echo "Updating hibernate configuration file for the configuration build database"
updateDatabaseConfigDialectOnly $domainLocation/config/nhin/hibernate/configdb.hibernate.build.cfg.xml

# Update Config Hibernate Configuration
echo "Updating hibernate configuration file for the configuration build database"
updateDatabaseConfigDialectOnly $domainLocation/config/nhin/hibernate/configdb.hibernate.cfg.xml

# Update CorrelatedIdentifiers Hibernate Configuration
echo "Updating hibernate configuration file for the correlated identifiers database"
updateDatabaseConfigDialectOnly $domainLocation/config/nhin/hibernate/CorrelatedIdentifers.hibernate.cfg.xml

# Update DocRepo Hibernate Configuration
echo "Updating hibernate configuration file for the docrepo database"
updateDatabaseConfigDialectOnly $domainLocation/config/nhin/hibernate/docrepo.hibernate.cfg.xml

# Update DynDocRepo Hibernate Configuration
echo "Updating hibernate configuration file for the dyndocrepo database"
updateDatabaseConfigDialectOnly $domainLocation/config/nhin/hibernate/dyndocrepo.hibernate.cfg.xml

# Update Event Hibernate Configuration
echo "Updating hibernate configuration file for the event database"
updateDatabaseConfigDialectOnly $domainLocation/config/nhin/hibernate/event.hibernate.cfg.xml

# Update HIEM Hibernate Configuration
echo "Updating hibernate configuration file for the HIEM database"
updateDatabaseConfigDialectOnly $domainLocation/config/nhin/hibernate/HiemSubRepHibernate.cfg.xml

# Update Message Monitoring Hibernate Configuration
echo "Updating hibernate configuration file for the message monitoring database"
updateDatabaseConfigDialectOnly $domainLocation/config/nhin/hibernate/messagemonitoringdb.hibernate.cfg.xml

# Update Patient Hibernate Configuration
echo "Updating hibernate configuration file for the patient database"
updateDatabaseConfigDialectOnly $domainLocation/config/nhin/hibernate/patientdb.hibernate.cfg.xml

# Update Transaction Repository Hibernate Configuration
echo "Updating hibernate configuration file for the transaction repository database"
updateDatabaseConfigDialectOnly $domainLocation/config/nhin/hibernate/transrepo.hibernate.cfg.xml

# Start server
asadmin start-domain $domainName

# Deploy EARs
for f in $DEPLOY_FILES
do
    echo "Deploying $f"
    asadmin deploy --libraries $domainLocation/config/nhin $f
done

echo "Aurion Deployment complete."

