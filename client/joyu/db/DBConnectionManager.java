package joyu.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.*;
import java.util.Properties;

public class DBConnectionManager {
	private static String driverName = "com.mysql.jdbc.Driver";
	private static String url = "jdbc:mysql://localhost:3306/joyuchat?user=root&password=1";
	static {
		try {
			File f2 = new File(System.getProperty("user.dir") + "\\Config\\");
			if (!f2.exists()) {
				f2.mkdir();
			}
			File f = new File(System.getProperty("user.dir")
					+ "\\Config\\DBCofig.ini");
			if (!f.exists()) {
				f.createNewFile();
				FileOutputStream bfos = new FileOutputStream(f);
				bfos.write("url = jdbc:mysql://localhost:3306/joyuchat?user=root&password=1"
						.getBytes());
				bfos.flush();
				bfos.close();
			} else {
				Properties cofigsFile = new Properties();
				cofigsFile.load(new FileInputStream(f));
				if (cofigsFile.getProperty("url") == null) {
					url = "jdbc:mysql://localhost:3306/joyuchat?user=root&password=1";
				} else {
					url = cofigsFile.getProperty("url");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	public static Connection getConnection() {
		try {
			Class.forName(driverName);
			Connection conn = DriverManager.getConnection(url);
			return conn;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}