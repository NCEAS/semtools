package org.ecoinformatics.oboe.datastorage;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.ecoinformatics.oboe.Debugger;
import org.ecoinformatics.oboe.model.ContextInstance;
import org.ecoinformatics.oboe.model.EntityInstance;
import org.ecoinformatics.oboe.model.MeasurementInstance;
import org.ecoinformatics.oboe.model.ObservationInstance;

import org.ecoinformatics.sms.annotation.*;
import org.ecoinformatics.oboe.model.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

public class PostgresDB {

	private String m_entityTypeTable = "entity_type";
	private String m_entityInstanceTable = "entity_instance";
	
	private String m_obsTypeTable = "observation_type";
	private String m_obsInstanceTable = "observation_instance";
	
	
	protected String m_measTypeTable = "measurement_type";
	protected String m_measInstanceTable = "measurement_instance";
	
	private String m_contextTypeTable = "context_relationship";
	private String m_contextInstanceTable = "context_instance";
	
	private String m_url = "jdbc:postgresql://localhost:5432/oboe";
	private String m_user = "oboe";
	private String m_password = "nceas";
	
	Connection m_conn = null;
	
	String m_insertEntityInstance ="INSERT INTO " +m_entityInstanceTable + " VALUES(?,?,?);";
	String m_insertObservationInstance ="INSERT INTO " +m_obsInstanceTable + " VALUES(?,?,?,?);";
	String m_insertMeasurementInstance ="INSERT INTO " +m_measInstanceTable + " VALUES(?,?,?,?,?);";
	String m_insertContextInstance ="INSERT INTO " +m_contextInstanceTable + " VALUES(?,?,?,?,?);";
	
	String m_insertAnnotation = "INSERT INTO annotation(annot_uri) VALUES(?);";
	String m_insertObservationType = "INSERT INTO " + m_obsTypeTable + " VALUES(?,?,?,?);";
	String m_insertMeasurementType = "INSERT INTO " + m_measTypeTable + " VALUES(?,?,?,?,?,?,?)";
	String m_insertContextType = "INSERT INTO " + m_contextTypeTable + " VALUES(?,?,?,?,?)";
	
	String m_maxAnnotationIdSql = "SELECT last_value FROM annot_id_seq;";
	
	/**
	 * Open the postgres connection 
	 * 
	 * @throws Exception
	 */
	public void open()
		throws Exception
	{
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException cnfe) {
			System.err.println(Debugger.getCallerPosition()+"Couldn't find driver class:");
			System.out.println(Debugger.getCallerPosition()+"Couldn't find driver class:");
			throw cnfe;
		}
		
		try {
			m_conn = DriverManager.getConnection(m_url, m_user, m_password);
        } catch (SQLException se) {
            System.out.println(Debugger.getCallerPosition()+"Couldn't connect: print out a stack trace and exit.");
            throw se;
        }
	}
	
	public Connection getConnection()
	{
		return m_conn;
	}
	
	/**
	 * Close the postgres connection 
	 */
	public void close() throws Exception
	{
		if(m_conn!=null){
			try {
				m_conn.close();
			} catch (SQLException e) {				
				e.printStackTrace();
				throw e;
			}
		}
	}
	
	
	/**
	 * Form the SQL to insert entity instances
	 * @param ei
	 * @return
	 * @deprecated
	 */
	private String formEntityInstanceSQL(EntityInstance ei){
		String sql = "INSERT INTO " +m_entityInstanceTable + " VALUES("+
					ei.getUniqueRecordId()+","+ //used to join with the observation instance table
					ei.getEntId()+"," +			//used to join with the observation instance table
					ei.getEntityType().getName()+");";
		
		//System.out.println(Debugger.getCallerPosition()+"sql="+sql);
		return sql;
	}
	
	/**
	 * Set the parameters for inserting entity instances
	 * 
	 * @param pstmt
	 * @param ei
	 * @throws SQLException
	 */
	private void setEntityInstanceParam(PreparedStatement pstmt, EntityInstance ei) 
		throws SQLException
	{
		pstmt.setString(1, ei.getUniqueRecordId());
		pstmt.setLong(2, ei.getEntId());
		pstmt.setString(3, ei.getEntityType().getName());		
	}
	
	
	/**
	 * For the SQL to insert observation instances 
	 * See "setObsInstanceParam" for field explanation
	 * @param oi
	 * @return
	 * @deprecated
	 */
	private String formObsInstanceSQL(ObservationInstance oi){
		String sql = "INSERT INTO " +m_obsInstanceTable + " values("+
				oi.getEntity().getUniqueRecordId()+","+ //join with the entity and measurement instance table
				oi.getEntity().getEntId()+","+			//join with the entity instance table
				oi.getObsId()+"," + 					//join with the measurement instance table
				oi.getObsType().getLabel()+");";
		
		System.out.println(Debugger.getCallerPosition()+"sql="+sql);
		return sql;
	}
	
	/**
	 * Set the parameters for inserting observation instances
	 * [1] entity unique record id
	 * [2] entity id
	 * [3] observation id
	 * [4] observation type label 
	 * @param pstmt
	 * @param ei
	 * @throws SQLException
	 */
	private void setObsInstanceParam(PreparedStatement pstmt, ObservationInstance oi) 
		throws SQLException
	{
		pstmt.setString(1, oi.getEntity().getUniqueRecordId());
		pstmt.setLong(2, oi.getEntity().getEntId());
		pstmt.setLong(3, oi.getObsId());
		pstmt.setString(4, oi.getObsType().getLabel());		
	}
			
