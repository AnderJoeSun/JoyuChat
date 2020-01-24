package joyu.chat.client.chatRoomFrame;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JToolBar;
import joyu.chat.client.FileBeanClientSide;
import joyu.chat.client.JoyuClientSocket;
import joyu.chat.client.JoyuTableModel;
import joyu.chat.client.JoyuTableSortShareFiles;
import joyu.chat.client.RButton;
import joyu.chat.client.RButtonPanelPain;
import joyu.chat.client.loginFrame.AboutJoyuFrame;

public class ChatRoomFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private Thread myDock = new Thread(new MyDock());
	private TextArea sendArea;
	private CardLayout cl;
	private JTable table;
	private JButton buttonShare;
	private boolean byFrameOnlineInfoActivated;
	private boolean byFrameAboutJoyuActivated;
	private boolean byFrameAboutJoyuJoined;
	private boolean byFrameSearchFilesActivated;
	private boolean byFrameSearchFilesJoined;
	private JPanel uploadAndDownloadPanel;
	private boolean byFrameOnlineInfoJoined;
	private JButton buttonShareRefresh;
	private TextArea contentArea;
	private JFrame searchFilesForShareFrame;
	private SearchFilesFrame joyuSearchFilesFrame;
	private JPanel uploadingInfoPanel;
	private JPanel uploadingInfoPanel2;
	private boolean mainFrameActivated = false;
	private String systemInfo;
	private JPanel chatCardPanel;
	private JPanel sharePanel;
	private CheckboxGroup sendSetCBG = new CheckboxGroup();
	private Checkbox ctrlAndEnterSet = new Checkbox("Ctrl+Enter", sendSetCBG,
			true);
	private Checkbox enterSet = new Checkbox("Enter", sendSetCBG, false);
	JPanel uploadingFilesPanel2;
	private boolean enterSetAdded;
	private boolean ctrlAndEnterSetAdded;
	private Object[][] data;
	private String[] columnNames = new String[] { "名称", "类型", "大小", "来自", "创于",
			"改于", "下载" };
	private FileBeanClientSide file;
	private JoyuTableModel tableModelShare;
	private Iterator it;
	private JoyuTableSortShareFiles joyuTableShare;
	private ChatRoomRightSideBar chatRoomRightSideBar;
	private String username;
	private JoyuClientSocket joyuClientSocket;
	private Image logoImage;
	JPanel uploadingFilesPanel;
	private AboutJoyuFrame aboutJoyuFrame;
	private ArrayList<File> filesUploadingList = new ArrayList<File>();
	private ChatOnlineInfoFrame chatOnlineInfoFrame;
	private HashSet<FileBeanClientSide> filesForShareSet = new HashSet<FileBeanClientSide>();
	private String aObj = "aaa";
	private String SayuServerIP;
	private String SayuServerport;

	public void clearFilesForShareSet() {
		synchronized (aObj) {
			filesForShareSet.clear();
		}
	}

	public void addToFilesForShareSet(FileBeanClientSide f) {
		synchronized (aObj) {
			filesForShareSet.add(f);
		}
	}

	public HashSet<FileBeanClientSide> getFilesForShareSet() {
		synchronized (aObj) {
			return filesForShareSet;
		}
	}

	public ChatRoomFrame(String username, Image logoImage, String registURL,
			String adminURL, JoyuClientSocket joyuClientSocket,
			AboutJoyuFrame aboutJoyuFrame, ChatHistoryFrame chatHistoryFrame,
			ChatOnlineInfoFrame chatOnlineInfoFrame, String JoyuQQURL,
			String systemInfo, String SayuServerIP, String SayuServerport) {
		this.joyuClientSocket = joyuClientSocket;
		this.sendArea = new UploadTextArea(null, 3, 6,
				TextArea.SCROLLBARS_VERTICAL_ONLY, username, this,
				joyuClientSocket, systemInfo, SayuServerIP, SayuServerport);
		this.contentArea = new UploadTextArea(null, 3, 6,
				TextArea.SCROLLBARS_BOTH, username, this, joyuClientSocket,
				systemInfo, SayuServerIP, SayuServerport);
		this.username = username;
		this.logoImage = logoImage;
		this.systemInfo = systemInfo;
		this.aboutJoyuFrame = aboutJoyuFrame;
		this.chatOnlineInfoFrame = chatOnlineInfoFrame;
		this.SayuServerIP = SayuServerIP;
		this.SayuServerport = SayuServerport;
		try {
			File f1 = new File(System.getProperty("user.dir") + "\\users\\");
			if (!f1.exists()) {
				f1.mkdir();
			}
			File f2 = new File(System.getProperty("user.dir") + "\\users\\"
					+ username);
			if (!f2.exists()) {
				f2.mkdir();
			}
			File f3 = new File(System.getProperty("user.dir") + "\\users\\"
					+ username + "\\cofig\\");
			if (!f3.exists()) {
				f3.mkdir();
			}
			File f = new File(System.getProperty("user.dir") + "\\users\\"
					+ username + "\\cofig\\" + username + "_cofig.ini");
			if (!f.exists()) {
				f.createNewFile();
				FileOutputStream bfos = new FileOutputStream(f);
				bfos.write("sendSet = ctrlAndEnterSetAdded".getBytes());
				bfos.flush();
				bfos.close();
			} else {
				Properties cofigsFile = new Properties();
				cofigsFile.load(new FileInputStream(f));
				if (cofigsFile.getProperty("sendSet") == null) {
					this.enterSetAdded = false;
					this.ctrlAndEnterSetAdded = true;
					sendSetCBG.setSelectedCheckbox(ctrlAndEnterSet);
				} else {
					if (cofigsFile.getProperty("sendSet").equalsIgnoreCase(
							"enterSetAdded")) {
						this.enterSetAdded = true;
						this.ctrlAndEnterSetAdded = false;
						sendSetCBG.setSelectedCheckbox(enterSet);
					} else if (cofigsFile.getProperty("sendSet")
							.equalsIgnoreCase("ctrlAndEnterSetAdded")) {
						this.enterSetAdded = false;
						this.ctrlAndEnterSetAdded = true;
						sendSetCBG.setSelectedCheckbox(ctrlAndEnterSet);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		chatRoomRightSideBar = new ChatRoomRightSideBar(adminURL, registURL,
				aboutJoyuFrame, chatHistoryFrame, chatOnlineInfoFrame, this,
				JoyuQQURL, username, joyuClientSocket, systemInfo,
				SayuServerIP, SayuServerport);
		showChatRoomFrame();
	}

	public void showChatRoomFrame() {
		setTitle(" " + username + "我重现江湖...");
		setIconImage(logoImage);
		contentArea.setEditable(false);
		contentArea.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
			}

			public void keyTyped(KeyEvent e) {
				sendArea.requestFocus();
				sendArea.setText((sendArea.getText())
						+ String.valueOf(e.getKeyChar()));
				sendArea.setCaretPosition(sendArea.getText().length());
			}
		});
		setBounds(260, 130, 520, 540);
		getContentPane().setBackground(Color.GREEN);
		setLayout(new BorderLayout());
		JPanel panelContent = new JPanel();
		panelContent.setBackground(new Color(180, 230, 147));
		panelContent.setLayout(new BorderLayout());
		contentArea.setBackground(new Color(204, 232, 207));
		JPanel contentAreaAndUploadingPanel = new JPanel();
		contentAreaAndUploadingPanel.setLayout(new BorderLayout());
		contentAreaAndUploadingPanel.setBackground(new Color(180, 230, 147));
		contentAreaAndUploadingPanel.add(contentArea);
		uploadingFilesPanel = new JPanel();
		uploadingFilesPanel.setBackground(new Color(180, 230, 147));
		uploadingFilesPanel.setLayout(new BorderLayout());
		JPanel uploadingDonghuaAndLabelPanel = new JPanel();
		uploadingDonghuaAndLabelPanel
				.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel uploadingDonghuaLabel = new JLabel(new ImageIcon(
				System.getProperty("user.dir") + "\\resources\\sayu.gif"));
		JLabel uploadingStrLabel = new JLabel();
		uploadingStrLabel.setFont(new Font("system", Font.PLAIN, 12));
		uploadingStrLabel.setText("上传:");
		uploadingDonghuaAndLabelPanel.add(uploadingDonghuaLabel);
		uploadingDonghuaAndLabelPanel.add(uploadingStrLabel);
		uploadingDonghuaAndLabelPanel.setBackground(new Color(204, 232, 207));
		uploadingInfoPanel = new JPanel();
		uploadingInfoPanel.setLayout(new GridLayout());
		uploadingInfoPanel.setVisible(false);
		uploadingInfoPanel.setBackground(new Color(204, 232, 207));
		uploadingFilesPanel.add(uploadingDonghuaAndLabelPanel,
				BorderLayout.WEST);
		uploadingFilesPanel.add(uploadingInfoPanel, BorderLayout.CENTER);
		uploadingFilesPanel.setVisible(false);
		contentAreaAndUploadingPanel.add(uploadingFilesPanel,
				BorderLayout.SOUTH);
		panelContent.add(contentAreaAndUploadingPanel);
		panelContent.add(chatRoomRightSideBar, BorderLayout.EAST);
		JPanel panelSend = new JPanel();
		panelSend.setBackground(new Color(180, 230, 147));
		panelSend.setLayout(new BorderLayout());
		ImageIcon sendImage = new ImageIcon(System.getProperty("user.dir")
				+ "\\resources\\SendImage5.jpg");
		JLabel sendImageLabel = new JLabel(sendImage);
		panelSend.add(sendImageLabel, BorderLayout.WEST);
		sendArea.setBackground(new Color(204, 232, 207));
		panelSend.add(sendArea, BorderLayout.CENTER);
		JPanel panelSendbuttons = new JPanel();
		panelSendbuttons.setLayout(new BorderLayout());
		final JButton buttonSendSet = new RButton(0);
		buttonSendSet.setFont(new Font("system", Font.PLAIN, 4));
		buttonSendSet.setText("***  ***  ***  ***");
		buttonSendSet.setSize(80, 20);
		buttonSendSet.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final JFrame sendSetFrame = new JFrame();
				sendSetFrame.setUndecorated(true);
				JButton sendSetButton = new RButtonPanelPain(2);
				sendSetButton.setLayout(new BorderLayout());
				enterSet.setBackground(new Color(180, 230, 147));
				enterSet.setForeground(new Color(51, 184, 47));
				ctrlAndEnterSet.setBackground(new Color(180, 230, 147));
				ctrlAndEnterSet.setForeground(new Color(51, 184, 47));
				enterSet.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent evt) {
						if (evt.getStateChange() == ItemEvent.SELECTED) {
							enterSetAdded = true;
							ctrlAndEnterSetAdded = false;
							try {
								File f = new File(System
										.getProperty("user.dir")
										+ "\\users\\"
										+ username
										+ "\\cofig\\"
										+ username
										+ "_cofig.ini");
								if (f.exists()) {
									FileOutputStream bfos = new FileOutputStream(
											f);
									bfos.write("sendSet = enterSetAdded"
											.getBytes());
									bfos.flush();
									bfos.close();
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				});
				ctrlAndEnterSet.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent evt) {
						if (evt.getStateChange() == ItemEvent.SELECTED) {
							ctrlAndEnterSetAdded = true;
							enterSetAdded = false;
							try {
								File f = new File(System
										.getProperty("user.dir")
										+ "\\users\\"
										+ username
										+ "\\cofig\\"
										+ username
										+ "_cofig.ini");
								if (f.exists()) {
									FileOutputStream bfos = new FileOutputStream(
											f);
									bfos.write("sendSet = ctrlAndEnterSetAdded"
											.getBytes());
									bfos.flush();
									bfos.close();
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				});
				sendSetButton.add(enterSet, BorderLayout.NORTH);
				sendSetButton.add(ctrlAndEnterSet, BorderLayout.SOUTH);
				sendSetButton.setBackground(new Color(205, 255, 205));
				sendSetButton.setForeground(new Color(205, 255, 205));
				// sendSetButton.setOpaque(false);
				sendSetFrame.setContentPane(sendSetButton);
				sendSetFrame.setLocation((getBounds().x + getBounds().width),
						(getBounds().height + getBounds().y - 57));
				sendSetFrame.setAlwaysOnTop(true);
				sendSetFrame.addWindowListener(new WindowAdapter() {
					public void windowDeactivated(WindowEvent e) {
						sendSetFrame.dispose();
					}
				});
				sendSetFrame.pack();
				sendSetFrame.setVisible(true);
			}
		});
		JButton buttonSend = new RButton(0);
		buttonSend.setText("发送信息");
		buttonSend.setFont(new Font("system", Font.PLAIN, 13));
		buttonSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String str = sendArea.getText().trim();
				if (str.isEmpty()) {
					JOptionPane.showMessageDialog(ChatRoomFrame.this,
							"发送内容不能为空,或不能全为空格哦!", "内容格式不正确",
							JOptionPane.WARNING_MESSAGE);
				} else {
					java.util.Date dateNow = new java.util.Date();
					SimpleDateFormat dateFormat = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					String dateNowStr = dateFormat.format(dateNow);
					String msn = username + " [" + dateNowStr + "]: \n  " + str
							+ "\n\n";
					sendArea.setText("");
					try {
						joyuClientSocket.sendString(msn);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		sendArea.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent ke1) {
				if (enterSetAdded) {
					if (ke1.getKeyCode() == KeyEvent.VK_ENTER) {
						String str = sendArea.getText().trim();
						if (str.isEmpty()) {
							sendArea.setText("");
							JOptionPane.showMessageDialog(ChatRoomFrame.this,
									"发送内容不能为空,或不能全为空格哦!", "内容格式不正确",
									JOptionPane.WARNING_MESSAGE);
						} else {
							java.util.Date dateNow = new java.util.Date();
							SimpleDateFormat dateFormat = new SimpleDateFormat(
									"yyyy-MM-dd HH:mm:ss");
							String dateNowStr = dateFormat.format(dateNow);
							String msn = username + " [" + dateNowStr
									+ "]: \n  " + str + "\n\n";
							sendArea.setText("");
							try {
								joyuClientSocket.sendString(msn);
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}
					}
				} else if (ctrlAndEnterSetAdded) {
					if (ke1.getModifiers() == KeyEvent.CTRL_MASK
							&& ke1.getKeyCode() == KeyEvent.VK_ENTER) {
						String str = sendArea.getText().trim();
						if (str.isEmpty()) {
							sendArea.setText("");
							JOptionPane.showMessageDialog(ChatRoomFrame.this,
									"发送内容不能为空,或不能全为空格哦!", "内容格式不正确",
									JOptionPane.WARNING_MESSAGE);
						} else {
							java.util.Date dateNow = new java.util.Date();
							SimpleDateFormat dateFormat = new SimpleDateFormat(
									"yyyy-MM-dd HH:mm:ss");
							String dateNowStr = dateFormat.format(dateNow);
							String msn = username + " [" + dateNowStr
									+ "]: \n  " + str + "\n\n";
							sendArea.setText("");
							try {
								joyuClientSocket.sendString(msn);
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}
					}
				}
			}

			public void keyReleased(KeyEvent ke) {
				if (enterSetAdded) {
					if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
						sendArea.setText("");
					}
				} else if (ctrlAndEnterSetAdded) {
					if (ke.getModifiers() == KeyEvent.CTRL_MASK
							&& ke.getKeyCode() == KeyEvent.VK_ENTER) {
						sendArea.setText("");
					}
				}
			}
		});
		panelSendbuttons.add(buttonSendSet, BorderLayout.SOUTH);
		panelSendbuttons.add(buttonSend, BorderLayout.CENTER);
		panelSendbuttons.setBackground(new Color(180, 230, 147));
		panelSend.add(panelSendbuttons, BorderLayout.EAST);
		JPanel chatPane = new JPanel();
		chatPane.setForeground(new Color(180, 230, 147));
		chatPane.setBackground(new Color(180, 230, 147));
		chatPane.setLayout(new BorderLayout());
		chatPane.add(panelContent, BorderLayout.CENTER);
		chatPane.add(panelSend, BorderLayout.SOUTH);
		cl = new CardLayout();
		chatCardPanel = new JPanel();
		chatCardPanel.setBackground(new Color(180, 230, 147));
		chatCardPanel.setLayout(cl);
		JToolBar chatToolBar = new JToolBar();
		final JButton buttonChat = new RButton(1);
		buttonChat.setText("议论纷纷");
		buttonChat.setFont(new Font("system", Font.PLAIN, 13));
		buttonShare = new RButton(2);
		buttonShare.setText("资源共享");
		buttonShare.setFont(new Font("system", Font.PLAIN, 13));
		chatToolBar.add(buttonChat);
		chatToolBar.add(buttonShare);
		chatToolBar.setFloatable(false);
		chatToolBar.setBackground(new Color(180, 230, 147));
		sharePanel = new JPanel();
		sharePanel.setBackground(new Color(180, 230, 147));
		sharePanel.setLayout(new BorderLayout());
		buttonChat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cl.show(chatCardPanel, "chatpane");
			}
		});
		buttonShare.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cl.show(chatCardPanel, "sharepane");
			}
		});
		add(chatToolBar, BorderLayout.NORTH);
		chatCardPanel.add("chatpane", chatPane);
		JPanel freshAndDeletePanel = new JPanel();
		freshAndDeletePanel.setLayout(new GridLayout(1, 3));
		freshAndDeletePanel.setBackground(new Color(180, 230, 147));
		buttonShareRefresh = new RButton(1, "刷   新");
		buttonShareRefresh.setFont(new Font("system", Font.PLAIN, 12));
		buttonShareRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					ChatRoomFrame.this.clearFilesForShareSet();
					joyuClientSocket.sendString(systemInfo
							+ "getAllFilesForShare");
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		JButton buttonDeleteFile = new RButton(0, "删  除");
		buttonDeleteFile.setFont(new Font("system", Font.PLAIN, 12));
		buttonDeleteFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (table != null) {
					int[] selectedRows = table.getSelectedRows();
					if (selectedRows.length == 0) {
						JOptionPane.showMessageDialog(ChatRoomFrame.this,
								"对不起，您还没选中任何文件哦！", "操作错误",
								JOptionPane.WARNING_MESSAGE);
					} else if (selectedRows.length > 0) {
						ArrayList<String> filenames = new ArrayList<String>();
						ArrayList<String> filenamesb = new ArrayList<String>();
						for (int i = 0; i < selectedRows.length; i++) {
							String fileFrom = (String) table.getValueAt(
									selectedRows[i], 3);
							String filename = (String) table.getValueAt(
									selectedRows[i], 0);
							if (fileFrom != null) {
								if (fileFrom.equalsIgnoreCase(username)) {
									if (filenames.size() < 1) {
										filenames.add(filename);
									} else {
										filenames.add("\n     " + filename);
									}
									filenamesb.add(filename);
								} else {
									JOptionPane
											.showMessageDialog(
													ChatRoomFrame.this,
													"对不起，选中的文件："
															+ filename
															+ " 不是您上传的文件，您不能删除哦！",
													"操作错误",
													JOptionPane.WARNING_MESSAGE);
								}
							}
						}
						if (!filenamesb.isEmpty()) {
							int result = JOptionPane.showConfirmDialog(
									ChatRoomFrame.this, " 您将删除以下文件：\n     "
											+ filenames + " ，\n是否仍要继续？");
							if (result == JOptionPane.YES_OPTION) {
								Iterator it = filenamesb.iterator();
								StringBuffer filesToDeleteSB = new StringBuffer();
								while (it.hasNext()) {
									filesToDeleteSB.append(((String) it.next())
											.trim());
									filesToDeleteSB.append(systemInfo);
								}
								if (filesToDeleteSB.length() > 0) {
									try {
										joyuClientSocket
												.sendString(filesToDeleteSB
														.toString()
														+ systemInfo
														+ "deleteFileOfMine");
									} catch (Exception e1) {
										e1.printStackTrace();
									}
								}
							}
						}
					}
				}
			}
		});
		JButton buttonShowSearchFileFrame = new RButton(2, "搜  索");
		buttonShowSearchFileFrame.setFont(new Font("system", Font.PLAIN, 12));
		buttonShowSearchFileFrame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				searchFilesForShareFrame.setBounds(
						((getBounds()).x + (getBounds()).width),
						(getBounds()).y, (getBounds().width),
						(getBounds()).height);
				searchFilesForShareFrame.setVisible(true);
			}
		});
		freshAndDeletePanel.add(buttonShareRefresh);
		freshAndDeletePanel.add(buttonDeleteFile);
		freshAndDeletePanel.add(buttonShowSearchFileFrame);
		uploadAndDownloadPanel = new JPanel();
		final JButton buttonUploadFile = new RButton(1, "上  传");
		buttonUploadFile.setFont(new Font("system", Font.PLAIN, 12));
		buttonUploadFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final JFileChooser fileChooser = new JFileChooser();
				fileChooser.setMultiSelectionEnabled(true);
				final class fileChooserThread implements Runnable {
					public void run() {
						int resultA = fileChooser
								.showOpenDialog(ChatRoomFrame.this);
						File[] uploadingFiles = null;
						if (resultA == JFileChooser.APPROVE_OPTION) {
							uploadingFiles = fileChooser.getSelectedFiles();
						} else if (resultA == JFileChooser.CANCEL_OPTION) {
							JOptionPane.showMessageDialog(ChatRoomFrame.this,
									"您已取消上传！", "取消上传",
									JOptionPane.WARNING_MESSAGE);
						} else if (resultA == JFileChooser.ERROR_OPTION) {
							JOptionPane.showMessageDialog(ChatRoomFrame.this,
									"错误!");
						}
						List<String> filesUploadableNamesList = new ArrayList<String>();
						List<String> filesUploadablePathlist = new ArrayList<String>();
						List<String> filesUnUploadableNameslist = new ArrayList<String>();
						if (uploadingFiles != null) {
							if (uploadingFiles.length > 0) {
								for (int i = 0; i < uploadingFiles.length; i++) {
									if (uploadingFiles[i] != null) {
										boolean uploadAble = true;
										Iterator filesForShareIt = (ChatRoomFrame.this
												.getFilesForShareSet())
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
												ChatRoomFrame.this,
												"以下上传文件:\n     "
														+ filesUnUploadableNameslist
														+ "\n与共享中的文件重名，并且该共享文件不是由您上传，\n所以您不能覆盖哦！请尝试更改文件名后再行上传吧！",
												"操作有误",
												JOptionPane.WARNING_MESSAGE);
							}
							if (!filesUploadablePathlist.isEmpty()) {
								int result = JOptionPane.showConfirmDialog(
										ChatRoomFrame.this, "您将上传以下文件：\n     "
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
															ChatRoomFrame.this,
															"您上传的文件为空，请选择其它文件吧！",
															"内容有误",
															JOptionPane.WARNING_MESSAGE);
										} else if (f.length() > 1000000000) {
											JOptionPane
													.showMessageDialog(
															ChatRoomFrame.this,
															"您上传的文件大于1GB，超过上限，请尝试压缩文件后再试吧！",
															"文件太大",
															JOptionPane.WARNING_MESSAGE);
										} else {
											UploadFileThread uploadFileThread = new UploadFileThread(
													username,
													ChatRoomFrame.this,
													joyuClientSocket, f,
													systemInfo, SayuServerIP,
													SayuServerport);
											uploadFileThread.run();
										}
									}
								} else {
									System.out
											.println(username
													+ " canceled upload files: "
													+ filesUploadableNamesList
													+ "....");
								}
							}
						}
					}
				}
				new Thread(new fileChooserThread()).start();
			}
		});
		final JButton buttonDownloadFile = new RButton(2, "下  载");
		buttonDownloadFile.setFont(new Font("system", Font.PLAIN, 12));
		buttonDownloadFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (table != null) {
					int[] selectedRows = table.getSelectedRows();
					List<String> filesToUploadList = new ArrayList<String>();
					HashMap<String, String> fileToUploadHM = new HashMap<String, String>();
					if (selectedRows.length > 0) {
						for (int i = 0; i < selectedRows.length; i++) {
							String filename = ((String) table.getValueAt(
									selectedRows[i], 0)).trim();
							if (!filename.equalsIgnoreCase("")) {
								fileToUploadHM.put(
										String.valueOf(selectedRows[i]),
										filename);
								if (filesToUploadList.size() < 1) {
									filesToUploadList.add(filename);
								} else {
									filesToUploadList.add("\n     " + filename);
								}
							}
						}
						joyuTableShare.repaint();
					}
					if (fileToUploadHM.size() > 0) {
						int result = JOptionPane.showConfirmDialog(
								ChatRoomFrame.this, " 您将下载以下文件：\n     "
										+ filesToUploadList + "，\n是否继续？");
						if (result == JOptionPane.YES_OPTION) {
							Set selectedRowIndexSet = fileToUploadHM.keySet();
							Iterator selectedRowIndexIt = selectedRowIndexSet
									.iterator();
							while (selectedRowIndexIt.hasNext()) {
								int rowIndex = Integer
										.parseInt((String) selectedRowIndexIt
												.next());
								String filename = (fileToUploadHM.get(String
										.valueOf(rowIndex))).trim();
								if (!filename.equalsIgnoreCase("")) {
									new Thread(new DownloadFileThread(username,
											ChatRoomFrame.this, filename,
											rowIndex, 6, joyuClientSocket,
											systemInfo, SayuServerIP,
											SayuServerport)).start();
								}
							}
						}
					} else {
						JOptionPane.showMessageDialog(ChatRoomFrame.this,
								"对不起，您还没选中任何文件哦！", "操作有误",
								JOptionPane.WARNING_MESSAGE);
					}
				}
			}
		});
		uploadAndDownloadPanel.setLayout(new GridLayout(1, 2));
		uploadAndDownloadPanel.setBackground(new Color(180, 230, 147));
		uploadAndDownloadPanel.add(buttonUploadFile);
		uploadAndDownloadPanel.add(buttonDownloadFile);
		JPanel uploadingAndUploadAndDownloadPanel = new JPanel();
		uploadingAndUploadAndDownloadPanel.setLayout(new BorderLayout());
		uploadingAndUploadAndDownloadPanel.setBackground(new Color(180, 230,
				147));
		uploadingFilesPanel2 = new JPanel();
		uploadingFilesPanel2.setBackground(new Color(180, 230, 147));
		uploadingFilesPanel2.setLayout(new BorderLayout());
		JPanel uploadingDonghuaAndLabelPanel2 = new JPanel();
		uploadingDonghuaAndLabelPanel2
				.setLayout(new FlowLayout(FlowLayout.LEFT));
		uploadingDonghuaAndLabelPanel2.setBackground(new Color(180, 230, 147));
		JLabel uploadingDonghuaLabel2 = new JLabel(new ImageIcon(
				System.getProperty("user.dir") + "\\resources\\sayu.gif"));
		JLabel uploadingStrLabel2 = new JLabel();
		uploadingStrLabel2.setFont(new Font("system", Font.PLAIN, 13));
		uploadingStrLabel2.setText("上传:");
		uploadingDonghuaAndLabelPanel2.add(uploadingDonghuaLabel2);
		uploadingDonghuaAndLabelPanel2.add(uploadingStrLabel2);
		uploadingInfoPanel2 = new JPanel();
		uploadingInfoPanel2.setLayout(new GridLayout());
		uploadingInfoPanel2.setVisible(false);
		uploadingInfoPanel2.setBackground(new Color(180, 230, 147));
		uploadingFilesPanel2.add(uploadingDonghuaAndLabelPanel2,
				BorderLayout.WEST);
		uploadingFilesPanel2.add(uploadingInfoPanel2, BorderLayout.CENTER);
		uploadingFilesPanel2.setVisible(false);
		uploadingAndUploadAndDownloadPanel.add(uploadingFilesPanel2,
				BorderLayout.NORTH);
		uploadingAndUploadAndDownloadPanel.add(uploadAndDownloadPanel,
				BorderLayout.SOUTH);
		sharePanel.add(freshAndDeletePanel, BorderLayout.NORTH);
		sharePanel.add(uploadingAndUploadAndDownloadPanel, BorderLayout.SOUTH);
		chatCardPanel.add("sharepane", sharePanel);
		add(chatCardPanel, BorderLayout.CENTER);
		addWindowListener(new WindowAdapter() {
			public void windowActivated(WindowEvent e) {
				mainFrameActivated = true;
			}

			public void windowDeactivated(WindowEvent e) {
				mainFrameActivated = false;
			}
		});
		chatOnlineInfoFrame.addWindowListener(new WindowAdapter() {
			public void windowActivated(WindowEvent e) {
				byFrameOnlineInfoActivated = true;
			}

			public void windowDeactivated(WindowEvent e) {
				byFrameOnlineInfoActivated = false;
			}
		});
		aboutJoyuFrame.addWindowListener(new WindowAdapter() {
			public void windowActivated(WindowEvent e) {
				byFrameAboutJoyuActivated = true;
			}

			public void windowDeactivated(WindowEvent e) {
				byFrameAboutJoyuActivated = false;
			}
		});
		joyuSearchFilesFrame = new SearchFilesFrame(ChatRoomFrame.this,
				columnNames, username, joyuClientSocket, systemInfo, logoImage,
				SayuServerIP, SayuServerport);
		searchFilesForShareFrame = joyuSearchFilesFrame.getSearchFilesFrame();
		searchFilesForShareFrame.setBounds(
				((getBounds()).x + (getBounds()).width), (getBounds()).y,
				(getBounds().width), (getBounds()).height);
		searchFilesForShareFrame.addWindowListener(new WindowAdapter() {
			public void windowActivated(WindowEvent e) {
				byFrameSearchFilesActivated = true;
			}

			public void windowDeactivated(WindowEvent e) {
				byFrameSearchFilesActivated = false;
			}
		});
		myDock.start();
	}

	public JLabel createALabel() {
		final JLabel alabel = new JLabel();
		alabel.setFont(new Font("system", Font.PLAIN, 12));
		alabel.setBackground(new Color(180, 230, 147));
		alabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				alabel.setToolTipText(alabel.getText());
			}

			public void mouseEntered(MouseEvent e) {
				alabel.setToolTipText(alabel.getText());
			}
		});
		return alabel;
	}

	public JPanel getUploadingFilesPanel() {
		return uploadingFilesPanel;
	}

	public JPanel getUploadingFilesPanel2() {
		return uploadingFilesPanel2;
	}

	private String bObj = new String("bbb");

	public void freshShareInfo() {
		synchronized (bObj) {
			if (this != null) {
				if (joyuTableShare != null) {
					joyuTableShare.invalidate();
					joyuTableShare.setVisible(false);
				}
				data = new Object[ChatRoomFrame.this.getFilesForShareSet()
						.size()][columnNames.length];
				it = ChatRoomFrame.this.getFilesForShareSet().iterator();
				while (it.hasNext()) {
					for (int i = 0; i < ChatRoomFrame.this
							.getFilesForShareSet().size(); i++) {
						file = (FileBeanClientSide) it.next();
						data[i][0] = new String(file.getFileName());
						data[i][1] = new String(file.getFileKind());
						data[i][2] = new String(file.getFileSize());
						data[i][3] = new String(file.getFileFrom());
						data[i][4] = new String(file.getFileCreatedTime());
						data[i][5] = new String(file.getFileModifiedTime());
						data[i][6] = new String("");
					}
				}
				tableModelShare = new JoyuTableModel(columnNames, data);
				joyuTableShare = new JoyuTableSortShareFiles(tableModelShare,
						username, ChatRoomFrame.this, joyuClientSocket,
						systemInfo, SayuServerIP, SayuServerport);
				joyuTableShare.setOpaque(true);
				table = joyuTableShare.getTable();
				table.addMouseListener(new java.awt.event.MouseAdapter() {
					public void mouseClicked(java.awt.event.MouseEvent e) {
						if ((e.getClickCount() == 2)
								&& table.getValueAt(table.getSelectedRow(), 0) != null) {
							String doubleClickedFilename = (String) table
									.getValueAt(table.getSelectedRow(), 0);
							if (!doubleClickedFilename.isEmpty()) {
								int result = JOptionPane.showConfirmDialog(
										ChatRoomFrame.this, " 您将下载以下文件：\n     "
												+ doubleClickedFilename
												+ "，\n是否继续？");
								if (result == JOptionPane.YES_OPTION) {
									new Thread(new DownloadFileThread(username,
											ChatRoomFrame.this,
											doubleClickedFilename, table
													.getSelectedRow(), 6,
											joyuClientSocket, systemInfo,
											SayuServerIP, SayuServerport))
											.start();
								}
							} else {
								JOptionPane.showMessageDialog(
										ChatRoomFrame.this, "对不起，您还没选中任何文件哦！",
										"操作有误", JOptionPane.WARNING_MESSAGE);
							}
						}
					}
				});
				sharePanel.add(joyuTableShare, BorderLayout.CENTER);
				validate();
				joyuSearchFilesFrame.showSearchResultTable();
			}
			joyuSearchFilesFrame.freshAfterDelete();
		}
	}

	public TextArea getSendArea() {
		return sendArea;
	}

	public TextArea getContentArea() {
		return contentArea;
	}

	private class MyDock implements Runnable {
		public void run() {
			while (true) {
				try {
					Thread.sleep(80);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				while (byFrameSearchFilesActivated) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// e.printStackTrace();
					}
					if (Math.abs((searchFilesForShareFrame.getBounds()).x
							- ((getBounds()).x + (getBounds()).width)) <= 15) {
						searchFilesForShareFrame.setBounds(
								((getBounds()).x + (getBounds()).width),
								(getBounds()).y,
								(searchFilesForShareFrame.getBounds()).width,
								(searchFilesForShareFrame.getBounds()).height);
						byFrameSearchFilesJoined = true;
					} else {
						byFrameSearchFilesJoined = false;
					}
				}
				while (byFrameAboutJoyuActivated) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// e.printStackTrace();
					}
					if (Math.abs((aboutJoyuFrame.getBounds()).x
							- ((getBounds()).x + (getBounds()).width)) <= 15) {
						aboutJoyuFrame.setBounds(
								((getBounds()).x + (getBounds()).width),
								(getBounds()).y,
								(aboutJoyuFrame.getBounds()).width,
								(aboutJoyuFrame.getBounds()).height);
						byFrameAboutJoyuJoined = true;
					} else {
						byFrameAboutJoyuJoined = false;
					}
				}
				while (mainFrameActivated) {
					if (Math.abs((searchFilesForShareFrame.getBounds()).x
							- ((getBounds()).x + (getBounds()).width)) <= 15) {
						searchFilesForShareFrame.setBounds(
								((getBounds()).x + (getBounds()).width),
								(getBounds()).y,
								(searchFilesForShareFrame.getBounds()).width,
								(searchFilesForShareFrame.getBounds()).height);
						byFrameSearchFilesJoined = true;
					}
					if (Math.abs((chatOnlineInfoFrame.getBounds()).x
							- ((getBounds()).x + (getBounds()).width)) <= 15) {
						chatOnlineInfoFrame.setBounds(
								((getBounds()).x + (getBounds()).width),
								(getBounds()).y,
								(chatOnlineInfoFrame.getBounds()).width,
								(chatOnlineInfoFrame.getBounds()).height);
						byFrameOnlineInfoJoined = true;
					}
					if (Math.abs((aboutJoyuFrame.getBounds()).x
							- ((getBounds()).x + (getBounds()).width)) <= 15) {
						aboutJoyuFrame.setBounds(
								((getBounds()).x + (getBounds()).width),
								(getBounds()).y,
								(aboutJoyuFrame.getBounds()).width,
								(aboutJoyuFrame.getBounds()).height);
						byFrameAboutJoyuJoined = true;
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// e.printStackTrace();
					}
					if (!byFrameAboutJoyuJoined && !byFrameOnlineInfoJoined
							&& byFrameSearchFilesJoined) {
						if ((Math.abs((searchFilesForShareFrame.getBounds()).x
								- ((getBounds()).x + (getBounds()).width)) > 0)
								|| (Math.abs((searchFilesForShareFrame
										.getBounds()).y - ((getBounds()).y)) > 0)) {
							searchFilesForShareFrame
									.setBounds(
											((getBounds()).x + (getBounds()).width),
											(getBounds()).y,
											(searchFilesForShareFrame
													.getBounds()).width,
											(searchFilesForShareFrame
													.getBounds()).height);
						}
					}
					if (byFrameAboutJoyuJoined && !byFrameOnlineInfoJoined
							&& byFrameSearchFilesJoined) {
						if ((Math.abs((aboutJoyuFrame.getBounds()).x
								- ((getBounds()).x + (getBounds()).width)) > 0)
								|| (Math.abs((aboutJoyuFrame.getBounds()).y
										- ((getBounds()).y)) > 0)) {
							aboutJoyuFrame.setBounds(
									((getBounds()).x + (getBounds()).width),
									(getBounds()).y,
									(aboutJoyuFrame.getBounds()).width,
									(aboutJoyuFrame.getBounds()).height);
							searchFilesForShareFrame
									.setBounds(
											((getBounds()).x + (getBounds()).width),
											(getBounds()).y,
											(searchFilesForShareFrame
													.getBounds()).width,
											(searchFilesForShareFrame
													.getBounds()).height);
						}
					}
					if (byFrameAboutJoyuJoined && !byFrameOnlineInfoJoined
							&& !byFrameSearchFilesJoined) {
						if ((Math.abs((aboutJoyuFrame.getBounds()).x
								- ((getBounds()).x + (getBounds()).width)) > 0)
								|| (Math.abs((aboutJoyuFrame.getBounds()).y
										- ((getBounds()).y)) > 0)) {
							aboutJoyuFrame.setBounds(
									((getBounds()).x + (getBounds()).width),
									(getBounds()).y,
									(aboutJoyuFrame.getBounds()).width,
									(aboutJoyuFrame.getBounds()).height);
						}
					}
					if (!byFrameAboutJoyuJoined && byFrameOnlineInfoJoined
							&& byFrameSearchFilesJoined) {
						if ((Math.abs((searchFilesForShareFrame.getBounds()).x
								- ((getBounds()).x + (getBounds()).width)) > 0)
								|| (Math.abs((searchFilesForShareFrame
										.getBounds()).y - ((getBounds()).y)) > 0)) {
							searchFilesForShareFrame
									.setBounds(
											((getBounds()).x + (getBounds()).width),
											(getBounds()).y,
											(searchFilesForShareFrame
													.getBounds()).width,
											(searchFilesForShareFrame
													.getBounds()).height);
							chatOnlineInfoFrame.setBounds(
									((getBounds()).x + (getBounds()).width),
									(getBounds()).y,
									(chatOnlineInfoFrame.getBounds()).width,
									(chatOnlineInfoFrame.getBounds()).height);
						}
					}
					if (!byFrameAboutJoyuJoined && byFrameOnlineInfoJoined
							&& !byFrameSearchFilesJoined) {
						if ((Math.abs((chatOnlineInfoFrame.getBounds()).x
								- ((getBounds()).x + (getBounds()).width)) > 0)
								|| (Math.abs((chatOnlineInfoFrame.getBounds()).y
										- ((getBounds()).y)) > 0)) {
							chatOnlineInfoFrame.setBounds(
									((getBounds()).x + (getBounds()).width),
									(getBounds()).y,
									(chatOnlineInfoFrame.getBounds()).width,
									(chatOnlineInfoFrame.getBounds()).height);
						}
					}
					if (byFrameAboutJoyuJoined && byFrameOnlineInfoJoined
							&& byFrameSearchFilesJoined) {
						if ((Math.abs((chatOnlineInfoFrame.getBounds()).x
								- ((getBounds()).x + (getBounds()).width)) > 0)
								|| (Math.abs((chatOnlineInfoFrame.getBounds()).y
										- ((getBounds()).y)) > 0)) {
							chatOnlineInfoFrame.setBounds(
									((getBounds()).x + (getBounds()).width),
									(getBounds()).y,
									(chatOnlineInfoFrame.getBounds()).width,
									(chatOnlineInfoFrame.getBounds()).height);
							aboutJoyuFrame.setBounds(
									((getBounds()).x + (getBounds()).width),
									(getBounds()).y,
									(aboutJoyuFrame.getBounds()).width,
									(aboutJoyuFrame.getBounds()).height);
							searchFilesForShareFrame
									.setBounds(
											((getBounds()).x + (getBounds()).width),
											(getBounds()).y,
											(searchFilesForShareFrame
													.getBounds()).width,
											(searchFilesForShareFrame
													.getBounds()).height);
						}
					}
					if (byFrameAboutJoyuJoined && byFrameOnlineInfoJoined
							&& !byFrameSearchFilesJoined) {
						if ((Math.abs((chatOnlineInfoFrame.getBounds()).x
								- ((getBounds()).x + (getBounds()).width)) > 0)
								|| (Math.abs((chatOnlineInfoFrame.getBounds()).y
										- ((getBounds()).y)) > 0)) {
							chatOnlineInfoFrame.setBounds(
									((getBounds()).x + (getBounds()).width),
									(getBounds()).y,
									(chatOnlineInfoFrame.getBounds()).width,
									(chatOnlineInfoFrame.getBounds()).height);
							aboutJoyuFrame.setBounds(
									((getBounds()).x + (getBounds()).width),
									(getBounds()).y,
									(aboutJoyuFrame.getBounds()).width,
									(aboutJoyuFrame.getBounds()).height);
						}
					}
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// e.printStackTrace();
					}
				}
				while (byFrameOnlineInfoActivated) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// e.printStackTrace();
					}
					if (Math.abs((chatOnlineInfoFrame.getBounds()).x
							- ((getBounds()).x + (getBounds()).width)) <= 15) {
						chatOnlineInfoFrame.setBounds(
								((getBounds()).x + (getBounds()).width),
								(getBounds()).y,
								(chatOnlineInfoFrame.getBounds()).width,
								(chatOnlineInfoFrame.getBounds()).height);
						byFrameOnlineInfoJoined = true;
					} else {
						byFrameOnlineInfoJoined = false;
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							// e.printStackTrace();
						}
					}
				}
			}
		}
	}

	public JTable getTable() {
		return table;
	}

	public JPanel getUploadingInfoPanel() {
		return uploadingInfoPanel;
	}

	public JPanel getUploadingInfoPanel2() {
		return uploadingInfoPanel2;
	}

	public void addAnUploadingFile(File f) {
		filesUploadingList.add(f);
	}

	public synchronized void removeAnUploadingFile(File f) {
		filesUploadingList.remove(f);
	}

	public synchronized boolean isFilesUploadingListEmpty() {
		return filesUploadingList.isEmpty();
	}

	public SearchFilesFrame getJoyuSearchFilesFrame() {
		return joyuSearchFilesFrame;
	}
}