package de.yourdelivery.mapper;

import de.yourdelivery.mapper.beans.mapping.Configuration;
import de.yourdelivery.mapper.beans.mapping.Documents;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.xml.sax.SAXException;

/**
 * 
 * This worker takes care of replicating mysql data into a mongodb. Based on the given information
 * inside the xml mapping files (@see resources/mapping/*.xml)
 * 
 * @author Matthias Laug <laug@lieferando.de>
 * @since 19.10.2012
 *
 */
public class DatabaseWorker {

	/**
	 * Main class to start the worker and listen for incoming html jobs
	 * 
	 * @author Matthias Laug <laug@lieferando.de>
	 * @author Daniel Scain <farenzena@lieferando.de>
	 * @since 14.09.2012
	 * @param args
	 * @throws IOException
	 * @throws AmqpException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public static void main(String[] args) throws IOException,
			ParserConfigurationException, SAXException, Exception {
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"ApplicationContext.xml");

		DatabaseWorker worker = (DatabaseWorker) context.getBean("databaseworker");

		try {
			worker.prepare();
		} catch (JAXBException e1) {
			e1.printStackTrace();
		}
	}

	@Autowired
	private MongoOperations mongo;

	@Autowired
	private Logger logger;

	@Autowired
	private Configuration configuration;

	@Value("${mapper.dir}")
	private String mappingDir;
	
	public void prepare() throws JAXBException {
		
		if ( mappingDir == null ){
			logger.error("no mappding directory given");
			return;
		}
		
		File dir = new File(mappingDir);
		for(File mappingFile : dir.listFiles() ){
			JAXBContext jaxbContext = JAXBContext.newInstance(Documents.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			Documents documents = (Documents) jaxbUnmarshaller.unmarshal(mappingFile);
			configuration.execute(documents);
		}
	}

}
