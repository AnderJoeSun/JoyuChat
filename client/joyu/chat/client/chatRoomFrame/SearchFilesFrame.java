package joyu.chat.client.chatRoomFrame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import joyu.chat.client.FileBeanClientSide;
import joyu.chat.client.JoyuClientSocket;
import joyu.chat.client.JoyuTableModel;
import joyu.chat.client.JoyuTableSortShareFiles;
import joyu.chat.client.RButton;

public class SearchFilesFrame {
	private static final long serialVersionUID = 1L;
	private final ChatRoomFrame chatRoomFrame;
	private JoyuTableSortShareFiles joyuTableShareForSearch;
	private JTextField searchField;
	private JPanel searchPanel;
	private String[] columnNames;
	private String username;
	private JoyuClientSocket joyuClientSocket;
	private String systemInfo;
	private JTable table;
	// private Object[][] data;
	private JFrame searchFilesFrame;
	private Set<FileBeanClientSide> searchedFilesSet = new HashSet<FileBeanClientSide>();
	private String SayuServerIP;
	private String SayuServerport;

	public SearchFilesFrame(ChatRoomFrame chatRoomFrame, String[] columnNames,
			String username, JoyuClientSocket joyuClientSocket,
			String systemInfo, Image logoImage, String SayuServerIP,
			String SayuServerport) {
		this.chatRoomFrame = chatRoomFrame;
		this.columnNames = columnNames;
		this.joyuClientSocket = joyuClientSocket;
		this.username = username;
		this.systemInfo = systemInfo;
		this.SayuServerIP = SayuServerIP;
		this.SayuServerport = SayuServerport;
		searchFilesFrame = new JFrame("查找文件");
		searchFilesFrame.setIconImage(logoImage);
		show();
	}

