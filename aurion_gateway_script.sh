#!/bin/bash

###########################################################################################
## This shell script deploys a new/updated version of the Aurion Gateway ear file.
## The usuage for this script is:
##
##        > ./aurion_gateway_script.sh <zip_deployment_file_name in /home/deploy>
##
## This script should be run in the directory: "/home/deploy" on the gateway server.
###########################################################################################


### Validate the script argument
if [ $# -eq 0 ]
  then
    echo "No argument specified. Please provide deployment zip bundle file name."
	echo "Make sure deployment zip file is copied to /home/deploy"
	echo "Script should be executed in /home/deploy as: ./aurion_gateway_script.sh <zip_deployment_file_name in /home/deploy>"
	exit 1
fi

if [ -f "/home/deploy/$1" ]
then
   echo "Found $1 in /home/deploy, deployment will start..."
else
   echo "File $1 does not exist in /home/deploy. Process will not continue..."
   exit 1
fi


today=$(date +%Y%m%d)

### Get the name of the currently deployed Aurion gateway ear file
currentEARFileName=$(basename /home/deploy/auriongateway/CONNECT-GF/*/*.ear)
currentEARFileName="${currentEARFileName%.ear}"
echo Current Gateway EAR deployed on the server is $currentEARFileName


### Back up the "imgateway" folder
mv /home/deploy/auriongateway /home/deploy/auriongateway.$today
mkdir /home/deploy/auriongateway
mv /home/deploy/$1 /home/deploy/auriongateway/
echo Rename auriongateway directory to auriongateway.$today 
echo Moved distribution ZIP file to new auriongateway folder 


### Back up "deploy.properties"
cp /home/deploy/deploy.properties /home/deploy/deploy.properties.$today
echo deploy.properties successfully backed up


### Unzip distribution zip file
echo "Unzipping zip deployment file START"
cd /home/deploy/auriongateway
unzip $1
echo "Unzipping zip deployment file DONE"


### Set up files for running script
cp /home/deploy/deploy.properties /home/deploy/auriongateway/scripts
echo "Copied deploy.properties to scripts folder"

chmod a+x /home/deploy/auriongateway/scripts/deploy.sh
echo "Ensure executatble right on deploy.sh"


### Back up current "nhin" folder
mkdir /usr/local/gfapps/aurion/config/nhin.$today
cp -r /usr/local/gfapps/aurion/config/nhin /usr/local/gfapps/aurion/config/nhin.$today/
echo "Backed up nhin directory"


### Undeploy current Aurion gateay ear file
echo "Undeploy existing gateway EAR $currentEARFileName  START"
asadmin start-domain aurion
asadmin undeploy $currentEARFileName
echo "Undeploy existing gateway EAR $currentEARFileName  DONE"


### Stop the domain prior to calling deploy script
asadmin stop-domain aurion
echo "Stopped aurion domain prior to calling deploy script"


### Run the "deploy.sh" script
echo "Execute deploy.sh START"
cd /home/deploy/auriongateway/scripts
./deploy.sh
echo "Execute deploy.sh DONE"
echo "Succesfully completed running script 'aurion_gateway_script.sh'"


