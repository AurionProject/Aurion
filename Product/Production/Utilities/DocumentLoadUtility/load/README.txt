##### DOCUMENT LOAD UTILITY #####

This directory is used for loading documents into the data repository.

NOTE: THIS SCRIPT ASSUMES AN AURION SOURCE INSTALLATION AND THAT THE SOURCE HAS ALREADY
BEEN COMPILED.

Prepare Directories and Copying Jar Files:
------------------------------------------
The first step is to run the ant build script "build.xml". Open a command window and type:

	"ant"<return>

The ant build script will copy all libraries and reference documents into the
appropriate locations for running the document load.

THE PROJECT BUILD FILE, IT IS THE "build.xml" FILE LOCATED IN THIS ("load") DIRECTORY.

Once the ant script has been run, the entire "load" directory may be copied to any location
or to a different computer that has a JDK installed.

The database connection is pre-configured to be on the local machine using MySQL as the database. 
If it is on a different machine or using a different database, modify the following entries in the 
docrepo.hibernate.cfg.xml file located in config/hibernate and replace values of the following properties as 
necessary for the correct path and database type.
<property name="connection.driver_class">com.mysql.jdbc.Driver</property>
<property name="connection.url">jdbc:mysql://localhost/docrepository</property>
<property name="dialect">org.hibernate.dialect.MySQLDialect</property>
The driver and dialect must match the correct database version. Correct values may be found in the hibernate
documentaiton. The database driver jar must be placed inside the lib directory. The name of this jar must be 
entered in the load.bat/load.sh file in place of the mysql driver (./lib/mysql-connector-java-5.1.10.jar).

Updating Paths:
---------------
The load.bat or load.sh file needs to be modified to set the path to the "config" directory located in this directory. The correct
location for this property is the full path to the config directory which is located inside the base "load"
directory. The property that needs to be modified is: -Dnhinc.properties.dir.  For example:

	-Dnhinc.properties.dir=/Projects/Aurion/Latest/Product/Production/Utilities/DocumentLoadUtility/load/config


Prepare Load Documents:
-----------------------
The documents are loaded in a format matching the "document.xsd" schema (located in this directory). A sample load document is provided in:

 	"Sample.xml" 

If the documentId element of a document has a value, it will try to update the document 
for that id. If this is missing, the document will be inserted.


Single File Upload Setup:
--------------------
Place the file to load in this directory and provide the name of the file as an argument to the java command in
the "load.bat" or "load.sh" file as shown in the example that follows.

	>java gov.hhs.fha.nhinc.docrepository.adapter.util.DocumentLoadUtil Sample.xml


Multiple Files Upload Setup:
---------------------
Multiple files are loaded by placing input files in a directory configured by the inFilePath property in the
filespath.properties file. The files are loaded from this input directory and processed. When finished, the files
are placed in the directory defined by the outFilePath property in the same properties file. Open the 
filespath.properties file and make sure that the locations in the properties file point to the locations you have 
would like to use for loading documents. 


Run the Document Load Utility:
---------------------
After completing the above steps run the load.bat or load.sh file, this will read the input file(s) and
process accordingly. 


Processing Log
---------------------
A log file will be created in the log directory after processing. This file location may be modified in the 
log4j.properties file.

------------------------------------------------------------------------------------------------------------------

--Database Configuration Options--
This is a reference for different database configuration settings

## MySQL ##
<property name="connection.driver_class">com.mysql.jdbc.Driver</property>
<property name="connection.url">jdbc:mysql://localhost/docrepository</property>
<property name="dialect">org.hibernate.dialect.MySQLDialect</property>

## Oracle ##
<property name="connection.driver_class">oracle.jdbc.driver.OracleDriver</property>
<property name="connection.url">jdbc:oracle:thin://localhost:1521/orcl</property>
<property name="dialect">org.hibernate.dialect.OracleDialect</property>


Trouble Shooting
-----------------------------------------------------------------------------------
If the name of the "Connect CommonCore lib" jar (CONNECTCoreLib-5.0.0-SNAPSHOT.jar) changes you will need to modify the following files
in the DocumentLoadUtility/load directory:

- build.xml
	Modify where it is copying the jar file to the lib folder. Make sure the jar name matches.
- load.sh
	Modify the class path exposed by the script. Make sure the jar name matches.
- load.bat
	Modify the class path exposed by the script. Make sure the jar name matches.




