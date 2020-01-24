package joyu.chat.client.chatRoomFrame;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import joyu.chat.client.FileBeanClientSide;
import joyu.chat.client.JoyuClientSocket;
import joyu.chat.client.RButton;
import joyu.chat.client.loginFrame.AboutJoyuFrame;

public class ChatRoomRightSideBar extends JPanel {
	private static final long serialVersionUID = 1L;
	private ChatHistoryFrame chatHistoryFrame;
	private String registURL;
	private String systemInfo;
	private ChatOnlineInfoFrame chatOnlineInfoFrame;
	private AboutJoyuFrame aboutJoyuFrame;
	private String username;
	private JoyuClientSocket joyuClientSocket;
	private String JoyuQQURL;
	private String adminURL;
	private ChatRoomFrame chatRoomFrame;
	private String SayuServerIP;
	private String SayuServerport;

	public ChatRoomRightSideBar(String adminURL, String registURL,
			AboutJoyuFrame aboutJoyuFrame, ChatHistoryFrame chatHistoryFrame,
			ChatOnlineInfoFrame chatOnlineInfoFrame,
			ChatRoomFrame chatRoomFrame, String JoyuQQURL, String username,
			JoyuClientSocket joyuClientSocket, String systemInfo,
			String SayuServerIP, String SayuServerport) {
		this.adminURL = adminURL;
		this.chatHistoryFrame = chatHistoryFrame;
		this.registURL = registURL;
		this.JoyuQQURL = JoyuQQURL;
		this.username = username;
		this.chatRoomFrame = chatRoomFrame;
		this.aboutJoyuFrame = aboutJoyuFrame;
		this.chatOnlineInfoFrame = chatOnlineInfoFrame;
		this.joyuClientSocket = joyuClientSocket;
		this.systemInfo = systemInfo;
		this.SayuServerIP = SayuServerIP;
		this.SayuServerport = SayuServerport;
		showChatRoomRightSideBar();
	}

