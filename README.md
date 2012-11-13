MysqlMongoMapper
================

Maps a mysql database into a mongo nosql database with a simple xml configuration

Creating an example
-------------------

1. Install MySQL and MongoDB Server and add connection settings to resources/properties/database.properties

2. Import the example database resources/data/example.sql

3. start the application mvn with 
    exec:java -Dexec.mainClass="de.yourdelivery.mapper.DatabaseWorker"
    
Expected result
---------------

In your mongodb you should find a new database "mapper" with two collections "simple" and "complex". Both should inherit three documents, the complex one with the expected
children in reference

    { "_id" : ObjectId("50a22f6b472803e4ebfa2252"), "_class" : "de.yourdelivery.mapper.dto.BasicDocument", "name" : "Blub", "prename" : "Samson" }
    { "_id" : ObjectId("50a22f6b472803e4ebfa2254"), "_class" : "de.yourdelivery.mapper.dto.BasicDocument", "name" : "Blab", "prename" : "Tiffy" }
    { "_id" : ObjectId("50a22f6b472803e4ebfa2256"), "_class" : "de.yourdelivery.mapper.dto.BasicDocument", "name" : "Blib", "prename" : "Bert" }

    { "_id" : ObjectId("50a22f6b472803e4ebfa2253"), "_class" : "de.yourdelivery.mapper.dto.BasicDocument", "id" : "1", "name" : "Blub", "prename" : "Samson", "children" : [ 	{ 	"name" : "Blab", 	"prename" : "Tiffy" }, 	{ 	"name" : "Blib", 	"prename" : "Bert" } ] }
    { "_id" : ObjectId("50a22f6b472803e4ebfa2255"), "_class" : "de.yourdelivery.mapper.dto.BasicDocument", "id" : "2", "name" : "Blab", "prename" : "Tiffy", "children" : [ ] }
    { "_id" : ObjectId("50a22f6b472803e4ebfa2257"), "_class" : "de.yourdelivery.mapper.dto.BasicDocument", "id" : "3", "name" : "Blib", "prename" : "Bert", "children" : [ ] }

