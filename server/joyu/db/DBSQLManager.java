package joyu.db;

import java.sql.*;

public class DBSQLManager {
	protected Connection conn = null;
	protected Statement stmt = null;
	protected ResultSet rs = null;
	protected String sqlStr;

	public DBSQLManager() {
		try {
			sqlStr = "";
			conn = DBConnectionManager.getConnection();
			conn.setAutoCommit(false);
			stmt = conn.createStatement();
		} catch (Exception e) {
			System.out.println(e);
		}

	}

	public Statement getStmt() {
		return stmt;
	}

	public Connection getConn() {
		return conn;
	}

	public ResultSet getRs() {
		return rs;
	}

	public void setSqlStr(String newSqlStr) {
		this.sqlStr = newSqlStr;
	}

	public String getSqlStr() {
		return sqlStr;
	}

	public void executeQuery() throws SQLException {
		rs = stmt.executeQuery(sqlStr);
	}

	public void executeUpdate() throws SQLException {

		stmt.executeUpdate(sqlStr);
	}

	public void close() throws SQLException {
		conn.commit();
		if (stmt != null) {
			stmt.close();
			stmt = null;
		}
		conn.close();
		conn = null;
	}
};