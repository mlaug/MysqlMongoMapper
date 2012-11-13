package de.yourdelivery.mapper.beans.mapping;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoOperations;

import de.yourdelivery.mapper.dto.BasicDocument;

public class Configuration {

	@Value("${mapper.workers}")
	private int countWorkers;

	@Autowired
	private SessionFactory session;

	@Autowired
	private MongoOperations destination;

	@Autowired
	private Logger logger;

	/**
	 * start a bunch of workers to take care of generating all defined documents
	 * with data from the mysql database
	 * 
	 * @author Matthias Laug <laug@lieferando.de>
	 * @since 16.10.2012
	 */
	public void execute(Documents documents) {

		for (Document xmlDocument : documents.getDocuments()) {

			logger.warn("Starting materializing of document "
					+ xmlDocument.name + " from mysql into mongodb");
			logger.debug("SQL base query: " + xmlDocument.basequery);

			// clearing collection before starting
			destination.dropCollection(xmlDocument.name);
			destination.createCollection(xmlDocument.name);

			// generate initial list from base query
			Integer listSize = session.openSession()
					.createSQLQuery(xmlDocument.basequery).list().size();

			// calculate jumpsize for available worker per document
			Integer jumpSize = listSize / countWorkers;
			if ( jumpSize == 0 ){
				jumpSize = countWorkers;
			}

			ExecutorService executor = Executors.newCachedThreadPool();
			for (int i = 0; i < listSize; i = i + jumpSize) {
				logger.warn("starting worker with offset " + i);
				Worker worker = new Worker();
				worker.offset = i;
				worker.limit = jumpSize;
				worker.xmlDocument = xmlDocument;
				executor.execute(worker);
			}

		}
	}

	class Worker implements Runnable {

		private Document xmlDocument;

		private Integer offset;

		private Integer limit;

		public void run() {
			logger.debug(xmlDocument.basequery);
			@SuppressWarnings("unchecked")
			List<Object> result = session
					.openSession()
					.createSQLQuery(
							xmlDocument.basequery + " limit " + limit
									+ " offset " + offset).list();

			Iterator<Object> iterBaseQuery = result.iterator();

			ArrayList<String> fields;
			try {
				fields = getFields(xmlDocument.basequery);
			} catch (Exception e2) {
				return;
			}

			HashMap<String, ArrayList<String>> fieldsOfListMap = new HashMap<String, ArrayList<String>>();

			while (iterBaseQuery.hasNext()) {
				Object[] item = (Object[]) iterBaseQuery.next();

				BasicDocument mongoDoc = null;
				try {
					mongoDoc = populateDocument(item, fields);
				} catch (Exception e1) {
					continue;
				}

				if (xmlDocument.getLists() != null) {

					for (Listing list : xmlDocument.getLists()) {

						if (list.query == null) {
							logger.warn("could not find sql query for listing "
									+ list.name);
							continue;
						}

						String sql = generateSqlQuery(list.query, mongoDoc,
								xmlDocument);

						try {

							// get the list from the query only once
							ArrayList<String> fieldsOfList = null;
							if (!fieldsOfListMap.containsKey(list.name)) {
								fieldsOfList = getFields(sql);
								fieldsOfListMap.put(list.name, fieldsOfList);
							} else {
								fieldsOfList = fieldsOfListMap.get(list.name);
							}

							ArrayList<BasicDocument> listDocs = new ArrayList<BasicDocument>();
							@SuppressWarnings("unchecked")
							List<Object> resultListQuery = session
									.openSession().createSQLQuery(sql).list();
							Iterator<Object> iterListQuery = resultListQuery
									.iterator();

							logger.debug(sql);
							while (iterListQuery.hasNext()) {
								Object[] listItems = (Object[]) iterListQuery
										.next();

								BasicDocument documentOfList = populateDocument(
										listItems, fieldsOfList);

								// add that document to the list
								listDocs.add(documentOfList);
							}

							// add that list to the base document
							mongoDoc.put(list.name, listDocs);

						} catch (Exception e) {
							logger.error(e.getMessage());
						}

					}
				} else {
					logger.debug("no listings found for " + xmlDocument.name);
				}

				destination.insert(mongoDoc, xmlDocument.name);
			}

		}

		/**
		 * @author Matthias Laug <laug@lieferando.de>
		 * @since 16.10.2012
		 * @param row
		 * @param fields
		 * @return
		 */
		private BasicDocument populateDocument(Object[] row,
				ArrayList<String> fields) {
			BasicDocument doc = new BasicDocument();

			// both "lists" should match in size otherwise a correct mapping may
			// not be possible
			if (row.length != fields.size()) {
				logger.warn("size of row and size of mapping elements do not match");
			}

			for (int i = 0; i < fields.size(); i++) {
				if (row[i] != null)
					doc.put(fields.get(i), row[i].toString());
				else
					doc.put(fields.get(i), null);
			}
			return doc;
		}

		/**
		 * Remove placeholder and replace with value from current doc
		 * 
		 * @author Matthias Laug <laug@lieferando.de>
		 * @since 16.10.2012
		 * @param sql
		 * @param xmlDocument
		 * @return
		 */
		private String generateSqlQuery(String sql, BasicDocument doc,
				Document xmlDocument) {
			String modifiedSql = sql;
			for (Reference ref : xmlDocument.getReferences()) {
				String map = doc.get(ref.map).toString();
				if (map.length() > 0) {
					modifiedSql = sql.replace(ref.placeholder, map);
				}
			}
			return modifiedSql;
		}

		/**
		 * extract the parameter list from the sql query to match again the
		 * mongodb document
		 * 
		 * @author Matthias Laug <laug@lieferando.de>
		 * @since 16.10.2012
		 * @param sql
		 * @return
		 * @throws Exception
		 */
		private ArrayList<String> getFields(String sql) throws Exception {

			String modifiedSql = sql.replace("\n", "").replace("\r", "");

			// extract the select list from the query to simplify the next regex
			// (as if that is possible)
			Matcher selectMatch = Pattern.compile("^select (.+) from",
					Pattern.CASE_INSENSITIVE).matcher(modifiedSql);

			if (selectMatch.find()) {
				ArrayList<String> fields = new ArrayList<String>();
				// extract all fields seperatly for later matching
				// we may use `col` as alias, col, `col`, r.col, `r`.col,
				// `r`.`col`
				Matcher fieldsMatch = Pattern.compile(
						"(`?\\w+`?\\.)?(`?\\w+`?\\s(as\\s))?`?(\\w+)`?(,|$)",
						Pattern.CASE_INSENSITIVE).matcher(selectMatch.group(1));
				while (fieldsMatch.find()) {
					fields.add(fieldsMatch.group(4));
				}
				return fields;
			}

			logger.error("could not extract field list from sql query "
					+ modifiedSql);
			throw new Exception("could not extract field list from sql query "
					+ modifiedSql);
		}

	}

}