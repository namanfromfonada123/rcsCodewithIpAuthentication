package com.messaging.rcs.service;

import java.sql.Types;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;

import com.messaging.rcs.model.CommonReportPojo;

@Service
public class ReportService {
	@Autowired
	private JdbcTemplate jdbcTemplate;

	/* Calling Stored Procedure using JdbcTemplate */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getRcsSummarySmsData(CommonReportPojo commonReportPojo) {
		Map<String, Object> outPut = null;
		try {
			SimpleJdbcCall jdbc = new SimpleJdbcCall(jdbcTemplate);
			jdbc.withProcedureName("RCS_SUMMARY_SMS_DATA")
					.declareParameters(new SqlParameter[] { new SqlParameter("_ACTION", Types.VARCHAR),
							new SqlParameter("_CLIENTID", Types.INTEGER), new SqlParameter("_USERNAME", Types.VARCHAR),
							new SqlParameter("_ROLE", Types.VARCHAR), new SqlParameter("_FROM_DATE", Types.VARCHAR),
							new SqlParameter("_TO_DATE", Types.VARCHAR), new SqlParameter("_VMN", Types.VARCHAR),
							new SqlParameter("_CAMPID", Types.VARCHAR), new SqlParameter("_CAMPTYPE", Types.VARCHAR),
							new SqlParameter("_MSISDN", Types.VARCHAR), new SqlOutParameter("count", Types.VARCHAR) });
			MapSqlParameterSource msps = new MapSqlParameterSource();
			msps.addValue("_ACTION", commonReportPojo.getAction());
			msps.addValue("_CLIENTID", Integer.valueOf(commonReportPojo.getClientId()));
			msps.addValue("_USERNAME", commonReportPojo.getUsername());
			msps.addValue("_ROLE", commonReportPojo.getRole());
			msps.addValue("_FROM_DATE", commonReportPojo.getFromDate());
			msps.addValue("_TO_DATE", commonReportPojo.getToDate());
			msps.addValue("_VMN", commonReportPojo.getVmn());
			msps.addValue("_CAMPID", commonReportPojo.getCamId());
			msps.addValue("_CAMPTYPE", commonReportPojo.getCamType());
			msps.addValue("_MSISDN", commonReportPojo.getMsisdn());
			msps.addValue("count", Types.VARCHAR);
			outPut = jdbc.execute(msps);
			System.out.println("Response From RCS_SUMMARY_SMS_DATA :::" + outPut.get("count"));

			// List<SqlParameter> prmtrsList = Arrays.asList(new
			// SqlParameter(Types.NVARCHAR));
			/*
			 * List prmtrsList = new ArrayList(); prmtrsList.add(new
			 * SqlParameter(Types.VARCHAR)); prmtrsList.add(new
			 * SqlParameter(Types.VARCHAR)); prmtrsList.add(new
			 * SqlParameter(Types.VARCHAR)); prmtrsList.add(new
			 * SqlParameter(Types.VARCHAR)); prmtrsList.add(new
			 * SqlParameter(Types.VARCHAR)); prmtrsList.add(new
			 * SqlParameter(Types.VARCHAR)); prmtrsList.add(new
			 * SqlParameter(Types.VARCHAR)); prmtrsList.add(new
			 * SqlParameter(Types.VARCHAR)); prmtrsList.add(new
			 * SqlParameter(Types.VARCHAR)); prmtrsList.add(new
			 * SqlParameter(Types.VARCHAR)); prmtrsList.add(new
			 * SqlParameter(Types.VARCHAR)); prmtrsList.add(new SqlOutParameter("count",
			 * Types.INTEGER));
			 */
			/*
			 * Map<String, Object> resultData = jdbcTemplate.call(connection -> {
			 * CallableStatement cs =
			 * connection.prepareCall("{call RCS_SUMMARY_SMS_DATA(?,?,?,?,?,?,?,?,?,?,?)}");
			 * cs.setString(1, commonReportPojo.getAction()); cs.setInt(2,
			 * commonReportPojo.getClientId()); cs.setString(3,
			 * commonReportPojo.getUsername()); cs.setString(4, commonReportPojo.getRole());
			 * cs.setString(5, commonReportPojo.getFromDate()); cs.setString(6,
			 * commonReportPojo.getToDate()); cs.setString(7, commonReportPojo.getVmn());
			 * cs.setString(8, commonReportPojo.getCamId()); cs.setString(9,
			 * commonReportPojo.getCamType()); cs.setString(10,
			 * commonReportPojo.getMsisdn()); cs.registerOutParameter(11, Types.VARCHAR);
			 * 
			 * // cs.setString(11, commonReportPojo.getCount()); cs.execute(); return cs;
			 * 
			 * }, prmtrsList); return resultData;
			 */} catch (Exception e) {
			e.printStackTrace();
		}
		return outPut;
	}

