package joyu.chat.client.chatRoomFrame;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.text.BadLocationException;
import joyu.chat.client.JoyuClientSocket;
import joyu.chat.client.RButton;

public class ChatHistoryFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private RandomAccessFile araf;
	private TextArea searchResultArea = new TextArea(null, 3, 6,
			TextArea.SCROLLBARS_VERTICAL_ONLY);
	private TextArea allLocalhostArea = new TextArea(null, 3, 6,
			TextArea.SCROLLBARS_VERTICAL_ONLY);
	private TextArea historyFromServerArea = new TextArea(null, 3, 6,
			TextArea.SCROLLBARS_VERTICAL_ONLY);
	private JTextField searchField = new JTextField(10);
	private StringBuffer localContentFromDiskBS;
	private StringBuffer localContentAddedBS = new StringBuffer();
	private StringBuffer localSearchResult = new StringBuffer();
	private int localLastAddedLength = 0;
	private StringBuffer historyFromServerSideBS;
	private int count = 0;
	private Image logo;
	private final byte[] KEYVALUE;
	private final String systemInfo;
	private TextArea searchResultAreab = new TextArea(null, 3, 6,
			TextArea.SCROLLBARS_VERTICAL_ONLY);
	private JTextField searchFieldb = new JTextField(10);
	private StringBuffer toSearchString;
	private JoyuClientSocket joyuClientSocket;

	public ChatHistoryFrame(byte[] KEYVALUE, Image logo, String username,
			String systemInfo, JoyuClientSocket joyuClientSocket) {
		this.logo = logo;
		this.KEYVALUE = KEYVALUE;
		this.systemInfo = systemInfo;
		this.joyuClientSocket = joyuClientSocket;
		localContentFromDiskBS = new StringBuffer(systemInfo);
		historyFromServerSideBS = new StringBuffer(systemInfo
				+ "getHistoryByTime");
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
					+ username + "\\history\\");
			if (!f3.exists()) {
				f3.mkdir();
			}
			File f = new File(System.getProperty("user.dir") + "\\users\\"
					+ username + "\\history\\" + username + "_history.txt");
			if (!f.exists()) {
				try {
					f.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			this.araf = new RandomAccessFile(f, "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		showHistoryFrame();
	}

	private StringBuffer getLocalHistoryAllFromDisk() {
		try {
			if (araf != null) {
				araf.seek(0);
				for (long j = 0; j < araf.length(); j = araf.getFilePointer()) {
					int pos, keylen;
					pos = 0;
					keylen = KEYVALUE.length;
					byte[] b = new byte[10000000];
					b = (araf.readUTF()).getBytes();
					for (int i = 0; i < b.length; i++) {
						b[i] ^= KEYVALUE[pos];
						pos++;
						if (pos == keylen) {
							pos = 0;
						}
					}
					localContentFromDiskBS.append(new String(b));
				}
			}
		} catch (Exception e) {
			try {
				if (araf != null) {
					araf.close();
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
			return null;
		}
		return localContentFromDiskBS;
	}

	private StringBuffer getLocalHistoryAdded() {
		try {
			if (araf != null && (araf.getFilePointer() < araf.length())) {
				long len = araf.length();
				for (long j = 0; j < len; j = araf.getFilePointer()) {
					int pos, keylen;
					pos = 0;
					keylen = KEYVALUE.length;
					byte[] b = new byte[10000000];
					b = (araf.readUTF()).getBytes();
					for (int i = 0; i < b.length; i++) {
						b[i] ^= KEYVALUE[pos];
						pos++;
						if (pos == keylen)
							pos = 0;
					}
					localContentAddedBS.append(new String(b));
				}
			}
		} catch (Exception e) {
			try {
				if (araf != null) {
					araf.close();
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
			return null;
		}
		return localContentAddedBS;
	}

	private StringBuffer getSearchResult(String searchStr) {
		toSearchString = new StringBuffer(localContentFromDiskBS.toString()
				+ localContentAddedBS.toString());
		localSearchResult.delete(0, localSearchResult.length());
		int start = 0;
		int end = 0;
		if (systemInfo.indexOf(searchStr) != -1) {
			while (toSearchString.length() > systemInfo.length()) {
				if ((toSearchString
						.substring(
								systemInfo.length(),
								toSearchString.indexOf(systemInfo,
										systemInfo.length())))
						.indexOf(searchStr) != -1) {
					localSearchResult.append(toSearchString.substring(
							systemInfo.length(),
							toSearchString.indexOf(systemInfo,
									systemInfo.length())));
				}
				toSearchString
						.delete(0,
								toSearchString.indexOf(systemInfo,
										systemInfo.length()));
			}
		} else {
			while (toSearchString.indexOf(searchStr) != -1) {
				start = (toSearchString.substring(0,
						toSearchString.indexOf(searchStr)))
						.lastIndexOf(systemInfo) + systemInfo.length();
				end = (toSearchString.substring(start, toSearchString.length()))
						.indexOf(systemInfo) + start;
				localSearchResult.append(toSearchString.substring(start, end));
				toSearchString.delete(0, end);
			}
		}
		return localSearchResult;
	}

	private StringBuffer getSearchResultFromResultFromServerArea(
			StringBuffer toSearchStra, String searchStr) {
		StringBuffer toSearchStr = new StringBuffer(toSearchStra.toString());
		StringBuffer resultStr = new StringBuffer();
		if ((systemInfo + "getHistoryByTime").indexOf(searchStr) != -1) {
			while (toSearchStr.length() > (systemInfo + "getHistoryByTime")
					.length()) {
				if ((toSearchStr.substring((systemInfo + "getHistoryByTime")
						.length(), toSearchStr.indexOf(
						(systemInfo + "getHistoryByTime"),
						(systemInfo + "getHistoryByTime").length())))
						.indexOf(searchStr) != -1) {
					resultStr
							.append(toSearchStr.substring(
									(systemInfo + "getHistoryByTime").length(),
									toSearchStr.indexOf(
											(systemInfo + "getHistoryByTime"),
											(systemInfo + "getHistoryByTime")
													.length())));
				}
				toSearchStr.delete(0, toSearchStr.indexOf(
						(systemInfo + "getHistoryByTime"),
						(systemInfo + "getHistoryByTime").length()));
			}
		} else {
			int start = 0;
			int end = 0;
			while (toSearchStr.indexOf(searchStr) != -1) {
				start = (toSearchStr.substring(0,
						toSearchStr.indexOf(searchStr))).lastIndexOf(systemInfo
						+ "getHistoryByTime")
						+ (systemInfo + "getHistoryByTime").length();
				end = (toSearchStr.substring(start, toSearchStr.length()))
						.indexOf(systemInfo + "getHistoryByTime") + start;
				resultStr.append(toSearchStr.substring(start, end));
				toSearchStr.delete(0, end);
			}
		}
		return resultStr;
	}

	private void showHistoryFrame() {
		RButton sinceButton = new RButton(0, "全  部");
		RButton threeDaysButton = new RButton(0, "前三天");
		RButton aWeekButton = new RButton(0, "前一周");
		RButton aMonthButton = new RButton(0, "前一月");
		this.setIconImage(logo);
		setTitle("查看聊天记录");
		final JPanel panelHistory = new JPanel();
		panelHistory.setBackground(new Color(180, 230, 147));
		final CardLayout clHistory = new CardLayout();
		panelHistory.setLayout(clHistory);
		JPanel panelLocalhostAll = new JPanel();
		panelLocalhostAll.setBackground(new Color(180, 230, 147));
		panelLocalhostAll.setLayout(new BorderLayout());
		allLocalhostArea.setBackground(new Color(204, 232, 207));
		allLocalhostArea.setText(getLocalHistoryAllFromDisk().toString()
				.replace(systemInfo, ""));
		allLocalhostArea.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				allLocalhostArea.setEditable(false);
				allLocalhostArea.setBackground(new Color(204, 232, 207));
			}

			public void focusLost(FocusEvent e) {
				allLocalhostArea.setEditable(true);
			}
		});
		JPanel panelLocalhostAllBar = new JPanel();
		panelLocalhostAllBar.setLayout(new BorderLayout());
		panelLocalhostAllBar.setBackground(new Color(180, 230, 147));
		JLabel labelLocalhostAll = new JLabel(" 本地记录: ");
		labelLocalhostAll.setFont(new Font("system", Font.PLAIN, 12));
		panelLocalhostAllBar.add(labelLocalhostAll, BorderLayout.WEST);
		JPanel aDayAgoAndADayLaterPanel = new JPanel();
		aDayAgoAndADayLaterPanel.setLayout(new BorderLayout());
		aDayAgoAndADayLaterPanel.setBackground(new Color(180, 230, 147));
		JButton buttonLocalhostOneDayAgo = new RButton(1, "前一天");
		buttonLocalhostOneDayAgo.setFont(new Font("system", Font.PLAIN, 12));
		buttonLocalhostOneDayAgo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				count++;
				java.util.Calendar ca = java.util.Calendar.getInstance();
				java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
						"yyyy-MM-dd");
				ca.add(Calendar.DAY_OF_MONTH, -count);
				String aDayAg = sdf.format(ca.getTime());
				searchField.setText(aDayAg);
				searchResultArea.setText(getSearchResult(aDayAg).toString());
				if (searchResultArea.getText().trim().isEmpty()) {
					searchResultArea.setText("没有查找到相关结果...");
				}
			}
		});
		JButton buttonLocalhostOneDayLater = new RButton(2, "后一天");
		buttonLocalhostOneDayLater.setFont(new Font("system", Font.PLAIN, 12));
		buttonLocalhostOneDayLater.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				count--;
				java.util.Calendar ca = java.util.Calendar.getInstance();
				java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
						"yyyy-MM-dd");
				ca.add(Calendar.DAY_OF_MONTH, -count);
				String aDayAfter = sdf.format(ca.getTime());
				searchField.setText(aDayAfter);
				searchResultArea.setText(getSearchResult(aDayAfter).toString());
				if (searchResultArea.getText().trim().isEmpty()) {
					searchResultArea.setText("没有查找到相关结果...");
				}
			}
		});
		JButton buttonLocalhostAllFrush = new RButton(0, "刷新记录");
		buttonLocalhostAllFrush.setFont(new Font("system", Font.PLAIN, 12));
		buttonLocalhostAllFrush.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getLocalHistoryAdded();
				StringBuffer toAddedSB = new StringBuffer(localContentAddedBS
						.substring(localLastAddedLength));
				allLocalhostArea.append(toAddedSB.toString().replace(
						systemInfo, ""));
				localLastAddedLength = localContentAddedBS.length();
			}
		});
		aDayAgoAndADayLaterPanel.add(buttonLocalhostOneDayAgo,
				BorderLayout.WEST);
		aDayAgoAndADayLaterPanel.add(buttonLocalhostOneDayLater,
				BorderLayout.EAST);
		panelLocalhostAllBar.add(aDayAgoAndADayLaterPanel, BorderLayout.EAST);
		panelLocalhostAllBar.add(buttonLocalhostAllFrush);
		panelLocalhostAll.add(panelLocalhostAllBar, BorderLayout.NORTH);
		panelLocalhostAll.add(allLocalhostArea, BorderLayout.CENTER);
		JPanel panelSearch = new JPanel();
		panelSearch.setBackground(new Color(180, 230, 147));
		panelSearch.setLayout(new BorderLayout());
		searchResultArea.setBackground(new Color(204, 232, 207));
		searchResultArea.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				searchResultArea.setEditable(false);
				searchResultArea.setBackground(new Color(204, 232, 207));
			}

			public void focusLost(FocusEvent e) {
				searchResultArea.setEditable(true);
			}
		});
		JPanel panelSearcha = new JPanel();
		panelSearcha.setBackground(new Color(180, 230, 147));
		panelSearcha.setLayout(new BorderLayout());
		JLabel labelSearch = new JLabel(" 本地查找: ");
		labelSearch.setFont(new Font("system", Font.PLAIN, 12));
		searchField.setBackground(new Color(51, 184, 47));
		searchField.setFont(new Font("system", Font.PLAIN, 12));
		searchField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (searchField.getText().trim().isEmpty()) {
					JOptionPane.showMessageDialog(ChatHistoryFrame.this,
							"输入不能为空哦!请重新输入...", "格式错误",
							JOptionPane.WARNING_MESSAGE);
					searchField.requestFocus();
				} else {
					searchResultArea.setText(getSearchResult(
							searchField.getText()).toString());
					if (searchResultArea.getText().trim().isEmpty()) {
						searchResultArea.setText("没有查找到相关结果...");
					}
				}
			}
		});
		searchField.setText("在这里查找");
		JButton buttonSearch = new RButton(0, "查找");
		buttonSearch.setFont(new Font("system", Font.PLAIN, 12));
		buttonSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (searchField.getText().trim().isEmpty()) {
					JOptionPane.showMessageDialog(ChatHistoryFrame.this,
							"输入不能为空哦!请重新输入...", "格式错误",
							JOptionPane.WARNING_MESSAGE);
					searchField.requestFocus();
				} else {
					searchResultArea.setText(getSearchResult(
							searchField.getText()).toString());
					if (searchResultArea.getText().trim().isEmpty()) {
						searchResultArea.setText("没有查找到相关结果...");
					}
				}
			}
		});
		panelSearcha.add(labelSearch, BorderLayout.WEST);
		panelSearcha.add(searchField);
		panelSearcha.add(buttonSearch, BorderLayout.EAST);
		panelSearch.add(panelSearcha, BorderLayout.NORTH);
		panelSearch.add(searchResultArea, BorderLayout.CENTER);
		JSplitPane paneHistoryLocalhost = new JSplitPane();
		paneHistoryLocalhost.setForeground(new Color(180, 230, 147));
		paneHistoryLocalhost.setBackground(new Color(180, 230, 147));
		paneHistoryLocalhost.setDividerSize(3);
		paneHistoryLocalhost.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		paneHistoryLocalhost.setDividerLocation(600);
		paneHistoryLocalhost.setLeftComponent(panelLocalhostAll);
		paneHistoryLocalhost.setRightComponent(panelSearch);
		JToolBar historyToolBar = new JToolBar();
		final JButton buttonLocalHistory = new RButton(1);
		buttonLocalHistory.setText("本地聊天记录");
		buttonLocalHistory.setFont(new Font("system", Font.PLAIN, 12));
		JButton buttonAllHistory = new RButton(2);
		buttonAllHistory.setText("服务器端记录");
		buttonAllHistory.setFont(new Font("system", Font.PLAIN, 12));
		historyToolBar.add(buttonLocalHistory);
		historyToolBar.add(buttonAllHistory);
		historyToolBar.setFloatable(false);
		historyToolBar.setBackground(new Color(180, 230, 147));
		buttonLocalHistory.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clHistory.show(panelHistory, "1");
			}
		});
		buttonAllHistory.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clHistory.show(panelHistory, "2");
			}
		});
		this.add(historyToolBar, BorderLayout.NORTH);
		panelHistory.add(paneHistoryLocalhost, "1");
		JPanel panelServerHistory = new JPanel();
		panelServerHistory.setBackground(new Color(180, 230, 147));
		panelServerHistory.setLayout(new BorderLayout());
		JPanel panelServerDownloadByTime = new JPanel(new GridLayout(2, 1));
		panelServerDownloadByTime.setBackground(new Color(180, 230, 147));
		JPanel panelTimeFrom = new JPanel();
		panelTimeFrom.setBackground(new Color(180, 230, 147));
		JLabel serverInputTimeFromLabel = new JLabel("从 ");
		serverInputTimeFromLabel.setFont(new Font("system", Font.PLAIN, 13));
		final JTextField serverInputTimeFromYearField = new JTextField(4);
		serverInputTimeFromYearField.setBackground(new Color(51, 184, 47));
		JLabel serverInputTimeFromYearLabel = new JLabel("年 ");
		serverInputTimeFromYearLabel
				.setFont(new Font("system", Font.PLAIN, 13));
		final JTextField serverInputTimeFromMonthField = new JTextField(2);
		serverInputTimeFromMonthField.setBackground(new Color(51, 184, 47));
		JLabel serverInputTimeFromMonthLabel = new JLabel("月 ");
		serverInputTimeFromMonthLabel
				.setFont(new Font("system", Font.PLAIN, 13));
		final JTextField serverInputTimeFromDayField = new JTextField(2);
		serverInputTimeFromDayField.setBackground(new Color(51, 184, 47));
		JLabel serverInputTimeFromDayLabel = new JLabel("日 ");
		serverInputTimeFromDayLabel.setFont(new Font("system", Font.PLAIN, 13));
		final JTextField serverInputTimeFromHourField = new JTextField(2);
		serverInputTimeFromHourField.setBackground(new Color(51, 184, 47));
		JLabel serverInputTimeFromHourLabel = new JLabel("时 ");
		serverInputTimeFromHourLabel
				.setFont(new Font("system", Font.PLAIN, 13));
		final JTextField serverInputTimeFromMinuteField = new JTextField(2);
		serverInputTimeFromMinuteField.setBackground(new Color(51, 184, 47));
		JLabel serverInputTimeFromMinuteLabel = new JLabel("分 ");
		serverInputTimeFromMinuteLabel.setFont(new Font("system", Font.PLAIN,
				13));
		final JTextField serverInputTimeFromSecondField = new JTextField(2);
		serverInputTimeFromSecondField.setBackground(new Color(51, 184, 47));
		JLabel serverInputTimeFromSecondLabel = new JLabel("秒 ");
		serverInputTimeFromSecondLabel.setFont(new Font("system", Font.PLAIN,
				13));
		panelTimeFrom.add(serverInputTimeFromLabel);
		panelTimeFrom.add(serverInputTimeFromYearField);
		panelTimeFrom.add(serverInputTimeFromYearLabel);
		panelTimeFrom.add(serverInputTimeFromMonthField);
		panelTimeFrom.add(serverInputTimeFromMonthLabel);
		panelTimeFrom.add(serverInputTimeFromDayField);
		panelTimeFrom.add(serverInputTimeFromDayLabel);
		panelTimeFrom.add(serverInputTimeFromHourField);
		panelTimeFrom.add(serverInputTimeFromHourLabel);
		panelTimeFrom.add(serverInputTimeFromMinuteField);
		panelTimeFrom.add(serverInputTimeFromMinuteLabel);
		panelTimeFrom.add(serverInputTimeFromSecondField);
		panelTimeFrom.add(serverInputTimeFromSecondLabel);
		panelTimeFrom.add(threeDaysButton);
		panelTimeFrom.add(aWeekButton);
		panelTimeFrom.add(aMonthButton);
		panelTimeFrom.add(sinceButton);
		JPanel panelTimeTo = new JPanel();
		// panelTimeTo.setLayout(new FlowLayout(FlowLayout.LEFT));
		panelTimeTo.setBackground(new Color(180, 230, 147));
		JLabel serverInputTimeTo = new JLabel("到 ");
		serverInputTimeTo.setFont(new Font("system", Font.PLAIN, 13));
		final JTextField serverInputTimeToYearField = new JTextField(4);
		serverInputTimeToYearField.setBackground(new Color(51, 184, 47));
		JLabel serverInputTimeToYearLabel = new JLabel("年 ");
		serverInputTimeToYearLabel.setFont(new Font("system", Font.PLAIN, 13));
		final JTextField serverInputTimeToMonthField = new JTextField(2);
		serverInputTimeToMonthField.setBackground(new Color(51, 184, 47));
		JLabel serverInputTimeToMonthLabel = new JLabel("月 ");
		serverInputTimeToMonthLabel.setFont(new Font("system", Font.PLAIN, 13));
		final JTextField serverInputTimeToDayField = new JTextField(2);
		serverInputTimeToDayField.setBackground(new Color(51, 184, 47));
		JLabel serverInputTimeToDayLabel = new JLabel("日 ");
		serverInputTimeToDayLabel.setFont(new Font("system", Font.PLAIN, 13));
		final JTextField serverInputTimeToHourField = new JTextField(2);
		serverInputTimeToHourField.setBackground(new Color(51, 184, 47));
		JLabel serverInputTimeToHourLabel = new JLabel("时 ");
		serverInputTimeToHourLabel.setFont(new Font("system", Font.PLAIN, 13));
		final JTextField serverInputTimeToMinuteField = new JTextField(2);
		serverInputTimeToMinuteField.setBackground(new Color(51, 184, 47));
		JLabel serverInputTimeToMinuteLabel = new JLabel("分 ");
		serverInputTimeToMinuteLabel
				.setFont(new Font("system", Font.PLAIN, 13));
		final JTextField serverInputTimeToSecondField = new JTextField(2);
		serverInputTimeToSecondField.setBackground(new Color(51, 184, 47));
		JLabel serverInputTimeToSecondLabel = new JLabel("秒 ");
		serverInputTimeToSecondLabel
				.setFont(new Font("system", Font.PLAIN, 13));
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
		SimpleDateFormat dateFormat1 = new SimpleDateFormat("MM");
		SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd");
		SimpleDateFormat dateFormat3 = new SimpleDateFormat("HH");
		SimpleDateFormat dateFormat4 = new SimpleDateFormat("mm");
		SimpleDateFormat dateFormat5 = new SimpleDateFormat("ss");
		java.util.Calendar ca = java.util.Calendar.getInstance();
		ca.add(Calendar.DAY_OF_MONTH, -3);
		String fromYearStr = dateFormat.format(ca.getTime());
		String fromMonthStr = dateFormat1.format(ca.getTime());
		String fromDayStr = dateFormat2.format(ca.getTime());
		String fromHourStr = dateFormat3.format(ca.getTime());
		String fromMinuteStr = dateFormat4.format(ca.getTime());
		String fromSecondStr = dateFormat5.format(ca.getTime());
		serverInputTimeFromYearField.setText(fromYearStr);
		serverInputTimeFromMonthField.setText(fromMonthStr);
		serverInputTimeFromDayField.setText(fromDayStr);
		serverInputTimeFromHourField.setText(fromHourStr);
		serverInputTimeFromMinuteField.setText(fromMinuteStr);
		serverInputTimeFromSecondField.setText(fromSecondStr);
		java.util.Date dateNow = new java.util.Date();
		String toYearStr = dateFormat.format(dateNow);
		String toMonthStr = dateFormat1.format(dateNow);
		String toDayStr = dateFormat2.format(dateNow);
		String toHourStr = dateFormat3.format(dateNow);
		String toMinuteStr = dateFormat4.format(dateNow);
		String toSecondStr = dateFormat5.format(dateNow);
		serverInputTimeToYearField.setText(toYearStr);
		serverInputTimeToMonthField.setText(toMonthStr);
		serverInputTimeToDayField.setText(toDayStr);
		serverInputTimeToHourField.setText(toHourStr);
		serverInputTimeToMinuteField.setText(toMinuteStr);
		serverInputTimeToSecondField.setText(toSecondStr);
		final TextField usernameTextField = new TextField(10);
		usernameTextField.setBackground(new Color(51, 184, 47));
		JLabel usernameLable = new JLabel(" 用户昵称：");
		usernameLable.setFont(new Font("system", Font.PLAIN, 13));
		usernameLable.setBackground(new Color(180, 230, 147));
		JButton serverSearchButton = new RButton(0);
		serverSearchButton.setText("   开始下载!   ");
		serverSearchButton.setFont(new Font("system", Font.PLAIN, 13));
		serverSearchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				historyFromServerSideBS.delete(
						(systemInfo + "getHistoryByTime").length(),
						historyFromServerSideBS.length());
				if ((serverInputTimeFromYearField.getText().trim()).length() > 0
						&& (serverInputTimeFromMonthField.getText().trim())
								.length() > 0
						&& (serverInputTimeFromDayField.getText().trim())
								.length() > 0
						&& (serverInputTimeFromHourField.getText().trim())
								.length() > 0
						&& (serverInputTimeFromMinuteField.getText().trim())
								.length() > 0
						&& (serverInputTimeFromSecondField.getText().trim())
								.length() > 0
						&& (serverInputTimeToYearField.getText().trim())
								.length() > 0
						&& (serverInputTimeToMonthField.getText().trim())
								.length() > 0
						&& (serverInputTimeToDayField.getText().trim())
								.length() > 0
						&& (serverInputTimeToHourField.getText().trim())
								.length() > 0
						&& (serverInputTimeToMinuteField.getText().trim())
								.length() > 0
						&& (serverInputTimeToSecondField.getText().trim())
								.length() > 0) {
					String fromYearStr = serverInputTimeFromYearField.getText()
							.trim();
					if ((serverInputTimeFromYearField.getText().trim())
							.length() == 1) {
						fromYearStr = "000"
								+ serverInputTimeFromYearField.getText().trim();
					} else if ((serverInputTimeFromYearField.getText().trim())
							.length() == 2) {
						fromYearStr = "00"
								+ serverInputTimeFromYearField.getText().trim();
					} else if ((serverInputTimeFromYearField.getText().trim())
							.length() == 3) {
						fromYearStr = "0"
								+ serverInputTimeFromYearField.getText().trim();
					}
					String fromMonthStr = serverInputTimeFromMonthField
							.getText().trim();
					if ((serverInputTimeFromMonthField.getText().trim())
							.length() == 1) {
						fromMonthStr = "0"
								+ serverInputTimeFromMonthField.getText()
										.trim();
					}
					String fromDayStr = serverInputTimeFromDayField.getText()
							.trim();
					if ((serverInputTimeFromDayField.getText().trim()).length() == 1) {
						fromDayStr = "0"
								+ serverInputTimeFromDayField.getText().trim();
					}
					String fromHourStr = serverInputTimeFromHourField.getText()
							.trim();
					if ((serverInputTimeFromHourField.getText().trim())
							.length() == 1) {
						fromHourStr = "0"
								+ serverInputTimeFromHourField.getText().trim();
					}
					String fromMinuteStr = serverInputTimeFromMinuteField
							.getText().trim();
					if ((serverInputTimeFromMinuteField.getText().trim())
							.length() == 1) {
						fromMinuteStr = "0"
								+ serverInputTimeFromMinuteField.getText()
										.trim();
					}
					String fromSecondStr = serverInputTimeFromSecondField
							.getText().trim();
					if ((serverInputTimeFromSecondField.getText().trim())
							.length() == 1) {
						fromSecondStr = "0"
								+ serverInputTimeFromSecondField.getText()
										.trim();
					}
					String toYearStr = serverInputTimeToYearField.getText()
							.trim();
					if ((serverInputTimeToYearField.getText().trim()).length() == 1) {
						toYearStr = "000"
								+ serverInputTimeToYearField.getText().trim();
					} else if ((serverInputTimeToYearField.getText().trim())
							.length() == 2) {
						toYearStr = "00"
								+ serverInputTimeToYearField.getText().trim();
					} else if ((serverInputTimeToYearField.getText().trim())
							.length() == 3) {
						toYearStr = "0"
								+ serverInputTimeToYearField.getText().trim();
					}
					String toMonthStr = serverInputTimeToMonthField.getText()
							.trim();
					if ((serverInputTimeToMonthField.getText().trim()).length() == 1) {
						toMonthStr = "0"
								+ serverInputTimeToMonthField.getText().trim();
					}
					String toDayStr = serverInputTimeToDayField.getText()
							.trim();
					if ((serverInputTimeToDayField.getText().trim()).length() == 1) {
						toDayStr = "0"
								+ serverInputTimeToDayField.getText().trim();
					}
					String toHourStr = serverInputTimeToHourField.getText()
							.trim();
					if ((serverInputTimeToHourField.getText().trim()).length() == 1) {
						toHourStr = "0"
								+ serverInputTimeToHourField.getText().trim();
					}
					String toMinuteStr = serverInputTimeToMinuteField.getText()
							.trim();
					if ((serverInputTimeToMinuteField.getText().trim())
							.length() == 1) {
						toMinuteStr = "0"
								+ serverInputTimeToMinuteField.getText().trim();
					}
					String toSecondStr = serverInputTimeToSecondField.getText()
							.trim();
					if ((serverInputTimeToSecondField.getText().trim())
							.length() == 1) {
						toSecondStr = "0"
								+ serverInputTimeToSecondField.getText().trim();
					}
					String formTimeStr = fromYearStr + "-" + fromMonthStr + "-"
							+ fromDayStr + " " + fromHourStr + ":"
							+ fromMinuteStr + ":" + fromSecondStr;
					String toTimeStr = toYearStr + "-" + toMonthStr + "-"
							+ toDayStr + " " + toHourStr + ":" + toMinuteStr
							+ ":" + toSecondStr;
					java.util.Date dateNowax = new java.util.Date();
					SimpleDateFormat dateFormatAx = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					String timeNowa = dateFormatAx.format(dateNowax);
					if ((formTimeStr.compareTo(toTimeStr) < 0)
							&& (toTimeStr.compareTo(timeNowa) <= 0)) {
						try {
							joyuClientSocket.sendString(usernameTextField
									.getText().trim()
									+ "username"
									+ formTimeStr
									+ systemInfo
									+ toTimeStr
									+ systemInfo + "getHistoryByTime");
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					} else {
						JOptionPane.showMessageDialog(ChatHistoryFrame.this,
								"截止时间应不大于当前时间并且必须大于起始时间哦！请重新输入吧！", "时间内容错误",
								JOptionPane.WARNING_MESSAGE);
					}
					ChatHistoryFrame.this.requestFocus();
				}
			}
		});
		serverInputTimeFromYearField.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				serverInputTimeFromYearField.selectAll();
			}
		});
		serverInputTimeFromYearField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent ke1) {
				if (ke1.getKeyCode() == KeyEvent.VK_ENTER) {
					String str = serverInputTimeFromYearField.getText().trim();
					if (str.isEmpty()) {
						JOptionPane.showMessageDialog(ChatHistoryFrame.this,
								"输入不能为空,或不能全为空格哦!", "内容格式不正确",
								JOptionPane.WARNING_MESSAGE);
					}
				}
			}

			public void keyReleased(KeyEvent ke) {
				if (serverInputTimeFromYearField.getCaretPosition() >= 5) {
					try {
						String a = serverInputTimeFromYearField.getText(4, 1);
						serverInputTimeFromYearField
								.setText(serverInputTimeFromYearField.getText(
										0, 4));
						try {
							SimpleDateFormat dateFormatA = new SimpleDateFormat(
									"yyyy");
							java.util.Calendar cal = java.util.Calendar
									.getInstance();
							String nowYearStrA = dateFormatA.format(cal
									.getTime());
							if ((Integer.parseInt(serverInputTimeFromYearField
									.getText(0, 4)) < 0)
									|| (Integer
											.parseInt(serverInputTimeFromYearField
													.getText(0, 4)) > Integer
											.parseInt(nowYearStrA))) {
								JOptionPane.showMessageDialog(
										ChatHistoryFrame.this,
										"输入错误，必须不大于当前哦！", "内容格式不正确",
										JOptionPane.WARNING_MESSAGE);
								serverInputTimeFromYearField.selectAll();
								serverInputTimeFromYearField.requestFocus();
							} else {
								serverInputTimeFromMonthField.requestFocus();
								serverInputTimeFromMonthField.setText(a);
							}
						} catch (NumberFormatException eee) {
							JOptionPane.showMessageDialog(
									ChatHistoryFrame.this, "输入不能为空且必须为数字哦!",
									"内容格式不正确", JOptionPane.WARNING_MESSAGE);
							serverInputTimeFromYearField.setText("");
							serverInputTimeFromYearField.requestFocus();
						}
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
				}
				if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
					try {
						SimpleDateFormat dateFormatA = new SimpleDateFormat(
								"yyyy");
						java.util.Calendar cal = java.util.Calendar
								.getInstance();
						String nowYearStrA = dateFormatA.format(cal.getTime());
						if ((Integer.parseInt(serverInputTimeFromYearField
								.getText()) < 0)
								|| (Integer
										.parseInt(serverInputTimeFromYearField
												.getText()) > Integer
										.parseInt(nowYearStrA))) {
							JOptionPane.showMessageDialog(
									ChatHistoryFrame.this, "输入错误，必须不大于当前哦！",
									"内容格式不正确", JOptionPane.WARNING_MESSAGE);
							serverInputTimeFromYearField.selectAll();
							serverInputTimeFromYearField.requestFocus();
						} else {
							serverInputTimeFromMonthField.selectAll();
							serverInputTimeFromMonthField.requestFocus();
						}
					} catch (NumberFormatException eee) {
						JOptionPane.showMessageDialog(ChatHistoryFrame.this,
								"输入不能为空且必须为数字哦!", "内容格式不正确",
								JOptionPane.WARNING_MESSAGE);
						serverInputTimeFromYearField.setText("");
						serverInputTimeFromYearField.requestFocus();
					}
				}
			}
		});
		serverInputTimeFromMonthField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent ke1) {
				if (ke1.getKeyCode() == KeyEvent.VK_ENTER) {
					String str = serverInputTimeFromMonthField.getText().trim();
					if (str.isEmpty()) {
						JOptionPane.showMessageDialog(ChatHistoryFrame.this,
								"输入不能为空,或不能全为空格哦!", "内容格式不正确",
								JOptionPane.WARNING_MESSAGE);
					}
				}
			}

			public void keyReleased(KeyEvent ke) {
				if (serverInputTimeFromMonthField.getCaretPosition() >= 3) {
					try {
						Integer.parseInt(serverInputTimeFromMonthField
								.getText());
						try {
							String a = serverInputTimeFromMonthField.getText(2,
									1);
							serverInputTimeFromDayField.requestFocus();
							serverInputTimeFromMonthField
									.setText(serverInputTimeFromMonthField
											.getText(0, 2));
							serverInputTimeFromDayField.setText(a);
						} catch (BadLocationException e) {
							e.printStackTrace();
						}
					} catch (NumberFormatException eee) {
						JOptionPane.showMessageDialog(ChatHistoryFrame.this,
								"输入不能为空且必须为数字哦!", "内容格式不正确",
								JOptionPane.WARNING_MESSAGE);
						serverInputTimeFromMonthField.setText("");
						serverInputTimeFromMonthField.requestFocus();
					}
				}
				if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
					try {
						Integer.parseInt(serverInputTimeFromMonthField
								.getText());
						serverInputTimeFromDayField.selectAll();
						serverInputTimeFromDayField.requestFocus();
					} catch (NumberFormatException eee) {
						JOptionPane.showMessageDialog(ChatHistoryFrame.this,
								"输入不能为空且必须为数字哦!", "内容格式不正确",
								JOptionPane.WARNING_MESSAGE);
						serverInputTimeFromMonthField.setText("");
						serverInputTimeFromMonthField.requestFocus();
					}
				}
			}
		});
		serverInputTimeFromDayField.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				serverInputTimeFromDayField.selectAll();
			}
		});
		serverInputTimeFromDayField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent ke1) {
				if (ke1.getKeyCode() == KeyEvent.VK_ENTER) {
					String str = serverInputTimeFromDayField.getText().trim();
					if (str.isEmpty()) {
						JOptionPane.showMessageDialog(ChatHistoryFrame.this,
								"输入不能为空,或不能全为空格哦!", "内容格式不正确",
								JOptionPane.WARNING_MESSAGE);
					}
				}
			}

			public void keyReleased(KeyEvent ke) {
				if (serverInputTimeFromDayField.getCaretPosition() >= 3) {
					try {
						Integer.parseInt(serverInputTimeFromDayField.getText());
						try {
							String a = serverInputTimeFromDayField
									.getText(2, 1);
							serverInputTimeFromHourField.requestFocus();
							serverInputTimeFromDayField
									.setText(serverInputTimeFromDayField
											.getText(0, 2));
							serverInputTimeFromHourField.setText(a);
						} catch (BadLocationException e) {
							e.printStackTrace();
						}
					} catch (NumberFormatException eee) {
						JOptionPane.showMessageDialog(ChatHistoryFrame.this,
								"输入不能为空且必须为数字哦!", "内容格式不正确",
								JOptionPane.WARNING_MESSAGE);
						serverInputTimeFromDayField.setText("");
						serverInputTimeFromDayField.requestFocus();
					}
				}
				if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
					try {
						Integer.parseInt(serverInputTimeFromDayField.getText());
						serverInputTimeFromHourField.selectAll();
						serverInputTimeFromHourField.requestFocus();
					} catch (NumberFormatException eee) {
						JOptionPane.showMessageDialog(ChatHistoryFrame.this,
								"输入不能为空且必须为数字哦!", "内容格式不正确",
								JOptionPane.WARNING_MESSAGE);
						serverInputTimeFromDayField.setText("");
						serverInputTimeFromDayField.requestFocus();
					}
				}
			}
		});
		serverInputTimeFromHourField.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				serverInputTimeFromHourField.selectAll();
			}
		});
		serverInputTimeFromHourField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent ke1) {
				if (ke1.getKeyCode() == KeyEvent.VK_ENTER) {
					String str = serverInputTimeFromHourField.getText().trim();
					if (str.isEmpty()) {
						JOptionPane.showMessageDialog(ChatHistoryFrame.this,
								"输入不能为空,或不能全为空格哦!", "内容格式不正确",
								JOptionPane.WARNING_MESSAGE);
					}
				}
			}

			public void keyReleased(KeyEvent ke) {
				if (serverInputTimeFromHourField.getCaretPosition() >= 3) {
					try {
						Integer.parseInt(serverInputTimeFromHourField.getText());
						try {
							String a = serverInputTimeFromHourField.getText(2,
									1);
							serverInputTimeFromMinuteField.requestFocus();
							serverInputTimeFromHourField
									.setText(serverInputTimeFromHourField
											.getText(0, 2));
							serverInputTimeFromMinuteField.setText(a);
						} catch (BadLocationException e) {
							e.printStackTrace();
						}
					} catch (NumberFormatException eee) {
						JOptionPane.showMessageDialog(ChatHistoryFrame.this,
								"输入不能为空且必须为数字哦!", "内容格式不正确",
								JOptionPane.WARNING_MESSAGE);
						serverInputTimeFromHourField.setText("");
						serverInputTimeFromHourField.requestFocus();
					}
				}
				if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
					try {
						Integer.parseInt(serverInputTimeFromHourField.getText());
						serverInputTimeFromMinuteField.selectAll();
						serverInputTimeFromMinuteField.requestFocus();
					} catch (NumberFormatException eee) {
						JOptionPane.showMessageDialog(ChatHistoryFrame.this,
								"输入不能为空且必须为数字哦!", "内容格式不正确",
								JOptionPane.WARNING_MESSAGE);
						serverInputTimeFromHourField.setText("");
						serverInputTimeFromHourField.requestFocus();
					}
				}
			}
		});
		serverInputTimeFromMinuteField.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				serverInputTimeFromMinuteField.selectAll();
			}
		});
		serverInputTimeFromMinuteField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent ke1) {
				if (ke1.getKeyCode() == KeyEvent.VK_ENTER) {
					String str = serverInputTimeFromMinuteField.getText()
							.trim();
					if (str.isEmpty()) {
						JOptionPane.showMessageDialog(ChatHistoryFrame.this,
								"输入不能为空,或不能全为空格哦!", "内容格式不正确",
								JOptionPane.WARNING_MESSAGE);
					}
				}
			}

			public void keyReleased(KeyEvent ke) {
				if (serverInputTimeFromMinuteField.getCaretPosition() >= 3) {
					try {
						Integer.parseInt(serverInputTimeFromMinuteField
								.getText());
						try {
							String a = serverInputTimeFromMinuteField.getText(
									2, 1);
							serverInputTimeFromSecondField.requestFocus();
							serverInputTimeFromMinuteField
									.setText(serverInputTimeFromMinuteField
											.getText(0, 2));
							serverInputTimeFromSecondField.setText(a);
						} catch (BadLocationException e) {
							e.printStackTrace();
						}
					} catch (NumberFormatException eee) {
						JOptionPane.showMessageDialog(ChatHistoryFrame.this,
								"输入不能为空且必须为数字哦!", "内容格式不正确",
								JOptionPane.WARNING_MESSAGE);
						serverInputTimeFromMinuteField.setText("");
						serverInputTimeFromMinuteField.requestFocus();
					}
				}
				if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
					try {
						Integer.parseInt(serverInputTimeFromMinuteField
								.getText());
						serverInputTimeFromSecondField.selectAll();
						serverInputTimeFromSecondField.requestFocus();
					} catch (NumberFormatException eee) {
						JOptionPane.showMessageDialog(ChatHistoryFrame.this,
								"输入不能为空且必须为数字哦!", "内容格式不正确",
								JOptionPane.WARNING_MESSAGE);
						serverInputTimeFromMinuteField.setText("");
						serverInputTimeFromMinuteField.requestFocus();
					}
				}
			}
		});
		serverInputTimeFromSecondField.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				serverInputTimeFromSecondField.selectAll();
			}
		});
		serverInputTimeFromSecondField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent ke1) {
				if (ke1.getKeyCode() == KeyEvent.VK_ENTER) {
					String str = serverInputTimeFromSecondField.getText()
							.trim();
					if (str.isEmpty()) {
						JOptionPane.showMessageDialog(ChatHistoryFrame.this,
								"输入不能为空,或不能全为空格哦!", "内容格式不正确",
								JOptionPane.WARNING_MESSAGE);
					}
				}
			}

			public void keyReleased(KeyEvent ke) {
				if (serverInputTimeFromSecondField.getCaretPosition() >= 3) {
					try {
						Integer.parseInt(serverInputTimeFromSecondField
								.getText());
						try {
							String a = serverInputTimeFromSecondField.getText(
									2, 1);
							serverInputTimeToYearField.requestFocus();
							serverInputTimeFromSecondField
									.setText(serverInputTimeFromSecondField
											.getText(0, 2));
							serverInputTimeToYearField.setText(a);
						} catch (BadLocationException e) {
							e.printStackTrace();
						}
					} catch (NumberFormatException eee) {
						JOptionPane.showMessageDialog(ChatHistoryFrame.this,
								"输入不能为空且必须为数字哦!", "内容格式不正确",
								JOptionPane.WARNING_MESSAGE);
						serverInputTimeFromSecondField.setText("");
						serverInputTimeFromSecondField.requestFocus();
					}
				}
				if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
					try {
						Integer.parseInt(serverInputTimeFromSecondField
								.getText());
						serverInputTimeToYearField.selectAll();
						serverInputTimeToYearField.requestFocus();
					} catch (NumberFormatException eee) {
						JOptionPane.showMessageDialog(ChatHistoryFrame.this,
								"输入不能为空且必须为数字哦!", "内容格式不正确",
								JOptionPane.WARNING_MESSAGE);
						serverInputTimeFromSecondField.setText("");
						serverInputTimeFromSecondField.requestFocus();
					}
				}
			}
		});
		serverInputTimeToYearField.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				serverInputTimeToYearField.selectAll();
			}
		});
		serverInputTimeToYearField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent ke1) {
				if (ke1.getKeyCode() == KeyEvent.VK_ENTER) {
					String str = serverInputTimeToYearField.getText().trim();
					if (str.isEmpty()) {
						JOptionPane.showMessageDialog(ChatHistoryFrame.this,
								"输入不能为空,或不能全为空格哦!", "内容格式不正确",
								JOptionPane.WARNING_MESSAGE);
					}
				}
			}

			public void keyReleased(KeyEvent ke) {
				if (serverInputTimeToYearField.getCaretPosition() >= 5) {
					try {
						String a = serverInputTimeToYearField.getText(4, 1);
						serverInputTimeToMonthField.requestFocus();
						serverInputTimeToYearField
								.setText(serverInputTimeToYearField.getText(0,
										4));
						try {
							SimpleDateFormat dateFormatA = new SimpleDateFormat(
									"yyyy");
							java.util.Calendar cal = java.util.Calendar
									.getInstance();
							String nowYearStrA = dateFormatA.format(cal
									.getTime());
							if ((Integer.parseInt(serverInputTimeToYearField
									.getText(0, 4)) < 0)
									|| (Integer
											.parseInt(serverInputTimeToYearField
													.getText(0, 4)) > Integer
											.parseInt(nowYearStrA))) {
								JOptionPane.showMessageDialog(
										ChatHistoryFrame.this,
										"输入错误，必须不大于当前哦！", "内容格式不正确",
										JOptionPane.WARNING_MESSAGE);
								serverInputTimeToYearField.selectAll();
								serverInputTimeToYearField.requestFocus();
							} else {
								serverInputTimeToMonthField.requestFocus();
								serverInputTimeToMonthField.setText(a);
							}
						} catch (NumberFormatException eee) {
							JOptionPane.showMessageDialog(
									ChatHistoryFrame.this, "输入不能为空且必须为数字哦!",
									"内容格式不正确", JOptionPane.WARNING_MESSAGE);
							serverInputTimeToYearField.setText("");
							serverInputTimeToYearField.requestFocus();
						}
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
				}
				if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
					try {
						try {
							SimpleDateFormat dateFormatA = new SimpleDateFormat(
									"yyyy");
							java.util.Calendar cal = java.util.Calendar
									.getInstance();
							String nowYearStrA = dateFormatA.format(cal
									.getTime());
							if ((Integer.parseInt(serverInputTimeToYearField
									.getText(0, 4)) < 0)
									|| (Integer
											.parseInt(serverInputTimeToYearField
													.getText(0, 4)) > Integer
											.parseInt(nowYearStrA))) {
								JOptionPane.showMessageDialog(
										ChatHistoryFrame.this,
										"输入错误，必须不大于当前哦！", "内容格式不正确",
										JOptionPane.WARNING_MESSAGE);
								serverInputTimeToYearField.selectAll();
								serverInputTimeToYearField.requestFocus();
							} else {
								serverInputTimeToMonthField.selectAll();
								serverInputTimeToMonthField.requestFocus();
							}
						} catch (NumberFormatException eee) {
							JOptionPane.showMessageDialog(
									ChatHistoryFrame.this, "输入必须为数字哦!",
									"内容格式不正确", JOptionPane.WARNING_MESSAGE);
							serverInputTimeToYearField.setText("");
							serverInputTimeToYearField.requestFocus();
						}
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
				}
			}
		});
		serverInputTimeToMonthField.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				serverInputTimeToMonthField.selectAll();
			}
		});
		serverInputTimeToMonthField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent ke1) {
				if (ke1.getKeyCode() == KeyEvent.VK_ENTER) {
					String str = serverInputTimeToMonthField.getText().trim();
					if (str.isEmpty()) {
						JOptionPane.showMessageDialog(ChatHistoryFrame.this,
								"输入不能为空,或不能全为空格哦!", "内容格式不正确",
								JOptionPane.WARNING_MESSAGE);
					}
				}
			}

			public void keyReleased(KeyEvent ke) {
				if (serverInputTimeToMonthField.getCaretPosition() >= 3) {
					try {
						Integer.parseInt(serverInputTimeToMonthField.getText());
						try {
							String a = serverInputTimeToMonthField
									.getText(2, 1);
							serverInputTimeToDayField.requestFocus();
							serverInputTimeToMonthField
									.setText(serverInputTimeToMonthField
											.getText(0, 2));
							serverInputTimeToDayField.setText(a);
						} catch (BadLocationException e) {
							e.printStackTrace();
						}
					} catch (NumberFormatException eee) {
						JOptionPane.showMessageDialog(ChatHistoryFrame.this,
								"输入必须为数字哦!", "内容格式不正确",
								JOptionPane.WARNING_MESSAGE);
						serverInputTimeToMonthField.setText("");
						serverInputTimeToMonthField.requestFocus();
					}
				}
				if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
					try {
						Integer.parseInt(serverInputTimeToMonthField.getText());
						serverInputTimeToDayField.selectAll();
						serverInputTimeToDayField.requestFocus();
					} catch (NumberFormatException eee) {
						JOptionPane.showMessageDialog(ChatHistoryFrame.this,
								"输入必须为数字哦!", "内容格式不正确",
								JOptionPane.WARNING_MESSAGE);
						serverInputTimeToMonthField.setText("");
						serverInputTimeToMonthField.requestFocus();
					}
				}
			}
		});
		serverInputTimeToDayField.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				serverInputTimeToDayField.selectAll();
			}
		});
		serverInputTimeToDayField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent ke1) {
				if (ke1.getKeyCode() == KeyEvent.VK_ENTER) {
					String str = serverInputTimeToDayField.getText().trim();
					if (str.isEmpty()) {
						JOptionPane.showMessageDialog(ChatHistoryFrame.this,
								"输入不能为空,或不能全为空格哦!", "内容格式不正确",
								JOptionPane.WARNING_MESSAGE);
					}
				}
			}

			public void keyReleased(KeyEvent ke) {
				if (serverInputTimeToDayField.getCaretPosition() >= 3) {
					try {
						Integer.parseInt(serverInputTimeToDayField.getText());
						try {
							String a = serverInputTimeToDayField.getText(2, 1);
							serverInputTimeToHourField.requestFocus();
							serverInputTimeToDayField
									.setText(serverInputTimeToDayField.getText(
											0, 2));
							serverInputTimeToHourField.setText(a);
						} catch (BadLocationException e) {
							e.printStackTrace();
						}
					} catch (NumberFormatException eee) {
						JOptionPane.showMessageDialog(ChatHistoryFrame.this,
								"输入必须为数字哦!", "内容格式不正确",
								JOptionPane.WARNING_MESSAGE);
						serverInputTimeToDayField.setText("");
						serverInputTimeToDayField.requestFocus();
					}
				}
				if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
					try {
						Integer.parseInt(serverInputTimeToDayField.getText());
						serverInputTimeToHourField.selectAll();
						serverInputTimeToHourField.requestFocus();
					} catch (NumberFormatException eee) {
						JOptionPane.showMessageDialog(ChatHistoryFrame.this,
								"输入必须为数字哦!", "内容格式不正确",
								JOptionPane.WARNING_MESSAGE);
						serverInputTimeToDayField.setText("");
						serverInputTimeToDayField.requestFocus();
					}
				}
			}
		});
		serverInputTimeToHourField.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				serverInputTimeToHourField.selectAll();
			}
		});
		serverInputTimeToHourField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent ke1) {
				if (ke1.getKeyCode() == KeyEvent.VK_ENTER) {
					String str = serverInputTimeToHourField.getText().trim();
					if (str.isEmpty()) {
						JOptionPane.showMessageDialog(ChatHistoryFrame.this,
								"输入不能为空,或不能全为空格哦!", "内容格式不正确",
								JOptionPane.WARNING_MESSAGE);
					}
				}
			}

			public void keyReleased(KeyEvent ke) {
				if (serverInputTimeToHourField.getCaretPosition() >= 3) {
					try {
						Integer.parseInt(serverInputTimeToHourField.getText());
						try {
							String a = serverInputTimeToHourField.getText(2, 1);
							serverInputTimeToMinuteField.requestFocus();
							serverInputTimeToHourField
									.setText(serverInputTimeToHourField
											.getText(0, 2));
							serverInputTimeToMinuteField.setText(a);
						} catch (BadLocationException e) {
							e.printStackTrace();
						}
					} catch (NumberFormatException eee) {
						JOptionPane.showMessageDialog(ChatHistoryFrame.this,
								"输入必须为数字哦!", "内容格式不正确",
								JOptionPane.WARNING_MESSAGE);
						serverInputTimeToHourField.setText("");
						serverInputTimeToHourField.requestFocus();
					}
				}
				if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
					try {
						Integer.parseInt(serverInputTimeToHourField.getText());
						serverInputTimeToMinuteField.selectAll();
						serverInputTimeToMinuteField.requestFocus();
					} catch (NumberFormatException eee) {
						JOptionPane.showMessageDialog(ChatHistoryFrame.this,
								"输入必须为数字哦!", "内容格式不正确",
								JOptionPane.WARNING_MESSAGE);
						serverInputTimeToHourField.setText("");
						serverInputTimeToHourField.requestFocus();
					}
				}
			}
		});
		serverInputTimeToMinuteField.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				serverInputTimeToMinuteField.selectAll();
			}
		});
		serverInputTimeToMinuteField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent ke1) {
				if (ke1.getKeyCode() == KeyEvent.VK_ENTER) {
					String str = serverInputTimeToMinuteField.getText().trim();
					if (str.isEmpty()) {
						JOptionPane.showMessageDialog(ChatHistoryFrame.this,
								"输入不能为空,或不能全为空格哦!", "内容格式不正确",
								JOptionPane.WARNING_MESSAGE);
					}
				}
			}

			public void keyReleased(KeyEvent ke) {
				if (serverInputTimeToMinuteField.getCaretPosition() >= 3) {
					try {
						Integer.parseInt(serverInputTimeToMinuteField.getText());
						try {
							String a = serverInputTimeToMinuteField.getText(2,
									1);
							serverInputTimeToSecondField.requestFocus();
							serverInputTimeToMinuteField
									.setText(serverInputTimeToMinuteField
											.getText(0, 2));
							serverInputTimeToSecondField.setText(a);
						} catch (BadLocationException e) {
							e.printStackTrace();
						}
					} catch (NumberFormatException eee) {
						JOptionPane.showMessageDialog(ChatHistoryFrame.this,
								"输入必须为数字哦!", "内容格式不正确",
								JOptionPane.WARNING_MESSAGE);
						serverInputTimeToMinuteField.setText("");
						serverInputTimeToMinuteField.requestFocus();
					}
				}
				if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
					try {
						Integer.parseInt(serverInputTimeToMinuteField.getText());
						serverInputTimeToSecondField.selectAll();
						serverInputTimeToSecondField.requestFocus();
					} catch (NumberFormatException eee) {
						JOptionPane.showMessageDialog(ChatHistoryFrame.this,
								"输入必须为数字哦!", "内容格式不正确",
								JOptionPane.WARNING_MESSAGE);
						serverInputTimeToMinuteField.setText("");
						serverInputTimeToMinuteField.requestFocus();
					}
				}
			}
		});
		serverInputTimeToSecondField.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				serverInputTimeToSecondField.selectAll();
			}
		});
		serverInputTimeToSecondField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent ke1) {
				if (ke1.getKeyCode() == KeyEvent.VK_ENTER) {
					String str = serverInputTimeToSecondField.getText().trim();
					if (str.isEmpty()) {
						JOptionPane.showMessageDialog(ChatHistoryFrame.this,
								"输入不能为空,或不能全为空格哦!", "内容格式不正确",
								JOptionPane.WARNING_MESSAGE);
					}
				}
			}

			public void keyReleased(KeyEvent ke) {
				if (serverInputTimeToSecondField.getCaretPosition() >= 3) {
					try {
						Integer.parseInt(serverInputTimeToSecondField.getText());
						try {
							String a = serverInputTimeToSecondField.getText(2,
									1);
							usernameTextField.requestFocus();
							serverInputTimeToSecondField
									.setText(serverInputTimeToSecondField
											.getText(0, 2));
							usernameTextField.setText(a);
						} catch (BadLocationException e) {
							e.printStackTrace();
						}
					} catch (NumberFormatException eee) {
						JOptionPane.showMessageDialog(ChatHistoryFrame.this,
								"输入必须为数字哦!", "内容格式不正确",
								JOptionPane.WARNING_MESSAGE);
						serverInputTimeToSecondField.setText("");
						serverInputTimeToSecondField.requestFocus();
					}
				}
				if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
					try {
						Integer.parseInt(serverInputTimeToSecondField.getText());
						usernameTextField.selectAll();
						usernameTextField.requestFocus();
					} catch (NumberFormatException eee) {
						JOptionPane.showMessageDialog(ChatHistoryFrame.this,
								"输入必须为数字哦!", "内容格式不正确",
								JOptionPane.WARNING_MESSAGE);
						serverInputTimeToSecondField.setText("");
						serverInputTimeToSecondField.requestFocus();
					}
				}
			}
		});
		usernameTextField.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				usernameTextField.selectAll();
			}
		});
		usernameTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				historyFromServerSideBS.delete(
						(systemInfo + "getHistoryByTime").length(),
						historyFromServerSideBS.length());
				if ((serverInputTimeFromYearField.getText().trim()).length() > 0
						&& (serverInputTimeFromMonthField.getText().trim())
								.length() > 0
						&& (serverInputTimeFromDayField.getText().trim())
								.length() > 0
						&& (serverInputTimeFromHourField.getText().trim())
								.length() > 0
						&& (serverInputTimeFromMinuteField.getText().trim())
								.length() > 0
						&& (serverInputTimeFromSecondField.getText().trim())
								.length() > 0
						&& (serverInputTimeToYearField.getText().trim())
								.length() > 0
						&& (serverInputTimeToMonthField.getText().trim())
								.length() > 0
						&& (serverInputTimeToDayField.getText().trim())
								.length() > 0
						&& (serverInputTimeToHourField.getText().trim())
								.length() > 0
						&& (serverInputTimeToMinuteField.getText().trim())
								.length() > 0
						&& (serverInputTimeToSecondField.getText().trim())
								.length() > 0) {
					String fromYearStr = serverInputTimeFromYearField.getText()
							.trim();
					if ((serverInputTimeFromYearField.getText().trim())
							.length() == 1) {
						fromYearStr = "000"
								+ serverInputTimeFromYearField.getText().trim();
					} else if ((serverInputTimeFromYearField.getText().trim())
							.length() == 2) {
						fromYearStr = "00"
								+ serverInputTimeFromYearField.getText().trim();
					} else if ((serverInputTimeFromYearField.getText().trim())
							.length() == 3) {
						fromYearStr = "0"
								+ serverInputTimeFromYearField.getText().trim();
					}
					String fromMonthStr = serverInputTimeFromMonthField
							.getText().trim();
					if ((serverInputTimeFromMonthField.getText().trim())
							.length() == 1) {
						fromMonthStr = "0"
								+ serverInputTimeFromMonthField.getText()
										.trim();
					}
					String fromDayStr = serverInputTimeFromDayField.getText()
							.trim();
					if ((serverInputTimeFromDayField.getText().trim()).length() == 1) {
						fromDayStr = "0"
								+ serverInputTimeFromDayField.getText().trim();
					}
					String fromHourStr = serverInputTimeFromHourField.getText()
							.trim();
					if ((serverInputTimeFromHourField.getText().trim())
							.length() == 1) {
						fromHourStr = "0"
								+ serverInputTimeFromHourField.getText().trim();
					}
					String fromMinuteStr = serverInputTimeFromMinuteField
							.getText().trim();
					if ((serverInputTimeFromMinuteField.getText().trim())
							.length() == 1) {
						fromMinuteStr = "0"
								+ serverInputTimeFromMinuteField.getText()
										.trim();
					}
					String fromSecondStr = serverInputTimeFromSecondField
							.getText().trim();
					if ((serverInputTimeFromSecondField.getText().trim())
							.length() == 1) {
						fromSecondStr = "0"
								+ serverInputTimeFromSecondField.getText()
										.trim();
					}
					String toYearStr = serverInputTimeToYearField.getText()
							.trim();
					if ((serverInputTimeToYearField.getText().trim()).length() == 1) {
						toYearStr = "000"
								+ serverInputTimeToYearField.getText().trim();
					} else if ((serverInputTimeToYearField.getText().trim())
							.length() == 2) {
						toYearStr = "00"
								+ serverInputTimeToYearField.getText().trim();
					} else if ((serverInputTimeToYearField.getText().trim())
							.length() == 3) {
						toYearStr = "0"
								+ serverInputTimeToYearField.getText().trim();
					}
					String toMonthStr = serverInputTimeToMonthField.getText()
							.trim();
					if ((serverInputTimeToMonthField.getText().trim()).length() == 1) {
						toMonthStr = "0"
								+ serverInputTimeToMonthField.getText().trim();
					}
					String toDayStr = serverInputTimeToDayField.getText()
							.trim();
					if ((serverInputTimeToDayField.getText().trim()).length() == 1) {
						toDayStr = "0"
								+ serverInputTimeToDayField.getText().trim();
					}
					String toHourStr = serverInputTimeToHourField.getText()
							.trim();
					if ((serverInputTimeToHourField.getText().trim()).length() == 1) {
						toHourStr = "0"
								+ serverInputTimeToHourField.getText().trim();
					}
					String toMinuteStr = serverInputTimeToMinuteField.getText()
							.trim();
					if ((serverInputTimeToMinuteField.getText().trim())
							.length() == 1) {
						toMinuteStr = "0"
								+ serverInputTimeToMinuteField.getText().trim();
					}
					String toSecondStr = serverInputTimeToSecondField.getText()
							.trim();
					if ((serverInputTimeToSecondField.getText().trim())
							.length() == 1) {
						toSecondStr = "0"
								+ serverInputTimeToSecondField.getText().trim();
					}
					String formTimeStr = fromYearStr + "-" + fromMonthStr + "-"
							+ fromDayStr + " " + fromHourStr + ":"
							+ fromMinuteStr + ":" + fromSecondStr;
					String toTimeStr = toYearStr + "-" + toMonthStr + "-"
							+ toDayStr + " " + toHourStr + ":" + toMinuteStr
							+ ":" + toSecondStr;
					java.util.Date dateNowax = new java.util.Date();
					SimpleDateFormat dateFormatAx = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					String timeNowa = dateFormatAx.format(dateNowax);
					if ((formTimeStr.compareTo(toTimeStr) < 0)
							&& (toTimeStr.compareTo(timeNowa) <= 0)) {
						try {
							joyuClientSocket.sendString(usernameTextField
									.getText().trim()
									+ "username"
									+ formTimeStr
									+ systemInfo
									+ toTimeStr
									+ systemInfo + "getHistoryByTime");
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					} else {
						JOptionPane.showMessageDialog(ChatHistoryFrame.this,
								"截止时间应不大于当前时间并且必须大于起始时间哦！请重新输入吧！", "时间内容错误",
								JOptionPane.WARNING_MESSAGE);
					}
					ChatHistoryFrame.this.requestFocus();
				}
			}
		});
		panelTimeTo.add(serverInputTimeTo);
		panelTimeTo.add(serverInputTimeToYearField);
		panelTimeTo.add(serverInputTimeToYearLabel);
		panelTimeTo.add(serverInputTimeToMonthField);
		panelTimeTo.add(serverInputTimeToMonthLabel);
		panelTimeTo.add(serverInputTimeToDayField);
		panelTimeTo.add(serverInputTimeToDayLabel);
		panelTimeTo.add(serverInputTimeToHourField);
		panelTimeTo.add(serverInputTimeToHourLabel);
		panelTimeTo.add(serverInputTimeToMinuteField);
		panelTimeTo.add(serverInputTimeToMinuteLabel);
		panelTimeTo.add(serverInputTimeToSecondField);
		panelTimeTo.add(serverInputTimeToSecondLabel);
		panelTimeTo.add(usernameLable);
		panelTimeTo.add(usernameTextField);
		panelTimeTo.add(serverSearchButton);
		threeDaysButton.setFont(new Font("system", Font.PLAIN, 12));
		threeDaysButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
				SimpleDateFormat dateFormat1 = new SimpleDateFormat("MM");
				SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd");
				SimpleDateFormat dateFormat3 = new SimpleDateFormat("HH");
				SimpleDateFormat dateFormat4 = new SimpleDateFormat("mm");
				SimpleDateFormat dateFormat5 = new SimpleDateFormat("ss");
				java.util.Calendar ca = java.util.Calendar.getInstance();
				ca.add(Calendar.DAY_OF_MONTH, -3);
				String fromYearStr = dateFormat.format(ca.getTime());
				String fromMonthStr = dateFormat1.format(ca.getTime());
				String fromDayStr = dateFormat2.format(ca.getTime());
				String fromHourStr = dateFormat3.format(ca.getTime());
				String fromMinuteStr = dateFormat4.format(ca.getTime());
				String fromSecondStr = dateFormat5.format(ca.getTime());
				serverInputTimeFromYearField.setText(fromYearStr);
				serverInputTimeFromMonthField.setText(fromMonthStr);
				serverInputTimeFromDayField.setText(fromDayStr);
				serverInputTimeFromHourField.setText(fromHourStr);
				serverInputTimeFromMinuteField.setText(fromMinuteStr);
				serverInputTimeFromSecondField.setText(fromSecondStr);
				java.util.Date dateNow = new java.util.Date();
				String toYearStr = dateFormat.format(dateNow);
				String toMonthStr = dateFormat1.format(dateNow);
				String toDayStr = dateFormat2.format(dateNow);
				String toHourStr = dateFormat3.format(dateNow);
				String toMinuteStr = dateFormat4.format(dateNow);
				String toSecondStr = dateFormat5.format(dateNow);
				serverInputTimeToYearField.setText(toYearStr);
				serverInputTimeToMonthField.setText(toMonthStr);
				serverInputTimeToDayField.setText(toDayStr);
				serverInputTimeToHourField.setText(toHourStr);
				serverInputTimeToMinuteField.setText(toMinuteStr);
				serverInputTimeToSecondField.setText(toSecondStr);
			}
		});
		aWeekButton.setFont(new Font("system", Font.PLAIN, 12));
		aWeekButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
				SimpleDateFormat dateFormat1 = new SimpleDateFormat("MM");
				SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd");
				SimpleDateFormat dateFormat3 = new SimpleDateFormat("HH");
				SimpleDateFormat dateFormat4 = new SimpleDateFormat("mm");
				SimpleDateFormat dateFormat5 = new SimpleDateFormat("ss");
				java.util.Calendar ca = java.util.Calendar.getInstance();
				ca.add(Calendar.DAY_OF_MONTH, -7);
				String fromYearStr = dateFormat.format(ca.getTime());
				String fromMonthStr = dateFormat1.format(ca.getTime());
				String fromDayStr = dateFormat2.format(ca.getTime());
				String fromHourStr = dateFormat3.format(ca.getTime());
				String fromMinuteStr = dateFormat4.format(ca.getTime());
				String fromSecondStr = dateFormat5.format(ca.getTime());
				serverInputTimeFromYearField.setText(fromYearStr);
				serverInputTimeFromMonthField.setText(fromMonthStr);
				serverInputTimeFromDayField.setText(fromDayStr);
				serverInputTimeFromHourField.setText(fromHourStr);
				serverInputTimeFromMinuteField.setText(fromMinuteStr);
				serverInputTimeFromSecondField.setText(fromSecondStr);
				java.util.Date dateNow = new java.util.Date();
				String toYearStr = dateFormat.format(dateNow);
				String toMonthStr = dateFormat1.format(dateNow);
				String toDayStr = dateFormat2.format(dateNow);
				String toHourStr = dateFormat3.format(dateNow);
				String toMinuteStr = dateFormat4.format(dateNow);
				String toSecondStr = dateFormat5.format(dateNow);
				serverInputTimeToYearField.setText(toYearStr);
				serverInputTimeToMonthField.setText(toMonthStr);
				serverInputTimeToDayField.setText(toDayStr);
				serverInputTimeToHourField.setText(toHourStr);
				serverInputTimeToMinuteField.setText(toMinuteStr);
				serverInputTimeToSecondField.setText(toSecondStr);
			}
		});
		aMonthButton.setFont(new Font("system", Font.PLAIN, 12));
		aMonthButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
				SimpleDateFormat dateFormat1 = new SimpleDateFormat("MM");
				SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd");
				SimpleDateFormat dateFormat3 = new SimpleDateFormat("HH");
				SimpleDateFormat dateFormat4 = new SimpleDateFormat("mm");
				SimpleDateFormat dateFormat5 = new SimpleDateFormat("ss");
				java.util.Calendar ca = java.util.Calendar.getInstance();
				ca.add(Calendar.DAY_OF_MONTH, -30);
				String fromYearStr = dateFormat.format(ca.getTime());
				String fromMonthStr = dateFormat1.format(ca.getTime());
				String fromDayStr = dateFormat2.format(ca.getTime());
				String fromHourStr = dateFormat3.format(ca.getTime());
				String fromMinuteStr = dateFormat4.format(ca.getTime());
				String fromSecondStr = dateFormat5.format(ca.getTime());
				serverInputTimeFromYearField.setText(fromYearStr);
				serverInputTimeFromMonthField.setText(fromMonthStr);
				serverInputTimeFromDayField.setText(fromDayStr);
				serverInputTimeFromHourField.setText(fromHourStr);
				serverInputTimeFromMinuteField.setText(fromMinuteStr);
				serverInputTimeFromSecondField.setText(fromSecondStr);
				java.util.Date dateNow = new java.util.Date();
				String toYearStr = dateFormat.format(dateNow);
				String toMonthStr = dateFormat1.format(dateNow);
				String toDayStr = dateFormat2.format(dateNow);
				String toHourStr = dateFormat3.format(dateNow);
				String toMinuteStr = dateFormat4.format(dateNow);
				String toSecondStr = dateFormat5.format(dateNow);
				serverInputTimeToYearField.setText(toYearStr);
				serverInputTimeToMonthField.setText(toMonthStr);
				serverInputTimeToDayField.setText(toDayStr);
				serverInputTimeToHourField.setText(toHourStr);
				serverInputTimeToMinuteField.setText(toMinuteStr);
				serverInputTimeToSecondField.setText(toSecondStr);
			}
		});
		sinceButton.setFont(new Font("system", Font.PLAIN, 12));
		sinceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
				SimpleDateFormat dateFormat1 = new SimpleDateFormat("MM");
				SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd");
				SimpleDateFormat dateFormat3 = new SimpleDateFormat("HH");
				SimpleDateFormat dateFormat4 = new SimpleDateFormat("mm");
				SimpleDateFormat dateFormat5 = new SimpleDateFormat("ss");
				String fromYearStr = "2009";
				String fromMonthStr = "01";
				String fromDayStr = "01";
				String fromHourStr = "00";
				String fromMinuteStr = "00";
				String fromSecondStr = "00";
				serverInputTimeFromYearField.setText(fromYearStr);
				serverInputTimeFromMonthField.setText(fromMonthStr);
				serverInputTimeFromDayField.setText(fromDayStr);
				serverInputTimeFromHourField.setText(fromHourStr);
				serverInputTimeFromMinuteField.setText(fromMinuteStr);
				serverInputTimeFromSecondField.setText(fromSecondStr);
				java.util.Date dateNow = new java.util.Date();
				String toYearStr = dateFormat.format(dateNow);
				String toMonthStr = dateFormat1.format(dateNow);
				String toDayStr = dateFormat2.format(dateNow);
				String toHourStr = dateFormat3.format(dateNow);
				String toMinuteStr = dateFormat4.format(dateNow);
				String toSecondStr = dateFormat5.format(dateNow);
				serverInputTimeToYearField.setText(toYearStr);
				serverInputTimeToMonthField.setText(toMonthStr);
				serverInputTimeToDayField.setText(toDayStr);
				serverInputTimeToHourField.setText(toHourStr);
				serverInputTimeToMinuteField.setText(toMinuteStr);
				serverInputTimeToSecondField.setText(toSecondStr);
			}
		});
		panelServerDownloadByTime.add(panelTimeFrom);
		panelServerDownloadByTime.add(panelTimeTo);
		JPanel paneHistoryFromServerByTimePanel = new JPanel();
		paneHistoryFromServerByTimePanel
				.setBackground(new Color(180, 230, 147));
		paneHistoryFromServerByTimePanel.setLayout(new BorderLayout());
		historyFromServerArea.setBackground(new Color(204, 232, 207));
		historyFromServerArea.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				historyFromServerArea.setEditable(false);
				historyFromServerArea.setBackground(new Color(204, 232, 207));
			}

			public void focusLost(FocusEvent e) {
				historyFromServerArea.setEditable(true);
			}
		});
		JPanel historyFromServerBarPanel = new JPanel();
		historyFromServerBarPanel.setLayout(new BorderLayout());
		historyFromServerBarPanel.setBackground(new Color(180, 230, 147));
		JLabel labelResultFromServer = new JLabel(" 下载结果: ");
		labelResultFromServer.setFont(new Font("system", Font.PLAIN, 12));
		historyFromServerBarPanel.add(labelResultFromServer, BorderLayout.WEST);
		JButton buttonFreshResultFromServer = new RButton(0, "刷新");
		buttonFreshResultFromServer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				historyFromServerSideBS.delete(
						(systemInfo + "getHistoryByTime").length(),
						historyFromServerSideBS.length());
				if ((serverInputTimeFromYearField.getText().trim()).length() > 0
						&& (serverInputTimeFromMonthField.getText().trim())
								.length() > 0
						&& (serverInputTimeFromDayField.getText().trim())
								.length() > 0
						&& (serverInputTimeFromHourField.getText().trim())
								.length() > 0
						&& (serverInputTimeFromMinuteField.getText().trim())
								.length() > 0
						&& (serverInputTimeFromSecondField.getText().trim())
								.length() > 0) {
					String fromYearStr = serverInputTimeFromYearField.getText()
							.trim();
					if ((serverInputTimeFromYearField.getText().trim())
							.length() == 1) {
						fromYearStr = "000"
								+ serverInputTimeFromYearField.getText().trim();
					} else if ((serverInputTimeFromYearField.getText().trim())
							.length() == 2) {
						fromYearStr = "00"
								+ serverInputTimeFromYearField.getText().trim();
					} else if ((serverInputTimeFromYearField.getText().trim())
							.length() == 3) {
						fromYearStr = "0"
								+ serverInputTimeFromYearField.getText().trim();
					}
					String fromMonthStr = serverInputTimeFromMonthField
							.getText().trim();
					if ((serverInputTimeFromMonthField.getText().trim())
							.length() == 1) {
						fromMonthStr = "0"
								+ serverInputTimeFromMonthField.getText()
										.trim();
					}
					String fromDayStr = serverInputTimeFromDayField.getText()
							.trim();
					if ((serverInputTimeFromDayField.getText().trim()).length() == 1) {
						fromDayStr = "0"
								+ serverInputTimeFromDayField.getText().trim();
					}
					String fromHourStr = serverInputTimeFromHourField.getText()
							.trim();
					if ((serverInputTimeFromHourField.getText().trim())
							.length() == 1) {
						fromHourStr = "0"
								+ serverInputTimeFromHourField.getText().trim();
					}
					String fromMinuteStr = serverInputTimeFromMinuteField
							.getText().trim();
					if ((serverInputTimeFromMinuteField.getText().trim())
							.length() == 1) {
						fromMinuteStr = "0"
								+ serverInputTimeFromMinuteField.getText()
										.trim();
					}
					String fromSecondStr = serverInputTimeFromSecondField
							.getText().trim();
					if ((serverInputTimeFromSecondField.getText().trim())
							.length() == 1) {
						fromSecondStr = "0"
								+ serverInputTimeFromSecondField.getText()
										.trim();
					}
					String formTimeStr = fromYearStr + "-" + fromMonthStr + "-"
							+ fromDayStr + " " + fromHourStr + ":"
							+ fromMinuteStr + ":" + fromSecondStr;
					java.util.Date dateNowax = new java.util.Date();
					SimpleDateFormat dateFormatAx = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					String timeNowa = dateFormatAx.format(dateNowax);
					if ((formTimeStr.compareTo(timeNowa) <= 0)) {
						try {
							joyuClientSocket.sendString(usernameTextField
									.getText().trim()
									+ "username"
									+ formTimeStr
									+ systemInfo
									+ timeNowa
									+ systemInfo + "getHistoryByTime");
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					} else {
						JOptionPane.showMessageDialog(ChatHistoryFrame.this,
								"截止时间应不大于当前时间并且必须大于起始时间哦！请重新输入吧！", "时间内容错误",
								JOptionPane.WARNING_MESSAGE);
					}
					ChatHistoryFrame.this.requestFocus();
				}
			}
		});
		JButton buttonExportToFilesResultFromServer = new RButton(0, "导出...");
		buttonExportToFilesResultFromServer
				.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						JFileChooser chooser = new JFileChooser(".");
						int ret = chooser.showSaveDialog(ChatHistoryFrame.this);
						if (ret == JFileChooser.APPROVE_OPTION) {
							try {
								FileOutputStream fos = new FileOutputStream(
										chooser.getSelectedFile().getPath());
								DataOutputStream dos = new DataOutputStream(fos);
								dos.writeUTF(historyFromServerArea.getText());
								dos.flush();
								dos.close();
							} catch (FileNotFoundException e1) {
								e1.printStackTrace();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
					}
				});
		historyFromServerBarPanel.add(buttonFreshResultFromServer);
		historyFromServerBarPanel.add(buttonExportToFilesResultFromServer,
				BorderLayout.EAST);
		paneHistoryFromServerByTimePanel.add(historyFromServerBarPanel,
				BorderLayout.NORTH);
		paneHistoryFromServerByTimePanel.add(historyFromServerArea,
				BorderLayout.CENTER);
		JPanel searchResultFromServerSidePanel = new JPanel();
		searchResultFromServerSidePanel.setBackground(new Color(180, 230, 147));
		searchResultFromServerSidePanel.setLayout(new BorderLayout());
		searchResultAreab.setBackground(new Color(204, 232, 207));
		searchResultAreab.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				searchResultAreab.setEditable(false);
				searchResultAreab.setBackground(new Color(204, 232, 207));
			}

			public void focusLost(FocusEvent e) {
				searchResultAreab.setEditable(true);
			}
		});
		JPanel panelSearchb = new JPanel();
		panelSearchb.setBackground(new Color(180, 230, 147));
		panelSearchb.setLayout(new BorderLayout());
		JLabel LabelSearchStr = new JLabel(" 结果中找: ");
		LabelSearchStr.setFont(new Font("system", Font.PLAIN, 12));
		searchFieldb.setBackground(new Color(51, 184, 47));
		searchFieldb.setFont(new Font("system", Font.PLAIN, 12));
		searchFieldb.setText("在这里查找");
		searchFieldb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (searchFieldb.getText().trim().isEmpty()) {
					JOptionPane.showMessageDialog(ChatHistoryFrame.this,
							"输入不能为空哦!请重新输入...", "格式错误",
							JOptionPane.WARNING_MESSAGE);
					searchFieldb.requestFocus();
				} else {
					searchResultAreab
							.setText((getSearchResultFromResultFromServerArea(
									historyFromServerSideBS, searchFieldb
											.getText().trim())).toString());
					if (searchResultAreab.getText().trim().isEmpty()) {
						searchResultAreab.setText("没有查找到相关结果...");
					}
				}
			}
		});
		JButton searchButtonb = new RButton(0, "查找");
		searchButtonb.setFont(new Font("system", Font.PLAIN, 12));
		searchButtonb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (searchFieldb.getText().trim().isEmpty()) {
					JOptionPane.showMessageDialog(ChatHistoryFrame.this,
							"输入不能为空哦!请重新输入...", "格式错误",
							JOptionPane.WARNING_MESSAGE);
					searchFieldb.requestFocus();
				} else {
					searchResultAreab
							.setText((getSearchResultFromResultFromServerArea(
									historyFromServerSideBS, searchFieldb
											.getText().trim())).toString());
					if (searchResultAreab.getText().trim().isEmpty()) {
						searchResultAreab.setText("没有查找到相关结果...");
					}
				}
			}
		});
		panelSearchb.add(LabelSearchStr, BorderLayout.WEST);
		panelSearchb.add(searchFieldb);
		panelSearchb.add(searchButtonb, BorderLayout.EAST);
		searchResultFromServerSidePanel.add(panelSearchb, BorderLayout.NORTH);
		searchResultFromServerSidePanel.add(searchResultAreab,
				BorderLayout.CENTER);
		JSplitPane paneHistoryFromServerByTime = new JSplitPane();
		paneHistoryFromServerByTime.setForeground(new Color(180, 230, 147));
		paneHistoryFromServerByTime.setDividerSize(3);
		paneHistoryFromServerByTime.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		paneHistoryFromServerByTime.setDividerLocation(600);
		paneHistoryFromServerByTime
				.setLeftComponent(paneHistoryFromServerByTimePanel);
		paneHistoryFromServerByTime
				.setRightComponent(searchResultFromServerSidePanel);
		panelServerHistory
				.add(paneHistoryFromServerByTime, BorderLayout.CENTER);
		panelServerHistory.add(panelServerDownloadByTime, BorderLayout.NORTH);
		panelHistory.add(panelServerHistory, "2");
		this.add(panelHistory, BorderLayout.CENTER);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				dispose();
			}
		});
		this.add(panelHistory);
		this.setBounds(140, 100, 800, 600);
	}

	public TextArea getSearchResultAreab() {
		return searchResultAreab;
	}

	public TextArea getHistoryFromServerArea() {
		return historyFromServerArea;
	}

	public StringBuffer getHistoryFromServerSideBS() {
		return historyFromServerSideBS;
	}
}