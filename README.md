MysqlMongoMapper
================

Maps a mysql database into a mongo nosql database with a simple xml configuration. 

During the development at lieferando.de (yd yourdelivery GmbH) we investigated in a scalable way to provide json in our API. So we decided to
materialize all needed data into a mongodb and deliver those documents directly on API calls. So all data has been preprocessed
and is available instantly. Sadly this part of code never exceeded prototype status, so we decided to share it with the community. 
Fell free to use it for any project

Creating an example
-------------------

1. Install MySQL and MongoDB Server and add connection settings to resources/properties/database.properties

2. Import the example database resources/data/example.sql

3. start the application 
    

    mvn exec:java -Dexec.mainClass="de.yourdelivery.mapper.DatabaseWorker"
    
Expected result
---------------

Based on the defined mapping.xml file we generate documents from rows out of mysql into mongodb:

    <documents>
    
        <document name="simple">
    		<basequery>select name, prename from customer</basequery>
    	</document>
    
    	<document name="complex">
    		<basequery>select id, name, prename from customer</basequery>
    		<reference placeholder="_ID_" map="id" />
    		<listing name="children">
    			<query>select name, prename from customer where bossId=_ID_</query>
    		</listing>
    	</document>
    
    </documents>


In your mongodb you should find a new database "mapper" with two collections "simple" and "complex". Both should inherit three documents, the complex one with the expected
children in reference

    { "_id" : ObjectId("50a22f6b472803e4ebfa2252"), "_class" : "de.yourdelivery.mapper.dto.BasicDocument", "name" : "Blub", "prename" : "Samson" }
    { "_id" : ObjectId("50a22f6b472803e4ebfa2254"), "_class" : "de.yourdelivery.mapper.dto.BasicDocument", "name" : "Blab", "prename" : "Tiffy" }
    { "_id" : ObjectId("50a22f6b472803e4ebfa2256"), "_class" : "de.yourdelivery.mapper.dto.BasicDocument", "name" : "Blib", "prename" : "Bert" }

    { "_id" : ObjectId("50a22f6b472803e4ebfa2253"), "_class" : "de.yourdelivery.mapper.dto.BasicDocument", "id" : "1", "name" : "Blub", "prename" : "Samson", "children" : [ 	{ 	"name" : "Blab", 	"prename" : "Tiffy" }, 	{ 	"name" : "Blib", 	"prename" : "Bert" } ] }
    { "_id" : ObjectId("50a22f6b472803e4ebfa2255"), "_class" : "de.yourdelivery.mapper.dto.BasicDocument", "id" : "2", "name" : "Blab", "prename" : "Tiffy", "children" : [ ] }
    { "_id" : ObjectId("50a22f6b472803e4ebfa2257"), "_class" : "de.yourdelivery.mapper.dto.BasicDocument", "id" : "3", "name" : "Blib", "prename" : "Bert", "children" : [ ] }

