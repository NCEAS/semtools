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
	
	
	private String m_measTypeTable = "measurement_type";
	private String m_measInstanceTable = "measurement_instance";
	
	private String m_contextTypeTable = "context_relationship";
	private String m_contextInstanceTable = "context_instance";
	
	private String m_url = "jdbc:postgresql://localhost:5432/oboe";
	private String m_user = "oboe";
	private String m_password = "nceas";
	
	Connection m_conn = null;
	
	String m_insertEntityInstance ="INSERT INTO " +m_entityInstanceTable + " VALUES(?,?,?);";
	
	String m_insertObservationType = "INSERT INTO " + m_obsTypeTable + " VALUES(?,?,?,?);";
	String m_insertMeasurementType = "INSERT INTO " + m_measTypeTable + " VALUES(?,?,?,?,?,?)";
	
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
	 */
	private String formEntityInstanceSQL(EntityInstance ei){
		String sql = "INSERT INTO " +m_entityInstanceTable + " values("+
					ei.getUniqueRecordId()+","+ //used to join with the observation instance table
					ei.getEntId()+"," +			//used to join with the observation instance table
					ei.getEntityType().getName()+");";
		
		//System.out.println(Debugger.getCallerPosition()+"sql="+sql);
		return sql;
	}
	
//	private String formEntityTypeSQL(Entity et){
//		String sql = "INSERT INTO " +m_entityTypeTable + " values("+
//					et.getOntology().getPrefix()+","+ //used to join with the observation instance table
//					et.getURI()+"," +			//used to join with the observation instance table
//					et.getName()+");";
//		
//		//System.out.println(Debugger.getCallerPosition()+"sql="+sql);
//		return sql;
//	}
	
	/**
	 * For the SQL to insert observation instances 
	 * (record id, entity id, observation id, observation type label)	 
	 * [1] entity unique record id
	 * [2] entity id
	 * [3] observation id
	 * [4] observation type label
	 * @param oi
	 * @return
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
	 * Form the sql to insert observation type of annotation
	 * 
	 * (1) Annotation id
	 * (2) Observation type label
	 * (3) Observation entity type name
	 * (4) isDistinct
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
	private void setObsTypeParam(PreparedStatement pstmt, String annotationId, Observation ot) 
		throws SQLException
	{
		pstmt.setString(1, annotationId);
		pstmt.setString(2,ot.getLabel());
		pstmt.setString(3, ot.getEntity().getName());
		pstmt.setBoolean(4, ot.isDistinct());
	}
	
	/**
	 * For the sql to insert measurement type
	 * [1] Measurement type char(16), 
	 * [2] Observation label varchar(64),
	 * [3] iskey boolean,
	 * [4] characteristic char(64),
	 * [5] standard char(16),
	 * [6] protocal varchar(256);
	
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
	 * [1] Measurement type char(16), 
	 * [2] Observation label varchar(64),
	 * [3] iskey boolean,
	 * [4] characteristic char(64),
	 * [5] standard char(16),
	 * [6] protocal varchar(256);
	 * @param stmt
	 * @param ot
	 * @param mt
	 * @return
	 * @throws SQLException 
	 */
	private void setMeasTypeParam(PreparedStatement stmt, Observation ot, Measurement mt) throws SQLException{
		String standardName = "";
		String protocalName= "";
		
		if(mt.getStandard()!=null){
			standardName = mt.getStandard().getName();
		}
		
		if(mt.getProtocol()!=null){
			protocalName = mt.getProtocol().getName();
		}
		
		stmt.setString(1,mt.getLabel());
		stmt.setString(2,ot.getLabel());
		stmt.setBoolean(3,mt.isKey());
		stmt.setString(4,null);
		stmt.setString(5,standardName);
		stmt.setString(6, protocalName);
	}
	
	/**
	 * Form the SQL to insert the measurement instances
	 * (1) unique record id, 
	 * (2) observation id, 
	 * (3) measurement id, 
	 * (4) measurement type id, 
	 * (5) measurement value
	 * 
	 * (1) + (2) together is used to join with observation instance
	 * @param mi
	 * @return
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
	 * Form the SQL to insert the context instances
	 * (1) unique record id of observation instance 
	 * (2) id of observation instance
	 * (3) unique record id of context observation instance
	 * (4) id of observation context instance 
	 * (5) context type relationship name
	 * 
	 * (1) + (2) together is used to join with observation instance
	 * (3) + (4) together is used to join with observation instance  
	 * (5) is used to join with context relationship
	 * 
	 * @param ci
	 * @return
	 */
	private String formContextInstanceSQL(ContextInstance ci){
		String sql = "INSERT INTO " +m_contextInstanceTable + " values("+
				ci.getObservationInstance().getEntity().getUniqueRecordId()+","+
				ci.getObservationInstance().getObsId()+","+
				ci.getContextObservationInstance().getEntity().getUniqueRecordId()+","+
				ci.getContextObservationInstance().getObsId()+","+
				ci.getContextType().getRelationship().getName()+");";
		
		System.out.println(Debugger.getCallerPosition()+"sql="+sql);
		return sql;		
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
		
		String annotationId = A.getURI();
		if(annotationId==null){
			annotationId = annotationFileName;
		}
		System.out.println(Debugger.getCallerPosition()+",annotationId="+annotationId);
		
		PreparedStatement pstmtObs = m_conn.prepareStatement(m_insertObservationType);
		PreparedStatement pstmtMeas = m_conn.prepareStatement(m_insertMeasurementType);
		for(Observation obsType: obsTypeList){
			setObsTypeParam(pstmtObs,annotationId,obsType);			
			pstmtObs.execute();
			//entityTypeSet.add(obs.getEntity());
			for(Measurement meas: obsType.getMeasurements()){
				setMeasTypeParam(pstmtMeas,obsType,meas);
				pstmtMeas.execute();
			}
			//measurementTypeSet.addAll();
			contextTypeSet.addAll(obsType.getContexts());
		}
	}
	
	/**
	 * Import the data instances
	 * @param oboe
	 */
	public void importInstance(OboeModel oboe)
	{
		//entity instance 
		 for(EntityInstance ei: oboe.m_entityInstances){
			 formEntityInstanceSQL(ei);
		 }
		 
		 //observation instance
		 for(ObservationInstance oi: oboe.m_observationInstances){
			 oi.getObsId();
			 formObsInstanceSQL(oi); 
		 }

		 //measurement instance
		 for(MeasurementInstance mi: oboe.m_measurementInstances){
			 formMeasInstanceSQL(mi);
		 }

		 //context instance
		 for(ContextInstance ci: oboe.m_contextInstances){
			 formContextInstanceSQL(ci);
		 }
	}
		
}
