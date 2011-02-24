package org.ecoinformatics.sms.annotation.search.data;

import java.io.InputStream;
import java.sql.ResultSet;

import org.ecoinformatics.datamanager.DataManager;
import org.ecoinformatics.datamanager.database.Condition;
import org.ecoinformatics.datamanager.database.DatabaseConnectionPoolInterface;
import org.ecoinformatics.datamanager.database.Query;
import org.ecoinformatics.datamanager.database.SelectionItem;
import org.ecoinformatics.datamanager.database.TableItem;
import org.ecoinformatics.datamanager.database.WhereClause;
import org.ecoinformatics.datamanager.database.pooling.DatabaseConnectionPoolFactory;
import org.ecoinformatics.datamanager.download.ConfigurableEcogridEndPoint;
import org.ecoinformatics.datamanager.parser.Attribute;
import org.ecoinformatics.datamanager.parser.DataPackage;
import org.ecoinformatics.datamanager.parser.Entity;
import org.ecoinformatics.datamanager.util.DocumentDownloadUtil;
import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.Measurement;

public class Materializer {

	private DataManager dataManager;
	
	private ConfigurableEcogridEndPoint endPoint = new ConfigurableEcogridEndPoint();

	public Materializer() {
		DatabaseConnectionPoolInterface connectionPool = 
			DatabaseConnectionPoolFactory.getDatabaseConnectionPoolInterface();
		String dbAdapterName = connectionPool.getDBAdapterName();
		dataManager = DataManager.getInstance(connectionPool, dbAdapterName);
	}

	public boolean checkData(Annotation annotation, Measurement measurement, String operator, Object value) throws Exception {
		
		String dataPackageId = annotation.getDataPackage();
		
		// for selecting the correct attribute from the correct entity
		String dataObject = measurement.getMapping().getDataObject();
		int dataObjectIndex = Integer.parseInt(dataObject);
		String attributeName = measurement.getMapping().getAttribute();

		// First create the DataPackage object that will be used in the query.
		DocumentDownloadUtil ddu = new DocumentDownloadUtil();
		InputStream inputStream = ddu.downloadDocument(dataPackageId, endPoint);
		DataPackage dataPackage = dataManager.parseMetadata(inputStream);
		boolean success = dataManager.loadDataToDB(dataPackage, endPoint);
		Entity entity = dataPackage.getEntityList()[dataObjectIndex];
		Attribute[] attributes = entity.getAttributeList().getAttributes();
		// find the index of the attribute based on name (id is not required)
		int attributeIndex = 0;
		for (Attribute a: attributes) {
			if (a.getName().equals(attributeName)) {
				break;
			}
			attributeIndex++;
		}
		Attribute attribute = attributes[attributeIndex];
		
		ResultSet resultSet = null;
		int rows = 0;
		
		//Now build a query, execute it, and see what we got
		if (success && dataPackage != null) {
			DataPackage[] dataPackages = {dataPackage};
			Query query = new Query();
			/* SELECT clause */
			SelectionItem selectionItem = new SelectionItem(entity, attribute);
			query.addSelectionItem(selectionItem);
			/* FROM clause */
			TableItem tableItem = new TableItem(entity);
			query.addTableItem(tableItem);
			/* WHERE clause with condition */
			Condition condition = 
				new Condition(entity, attribute, operator, value);
			WhereClause whereClause = new WhereClause(condition);
			query.setWhereClause(whereClause);
			System.out.println("Query SQL = " + query.toSQLString());

			try {
				resultSet = dataManager.selectData(query, dataPackages);
				if (resultSet != null) {
					while (resultSet.next()) {
						Object val = resultSet.getObject(1);
						System.out.println("resultSet[" + rows + "], value =  " + val);
						rows++;
					}
				}
			} finally {
				if (resultSet != null) {
					resultSet.close();
				}
				// clean up
				dataManager.dropTables(dataPackage);
			}
		}
		
		// were there any data rows for our criteria?
		return (rows > 0);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Materializer materializer = new Materializer();
			
			// get the annotation and measurement to check
			String annotationId = "benriver.278.3";
			DocumentDownloadUtil ddu = new DocumentDownloadUtil();
			materializer.endPoint.setSessionId("usePublic");
			InputStream annotationInputStream = ddu.downloadDocument(annotationId, materializer.endPoint);
			Annotation annotation = Annotation.read(annotationInputStream);
			Measurement measurement = annotation.getMeasurement("m1");
			
			// check for values matching condition
			materializer.checkData(annotation, measurement, ">", 35);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