	private void showChatRoomRightSideBar() {
		setBackground(new Color(180, 230, 147));
		JButton buttonAboutJoyu = new RButton(0);
		buttonAboutJoyu.setText("关于作者");
		buttonAboutJoyu.setFont(new Font("system", Font.PLAIN, 12));
		buttonAboutJoyu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				aboutJoyuFrame.setBounds(
						(chatRoomFrame.getBounds().x + chatRoomFrame
								.getBounds().width),
						chatRoomFrame.getBounds().y, 239, 251);
				aboutJoyuFrame.setVisible(true);
			}
		});
		JButton buttonAdmin = new RButton(0);
		buttonAdmin.setText("我是网管");
		buttonAdmin.setFont(new Font("system", Font.PLAIN, 12));
		buttonAdmin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Process p = Runtime.getRuntime().exec(
							"cmd.exe /c start " + adminURL);
					p.waitFor();
				} catch (Exception ex) {
					System.out.println(ex.getMessage());
				}
			}
		});
		JButton buttonRegister = new RButton(0);
		buttonRegister.setText("新人注册");
		buttonRegister.setFont(new Font("system", Font.PLAIN, 12));
		buttonRegister.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Process p = Runtime.getRuntime().exec(
							"cmd.exe /c start " + registURL);
					p.waitFor();
				} catch (Exception ex) {
					System.out.println(ex.getMessage());
				}
			}
		});
		JButton buttonJoyuWeb = new RButton(0);
		buttonJoyuWeb.setText("东方社区");
		buttonJoyuWeb.setFont(new Font("system", Font.PLAIN, 12));
		buttonJoyuWeb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Process p = Runtime.getRuntime().exec(
							"cmd.exe /c start " + JoyuQQURL);
					p.waitFor();
				} catch (Exception ex) {
					System.out.println(ex.getMessage());
				}
			}
		});
		JButton buttonFile = new RButton(0);
		buttonFile.setText("上传文件");
		buttonFile.setFont(new Font("system", Font.PLAIN, 12));
		buttonFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final JFileChooser fileChooser = new JFileChooser();
				fileChooser.setMultiSelectionEnabled(true);
				final class fileChooserThread implements Runnable {
					public void run() {
						int resultA = fileChooser.showOpenDialog(chatRoomFrame);
						File[] uploadingFiles = null;
						if (resultA == JFileChooser.APPROVE_OPTION) {
							uploadingFiles = fileChooser.getSelectedFiles();
						} else if (resultA == JFileChooser.CANCEL_OPTION) {
							JOptionPane.showMessageDialog(chatRoomFrame,
									"您已取消上传！", "取消上传",
									JOptionPane.WARNING_MESSAGE);
						} else if (resultA == JFileChooser.ERROR_OPTION) {
							JOptionPane.showMessageDialog(chatRoomFrame, "错误!");
						}
						List<String> filesUploadableNamesList = new ArrayList<String>();
						List<String> filesUploadablePathlist = new ArrayList<String>();
						List<String> filesUnUploadableNameslist = new ArrayList<String>();
						if (uploadingFiles != null) {
							if (uploadingFiles.length > 0) {
								for (int i = 0; i < uploadingFiles.length; i++) {
									if (uploadingFiles[i] != null) {
										boolean uploadAble = true;
										Iterator filesForShareIt = chatRoomFrame
												.getFilesForShareSet()
												.iterator();
										while (filesForShareIt.hasNext()) {
											FileBeanClientSide afilebean = (FileBeanClientSide) (filesForShareIt
													.next());
											if ((uploadingFiles[i].getName()
													.replace(" ", "_"))
													.equalsIgnoreCase(afilebean
															.getFileName())
													&& (!(afilebean
															.getFileFrom()
															.equalsIgnoreCase(username)))) {
												uploadAble = false;
												break;
											}
										}
										if (uploadAble) {
											if (filesUploadableNamesList.size() < 1) {
												filesUploadableNamesList
														.add(uploadingFiles[i]
																.getName());
											} else {
												filesUploadableNamesList.add("\n     "
														+ uploadingFiles[i]
																.getName());
											}
											filesUploadablePathlist
													.add(uploadingFiles[i]
															.getAbsolutePath());
										} else {
											if (filesUnUploadableNameslist
													.size() < 1) {
												filesUnUploadableNameslist
														.add(uploadingFiles[i]
																.getName());
											} else {
												filesUnUploadableNameslist.add("\n     "
														+ uploadingFiles[i]
																.getName());
											}
										}
									}
								}
							}
							if (!filesUnUploadableNameslist.isEmpty()) {
								JOptionPane
										.showMessageDialog(
												chatRoomFrame,
												"以下上传文件:\n     "
														+ filesUnUploadableNameslist
														+ "\n与共享中的文件重名，并且该共享文件不是由您上传，\n所以您不能覆盖哦！请尝试更改文件名后再行上传吧！",
												"操作有误",
												JOptionPane.WARNING_MESSAGE);
							}
							if (!filesUploadablePathlist.isEmpty()) {
								int result = JOptionPane.showConfirmDialog(
										chatRoomFrame, "您将上传以下文件：\n     "
												+ filesUploadableNamesList
												+ "，\n是否继续？");
								if (result == JOptionPane.YES_OPTION) {
									Iterator iterator2 = filesUploadablePathlist
											.iterator();
									while (iterator2.hasNext()) {
										File f = new File((String) iterator2
												.next());
										if (f.length() == 0) {
											JOptionPane
													.showMessageDialog(
															chatRoomFrame,
															"您上传的文件为空，请选择其它文件吧！",
															"内容有误",
															JOptionPane.WARNING_MESSAGE);
										} else if (f.length() > 1000000000) {
											JOptionPane
													.showMessageDialog(
															chatRoomFrame,
															"您上传的文件大于1GB，超过上限，请尝试压缩文件后再试吧！",
															"文件太大",
															JOptionPane.WARNING_MESSAGE);
										} else {
											UploadFileThread uploadFileThread = new UploadFileThread(
													username, chatRoomFrame,
													joyuClientSocket, f,
													systemInfo, SayuServerIP,
													SayuServerport);
											uploadFileThread.run();
										}
									}
								}
							}
						}
					}
				}
				new Thread(new fileChooserThread()).start();
			}
		});
		JButton buttonLogAnother = new RButton(0);
		buttonLogAnother.setText("再登一个");
		buttonLogAnother.setFont(new Font("system", Font.PLAIN, 12));
		buttonLogAnother.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Runtime r = Runtime.getRuntime();
				try {
					r.exec(System.getProperty("user.dir")
							+ "\\JoyuQQClient.bat");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		JButton buttonShowHistoryFrame = new RButton(0);
		buttonShowHistoryFrame.setText("聊天记录");
		buttonShowHistoryFrame.setFont(new Font("system", Font.PLAIN, 12));
		buttonShowHistoryFrame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chatHistoryFrame.setVisible(true);
			}
		});
		JButton buttonShowOnline = new RButton(0);
		buttonShowOnline.setText("在线用户");
		buttonShowOnline.setFont(new Font("system", Font.PLAIN, 13));
		buttonShowOnline.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chatOnlineInfoFrame.setBounds(
						(chatRoomFrame.getBounds().x + chatRoomFrame
								.getBounds().width),
						chatRoomFrame.getBounds().y, 500, chatRoomFrame
								.getBounds().height);
				chatOnlineInfoFrame.freshOnlineInfo();
			}
		});
		setLayout(new GridLayout(8, 1));
		add(buttonAboutJoyu);
		add(buttonAdmin);
		add(buttonJoyuWeb);
		add(buttonRegister);
		add(buttonLogAnother);
		add(buttonShowHistoryFrame);
		add(buttonShowOnline);
		add(buttonFile);
	}
}