//	private String formEntityTypeSQL(Entity et){
//	String sql = "INSERT INTO " +m_entityTypeTable + " values("+
//				et.getOntology().getPrefix()+","+ //used to join with the observation instance table
//				et.getURI()+"," +			//used to join with the observation instance table
//				et.getName()+");";
//	
//	//System.out.println(Debugger.getCallerPosition()+"sql="+sql);
//	return sql;
//}
	
	/**
	 * Form the sql to insert observation type of annotation
	 * See "setObsTypeParam" for field explanation
	 * 
	 * @param ot
	 * @return
	 * @deprecated
	 */
	private String formObsTypeSQL(String annotationId, Observation ot){
		String sql = "INSERT INTO " +m_obsTypeTable + " values("+
				annotationId+","+ 
				ot.getLabel()+","+ //join with the entity and measurement instance table
				ot.getEntity().getName()+","+			//join with the entity instance table
				ot.isDistinct()+");";
		
		System.out.println(Debugger.getCallerPosition()+"sql="+sql);
		return sql;
	}
	
	
	/**
	 * Set the parameters for insertion to observation type table
	 * (1) Annotation id
	 * (2) Observation type label
	 * (3) Observation entity type name
	 * (4) isDistinct
	 * 
	 * @param pstmt
	 * @param annotationId
	 * @param ot
	 * @throws SQLException
	 */
	private void setObsTypeParam(PreparedStatement pstmt, long annotationId, Observation ot) 
		throws SQLException
	{
		pstmt.setLong(1, annotationId);
		pstmt.setString(2,ot.getLabel());
		pstmt.setString(3, ot.getEntity().getName());
		pstmt.setBoolean(4, ot.isDistinct());
	}
	
	
	/**
	 * Set the parameters for insertion to context type table
	 * (1) annotation id
	 * (2) observation type label
	 * (3) context observation type label
	 * (4) context relationship name
	 * (5) is identifying
	 * 
	 * @param pstmtContext
	 * @param annotationId
	 * @param obsType
	 * @param context
	 * @throws SQLException
	 */
	private void setContextTypeParam(PreparedStatement pstmtContext, Long annotationId,
			Observation obsType, Context context) throws SQLException
	{
		pstmtContext.setLong(1, annotationId);
		pstmtContext.setString(2, obsType.getLabel());
		pstmtContext.setString(3, context.getObservation().getLabel());
		pstmtContext.setString(4, context.getRelationship().getName());
		pstmtContext.setBoolean(5, context.isIdentifying());		
	}
	
	/**
	 * For the sql to insert measurement type
	 * See "setMeasTypeParam" for field explanation
	 * @param mt
	 * @return
	 * @deprecated
	 */
	private String formMeasTypeSQL(Observation ot, Measurement mt){
		String standardName = "";
		String protocalName= "";
		
		if(mt.getStandard()!=null){
			standardName = mt.getStandard().getName();
		}
		
		if(mt.getProtocol()!=null){
			protocalName = mt.getProtocol().getName();
		}
		
		String sql = "INSERT INTO " + m_measTypeTable + " values("+
				mt.getLabel()+","+
				ot.getLabel()+","+
				mt.isKey()+","+
				","+
				standardName+","+
				protocalName+");";
		
		System.out.println(Debugger.getCallerPosition()+"sql="+sql);
		return sql;
	}
	
	/**
	 * Set the parameters for insertion to observation type table
	 * [1] Annotation id (long)
	 * [2] Measurement type char(16), 
	 * [3] Observation label varchar(64),
	 * [4] iskey boolean,
	 * [5] characteristic char(64),
	 * [6] standard char(16),
	 * [7] protocal varchar(256);
	 * 
	 * @param stmt
	 * @param ot
	 * @param mt
	 * @return
	 * @throws SQLException 
	 */
	private void setMeasTypeParam(PreparedStatement stmt, Long annotId,Observation ot, Measurement mt) throws SQLException{
		String characteristicName = "";
		String standardName = "";
		String protocalName= "";
		
		if(mt.getCharacteristics()!=null){
			characteristicName = mt.getCharacteristics().get(0).getName();
		}
		if(mt.getStandard()!=null){
			standardName = mt.getStandard().getName();
		}
		
		if(mt.getProtocol()!=null){
			protocalName = mt.getProtocol().getName();
		}
		
		stmt.setLong(1,annotId);
		stmt.setString(2,mt.getLabel());
		stmt.setString(3,ot.getLabel());
		stmt.setBoolean(4,mt.isKey());
		stmt.setString(5,characteristicName);
		stmt.setString(6,standardName);
		stmt.setString(7, protocalName);
	}
	
	/**
	 * Form the SQL to insert the measurement instances
	 * See "setMeasInstanceParam" for field explanations
	 * 
	 * @param mi
	 * @return
	 * @deprecated
	 */
	private String formMeasInstanceSQL(MeasurementInstance mi){
		String sql = "INSERT INTO " + m_measInstanceTable + " values("+
				mi.getObservationInstance().getEntity().getUniqueRecordId()+","+ //[1]
				mi.getObservationInstance().getObsId()+"," + 					//[2]
				mi.getMeasId()+","+ //[3]
				mi.getMeasurementType().getLabel()+","+ //[4]
				mi.getMeasValue()+");"; //[5]
		
		System.out.println(Debugger.getCallerPosition()+"sql="+sql);
		return sql;
	}
	
	/**
	 * Set the parameters for inserting measurement instances
	 * (1) unique record id, 
	 * (2) observation id, 
	 * (3) measurement id, 
	 * (4) measurement type id, 
	 * (5) measurement value
	 * 
	 * (1) + (2) together is used to join with observation instance
	 * 
	 * @param pstmt
	 * @param ei
	 * @throws SQLException
	 */
	private void setMeasInstanceParam(PreparedStatement pstmt, MeasurementInstance mi) 
		throws SQLException
	{
		pstmt.setString(1, mi.getObservationInstance().getEntity().getUniqueRecordId());
		pstmt.setLong(2, mi.getObservationInstance().getObsId());
		pstmt.setLong(3, mi.getMeasId());
		pstmt.setString(4, mi.getMeasurementType().getLabel());		
		pstmt.setString(5, mi.getMeasValue());
	}
	
	
	/**
	 * Form the SQL to insert the context instances
	 * See "setContextInstanceParam" for the fields explanation
	 * @param ci 
	 * @return
	 * @deprecated
	 */
	private String formContextInstanceSQL(ContextInstance ci){
		String sql = "INSERT INTO " +m_contextInstanceTable + " values("+
				ci.getObservationInstance().getEntity().getUniqueRecordId()+","+
				ci.getContextObservationInstance().getEntity().getUniqueRecordId()+","+
				ci.getObservationInstance().getObsId()+","+
				ci.getContextObservationInstance().getObsId()+","+
				ci.getContextType().getRelationship().getName()+");";
		
		System.out.println(Debugger.getCallerPosition()+"sql="+sql);
		return sql;		
	}
	
	
	/**
	 * Set the parameters for inserting context instances
	 * (1) unique record id of observation instance 
	 * (2) id of observation instance
	 * (3) unique record id of context observation instance
	 * (4) id of observation context instance 
	 * (5) context type relationship name
	 * 
	 * (1) + (2) together is used to join with observation instance
	 * (3) + (4) together is used to join with observation instance  
	 * (5) is used to join with context relationship
	 * @param pstmt
	 * @param ei
	 * @throws SQLException
	 */
	private void setContextInstanceParam(PreparedStatement pstmt, ContextInstance ci) 
		throws SQLException
	{
		pstmt.setString(1, ci.getObservationInstance().getEntity().getUniqueRecordId());
		pstmt.setString(2, ci.getContextObservationInstance().getEntity().getUniqueRecordId());
		pstmt.setLong(3, ci.getObservationInstance().getObsId());
		pstmt.setLong(4, ci.getContextObservationInstance().getObsId());
		pstmt.setString(5, ci.getContextType().getRelationship().getName());
	}
	
	/**
	 * Return the maximum annotation id 
	 * @return
	 * @throws SQLException
	 */
	private long getMaxAnnotId() throws SQLException
	{
		Statement stmt = m_conn.createStatement();
		
		ResultSet rs = stmt.executeQuery(m_maxAnnotationIdSql);
		long maxAnnotId = 1;
		while(rs.next()){
			maxAnnotId = rs.getLong(1);
			break;
		}
		
		return maxAnnotId;
	}
	
	/**
	 * Insert the annotation URI to the table, and get the last annotation. 
	 * 
	 * @param annotationUri
	 * @return
	 * @throws SQLException
	 */
	private long insAnnotationFile(String annotationUri) throws SQLException
	{
		PreparedStatement pstmt = m_conn.prepareStatement(m_insertAnnotation);
		pstmt.setString(1, annotationUri);
		pstmt.execute();
		pstmt.close();
		
		long maxAnnotId = getMaxAnnotId();
		return maxAnnotId;
	}
	
	/**
	 * import the annotation type information
	 * @param A
	 * @throws SQLException 
	 */
	public void importAnnotation(Annotation A, String annotationFileName) throws SQLException
	{
		//entity type,observation type, measurement type, context type
		List<Observation> obsTypeList= A.getObservations();
		//Set<Entity> entityTypeSet = new TreeSet<Entity>();
		//Set<Measurement> measurementTypeSet = new TreeSet<Measurement>();
		Set<Context> contextTypeSet = new TreeSet<Context>();
		
		String annotationUri = A.getURI();
		if(annotationUri==null){
			annotationUri = annotationFileName;
		}
		
		long annotationId = insAnnotationFile(annotationUri);
		System.out.println(Debugger.getCallerPosition()+"annotationUri="+annotationUri+", annotationId="+annotationId);
		
		PreparedStatement pstmtObs = m_conn.prepareStatement(m_insertObservationType);
		PreparedStatement pstmtMeas = m_conn.prepareStatement(m_insertMeasurementType);
		PreparedStatement pstmtContext = m_conn.prepareStatement(m_insertContextType);
		for(Observation obsType: obsTypeList){
			//Add this observation type
			setObsTypeParam(pstmtObs,annotationId,obsType);			
			pstmtObs.execute();
			
			//Add the measurement types related to this observation type
			for(Measurement meas: obsType.getMeasurements()){
				setMeasTypeParam(pstmtMeas,annotationId,obsType,meas);
				pstmtMeas.execute();
			}
			
			//Add the context types related to this observation type
			for(Context context: obsType.getContexts()){
				setContextTypeParam(pstmtContext,annotationId,obsType,context);
				//System.out.println(Debugger.getCallerPosition()+pstmtContext.toString());
				pstmtContext.execute();
			}
		}
		
		pstmtContext.close();
		pstmtMeas.close();
		pstmtObs.close();
	}
	
	/**
	 * Import the data instances
	 * @param oboe
	 * @throws SQLException 
	 */
	public void importInstance(OboeModel oboe) throws SQLException
	{
		PreparedStatement pstmtEntity = m_conn.prepareStatement(this.m_insertEntityInstance);
		PreparedStatement pstmtObs = m_conn.prepareStatement(this.m_insertObservationInstance);
		PreparedStatement pstmtMeas = m_conn.prepareStatement(this.m_insertMeasurementInstance);
		PreparedStatement pstmtContext = m_conn.prepareStatement(this.m_insertContextInstance);
		
		 //entity instance 
		 for(EntityInstance ei: oboe.m_entityInstances){
			 //formEntityInstanceSQL(ei);
			 this.setEntityInstanceParam(pstmtEntity, ei);
			 pstmtEntity.execute();
		 }
		 
		 //observation instance
		 for(ObservationInstance oi: oboe.m_observationInstances){
			 //formObsInstanceSQL(oi);
			 this.setObsInstanceParam(pstmtObs, oi);
			 pstmtObs.execute();
		 }

		 //measurement instance
		 for(MeasurementInstance mi: oboe.m_measurementInstances){
			 //formMeasInstanceSQL(mi);
			 this.setMeasInstanceParam(pstmtMeas, mi);
			 pstmtMeas.execute();
		 }

		 //context instance
		 for(ContextInstance ci: oboe.m_contextInstances){
			 //formContextInstanceSQL(ci);
			 this.setContextInstanceParam(pstmtContext, ci);
			 pstmtContext.execute();
		 }
	}
		
}