	/* Calling Stored Procedure using JdbcTemplate */
	public Map<String, Object> getRcsDetailedSmsData(CommonReportPojo commonReportPojo) {
		SimpleJdbcCall jdbc = new SimpleJdbcCall(jdbcTemplate);
		Map<String, Object> outPut = null;
		try {
			jdbc.withProcedureName("RCS_DETAILED_SMS_DATA")
					.declareParameters(new SqlParameter[] { new SqlParameter("_ACTION", Types.VARCHAR),
							new SqlParameter("_CLIENTID", Types.INTEGER), new SqlParameter("_USERNAME", Types.VARCHAR),
							new SqlParameter("_ROLE", Types.VARCHAR), new SqlParameter("_FROM_DATE", Types.VARCHAR),
							new SqlParameter("_TO_DATE", Types.VARCHAR), new SqlParameter("_VMN", Types.VARCHAR),
							new SqlParameter("_CAMPID", Types.VARCHAR), new SqlParameter("_CAMPTYPE", Types.VARCHAR),
							new SqlParameter("_MSISDN", Types.VARCHAR), new SqlOutParameter("count", Types.VARCHAR) });
			MapSqlParameterSource msps = new MapSqlParameterSource();
			msps.addValue("_ACTION", commonReportPojo.getAction());
			msps.addValue("_CLIENTID", Integer.valueOf(commonReportPojo.getClientId()));
			msps.addValue("_USERNAME", commonReportPojo.getUsername());
			msps.addValue("_ROLE", commonReportPojo.getRole());
			msps.addValue("_FROM_DATE", commonReportPojo.getFromDate());
			msps.addValue("_TO_DATE", commonReportPojo.getToDate());
			msps.addValue("_VMN", commonReportPojo.getVmn());
			msps.addValue("_CAMPID", commonReportPojo.getCamId());
			msps.addValue("_CAMPTYPE", commonReportPojo.getCamType());
			msps.addValue("_MSISDN", commonReportPojo.getMsisdn());
			msps.addValue("count", Types.VARCHAR);
			outPut = jdbc.execute(msps);
			System.out.println("Response From RCS_DETAILED_SMS_DATA :::" + outPut.get("count"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return outPut;
		/*
		 * List<SqlParameter> parameters = Arrays.asList(new
		 * SqlParameter(Types.NVARCHAR));
		 * 
		 * return jdbcTemplate.call(new CallableStatementCreator() {
		 * 
		 * @Override public CallableStatement createCallableStatement(Connection con)
		 * throws SQLException {
		 * 
		 * CallableStatement cs =
		 * con.prepareCall("{call RCS_DETAILED_SMS_DATA(?,?,?,?,?,?,?,?,?,?,?)}");
		 * cs.setString(1, commonReportPojo.getAction()); cs.setInt(2,
		 * commonReportPojo.getClientId()); cs.setString(3,
		 * commonReportPojo.getUsername()); cs.setString(4, commonReportPojo.getRole());
		 * cs.setString(5, commonReportPojo.getFromDate()); cs.setString(6,
		 * commonReportPojo.getToDate()); cs.setString(7, commonReportPojo.getVmn());
		 * cs.setString(8, commonReportPojo.getCamId()); cs.setString(9,
		 * commonReportPojo.getCamType()); cs.setString(10,
		 * commonReportPojo.getMsisdn()); cs.setString(11, commonReportPojo.getCount());
		 * 
		 * return cs; } }, parameters);
		 */
	}
}
