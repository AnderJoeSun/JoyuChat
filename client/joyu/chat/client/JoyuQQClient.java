package joyu.chat.client;

import java.awt.Image;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Properties;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import joyu.chat.client.chatRoomFrame.ChatHistoryFrame;
import joyu.chat.client.chatRoomFrame.ChatOnlineInfoFrame;
import joyu.chat.client.chatRoomFrame.ChatRoomFrame;
import joyu.chat.client.loggingFrame.LoggingFrame;
import joyu.chat.client.loginFrame.AboutJoyuFrame;
import joyu.chat.client.loginFrame.LoginFrame;

public class JoyuQQClient {
	private static final long serialVersionUID = 6335283416711359108L;
	private static String SayuServerIP = "192.168.1.100";
	private static String SayuServerport = "8888";
	private static String WebServerIP = "192.168.1.100";
	private static String WebServerPort = "8080";
	private JoyuClientSocket joyuClientSocket;
	private TextArea sendArea;
	int getOnlineInfoTimes = 0;
	private TextArea contentArea;
	private static String registURL;
	private static String joyuQQURL;
	private static String adminURL;
	static {
		SayuServerIP = "192.168.1.100";
		SayuServerport = "8888";
		WebServerIP = "192.168.1.100";
		WebServerPort = "8080";
		try {
			File f2 = new File(System.getProperty("user.dir") + "\\Config\\");
			if (!f2.exists()) {
				f2.mkdir();
			}
			File f = new File(System.getProperty("user.dir")
					+ "\\Config\\ConnectCofig.ini");
			if (!f.exists()) {
				f.createNewFile();
				FileOutputStream bfos = new FileOutputStream(f);
				bfos.write("SayuServerIP = 192.168.1.100\n".getBytes());
				bfos.write("SayuServerport = 8888\n".getBytes());
				bfos.write("WebServerIP = 192.168.1.100\n".getBytes());
				bfos.write("WebServerPort = 8080".getBytes());
				bfos.flush();
				bfos.close();
			} else {
				Properties cofigsFile = new Properties();
				cofigsFile.load(new FileInputStream(f));
				if (cofigsFile.getProperty("SayuServerIP") == null) {
					SayuServerIP = "192.168.1.100";
				} else {
					SayuServerIP = cofigsFile.getProperty("SayuServerIP");
				}
				if (cofigsFile.getProperty("SayuServerport") == null) {
					SayuServerport = "8888";
				} else {
					SayuServerport = cofigsFile.getProperty("SayuServerport");
				}
				if (cofigsFile.getProperty("WebServerIP") == null) {
					WebServerIP = "192.168.1.100";
				} else {
					WebServerIP = cofigsFile.getProperty("WebServerIP");
				}
				if (cofigsFile.getProperty("WebServerPort") == null) {
					WebServerPort = "8080";
				} else {
					WebServerPort = cofigsFile.getProperty("WebServerPort");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		registURL = "http://" + WebServerIP + ":" + WebServerPort
				+ "/JoyuQQWebService/register.jsp";
		joyuQQURL = "http://" + WebServerIP + ":" + WebServerPort
				+ "/JoyuQQWebService/index.jsp";
		adminURL = "http://" + WebServerIP + ":" + WebServerPort
				+ "/JoyuQQWebService/admin/adminLogin.jsp";
	}
	private AboutJoyuFrame aboutJoyuFrame = new AboutJoyuFrame(joyuQQURL);
	private ImageIcon logoIcon = new ImageIcon(System.getProperty("user.dir")
			+ "\\resources\\logo.JPG");
	private Image logoImage = logoIcon.getImage();
	private final String systemInfo = "S~a!y@u#Q$Q%:S";
	private RandomAccessFile raf;
	private final byte[] KEYVALUE = "abcandsayu".getBytes();
	private boolean cConnected = false;
	private String username = null;
	private String password = null;
	private LoginFrame loginFrame;
	private JFrame loggingFrame = null;
	private ChatRoomFrame chatRoomFrame = null;
	private ChatOnlineInfoFrame chatOnlineInfoFrame;
	private ChatHistoryFrame chatHistoryFrame;
	private Thread tRecv = new Thread(new RecvThread());
	private HashSet<String> usersOnlineSet = new HashSet<String>();

	private void launchLoginFrame() {
		loginFrame = new LoginFrame(logoImage, registURL, adminURL,
				aboutJoyuFrame);
		JButton buttonLogin = loginFrame.getButtonLogin();
		loginFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				System.exit(0);
			}
		});
		loginFrame.getTextFieldPassword().addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						username = loginFrame.getUsernameInput();
						password = loginFrame.getPasswordInput();
						if (username.isEmpty()) {
							loginFrame.setAlwaysOnTop(false);
							JOptionPane.showMessageDialog(loginFrame,
									"用户昵称不能为空哦!", "登录出错!",
									JOptionPane.WARNING_MESSAGE);
							loginFrame.setAlwaysOnTop(true);
						} else {
							if (password.isEmpty()) {
								loginFrame.setAlwaysOnTop(false);
								JOptionPane.showMessageDialog(loginFrame,
										"用户密码不能为空哦!", "登录出错!",
										JOptionPane.WARNING_MESSAGE);
								loginFrame.setAlwaysOnTop(true);
							} else {
								loginFrame.dispose();
								aboutJoyuFrame.dispose();
								launchLoginningFrame();
								class Connecting implements Runnable {
									public void run() {
										connect();
									}
								}
								new Thread(new Connecting()).start();
							}
						}
					}
				});
		buttonLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				username = loginFrame.getUsernameInput();
				password = loginFrame.getPasswordInput();
				if (username.isEmpty()) {
					loginFrame.setAlwaysOnTop(false);
					JOptionPane.showMessageDialog(loginFrame, "用户昵称不能为空哦!",
							"登录出错!", JOptionPane.WARNING_MESSAGE);
					loginFrame.setAlwaysOnTop(true);
				} else {
					if (password.isEmpty()) {
						loginFrame.setAlwaysOnTop(false);
						JOptionPane.showMessageDialog(loginFrame, "用户密码不能为空哦!",
								"登录出错!", JOptionPane.WARNING_MESSAGE);
						loginFrame.setAlwaysOnTop(true);
					} else {
						loginFrame.dispose();
						aboutJoyuFrame.dispose();
						launchLoginningFrame();
						class Connecting implements Runnable {
							public void run() {
								connect();
							}
						}
						new Thread(new Connecting()).start();
					}
				}
			}
		});
		loginFrame.setVisible(true);
	}

	private void launchLoginningFrame() {
		loggingFrame = new LoggingFrame(logoImage);
		loggingFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				System.exit(0);
			}
		});
		loggingFrame.setVisible(true);
	}

	private void launchChatRoomFrame() {
		chatOnlineInfoFrame = new ChatOnlineInfoFrame(usersOnlineSet,
				systemInfo, joyuClientSocket, logoImage);
		chatHistoryFrame = new ChatHistoryFrame(KEYVALUE, logoImage, username,
				systemInfo, joyuClientSocket);
		chatRoomFrame = new ChatRoomFrame(username, logoImage, registURL,
				adminURL, joyuClientSocket, aboutJoyuFrame, chatHistoryFrame,
				chatOnlineInfoFrame, joyuQQURL, systemInfo, SayuServerIP,
				SayuServerport);
		contentArea = chatRoomFrame.getContentArea();
		sendArea = chatRoomFrame.getSendArea();
		chatRoomFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (raf != null) {
					try {
						raf.close();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
				joyuClientSocket.shutDownConnection();
				System.exit(0);
			}
		});
		try {
			File file = new File(System.getProperty("user.dir") + "\\users\\"
					+ username + "\\history\\" + username + "_history.txt");
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			raf = new RandomAccessFile(file, "rw");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			raf.seek(raf.length());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		chatRoomFrame.setVisible(false);
		sendArea.requestFocus();
		tRecv.start();
	}

	private String validateInput(String username, String passwo) {
		if (cConnected) {
			String aname = username.trim();
			String validinfo = aname + " " + passwo;
			String result = "";
			try {
				joyuClientSocket.sendString(validinfo);
				result = joyuClientSocket.readString();
			} catch (SocketException e) {
				e.printStackTrace();
				System.out.println(username + "退出了，bye! Client Socket closed");
			} catch (EOFException e1) {
				e1.printStackTrace();
				System.out.println(username + "退出了，bye - bye! EOFException");
			} catch (IOException e2) {
				e2.printStackTrace();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			if (result.equalsIgnoreCase("istrue")) {
				return "istrue";
			} else if (result.equalsIgnoreCase("doubled")) {
				return "doubled";
			} else {
				return "isfalse";
			}
		} else {
			return "wrong";
		}
	}

	private void connect() {
		try {
			joyuClientSocket = new JoyuClientSocket(SayuServerIP,
					Integer.parseInt(SayuServerport));
			cConnected = true;
			String validateResult = validateInput(username, password);
			if (validateResult.equalsIgnoreCase("istrue")) {
				loginFrame.dispose();
				loginFrame.setMyDockStoppable(true);
				launchChatRoomFrame();
				loggingFrame.dispose();
				// chatRoomFrame.setVisible(true);
			} else if (validateResult.equalsIgnoreCase("isfalse")) {
				loggingFrame.dispose();
				loginFrame.setAlwaysOnTop(false);
				JOptionPane.showMessageDialog(loginFrame, "用户名与密码不正确!请重新登录",
						"登录出错!", JOptionPane.WARNING_MESSAGE);
				loginFrame.setAlwaysOnTop(true);
				loginFrame.setVisible(true);
			} else if (validateResult.equalsIgnoreCase("doubled")) {
				loggingFrame.dispose();
				loginFrame.setAlwaysOnTop(false);
				JOptionPane.showMessageDialog(loginFrame, "您已经成功登录，不可重复登录哦！",
						"登录出错!", JOptionPane.WARNING_MESSAGE);
				loginFrame.setAlwaysOnTop(true);
				loginFrame.setVisible(true);
			} else if (validateResult.equalsIgnoreCase("wrong")) {
				loggingFrame.dispose();
				loginFrame.setAlwaysOnTop(false);
				JOptionPane.showMessageDialog(loginFrame, "程序出现错误!请重新登录",
						"登录出错!", JOptionPane.WARNING_MESSAGE);
				loginFrame.setAlwaysOnTop(true);
				loginFrame.setVisible(true);
			}
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null, "服务器未开启，拒绝连接。请联系鱼鱼集团科技工作室...",
					"登录出错!", JOptionPane.WARNING_MESSAGE);
		} catch (java.net.ConnectException e3) {
			loggingFrame.dispose();
			JOptionPane.showMessageDialog(null, "服务器未开启，拒绝连接。请联系鱼鱼集团科技工作室...",
					"登录出错!", JOptionPane.WARNING_MESSAGE);
			e3.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		} catch (Exception e3) {
			e3.printStackTrace();
		}
	}

	private class RecvThread implements Runnable {
		public void run() {
			try {
				while (cConnected) {
					String str = joyuClientSocket.readString();
					if (str.endsWith(systemInfo + "getAllUsers")) {
						while (!str
								.equalsIgnoreCase(systemInfo + "getAllUsers")) {
							UserClientSide aUser = new UserClientSide();
							String aUserName = str.substring(0,
									str.indexOf(systemInfo));
							aUser.setUsername(aUserName);
							str = str.substring(str.indexOf(systemInfo)
									+ systemInfo.length());
							String aUserRealname = str.substring(0,
									str.indexOf(systemInfo));
							aUser.setRealname(aUserRealname);
							str = str.substring(str.indexOf(systemInfo)
									+ systemInfo.length());
							String aUserSex = str.substring(0,
									str.indexOf(systemInfo));
							aUser.setSex(aUserSex);
							str = str.substring(str.indexOf(systemInfo)
									+ systemInfo.length());
							String aUserEmail = str.substring(0,
									str.indexOf(systemInfo));
							aUser.setEmail(aUserEmail);
							str = str.substring(str.indexOf(systemInfo)
									+ systemInfo.length());
							String aUserPhone = str.substring(0,
									str.indexOf(systemInfo));
							aUser.setPhone(aUserPhone);
							str = str.substring(str.indexOf(systemInfo)
									+ systemInfo.length());
							String aUserAddress = str.substring(0,
									str.indexOf(systemInfo));
							aUser.setAddress(aUserAddress);
							str = str.substring(str.indexOf(systemInfo)
									+ systemInfo.length());
							chatOnlineInfoFrame.addToAllUsersSet(aUser);
						}
					} else if (str.endsWith(systemInfo + "getAllFilesForShare")) {
						if (!str.equalsIgnoreCase("end" + systemInfo
								+ "getAllFilesForShare")) {
							FileBeanClientSide file = new FileBeanClientSide();
							String fileName = str.substring(0,
									str.indexOf(systemInfo));
							file.setFileName(fileName);
							str = str.substring(str.indexOf(systemInfo)
									+ systemInfo.length());
							String fileKind = str.substring(0,
									str.indexOf(systemInfo));
							file.setFileKind(fileKind);
							str = str.substring(str.indexOf(systemInfo)
									+ systemInfo.length());
							String fileSize = str.substring(0,
									str.indexOf(systemInfo));
							file.setFileSize(fileSize);
							str = str.substring(str.indexOf(systemInfo)
									+ systemInfo.length());
							String fileFrom = str.substring(0,
									str.indexOf(systemInfo));
							file.setFileFrom(fileFrom);
							str = str.substring(str.indexOf(systemInfo)
									+ systemInfo.length());
							String fileCreatedTime = str.substring(0,
									str.indexOf(systemInfo));
							file.setFileCreatedTime(fileCreatedTime);
							str = str.substring(str.indexOf(systemInfo)
									+ systemInfo.length());
							String fileModifiedTime = str.substring(0,
									str.indexOf(systemInfo));
							file.setFileModifiedTime(fileModifiedTime);
							str = str.substring(str.indexOf(systemInfo)
									+ systemInfo.length());
							chatRoomFrame.addToFilesForShareSet(file);
						} else if (str.equalsIgnoreCase("end" + systemInfo
								+ "getAllFilesForShare")) {
							chatRoomFrame.freshShareInfo();
						}
					} else if (str.endsWith(systemInfo + "getUsersOnline")) {
						while (!str.equalsIgnoreCase(systemInfo
								+ "getUsersOnline")) {
							String aUserName = str.substring(0,
									str.indexOf(", "));
							usersOnlineSet.add(aUserName);
							str = str.substring(str.indexOf(", ") + 2);
						}
						getOnlineInfoTimes++;
						if (getOnlineInfoTimes > 1) {
							chatOnlineInfoFrame.freshOnlineInfo();
						}
					} else if (str.endsWith(systemInfo + "getHistoryByTime")) {
						if (!str.equalsIgnoreCase(systemInfo + "end"
								+ systemInfo + "getHistoryByTime")) {
							chatHistoryFrame.getHistoryFromServerSideBS()
									.append(str);
						}
						if (str.equalsIgnoreCase(systemInfo + "end"
								+ systemInfo + "getHistoryByTime")) {
							if (chatHistoryFrame.getHistoryFromServerSideBS()
									.toString()
									.indexOf(systemInfo + "getHistoryByTime") != -1) {
								chatHistoryFrame
										.getHistoryFromServerArea()
										.setText(
												chatHistoryFrame
														.getHistoryFromServerSideBS()
														.toString()
														.replace(
																(systemInfo + "getHistoryByTime"),
																""));
								chatHistoryFrame.validate();
							} else {
								chatHistoryFrame.getHistoryFromServerArea()
										.setText("没有找到相关结果哦...");
								chatHistoryFrame.validate();
							}
						}
					} else if (str.endsWith(systemInfo + "deleteSuccess")) {
						JOptionPane.showMessageDialog(null, "以下文件：\n     "
								+ str.substring(0, str.lastIndexOf(systemInfo))
								+ "，\n删除成功喽！", "删除成功",
								JOptionPane.WARNING_MESSAGE);
						chatRoomFrame.clearFilesForShareSet();
						joyuClientSocket.sendString(systemInfo
								+ "getAllFilesForShare");
					} else if (str.endsWith("updateDBFailed" + systemInfo
							+ "deleteFailed")) {
						JOptionPane.showMessageDialog(
								null,
								"对不起，文件：["
										+ str.substring(0,
												str.indexOf("updateDBFailed"))
										+ "] 删除失败了，服务器数据库发生故障，请和管理员联系吧！",
								"删除失败", JOptionPane.WARNING_MESSAGE);
					} else if (str.endsWith("fileDeleteFailed" + systemInfo
							+ "deleteFailed")) {
						JOptionPane
								.showMessageDialog(
										null,
										"对不起，文件：["
												+ str.substring(
														0,
														str.indexOf("fileDeleteFailed"))
												+ "] 删除失败了，可能文件正在被使用中，请换个时间再试一次吧！",
										"删除失败", JOptionPane.WARNING_MESSAGE);
					} else if (str.equalsIgnoreCase(systemInfo + "cChatable")) {
						if (!chatRoomFrame.isVisible()) {
							chatRoomFrame.setVisible(true);
						}
					} else {
						contentArea.setEditable(true);
						contentArea.append(str);
						contentArea.select(contentArea.getText().length(),
								contentArea.getText().length());
						try {
							int pos, keylen;
							pos = 0;
							keylen = KEYVALUE.length;
							byte[] b = (str + systemInfo).getBytes();
							for (int i = 0; i < b.length; i++) {
								b[i] ^= KEYVALUE[pos];
								pos++;
								if (pos == keylen) {
									pos = 0;
								}
							}
							raf.writeUTF(new String(b));
							contentArea.setEditable(false);
						} catch (Exception e) {
							if (raf != null) {
								try {
									raf.close();
								} catch (IOException ex) {
									ex.printStackTrace();
								}
							}
							e.printStackTrace();
						}
					}
				}
			} catch (Exception e4) {
				if (raf != null) {
					try {
						raf.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				joyuClientSocket.shutDownConnection();
				e4.printStackTrace();
				JOptionPane.showMessageDialog(null, "服务器已关闭，失去连接！", "断开连接",
						JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	public static void main(String[] args) {
		new JoyuQQClient().launchLoginFrame();
	}
}