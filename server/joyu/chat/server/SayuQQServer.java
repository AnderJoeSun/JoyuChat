package joyu.chat.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import joyu.db.DBConnectionManager;
import joyu.db.DBSQLManager;

public class SayuQQServer {
	static {
		File f1 = new File(System.getProperty("user.dir") + "\\Shares\\");
		if (!f1.exists()) {
			f1.mkdir();
		}
	}
	private static String serverPort;
	private static final String systemInfo = "S~a!y@u#Q$Q%:S";
	private static final String systemSigleQuote = "$Sayu$%Quo";
	private static boolean started = false;
	private static ServerSocket ss = null;
	private static final String savePath = System.getProperty("user.dir")
			+ "\\Shares\\";
	private static List<Client> clients = new ArrayList<Client>();
	private static List<String> beoff = new ArrayList<String>();

	private static synchronized void addOff(String str) {
		if (str != null) {
			beoff.add(str);
		}
	}

	private static synchronized String removeOff() {
		if (beoff.size() > 0) {
			String str = beoff.get(0);
			beoff.remove(0);
			return str;
		} else {
			return null;
		}
	}

	private static List<String> usersOnline = new ArrayList<String>();

	public static void main(String[] args) {
		new SayuQQServer().start();
	}

	public static synchronized void updateDBAndFilesForShare() {
		File dir = new File(savePath);
		if (!dir.exists()) {
			dir.mkdir();
		}
		System.out.println(" updating DB ...");
		try {
			DBSQLManager dbsm = new DBSQLManager();
			String select = "SELECT fileName from filesForShare";
			dbsm.setSqlStr(select);
			dbsm.executeQuery();
			ResultSet rs = dbsm.getRs();
			while (rs.next()) {
				String afilename = rs.getString("fileName");
				File file = new File(savePath + afilename);
				if (!file.exists()) {
					DBSQLManager dbsm2 = new DBSQLManager();
					String delete = "delete from filesForShare where fileName ='"
							+ afilename + "'";
					dbsm2.setSqlStr(delete);
					dbsm2.executeUpdate();
					dbsm2.close();
					System.out
							.println("file : "
									+ afilename
									+ " already deleted, so delete the record of it from db...");
				}
			}
			System.out.println(" update DB success...");
			dbsm.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println(" updating Files for share ...");
		try {
			File[] f = dir.listFiles();
			for (int i = 0; i < f.length; i++) {
				DBSQLManager dbsma = new DBSQLManager();
				String selecta = "SELECT * from filesForShare where fileName='"
						+ f[i].getName().trim() + "';";
				dbsma.setSqlStr(selecta);
				dbsma.executeQuery();
				ResultSet rs = dbsma.getRs();
				if (!rs.next()) {
					f[i].delete();
				}
				dbsma.close();
			}
			System.out.println(" update  Files for share  success...");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void start() {
		updateDBAndFilesForShare();
		try {
			File f2 = new File(System.getProperty("user.dir") + "\\Config\\");
			if (!f2.exists()) {
				f2.mkdir();
			}
			File f = new File(System.getProperty("user.dir")
					+ "\\Config\\ServerCofig.ini");
			if (!f.exists()) {
				f.createNewFile();
				FileOutputStream bfos = new FileOutputStream(f);
				bfos.write("ServerPort = 8888".getBytes());
				bfos.flush();
				bfos.close();
				serverPort = "8888";
			} else {
				Properties cofigsFile = new Properties();
				cofigsFile.load(new FileInputStream(f));
				if (cofigsFile.getProperty("ServerPort") == null) {
					serverPort = "8888";
				} else {
					serverPort = cofigsFile.getProperty("ServerPort");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			System.out.println("Server is starting...");
			ss = new ServerSocket(Integer.parseInt(serverPort));
			started = true;
			System.out.println("Server started already!");
		} catch (BindException e) {
			System.out
					.println("端口格式不正确或端口已经被其它程序占用....\n请更改端口号或者关闭占用此端口的程序再重新启动此Server...");
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			while (started) {
				Socket s = ss.accept();
				Client c = new Client(s);
				new Thread(c).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				ss.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	class Client implements Runnable {
		private Socket s;
		private Timer t = new Timer();
		private DataInputStream dis = null;
		private DataOutputStream dos = null;
		private boolean scConnected = false;
		private boolean chatable = false;
		private String ausername;
		private boolean isOnline = false;
		private String lastOffTime;

		public Client(Socket s) {
			this.s = s;
			try {
				dis = new DataInputStream(s.getInputStream());
				dos = new DataOutputStream(s.getOutputStream());
				scConnected = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private class BeoffListener implements Runnable {
			public void run() {
				try {
					while (true) {
						Thread.sleep(800);
						String someoneoff = removeOff();
						if (someoneoff != null) {
							for (int j = 0; j < clients.size(); j++) {
								Client c = clients.get(j);
								c.send(someoneoff);
							}
						}
					}
				} catch (Exception e) {
					System.out
							.println("Thread BeoffListener's sleeping and client himself shutdown! So causes Exceptions!");
				}
			}
		}

		private class ClientUploadThread implements Runnable {
			String fileName = null;
			long filelength = 0;
			String fileSize = null;
			String fileKind = null;
			String fileFrom = null;
			String fileCreatedTime = null;
			String fileModifiedTime = null;

			public void run() {
				try {
					fileFrom = dis.readUTF();
					System.out.println("fileFrom:  " + fileFrom);
					fileName = dis.readUTF();
					fileName = fileName.replace("'", systemSigleQuote);
					DBSQLManager dbsmx = new DBSQLManager();
					String querySqlx = "select * from filesForShare where fileName='"
							+ fileName + "'";
					dbsmx.setSqlStr(querySqlx);
					dbsmx.executeQuery();
					ResultSet rsx = dbsmx.getRs();
					if (rsx.next()) {
						System.out.println("文件名为:" + fileName + "\n");
						String filePath = savePath + fileName;
						File file = new File(filePath);
						if (file.exists()) {
							dos.writeUTF("fileExists");
							dos.flush();
							String string = dis.readUTF();
							if (string.equalsIgnoreCase("overWrite")) {
								System.out.println("  And client: " + fileFrom
										+ " stats to overwrite it...");
								file.delete();
								if (fileName.lastIndexOf('.') != -1
										&& fileName != null) {
									fileKind = fileName.substring(fileName
											.lastIndexOf('.') + 1);
								} else {
									fileKind = "";
								}
								filelength = dis.readLong();
								if (filelength < 1000) {
									for (int j = 1; j < 4; j++) {
										if (String.valueOf(filelength).length() == j) {
											fileSize = "000"
													.substring(0, 3 - j)
													+ String.valueOf(filelength)
													+ "B";
											fileSize = "000M 000K " + fileSize;
										}
									}
								} else if ((1000 <= filelength)
										&& (filelength < 1000000)) {
									for (int j = 1; j < 4; j++) {
										if (String.valueOf(
												(int) (filelength / 1000))
												.length() == j) {
											fileSize = "000"
													.substring(0, 3 - j)
													+ String.valueOf((int) (filelength / 1000))
													+ "K ";
											fileSize = "000M " + fileSize;
										}
									}
									for (int j = 1; j < 4; j++) {
										if (String.valueOf(
												(int) (filelength % 1000))
												.length() == j) {
											fileSize = fileSize
													+ "000".substring(0, 3 - j)
													+ String.valueOf((int) (filelength % 1000))
													+ "B";
										}
									}
								} else if ((1000000 <= filelength)
										&& (filelength < 1000000 * 1000)) {
									for (int j = 1; j < 4; j++) {
										if (String.valueOf(
												(int) (filelength / 1000000))
												.length() == j) {
											fileSize = "000"
													.substring(0, 3 - j)
													+ String.valueOf((int) (filelength / 1000000))
													+ "M ";
										}
									}
									for (int j = 1; j < 4; j++) {
										if (String
												.valueOf(
														(int) ((filelength % 1000000) / 1000))
												.length() == j) {
											fileSize = fileSize
													+ "000".substring(0, 3 - j)
													+ String.valueOf((int) ((filelength % 1000000) / 1000))
													+ "K ";
										}
									}
									for (int j = 1; j < 4; j++) {
										if (String
												.valueOf(
														(int) ((filelength % 1000000) % 1000))
												.length() == j) {
											fileSize = fileSize
													+ "000".substring(0, 3 - j)
													+ String.valueOf((int) ((filelength % 1000000) % 1000))
													+ "B";
										}
									}
								}
								try {
									java.util.Date dateNow = new java.util.Date();
									SimpleDateFormat dateFormat = new SimpleDateFormat(
											"yyyy-MM-dd HH:mm:ss");
									fileModifiedTime = dateFormat
											.format(dateNow);
									DBSQLManager dbsm2 = new DBSQLManager();
									String sql2 = "UPDATE filesForShare SET "
											+ "fileSize = '" + fileSize.trim()
											+ "', status = 'uploading"
											+ "', fileFrom = '"
											+ fileFrom.trim()
											+ "' ,fileModifiedTime = '"
											+ fileModifiedTime.trim() + "' "
											+ " WHERE fileName = '"
											+ fileName.trim() + "' ; ";
									dbsm2.setSqlStr(sql2);
									dbsm2.executeUpdate();
									dbsm2.close();
									try {
										System.out.println("开始接收文件!" + "\n");
										DataOutputStream fileOut = new DataOutputStream(
												new BufferedOutputStream(
														new BufferedOutputStream(
																new FileOutputStream(
																		filePath))));
										int bufferSize = 8192;
										byte[] buf = new byte[bufferSize];
										int passedlen = 0;
										int count = 0;
										while (true) {
											count++;
											int readed = 0;
											if (dis != null) {
												readed = dis.read(buf);
											}
											fileOut.write(buf, 0, readed);
											passedlen = passedlen + readed;
											if (count % 80 == 0) {
												dos.writeUTF("percent:"
														+ (passedlen * 100L / filelength)
														+ "% passedlen:"
														+ passedlen);
												dos.flush();
											}
											if (passedlen >= filelength) {
												System.out.println("接收完成，文件存为"
														+ filePath + "\n");
												DBSQLManager dbsmw = new DBSQLManager();
												String sqlw = "UPDATE filesForShare SET "
														+ "status = 'uploaded' WHERE fileName = '"
														+ fileName.trim()
														+ "' ; ";
												dbsmw.setSqlStr(sqlw);
												dbsmw.executeUpdate();
												System.out
														.println(" file uploaded and into db success ...");
												dbsmw.close();
												break;
											}
										}
										fileOut.flush();
										fileOut.close();
										fileName = fileName.replace(
												systemSigleQuote, "'");
										dos.writeUTF("uploadedSuccess"
												+ fileName.trim()
												+ systemInfo
												+ fileKind.trim()
												+ systemInfo
												+ fileSize.trim()
												+ systemInfo
												+ fileFrom.trim()
												+ systemInfo
												+ rsx.getString(
														"fileCreatedTime")
														.trim() + systemInfo
												+ fileModifiedTime.trim()
												+ systemInfo);
										dos.flush();
										System.out
												.println("uploadedSuccess...");
									} catch (Exception ex) {
										try {
											dos.writeUTF("uploadedFailed");
											dos.flush();
										} catch (IOException e1) {
											e1.printStackTrace();
										}
										System.out
												.println("file exists and upload failed...");
										try {
											DBSQLManager dbsm3 = new DBSQLManager();
											String sql3 = "delete from filesForShare where fileName ='"
													+ fileName + "'";
											dbsm3.setSqlStr(sql3);
											dbsm3.executeUpdate();
											System.out
													.println(" file not exits and upload failed so delete record from db ...");
											dbsm3.close();
										} catch (SQLException exa) {
											exa.printStackTrace();
										}
										ex.printStackTrace();
									}
								} catch (SQLException e) {
									try {
										dos.writeUTF("uploadedFailed");
										dos.flush();
									} catch (IOException e1) {
										e1.printStackTrace();
									}
									System.out
											.println(" file not exists and into db failed...");
									e.printStackTrace();
								}
							} else if (string.equalsIgnoreCase("cancel")) {
								System.out.println(fileFrom
										+ " canseled upload file "
										+ file.getName() + "....");
							}
						} else if (!file.exists()) {
							try {
								DBSQLManager dbsm3x = new DBSQLManager();
								String sql3x = "delete from filesForShare where fileName ='"
										+ fileName + "'";
								dbsm3x.setSqlStr(sql3x);
								dbsm3x.executeUpdate();
								System.out
										.println(" file not exits but exits in db so delete it's record from db ...");
								dbsm3x.close();
							} catch (SQLException ex) {
								ex.printStackTrace();
							}
							dos.writeUTF("fileNotExists");
							dos.flush();
							if (fileName.lastIndexOf('.') != -1
									&& fileName != null) {
								fileKind = fileName.substring(fileName
										.lastIndexOf('.') + 1);
							} else {
								fileKind = "";
							}
							filelength = dis.readLong();
							if (filelength < 1000) {
								for (int j = 1; j < 4; j++) {
									if (String.valueOf(filelength).length() == j)
										fileSize = "000".substring(0, 3 - j)
												+ String.valueOf(filelength)
												+ "B";
									fileSize = "000M 000K " + fileSize;
								}
							} else if ((1000 <= filelength)
									&& (filelength < 1000000)) {
								for (int j = 1; j < 4; j++) {
									if (String.valueOf(
											(int) (filelength / 1000)).length() == j) {
										fileSize = "000".substring(0, 3 - j)
												+ String.valueOf((int) (filelength / 1000))
												+ "K ";
										fileSize = "000M " + fileSize;
									}
								}
								for (int j = 1; j < 4; j++) {
									if (String.valueOf(
											(int) (filelength % 1000)).length() == j)
										fileSize = fileSize
												+ "000".substring(0, 3 - j)
												+ String.valueOf((int) (filelength % 1000))
												+ "B";
								}
							} else if ((1000000 <= filelength)
									&& (filelength < 1000000 * 1000)) {
								for (int j = 1; j < 4; j++) {
									if (String.valueOf(
											(int) (filelength / 1000000))
											.length() == j)
										fileSize = "000".substring(0, 3 - j)
												+ String.valueOf((int) (filelength / 1000000))
												+ "M ";
								}
								for (int j = 1; j < 4; j++) {
									if (String
											.valueOf(
													(int) ((filelength % 1000000) / 1000))
											.length() == j)
										fileSize = fileSize
												+ "000".substring(0, 3 - j)
												+ String.valueOf((int) ((filelength % 1000000) / 1000))
												+ "K ";
								}
								for (int j = 1; j < 4; j++) {
									if (String
											.valueOf(
													(int) ((filelength % 1000000) % 1000))
											.length() == j)
										fileSize = fileSize
												+ "000".substring(0, 3 - j)
												+ String.valueOf((int) ((filelength % 1000000) % 1000))
												+ "B";
								}
							}
							try {
								java.util.Date dateNow = new java.util.Date();
								SimpleDateFormat dateFormat = new SimpleDateFormat(
										"yyyy-MM-dd HH:mm:ss");
								fileCreatedTime = dateFormat.format(dateNow);
								fileModifiedTime = fileCreatedTime;
								DBSQLManager dbsm = new DBSQLManager();
								String sql = "INSERT INTO filesForShare(fileName ,status,fileKind ,fileSize ,fileFrom,fileCreatedTime,fileModifiedTime,savePath) VALUES('"
										+ fileName.trim()
										+ "','uploading','"
										+ fileKind.trim()
										+ "','"
										+ fileSize.trim()
										+ "','"
										+ fileFrom.trim()
										+ "','"
										+ fileCreatedTime.trim()
										+ "','"
										+ fileModifiedTime.trim()
										+ "','"
										+ filePath.trim() + "')";
								dbsm.setSqlStr(sql);
								dbsm.executeUpdate();
								System.out
										.println(" file not exits and into db success ...");
								dbsm.close();
								try {
									DataOutputStream fileOut = new DataOutputStream(
											new BufferedOutputStream(
													new BufferedOutputStream(
															new FileOutputStream(
																	file))));
									System.out.println("开始接收文件!" + "\n");
									int bufferSize = 8192;
									byte[] buf = new byte[bufferSize];
									int passedlen = 0;
									int count = 0;
									while (true) {
										count++;
										int readed = 0;
										if (dis != null) {
											readed = dis.read(buf);
										}
										fileOut.write(buf, 0, readed);
										passedlen = passedlen + readed;
										if (count % 80 == 0) {
											dos.writeUTF("percent:"
													+ (passedlen * 100L / filelength)
													+ "% passedlen:"
													+ passedlen);
											dos.flush();
										}
										if (passedlen >= filelength) {
											System.out.println("接收完成，文件存为"
													+ filePath + "\n");
											DBSQLManager dbsmw = new DBSQLManager();
											String sqlw = "UPDATE filesForShare SET "
													+ "status = 'uploaded' WHERE fileName = '"
													+ fileName.trim() + "' ; ";
											dbsmw.setSqlStr(sqlw);
											dbsmw.executeUpdate();
											System.out
													.println(" file uploaded and into db success ...");
											dbsmw.close();
											break;
										}
									}
									fileOut.flush();
									fileOut.close();
									fileName = fileName.replace(
											systemSigleQuote, "'");
									dos.writeUTF("uploadedSuccess"
											+ fileName.trim()
											+ systemInfo
											+ fileKind.trim()
											+ systemInfo
											+ fileSize.trim()
											+ systemInfo
											+ fileFrom.trim()
											+ systemInfo
											+ rsx.getString("fileCreatedTime")
													.trim() + systemInfo
											+ fileModifiedTime.trim()
											+ systemInfo);
									dos.flush();
									System.out
											.println(" file not exits and uploadedSuccess...");
								} catch (Exception e) {
									try {
										DBSQLManager dbsm3 = new DBSQLManager();
										String sql3 = "delete from filesForShare where fileName ='"
												+ fileName + "'";
										dbsm3.setSqlStr(sql3);
										dbsm3.executeUpdate();
										System.out
												.println(" file not exits and upload failed so delete record from db ...");
										dbsm3.close();
									} catch (SQLException ex) {
										ex.printStackTrace();
									}
									try {
										dos.writeUTF("uploadedFailed");
										dos.flush();
									} catch (IOException e1) {
										e1.printStackTrace();
									}
									System.out.println("  uploaded failed...");
									e.printStackTrace();
								}
							} catch (SQLException e) {
								try {
									dos.writeUTF("uploadedFailed");
									dos.flush();
								} catch (IOException e1) {
									e1.printStackTrace();
								}
								e.printStackTrace();
							}
						}
					} else {
						String filePath = savePath + fileName;
						File file = new File(filePath);
						if (file.exists()) {
							dos.writeUTF("fileExists");
							dos.flush();
							String string = dis.readUTF();
							if (string.equalsIgnoreCase("overWrite")) {
								System.out.println("  And client: " + fileFrom
										+ " stats to overwrite it...");
								file.delete();
								if (fileName.lastIndexOf('.') != -1
										&& fileName != null) {
									fileKind = fileName.substring(fileName
											.lastIndexOf('.') + 1);
								} else {
									fileKind = "";
								}
								filelength = dis.readLong();
								if (filelength < 1000) {
									for (int j = 1; j < 4; j++) {
										if (String.valueOf(filelength).length() == j) {
											fileSize = "000"
													.substring(0, 3 - j)
													+ String.valueOf(filelength)
													+ "B";
											fileSize = "000M 000K " + fileSize;
										}
									}
								} else if ((1000 <= filelength)
										&& (filelength < 1000000)) {
									for (int j = 1; j < 4; j++) {
										if (String.valueOf(
												(int) (filelength / 1000))
												.length() == j) {
											fileSize = "000"
													.substring(0, 3 - j)
													+ String.valueOf((int) (filelength / 1000))
													+ "K ";
											fileSize = "000M " + fileSize;
										}
									}
									for (int j = 1; j < 4; j++) {
										if (String.valueOf(
												(int) (filelength % 1000))
												.length() == j)
											fileSize = fileSize
													+ "000".substring(0, 3 - j)
													+ String.valueOf((int) (filelength % 1000))
													+ "B";
									}
								} else if ((1000000 <= filelength)
										&& (filelength < 1000000 * 1000)) {
									for (int j = 1; j < 4; j++) {
										if (String.valueOf(
												(int) (filelength / 1000000))
												.length() == j)
											fileSize = "000"
													.substring(0, 3 - j)
													+ String.valueOf((int) (filelength / 1000000))
													+ "M ";
									}
									for (int j = 1; j < 4; j++) {
										if (String
												.valueOf(
														(int) ((filelength % 1000000) / 1000))
												.length() == j)
											fileSize = fileSize
													+ "000".substring(0, 3 - j)
													+ String.valueOf((int) ((filelength % 1000000) / 1000))
													+ "K ";
									}
									for (int j = 1; j < 4; j++) {
										if (String
												.valueOf(
														(int) ((filelength % 1000000) % 1000))
												.length() == j)
											fileSize = fileSize
													+ "000".substring(0, 3 - j)
													+ String.valueOf((int) ((filelength % 1000000) % 1000))
													+ "B";
									}
								}
								try {
									java.util.Date dateNow = new java.util.Date();
									SimpleDateFormat dateFormat = new SimpleDateFormat(
											"yyyy-MM-dd HH:mm:ss");
									fileCreatedTime = dateFormat
											.format(dateNow);
									fileModifiedTime = fileCreatedTime;
									DBSQLManager dbsm2 = new DBSQLManager();
									String sql2 = "INSERT INTO filesForShare(fileName ,status,fileKind ,fileSize ,fileFrom,fileCreatedTime,fileModifiedTime,savePath) VALUES('"
											+ fileName.trim()
											+ "','uploading','"
											+ fileKind.trim()
											+ "','"
											+ fileSize.trim()
											+ "','"
											+ fileFrom.trim()
											+ "','"
											+ fileCreatedTime.trim()
											+ "','"
											+ fileModifiedTime.trim()
											+ "','"
											+ filePath.trim() + "')";
									dbsm2.setSqlStr(sql2);
									dbsm2.executeUpdate();
									System.out
											.println(" file not in db but exists in share folder so insert it into db and successed ...");
									dbsm2.close();
									try {
										System.out.println("开始接收文件!" + "\n");
										DataOutputStream fileOut = new DataOutputStream(
												new BufferedOutputStream(
														new BufferedOutputStream(
																new FileOutputStream(
																		filePath))));
										int bufferSize = 8192;
										byte[] buf = new byte[bufferSize];
										int passedlen = 0;
										int count = 0;
										while (true) {
											count++;
											int readed = 0;
											if (dis != null) {
												readed = dis.read(buf);
											}
											fileOut.write(buf, 0, readed);
											passedlen = passedlen + readed;
											if (count % 80 == 0) {
												dos.writeUTF("percent:"
														+ (passedlen * 100L / filelength)
														+ "% passedlen:"
														+ passedlen);
												dos.flush();
											}
											if (passedlen >= filelength) {
												System.out.println("接收完成，文件存为"
														+ filePath + "\n");
												DBSQLManager dbsmw = new DBSQLManager();
												String sqlw = "UPDATE filesForShare SET "
														+ "status = 'uploaded' WHERE fileName = '"
														+ fileName.trim()
														+ "' ; ";
												dbsmw.setSqlStr(sqlw);
												dbsmw.executeUpdate();
												System.out
														.println(" file uploaded and into db success ...");
												dbsmw.close();
												break;
											}
										}
										fileOut.flush();
										fileOut.close();
										fileName = fileName.replace(
												systemSigleQuote, "'");
										dos.writeUTF("uploadedSuccess"
												+ fileName.trim() + systemInfo
												+ fileKind.trim() + systemInfo
												+ fileSize.trim() + systemInfo
												+ fileFrom.trim() + systemInfo
												+ fileCreatedTime.trim()
												+ systemInfo
												+ fileModifiedTime.trim()
												+ systemInfo);
										dos.flush();
										System.out
												.println("uploadedSuccess...");
									} catch (Exception ex) {
										try {
											dos.writeUTF("uploadedFailed");
											dos.flush();
										} catch (IOException e1) {
											e1.printStackTrace();
										}
										System.out
												.println("file exists and upload failed...");
										try {
											DBSQLManager dbsm3 = new DBSQLManager();
											String sql3 = "delete from filesForShare where fileName ='"
													+ fileName + "'";
											dbsm3.setSqlStr(sql3);
											dbsm3.executeUpdate();
											System.out
													.println(" file not exits and upload failed so delete record from db ...");
											dbsm3.close();
										} catch (SQLException exa) {
											exa.printStackTrace();
										}
										ex.printStackTrace();
									}
								} catch (SQLException e) {
									try {
										dos.writeUTF("uploadedFailed");
										dos.flush();
									} catch (IOException e1) {
										e1.printStackTrace();
									}
									System.out
											.println(" file not exists and into db failed...");
									e.printStackTrace();
								}
							} else if (string.equalsIgnoreCase("cancel")) {
								System.out.println(fileFrom
										+ " canseled upload file "
										+ file.getName() + "....");
							}
						} else if (!file.exists()) {
							dos.writeUTF("fileNotExists");
							dos.flush();
							if (fileName.lastIndexOf('.') != -1
									&& fileName != null) {
								fileKind = fileName.substring(fileName
										.lastIndexOf('.') + 1);
							} else {
								fileKind = "";
							}
							filelength = dis.readLong();
							if (filelength < 1000) {
								for (int j = 1; j < 4; j++) {
									if (String.valueOf(filelength).length() == j) {
										fileSize = "000".substring(0, 3 - j)
												+ String.valueOf(filelength)
												+ "B";
										fileSize = "000M 000K " + fileSize;
									}
								}
							} else if ((1000 <= filelength)
									&& (filelength < 1000000)) {
								for (int j = 1; j < 4; j++) {
									if (String.valueOf(
											(int) (filelength / 1000)).length() == j) {
										fileSize = "000".substring(0, 3 - j)
												+ String.valueOf((int) (filelength / 1000))
												+ "K ";
										fileSize = "000M " + fileSize;
									}
								}
								for (int j = 1; j < 4; j++) {
									if (String.valueOf(
											(int) (filelength % 1000)).length() == j)
										fileSize = fileSize
												+ "000".substring(0, 3 - j)
												+ String.valueOf((int) (filelength % 1000))
												+ "B";
								}
							} else if ((1000000 <= filelength)
									&& (filelength < 1000000 * 1000)) {
								for (int j = 1; j < 4; j++) {
									if (String.valueOf(
											(int) (filelength / 1000000))
											.length() == j)
										fileSize = "000".substring(0, 3 - j)
												+ String.valueOf((int) (filelength / 1000000))
												+ "M ";
								}
								for (int j = 1; j < 4; j++) {
									if (String
											.valueOf(
													(int) ((filelength % 1000000) / 1000))
											.length() == j)
										fileSize = fileSize
												+ "000".substring(0, 3 - j)
												+ String.valueOf((int) ((filelength % 1000000) / 1000))
												+ "K ";
								}
								for (int j = 1; j < 4; j++) {
									if (String
											.valueOf(
													(int) ((filelength % 1000000) % 1000))
											.length() == j)
										fileSize = fileSize
												+ "000".substring(0, 3 - j)
												+ String.valueOf((int) ((filelength % 1000000) % 1000))
												+ "B";
								}
							}
							try {
								java.util.Date dateNow = new java.util.Date();
								SimpleDateFormat dateFormat = new SimpleDateFormat(
										"yyyy-MM-dd HH:mm:ss");
								fileCreatedTime = dateFormat.format(dateNow);
								fileModifiedTime = fileCreatedTime;
								DBSQLManager dbsm = new DBSQLManager();
								String sql = "INSERT INTO filesForShare(fileName ,status,fileKind ,fileSize ,fileFrom,fileCreatedTime,fileModifiedTime,savePath) VALUES('"
										+ fileName.trim()
										+ "','uploading','"
										+ fileKind.trim()
										+ "','"
										+ fileSize.trim()
										+ "','"
										+ fileFrom.trim()
										+ "','"
										+ fileCreatedTime.trim()
										+ "','"
										+ fileModifiedTime.trim()
										+ "','"
										+ filePath.trim() + "')";
								dbsm.setSqlStr(sql);
								dbsm.executeUpdate();
								System.out
										.println(" file not exits and into db success ...");
								dbsm.close();
								try {
									DataOutputStream fileOut = new DataOutputStream(
											new BufferedOutputStream(
													new BufferedOutputStream(
															new FileOutputStream(
																	file))));
									System.out.println("开始接收文件!" + "\n");
									int bufferSize = 8192;
									byte[] buf = new byte[bufferSize];
									int passedlen = 0;
									int count = 0;
									while (true) {
										count++;
										int readed = 0;
										if (dis != null) {
											readed = dis.read(buf);
										}
										fileOut.write(buf, 0, readed);
										passedlen = passedlen + readed;
										if (count % 80 == 0) {
											dos.writeUTF("percent:"
													+ (passedlen * 100L / filelength)
													+ "% passedlen:"
													+ passedlen);
											dos.flush();
										}
										if (passedlen >= filelength) {
											System.out.println("接收完成，文件存为"
													+ filePath + "\n");
											DBSQLManager dbsmw = new DBSQLManager();
											String sqlw = "UPDATE filesForShare SET "
													+ "status = 'uploaded' WHERE fileName = '"
													+ fileName.trim() + "' ; ";
											dbsmw.setSqlStr(sqlw);
											dbsmw.executeUpdate();
											System.out
													.println(" file uploaded and into db success ...");
											dbsmw.close();
											break;
										}
									}
									fileOut.flush();
									fileOut.close();
									fileName = fileName.replace(
											systemSigleQuote, "'");
									dos.writeUTF("uploadedSuccess"
											+ fileName.trim() + systemInfo
											+ fileKind.trim() + systemInfo
											+ fileSize.trim() + systemInfo
											+ fileFrom.trim() + systemInfo
											+ fileCreatedTime.trim()
											+ systemInfo
											+ fileModifiedTime.trim()
											+ systemInfo);
									dos.flush();
									System.out
											.println(" file not exits and uploadedSuccess...");
								} catch (Exception e) {
									try {
										DBSQLManager dbsm3 = new DBSQLManager();
										String sql3 = "delete from filesForShare where fileName ='"
												+ fileName + "'";
										dbsm3.setSqlStr(sql3);
										dbsm3.executeUpdate();
										System.out
												.println(" file not exits and upload failed so delete record from db ...");
										dbsm3.close();
									} catch (SQLException ex) {
										ex.printStackTrace();
									}
									try {
										dos.writeUTF("uploadedFailed");
										dos.flush();
									} catch (IOException e1) {
										e1.printStackTrace();
									}
									System.out.println("  uploaded failed...");
									e.printStackTrace();
								}
							} catch (SQLException e) {
								try {
									dos.writeUTF("uploadedFailed");
									dos.flush();
								} catch (IOException e1) {
									e1.printStackTrace();
								}
								e.printStackTrace();
							}
						}
					}
					dbsmx.close();
				} catch (Exception e) {
					try {
						dos.writeUTF("uploadedFailed");
						dos.flush();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					System.out.println("  uploaded failed...");
					e.printStackTrace();
				} finally {
					try {
						if (dos != null)
							dos.close();
						if (dis != null)
							dis.close();
						if (s != null)
							s.close();
						System.out.println("server socket for upload "
								+ fileName + " closed...");
					} catch (Exception e) {
					}
				}
			}
		}

		final class ClientDownloadThread implements Runnable {
			String filename = null;
			String filePath = null;

			public void run() {
				try {
					ausername = dis.readUTF();
					filename = dis.readUTF();
					filename = filename.replace("'", systemSigleQuote);
					sendFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			public void sendFile() {
				try {
					filePath = savePath + filename;
					File file = new File(filePath);
					if (!file.exists()) {
						dos.writeUTF("fileNotFound");
						dos.flush();
						DBSQLManager dbsm = new DBSQLManager();
						String delete = "delete from filesForShare where fileName ='"
								+ filename + "'";
						dbsm.setSqlStr(delete);
						dbsm.executeUpdate();
						dbsm.close();
					} else {
						dos.writeUTF("fileFound");
						dos.writeLong(file.length());
						dos.flush();
						DataInputStream adis = new DataInputStream(
								new BufferedInputStream(new FileInputStream(
										file.getAbsoluteFile())));
						System.out.println(" start to send " + filename
								+ " to client ..");
						int bufferSize = 8192;
						byte[] buf = new byte[bufferSize];
						int count = 0;
						while (true) {
							count++;
							int readed = 0;
							if (adis != null) {
								readed = adis.read(buf);
							}
							if (readed == -1) {
								break;
							}
							dos.write(buf, 0, readed);
						}
						dos.flush();
						String str = dis.readUTF();
						if (str.equalsIgnoreCase("downloadedSuccess")) {
							System.out.println(filename
									+ " downloadedSuccess...");
						} else if (str.equalsIgnoreCase("downloadedFailed")) {
							System.out.println(filename
									+ " downloaded failed...");
						}
					}
				} catch (Exception ufe) {
					ufe.printStackTrace();
				}
				try {
					if (dos != null)
						dos.close();
					if (dis != null)
						dis.close();
					if (s != null) {
						s.close();
						java.util.Date dateNow = new java.util.Date();
						SimpleDateFormat dateFormat = new SimpleDateFormat(
								"yyyy-MM-dd HH:mm:ss");
						String thetime = dateFormat.format(dateNow);
						System.out.println(thetime + " file: " + filename
								+ " send to " + ausername
								+ " over, download thread'socket closed... ");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public synchronized void send(String str) throws Exception {
			try {
				dos.writeUTF(str);
				dos.flush();
			} catch (IOException e) {
				throw new Exception(e);
			}
		}

		public boolean validateUserInfo(String validinfo) {
			ausername = null;
			String apassword = null;
			java.sql.Connection conn = null;
			java.sql.Statement stmt = null;
			java.sql.ResultSet rs = null;
			try {
				if (scConnected) {
					int indexOfSpace;
					indexOfSpace = validinfo.indexOf(" ");
					ausername = validinfo.substring(0, indexOfSpace);
					apassword = validinfo.substring((indexOfSpace + 1),
							validinfo.length());
					conn = DBConnectionManager.getConnection();
					stmt = conn.createStatement();
					rs = stmt
							.executeQuery("select * from users where username='"
									+ ausername
									+ "' and password='"
									+ apassword + "'");
					if ((rs != null) & (rs.next())) {
						return true;
					} else {
						return false;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out
						.println("Errors happen while receive validte infos from Client!");
				return false;
			}
			return false;
		}

		public void run() {
			try {
				if (scConnected) {
					String str = dis.readUTF();
					if (str.equalsIgnoreCase("UploadAFile")) {
						new Thread(new ClientUploadThread()).start();
					} else if (str.equalsIgnoreCase("DownloadAFile")) {
						new Thread(new ClientDownloadThread()).start();
					} else {
						boolean validOK = validateUserInfo(str);
						if (validOK) {
							isOnline = usersOnline.contains(ausername);
							if (isOnline) {
								java.util.Date dateNow = new java.util.Date();
								SimpleDateFormat dateFormat = new SimpleDateFormat(
										"yyyy-MM-dd HH:mm:ss");
								String dateNowStr = dateFormat.format(dateNow);
								dos.writeUTF("doubled");
								dos.flush();
								System.out.println("[" + dateNowStr + "]"
										+ ausername + "重复登录失败...");
								chatable = false;
							} else {
								java.util.Date dateNow = new java.util.Date();
								SimpleDateFormat dateFormat = new SimpleDateFormat(
										"yyyy-MM-dd HH:mm:ss");
								String dateNowStr = dateFormat.format(dateNow);
								System.out
										.println("******* ["
												+ dateNowStr
												+ "]: "
												+ ausername
												+ " logs on, validated and chatable. *******\n");
								dos.writeUTF("istrue");
								dos.flush();
								System.out
										.println("validate result sent to Client already!");
								chatable = true;
							}
						} else {
							dos.writeUTF("isfalse");
							dos.flush();
							chatable = false;
						}
					}
					if (chatable) {
						clients.add(this);
						System.out.println("server chatable");
						usersOnline.add(ausername);
						System.out
								.println("Send All Users to Client when it starts up...");
						try {
							DBSQLManager dbsm = new DBSQLManager();
							String querySql = "select * from users;";
							dbsm.setSqlStr(querySql);
							dbsm.executeQuery();
							ResultSet rs = dbsm.getRs();
							UserServerSide user = new UserServerSide(systemInfo);
							while (rs.next()) {
								user.setUsername(rs.getString("username"));
								user.setRealname(rs.getString("realname"));
								user.setSex(rs.getString("sex"));
								user.setEmail(rs.getString("email"));
								user.setPhone(rs.getString("phone"));
								user.setAddress(rs.getString("address"));
								dos.writeUTF(user.toString() + systemInfo
										+ "getAllUsers");
							}
							dos.flush();
							dbsm.close();
						} catch (SQLException e) {
							e.printStackTrace();
						}
						System.out.println("Send All Files for Share to "
								+ ausername + " when it starts up...");
						try {
							DBSQLManager dbsm = new DBSQLManager();
							String querySql = "select * from filesForShare;";
							dbsm.setSqlStr(querySql);
							dbsm.executeQuery();
							ResultSet rs = dbsm.getRs();
							FileBeanServerSide file = new FileBeanServerSide(
									systemInfo, systemSigleQuote);
							while (rs.next()) {
								file.setFileName(rs.getString("fileName"));
								file.setFileKind(rs.getString("fileKind"));
								file.setFileSize(rs.getString("fileSize"));
								file.setFileFrom(rs.getString("fileFrom"));
								file.setFileCreatedTime(rs
										.getString("fileCreatedTime"));
								file.setFileModifiedTime(rs
										.getString("fileModifiedTime"));
								dos.writeUTF(file.toString() + systemInfo
										+ "getAllFilesForShare");
							}
							dos.writeUTF("end" + systemInfo
									+ "getAllFilesForShare");
							dos.flush();
							dbsm.close();
						} catch (SQLException e) {
							e.printStackTrace();
						}
						System.out.println("Send today's messages to "
								+ ausername + " when it starts up...");
						try {
							java.util.Date dateNowa = new java.util.Date();
							SimpleDateFormat dateFormata = new SimpleDateFormat(
									"yyyy-MM-dd");
							String thetimea = dateFormata.format(dateNowa);
							lastOffTime = thetimea + " 00:00:00";
							DBSQLManager dbsma = new DBSQLManager();
							String querySqla = "select thetime from log_info  where id=(select max(id) from log_Info where status='offline' and username='"
									+ ausername + "' ) ;";
							dbsma.setSqlStr(querySqla);
							dbsma.executeQuery();
							ResultSet rsa = dbsma.getRs();
							while (rsa.next()) {
								lastOffTime = rsa.getString("thetime");
							}
							dbsma.close();
							DBSQLManager dbsm = new DBSQLManager();
							String querySql = "select * from messages where thetime regexp '^"
									+ thetimea
									+ "' and thetime>'"
									+ lastOffTime + "';";
							dbsm.setSqlStr(querySql);
							dbsm.executeQuery();
							ResultSet rs = dbsm.getRs();
							MessageServerSide message = new MessageServerSide(
									systemSigleQuote);
							while (rs.next()) {
								message.setUsername(rs.getString("username"));
								message.setThetime(rs.getString("thetime"));
								message.setMessage(rs.getString("message"));
								dos.writeUTF(message.toString());
							}
							dos.flush();
							dbsm.close();
						} catch (SQLException e) {
							e.printStackTrace();
						}
						System.out.println("Send UsersOnline to " + ausername
								+ " where it starts up...");
						String usersOnlineStr = (usersOnline.toString())
								.substring(1,
										(usersOnline.toString()).length() - 1)
								+ ", " + systemInfo + "getUsersOnline";
						dos.writeUTF(usersOnlineStr);
						dos.writeUTF(systemInfo + "cChatable");
						dos.flush();
						java.util.Date dateNow = new java.util.Date();
						SimpleDateFormat dateFormat = new SimpleDateFormat(
								"yyyy-MM-dd HH:mm:ss");
						String dateNowStr = dateFormat.format(dateNow);
						String iamon = "************ 系统信息 [" + dateNowStr
								+ "]: " + ausername + " 我上线啦! ************\n\n";
						try {
							DBSQLManager dbsm = new DBSQLManager();
							String sql = "INSERT INTO messages(username ,thetime ,message ) VALUES('sysInfomation','"
									+ dateNowStr.trim() + "','" + iamon + "')";
							dbsm.setSqlStr(sql);
							dbsm.executeUpdate();
							dbsm.close();
						} catch (SQLException e) {
							e.printStackTrace();
						}
						try {
							DBSQLManager dbsm = new DBSQLManager();
							java.util.Date dateNowee = new java.util.Date();
							SimpleDateFormat dateFormatee = new SimpleDateFormat(
									"yyyy-MM-dd HH:mm:ss");
							String thetimeee = dateFormatee.format(dateNowee);
							String sql = "INSERT INTO log_Info(username ,thetime ,status ) VALUES('"
									+ ausername.trim()
									+ "','"
									+ thetimeee.trim() + "','" + "online')";
							dbsm.setSqlStr(sql);
							dbsm.executeUpdate();
							dbsm.close();
						} catch (SQLException e) {
							e.printStackTrace();
						}
						for (int j = 0; j < clients.size(); j++) {
							Client c = clients.get(j);
							c.send(iamon);
						}
						new Thread(new BeoffListener()).start();
						t.scheduleAtFixedRate(new TimerTask() {
							public void run() {
								System.out.println("Send files update info to "
										+ ausername
										+ " while it is chatable ...");
								try {
									java.util.Date dateNow = new java.util.Date();
									SimpleDateFormat dateFormat = new SimpleDateFormat(
											"yyyy-MM-dd HH:mm:ss");
									String thetimeNow = dateFormat
											.format(dateNow);

									StringBuffer filesCreatedSinceLastOffline = new StringBuffer(
											"\n&&&&& 共享中最近新上传了以下文件：\n\n");
									int len1 = filesCreatedSinceLastOffline
											.length();
									DBSQLManager dbsm = new DBSQLManager();
									String querySql = "select fileName from filesForShare where fileFrom!='"
											+ ausername
											+ "' and status='uploaded' and fileCreatedTime>'"
											+ lastOffTime + "';";
									dbsm.setSqlStr(querySql);
									dbsm.executeQuery();
									ResultSet rs = dbsm.getRs();
									while (rs.next()) {
										filesCreatedSinceLastOffline
												.append("   ");
										filesCreatedSinceLastOffline.append(rs
												.getString("fileName").replace(
														systemSigleQuote, "'"));
										filesCreatedSinceLastOffline
												.append("\n");
									}
									dbsm.close();
									int len2 = filesCreatedSinceLastOffline
											.length();
									StringBuffer filesModifiedSinceLastOffline = new StringBuffer(
											"\n&&&&& 共享中最近更新了以下文件：\n\n");
									int len3 = filesModifiedSinceLastOffline
											.length();
									DBSQLManager dbsmx = new DBSQLManager();
									String querySqlx = "select fileName from filesForShare where fileFrom!='"
											+ ausername
											+ "' and status='uploaded' and fileModifiedTime >'"
											+ lastOffTime
											+ "' and fileModifiedTime > fileCreatedTime;";
									dbsmx.setSqlStr(querySqlx);
									dbsmx.executeQuery();
									ResultSet rsx = dbsmx.getRs();
									while (rsx.next()) {
										filesModifiedSinceLastOffline
												.append("                      ");
										filesModifiedSinceLastOffline
												.append(rsx.getString(
														"fileName").replace(
														systemSigleQuote, "'"));
										filesModifiedSinceLastOffline
												.append("\n");
									}
									dbsmx.close();
									int len4 = filesModifiedSinceLastOffline
											.length();
									if (len2 > len1) {
										filesCreatedSinceLastOffline
												.append("\n&&&&& 请注意查收!\n\n\n");
										dos.writeUTF(filesCreatedSinceLastOffline
												.toString());
										dos.flush();
									}
									if (len4 > len3) {
										filesModifiedSinceLastOffline
												.append("\n&&&&& 请注意查收!\n\n\n");
										dos.writeUTF(filesModifiedSinceLastOffline
												.toString());
										dos.flush();
									}

									lastOffTime = thetimeNow;
								} catch (SQLException e) {
									e.printStackTrace();
								} catch (Exception e) {
									System.out
											.println("send files update info to chient but client exits...");
								}
							}
						}, 0l, 60950l);
					}
					while (chatable) {
						String message = dis.readUTF();
						if (message
								.equalsIgnoreCase(systemInfo + "getAllUsers")) {
							System.out.println("Send All Users to " + ausername
									+ "");
							try {
								DBSQLManager dbsm = new DBSQLManager();
								String querySql = "select * from users;";
								dbsm.setSqlStr(querySql);
								dbsm.executeQuery();
								ResultSet rs = dbsm.getRs();
								UserServerSide user = new UserServerSide(
										systemInfo);
								while (rs.next()) {
									user.setUsername(rs.getString("username"));
									user.setRealname(rs.getString("realname"));
									user.setSex(rs.getString("sex"));
									user.setEmail(rs.getString("email"));
									user.setPhone(rs.getString("phone"));
									user.setAddress(rs.getString("address"));
									dos.writeUTF(user.toString() + systemInfo
											+ "getAllUsers");
								}
								dos.flush();
								dbsm.close();
							} catch (SQLException e) {
								e.printStackTrace();
							}
						} else if (message.equalsIgnoreCase(systemInfo
								+ "getAllFilesForShare")) {
							System.out.println("Send All files for share to "
									+ ausername + "");
							try {
								updateDBAndFilesForShare();
								DBSQLManager dbsm = new DBSQLManager();
								String querySql = "select * from filesForShare;";
								dbsm.setSqlStr(querySql);
								dbsm.executeQuery();
								ResultSet rs = dbsm.getRs();
								FileBeanServerSide file = new FileBeanServerSide(
										systemInfo, systemSigleQuote);
								while (rs.next()) {
									file.setFileName(rs.getString("fileName"));
									file.setFileKind(rs.getString("fileKind"));
									file.setFileSize(rs.getString("fileSize"));
									file.setFileFrom(rs.getString("fileFrom"));
									file.setFileCreatedTime(rs
											.getString("fileCreatedTime"));
									file.setFileModifiedTime(rs
											.getString("fileModifiedTime"));
									dos.writeUTF(file.toString() + systemInfo
											+ "getAllFilesForShare");
								}
								dos.writeUTF("end" + systemInfo
										+ "getAllFilesForShare");
								dos.flush();
								System.out.println("end" + systemInfo
										+ "getAllFilesForShare");
								dbsm.close();
							} catch (SQLException e) {
								e.printStackTrace();
							}
						} else if (message.equalsIgnoreCase(systemInfo
								+ "getUsersOnline")) {
							System.out.println("getUsersOnline");
							String usersOnlineStr = (usersOnline.toString())
									.substring(
											1,
											(usersOnline.toString()).length() - 1)
									+ ", " + systemInfo + "getUsersOnline";
							dos.writeUTF(usersOnlineStr);
							dos.flush();
						} else if (message.endsWith(systemInfo
								+ "deleteFileOfMine")) {
							ArrayList<String> filesDeleteSuccessList = new ArrayList<String>();
							StringBuffer filesToDeleteSB = new StringBuffer(
									message.substring(0,
											message.lastIndexOf(systemInfo)));
							while (filesToDeleteSB.indexOf(systemInfo) != -1) {
								String aFileName = (filesToDeleteSB.substring(
										0, filesToDeleteSB.indexOf(systemInfo)))
										.trim();
								aFileName = aFileName.replace("'",
										systemSigleQuote);
								try {
									DBSQLManager dbsm = new DBSQLManager();
									String deleteSql = "delete from filesForShare where fileName= '"
											+ aFileName + "' ;";
									dbsm.setSqlStr(deleteSql);
									dbsm.executeUpdate();
									dbsm.close();
									try {
										File bfile = new File(savePath
												+ aFileName);
										if (bfile.exists()) {
											bfile.delete();
										}
										aFileName = aFileName.replace(
												systemSigleQuote, "'");
										if (filesDeleteSuccessList.size() < 1) {
											filesDeleteSuccessList
													.add(aFileName);
										} else {
											filesDeleteSuccessList
													.add("\n     " + aFileName);
										}
									} catch (Exception ea) {
										aFileName = aFileName.replace(
												systemSigleQuote, "'");
										dos.writeUTF(aFileName
												+ "fileDeleteFailed"
												+ systemInfo + "deleteFailed");
										dos.flush();
										ea.printStackTrace();
										System.out
												.println(aFileName
														+ " delete Failed maybe the file is being used....");
									}
								} catch (SQLException e) {
									aFileName = aFileName.replace(
											systemSigleQuote, "'");
									dos.writeUTF(aFileName + "updateDBFailed"
											+ systemInfo + "deleteFailed");
									dos.flush();
									e.printStackTrace();
									System.out.println(aFileName
											+ " delete from DB failed ....");
								}
								filesToDeleteSB.delete(0,
										filesToDeleteSB.indexOf(systemInfo)
												+ systemInfo.length());
							}
							dos.writeUTF(filesDeleteSuccessList.toString()
									+ systemInfo + "deleteSuccess");
							dos.flush();
						} else if (message.endsWith(systemInfo
								+ "getHistoryByTime")) {
							try {
								DBSQLManager dbsm = new DBSQLManager();
								String usernameStr = message.substring(0,
										message.indexOf("username")).trim();
								String querySql;
								String fromTimeStr = message.substring(
										message.indexOf("username")
												+ "username".length(),
										message.indexOf(systemInfo));
								String toTimeStr = message.substring(
										message.indexOf(systemInfo)
												+ systemInfo.length(),
										message.lastIndexOf(systemInfo));
								if (!usernameStr.equalsIgnoreCase("")) {
									querySql = "select * from messages where username='"
											+ usernameStr
											+ "' and thetime > '"
											+ fromTimeStr
											+ "' and thetime < '"
											+ toTimeStr + "'  ;";
								} else {
									querySql = "select * from messages where thetime > '"
											+ fromTimeStr
											+ "' and thetime < '"
											+ toTimeStr + "'  ;";
								}
								dbsm.setSqlStr(querySql);
								dbsm.executeQuery();
								ResultSet rs = dbsm.getRs();
								MessageServerSide messageBean = new MessageServerSide(
										systemSigleQuote);
								while (rs.next()) {
									messageBean.setUsername(rs
											.getString("username"));
									messageBean.setThetime(rs
											.getString("thetime"));
									messageBean.setMessage(rs
											.getString("message"));
									dos.writeUTF(messageBean.toString()
											+ systemInfo + "getHistoryByTime");
								}
								dos.writeUTF(systemInfo + "end" + systemInfo
										+ "getHistoryByTime");
								dos.flush();
								dbsm.close();
							} catch (SQLException e) {
								e.printStackTrace();
							}
						} else if (message.startsWith(systemInfo
								+ "sysInfomation")) {
							message = message
									.substring((systemInfo + "sysInfomation")
											.length());
							for (int j = 0; j < clients.size(); j++) {
								Client c = clients.get(j);
								c.send(message);
							}
							try {
								message = message
										.replace("'", systemSigleQuote);
								DBSQLManager dbsm = new DBSQLManager();
								java.util.Date dateNow = new java.util.Date();
								SimpleDateFormat dateFormat = new SimpleDateFormat(
										"yyyy-MM-dd HH:mm:ss");
								String thetime = dateFormat.format(dateNow);
								String sql = "INSERT INTO messages(username ,thetime ,message ) VALUES('sysInfomation','"
										+ thetime.trim()
										+ "','"
										+ message
										+ "')";
								dbsm.setSqlStr(sql);
								dbsm.executeUpdate();
								dbsm.close();
							} catch (SQLException e) {
								e.printStackTrace();
							}
						} else {
							for (int j = 0; j < clients.size(); j++) {
								Client c = clients.get(j);
								c.send(message);
							}
							message = message.replace("'", systemSigleQuote);
							try {
								DBSQLManager dbsm = new DBSQLManager();
								java.util.Date dateNow = new java.util.Date();
								SimpleDateFormat dateFormat = new SimpleDateFormat(
										"yyyy-MM-dd HH:mm:ss");
								String thetime = dateFormat.format(dateNow);
								int abc = message.indexOf("]: \n");
								message = message.substring(abc + 4);
								String sql = "INSERT INTO messages(username ,thetime ,message ) VALUES('"
										+ ausername.trim()
										+ "','"
										+ thetime.trim()
										+ "','"
										+ "  "
										+ message + "')";
								dbsm.setSqlStr(sql);
								dbsm.executeUpdate();
								dbsm.close();
							} catch (SQLException e) {
								e.printStackTrace();
							}
						}
					}
				}
			} catch (SocketException e) {
				t.cancel();
				e.printStackTrace();
				usersOnline.remove(ausername);
				clients.remove(this);
				java.util.Date dateNow = new java.util.Date();
				SimpleDateFormat dateFormat = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
				String dateNowStr = dateFormat.format(dateNow);
				addOff("************ 系统信息 [" + dateNowStr + "]: " + ausername
						+ " 下线喽！Bye! ************\n\n");
				String iamoff = "************ 系统信息 [" + dateNowStr + "]: "
						+ ausername + " 下线喽！Bye! ************\n\n";
				try {
					DBSQLManager dbsm = new DBSQLManager();
					String sql = "INSERT INTO messages(username ,thetime ,message ) VALUES('sysInfomation','"
							+ dateNowStr.trim() + "','" + iamoff + "')";
					dbsm.setSqlStr(sql);
					dbsm.executeUpdate();
					dbsm.close();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
				try {
					DBSQLManager dbsm = new DBSQLManager();
					java.util.Date dateNoweea = new java.util.Date();
					SimpleDateFormat dateFormateea = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					String thetimeeea = dateFormateea.format(dateNoweea);
					String sql = "INSERT INTO log_Info(username ,thetime ,status ) VALUES('"
							+ ausername.trim()
							+ "','"
							+ thetimeeea.trim()
							+ "','" + "offline')";
					dbsm.setSqlStr(sql);
					dbsm.executeUpdate();
					dbsm.close();
				} catch (SQLException eee) {
					eee.printStackTrace();
				}
				System.out.println("************ 系统信息: [" + dateNowStr + "] "
						+ ausername + "下线喽！Bye! ************\n");
				try {
					if (dis != null)
						dis.close();
					if (dos != null)
						dos.close();
					if (s != null) {
						s.close();
					}
				} catch (IOException e1) {
					usersOnline.remove(ausername);
					clients.remove(this);
					System.out
							.println("A client's exited and errors happen while release sources related!");
					e1.printStackTrace();
				}
			} catch (Exception e2) {
				t.cancel();
				usersOnline.remove(ausername);
				clients.remove(this);
				java.util.Date dateNow = new java.util.Date();
				SimpleDateFormat dateFormat = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
				String dateNowStr = dateFormat.format(dateNow);
				addOff("************ 系统信息 [" + dateNowStr + "]: " + ausername
						+ " 下线喽！Bye! ************\n\n");
				String iamoff = "************ 系统信息 [" + dateNowStr + "]: "
						+ ausername + " 下线喽！Bye! ************\n\n";
				try {
					DBSQLManager dbsm = new DBSQLManager();
					String sql = "INSERT INTO messages(username ,thetime ,message ) VALUES('sysInfomation','"
							+ dateNowStr.trim() + "','" + iamoff + "')";
					dbsm.setSqlStr(sql);
					dbsm.executeUpdate();
					dbsm.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				try {
					DBSQLManager dbsm = new DBSQLManager();
					java.util.Date dateNoweea = new java.util.Date();
					SimpleDateFormat dateFormateea = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					String thetimeeea = dateFormateea.format(dateNoweea);
					String sql = "INSERT INTO log_Info(username ,thetime ,status ) VALUES('"
							+ ausername.trim()
							+ "','"
							+ thetimeeea.trim()
							+ "','" + "offline')";
					dbsm.setSqlStr(sql);
					dbsm.executeUpdate();
					dbsm.close();
				} catch (SQLException eee) {
					eee.printStackTrace();
				}
				e2.printStackTrace();
				System.out.println("************ 系统信息: [" + dateNowStr + "] "
						+ ausername + "下线喽！Bye! ************\n");
			}
		}
	}
}