	public void show() {
		searchPanel = new JPanel();
		searchPanel.setBackground(new Color(180, 230, 147));
		searchPanel.setLayout(new BorderLayout());
		JPanel deleteAndDownloadPanel = new JPanel();
		deleteAndDownloadPanel.setLayout(new GridLayout(1, 2));
		deleteAndDownloadPanel.setBackground(new Color(180, 230, 147));
		RButton buttonDeleteFile = new RButton(1, "删除");
		buttonDeleteFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (table != null) {
					int[] selectedRows = table.getSelectedRows();
					if (selectedRows.length == 0) {
						JOptionPane.showMessageDialog(searchFilesFrame,
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
													searchFilesFrame,
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
									searchFilesFrame, " 您将删除以下文件：\n    "
											+ filenames + "，\n是否仍要继续？");
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
		final RButton buttonDownload = new RButton(2, "下载");
		buttonDownload.setFont(new Font("system", Font.PLAIN, 12));
		buttonDownload.addActionListener(new ActionListener() {
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
					}
					if (fileToUploadHM.size() > 0) {
						int result = JOptionPane.showConfirmDialog(
								searchFilesFrame, " 您将下载以下文件：\n     "
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
									new Thread(
											new DownloadFileThreadForSearchFrame(
													chatRoomFrame, username,
													searchFilesFrame, filename,
													rowIndex, 6,
													joyuClientSocket,
													systemInfo, SayuServerIP,
													SayuServerport)).start();
								}
							}
						}
					} else {
						JOptionPane.showMessageDialog(searchFilesFrame,
								"对不起，您还没选中任何文件哦！", "操作有误",
								JOptionPane.WARNING_MESSAGE);
					}
				}
			}
		});
		deleteAndDownloadPanel.add(buttonDeleteFile, BorderLayout.WEST);
		deleteAndDownloadPanel.add(buttonDownload, BorderLayout.EAST);
		searchPanel.add(deleteAndDownloadPanel, BorderLayout.SOUTH);
		JPanel panelSearch = new JPanel();
		panelSearch.setBackground(new Color(180, 230, 147));
		panelSearch.setLayout(new BorderLayout());
		JLabel labelSearch = new JLabel(" 查找文件: ");
		labelSearch.setFont(new Font("system", Font.PLAIN, 12));
		searchField = new JTextField();
		searchField.setBackground(new Color(51, 184, 47));
		searchField.setFont(new Font("system", Font.PLAIN, 12));
		searchField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (searchField.getText().trim().isEmpty()) {
					JOptionPane.showMessageDialog(searchFilesFrame,
							"输入不能为空哦!请重新输入...", "格式错误",
							JOptionPane.WARNING_MESSAGE);
					searchField.requestFocus();
				} else {
					search();
					if (searchedFilesSet.size() == 0) {
						showSearchResultTable();
						JOptionPane.showMessageDialog(searchFilesFrame,
								"对不起，没有找到相关文件哦！", "没有找到",
								JOptionPane.WARNING_MESSAGE);
						searchField.requestFocus();
					} else {
						showSearchResultTable();
					}
				}
			}
		});
		searchField.setText("请在这里输入文件名...");
		JButton buttonSearch = new RButton(0, "查找");
		buttonSearch.setFont(new Font("system", Font.PLAIN, 12));
		buttonSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (searchField.getText().trim().isEmpty()) {
					JOptionPane.showMessageDialog(searchFilesFrame,
							"输入不能为空哦!请重新输入...", "格式错误",
							JOptionPane.WARNING_MESSAGE);
					searchField.requestFocus();
				} else {
					search();
					if (searchedFilesSet.size() == 0) {
						showSearchResultTable();
						JOptionPane.showMessageDialog(searchFilesFrame,
								"对不起，没有找到相关文件哦！", "没有找到",
								JOptionPane.WARNING_MESSAGE);
						searchField.requestFocus();
					} else {
						showSearchResultTable();
					}
				}
			}
		});
		panelSearch.add(labelSearch, BorderLayout.WEST);
		panelSearch.add(searchField);
		panelSearch.add(buttonSearch, BorderLayout.EAST);
		searchPanel.add(panelSearch, BorderLayout.NORTH);
		searchFilesFrame.add(searchPanel);
		searchFilesFrame.validate();
		searchFilesFrame.setVisible(false);
	}

	public void search() {
		if (searchedFilesSet != null) {
			searchedFilesSet.clear();
		}
		List<String> searchStrsList = new ArrayList<String>();
		StringBuffer searchStr = new StringBuffer(searchField.getText().trim()
				+ " ");
		while (!(searchStr.length() == 0)) {
			searchStrsList.add(searchStr.substring(0, searchStr.indexOf(" ")));
			searchStr.delete(0, searchStr.indexOf(" ") + 1);
		}
		Iterator filesForShareIt = (chatRoomFrame.getFilesForShareSet())
				.iterator();
		while (filesForShareIt.hasNext()) {
			FileBeanClientSide afilebean = (FileBeanClientSide) (filesForShareIt
					.next());
			boolean FileAddable = false;
			int count = 0;
			Iterator searchStrsIt = searchStrsList.iterator();
			while (searchStrsIt.hasNext()) {
				if (afilebean.getFileName().contains(
						(String) searchStrsIt.next())) {
					count++;
				}
				if (count == searchStrsList.size()) {
					FileAddable = true;
				}
			}
			if (FileAddable) {
				searchedFilesSet.add(afilebean);
			}
		}
	}

	public void freshAfterDelete() {
		search();
		showSearchResultTable();
	}

	public void showSearchResultTable() {
		if (searchFilesFrame != null) {
			if (joyuTableShareForSearch != null) {
				joyuTableShareForSearch.invalidate();
				joyuTableShareForSearch.setVisible(false);
			}
			Object[][] data = new Object[searchedFilesSet.size()][columnNames.length];
			Iterator it = searchedFilesSet.iterator();
			int j = 0;
			while (it.hasNext()) {
				for (int i = 0; i < searchedFilesSet.size(); i++) {
					FileBeanClientSide file = (FileBeanClientSide) it.next();
					data[j][0] = new String(file.getFileName());
					data[j][1] = new String(file.getFileKind());
					data[j][2] = new String(file.getFileSize());
					data[j][3] = new String(file.getFileFrom());
					data[j][4] = new String(file.getFileCreatedTime());
					data[j][5] = new String(file.getFileModifiedTime());
					data[j][6] = new String("");
					j++;
				}
			}
			JoyuTableModel tableModelShare = new JoyuTableModel(columnNames,
					data);
			joyuTableShareForSearch = new JoyuTableSortShareFiles(
					tableModelShare, username, chatRoomFrame, joyuClientSocket,
					systemInfo, SayuServerIP, SayuServerport);
			joyuTableShareForSearch.setOpaque(true);
			table = joyuTableShareForSearch.getTable();
			table.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					if ((e.getClickCount() == 2)
							&& table.getValueAt(table.getSelectedRow(), 0) != null) {
						String doubleClickedFilename = (String) table
								.getValueAt(table.getSelectedRow(), 0);
						if (!doubleClickedFilename.isEmpty()) {
							int result = JOptionPane.showConfirmDialog(
									searchFilesFrame, " 您将下载以下文件：\n     "
											+ doubleClickedFilename
											+ "，\n是否继续？");
							if (result == JOptionPane.YES_OPTION) {
								new Thread(
										new DownloadFileThreadForSearchFrame(
												chatRoomFrame, username,
												searchFilesFrame,
												doubleClickedFilename, table
														.getSelectedRow(), 6,
												joyuClientSocket, systemInfo,
												SayuServerIP, SayuServerport))
										.start();
							}
						} else {
							JOptionPane.showMessageDialog(searchFilesFrame,
									"对不起，您还没选中任何文件哦！", "操作有误",
									JOptionPane.WARNING_MESSAGE);
						}
					}
				}
			});
			searchPanel.add(joyuTableShareForSearch, BorderLayout.CENTER);
			searchFilesFrame.validate();
		}
	}

	public JTable getTable() {
		return table;
	}

	public JFrame getSearchFilesFrame() {
		return searchFilesFrame;
	}
}