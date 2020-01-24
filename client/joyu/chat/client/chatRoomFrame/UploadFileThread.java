package joyu.chat.client.chatRoomFrame;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import joyu.chat.client.FileBeanClientSide;
import joyu.chat.client.JoyuClientSocket;

public class UploadFileThread implements Runnable {
	private Socket asocket = null;
	private DataOutputStream ados = null;
	DataInputStream fis;
	private DataInputStream adis = null;
	private boolean ucConnected = false;
	private int bufferSize = 8192;
	private byte[] buf = new byte[bufferSize];
	private long passedlen;
	private String username;
	private ChatRoomFrame chatRoomFrame;
	private JoyuClientSocket joyuClientSocket;
	private String systemInfo;
	private JLabel progressLabel;
	private int percent;
	private long twoSecAgoPassedlen = 0;
	private JPanel uploadingInfoPanel;
	private JPanel uploadingInfoPanel2;
	private JPanel uploadingFilesPanel;
	private JLabel progressLabel2;
	private JPanel uploadingFilesPanel2;
	private File file;
	private String SayuServerIP;
	private String SayuServerport;

	public UploadFileThread(String username, ChatRoomFrame chatRoomFrame,
			JoyuClientSocket joyuClientSocket, File file, String systemInfo,
			String SayuServerIP, String SayuServerport) {
		this.username = username;
		this.chatRoomFrame = chatRoomFrame;
		this.joyuClientSocket = joyuClientSocket;
		this.file = file;
		this.systemInfo = systemInfo;
		this.SayuServerIP = SayuServerIP;
		this.SayuServerport = SayuServerport;
		progressLabel = chatRoomFrame.createALabel();
		progressLabel.setText(file.getName() + "  即将上传...");
		uploadingFilesPanel = chatRoomFrame.getUploadingFilesPanel();
		uploadingInfoPanel = chatRoomFrame.getUploadingInfoPanel();
		uploadingInfoPanel.add(progressLabel);
		progressLabel2 = chatRoomFrame.createALabel();
		progressLabel2.setText(file.getName() + "  即将上传...");
		uploadingFilesPanel2 = chatRoomFrame.getUploadingFilesPanel2();
		uploadingInfoPanel2 = chatRoomFrame.getUploadingInfoPanel2();
		uploadingInfoPanel2.add(progressLabel2);
	}

	public void run() {
		connect();
		if (ucConnected) {
			uploadFiles();
		}
	}

	public void connect() {
		try {
			asocket = new Socket(SayuServerIP, Integer.parseInt(SayuServerport));
			adis = new DataInputStream(new BufferedInputStream(
					asocket.getInputStream()));
			ados = new DataOutputStream(asocket.getOutputStream());
			ucConnected = true;
		} catch (Exception e) {
			e.printStackTrace();
			if (asocket != null)
				try {
					asocket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
		}
	}

	private class UploadingSend implements Runnable {
		public void run() {
			try {
				while (true) {
					int readed = 0;
					if (fis != null) {
						readed = fis.read(buf);
					}
					if (readed == -1) {
						break;
					}
					ados.write(buf, 0, readed);
					ados.flush();
					Thread.sleep(10);
				}
			} catch (Exception ufe) {
				ufe.printStackTrace();
			}
		}
	}

	private class UploadingInfo implements Runnable {
		public void run() {
			chatRoomFrame.addAnUploadingFile(file);
			uploadingInfoPanel.setVisible(true);
			uploadingInfoPanel2.setVisible(true);
			uploadingFilesPanel.validate();
			uploadingFilesPanel.setVisible(true);
			uploadingFilesPanel2.validate();
			uploadingFilesPanel2.setVisible(true);
			Timer t = new Timer();
			t.scheduleAtFixedRate(new TimerTask() {
				public void run() {
					if (passedlen - twoSecAgoPassedlen > 0) {
						int speed = (int) (((passedlen - twoSecAgoPassedlen)) / (1 * 1024));
						String timeLeftStr = null;
						if ((int) ((((file.length() - passedlen) / (speed * 1024)) / (60 * 60))) >= 1) {
							timeLeftStr = String
									.valueOf((int) ((((file.length() - passedlen) / (speed * 1024)) / (60 * 60))))
									+ "时"
									+ String.valueOf(((int) ((((file.length() - passedlen) / (speed * 1024)) % (60 * 60)) / 60)))
									+ "分"
									+ String.valueOf(((int) ((((file.length() - passedlen) / (speed * 1024)) % (60 * 60)) % 60)))
									+ "秒 ";
						} else if (((int) ((((file.length() - passedlen) / (speed * 1024)) / (60 * 60))) < 1)
								&& ((int) ((((file.length() - passedlen) / (speed * 1024)) / 60)) >= 1)) {
							timeLeftStr = String
									.valueOf((int) ((((file.length() - passedlen) / (speed * 1024)) / 60)))
									+ "分"
									+ String.valueOf(((int) (((file.length() - passedlen) / (speed * 1024)) % 60)))
									+ "秒 ";
						} else if ((int) ((((file.length() - passedlen) / (speed * 1024)) / 60)) < 1) {
							timeLeftStr = String
									.valueOf((int) (((file.length() - passedlen) / (speed * 1024))))
									+ "秒 ";
						}
						progressLabel.setText("{" + percent + "%  "
								+ String.valueOf(speed) + "K/s  " + timeLeftStr
								+ file.getName() + "}");
						progressLabel.setToolTipText("{" + file.getName()
								+ "  进度：" + percent + "%  速度："
								+ String.valueOf(speed) + "K/s  还剩："
								+ timeLeftStr + "}");
						progressLabel2.setText("{" + percent + "%  "
								+ String.valueOf(speed) + "K/s  " + timeLeftStr
								+ file.getName() + "}");
						progressLabel2.setToolTipText("{" + file.getName()
								+ "  进度：" + percent + "%  速度："
								+ String.valueOf(speed) + "K/s  还剩："
								+ timeLeftStr + "}");
						twoSecAgoPassedlen = passedlen;
					}
				}
			}, 10l, 950l);
			try {
				while (true) {
					String str = adis.readUTF();
					if (str.startsWith("percent:")) {
						percent = Integer.parseInt(str.substring(
								str.indexOf(':') + 1, str.indexOf('%')));
						passedlen = Long.parseLong(str.substring(str
								.lastIndexOf(":") + 1));
					} else if (str.startsWith("uploadedSuccess")) {
						t.cancel();
						System.out.println(file.getName()
								+ " uploadedSuccess...");
						progressLabel.setText("100% " + file.getName());
						progressLabel2.setText("100% " + file.getName());
						new Timer().schedule(new TimerTask() {
							public void run() {
								progressLabel.setVisible(false);
								uploadingInfoPanel.remove(progressLabel);
								uploadingInfoPanel.validate();
								progressLabel2.setVisible(false);
								uploadingInfoPanel2.remove(progressLabel2);
								uploadingInfoPanel2.validate();
							}
						}, 2000);
						chatRoomFrame.removeAnUploadingFile(file);
						if (chatRoomFrame.isFilesUploadingListEmpty()) {
							new Timer().schedule(new TimerTask() {
								public void run() {
									if (chatRoomFrame
											.isFilesUploadingListEmpty()) {
										uploadingFilesPanel.setVisible(false);
										uploadingFilesPanel2.setVisible(false);
									}
								}
							}, 2000);
						}
						FileBeanClientSide afileBean = new FileBeanClientSide();
						String fileName = str.substring(
								"uploadedSuccess".length(),
								str.indexOf(systemInfo));
						afileBean.setFileName(fileName);
						str = str.substring(str.indexOf(systemInfo)
								+ systemInfo.length());
						String fileKind = str.substring(0,
								str.indexOf(systemInfo));
						afileBean.setFileKind(fileKind);
						str = str.substring(str.indexOf(systemInfo)
								+ systemInfo.length());
						String fileSize = str.substring(0,
								str.indexOf(systemInfo));
						afileBean.setFileSize(fileSize);
						str = str.substring(str.indexOf(systemInfo)
								+ systemInfo.length());
						String fileFrom = str.substring(0,
								str.indexOf(systemInfo));
						afileBean.setFileFrom(fileFrom);
						str = str.substring(str.indexOf(systemInfo)
								+ systemInfo.length());
						String fileCreatedTime = str.substring(0,
								str.indexOf(systemInfo));
						afileBean.setFileCreatedTime(fileCreatedTime);
						str = str.substring(str.indexOf(systemInfo)
								+ systemInfo.length());
						String fileModifiedTime = str.substring(0,
								str.indexOf(systemInfo));
						afileBean.setFileModifiedTime(fileModifiedTime);
						str = str.substring(str.indexOf(systemInfo)
								+ systemInfo.length());
						chatRoomFrame.addToFilesForShareSet(afileBean);
						chatRoomFrame.freshShareInfo();
						java.util.Date dateNow2 = new java.util.Date();
						SimpleDateFormat dateFormat2 = new SimpleDateFormat(
								"yyyy-MM-dd HH:mm:ss");
						String dateNowStr2 = dateFormat2.format(dateNow2);
						String uploadStr2 = systemInfo + "sysInfomation"
								+ "******* 系统信息 [" + dateNowStr2 + "]: "
								+ username + " 的文件: " + file.getName().trim()
								+ " 上传完毕! *******\n\n";
						joyuClientSocket.sendString(uploadStr2);
						break;
					} else if (str.endsWith("uploadedFailed")) {
						t.cancel();
						JOptionPane.showMessageDialog(chatRoomFrame, "对不起，"
								+ file.getName().trim() + "上传失败，请重试一次吧!",
								"上传失败", JOptionPane.WARNING_MESSAGE);
						progressLabel.setText("0% " + file.getName());
						progressLabel2.setText("0% " + file.getName());
						new Timer().schedule(new TimerTask() {
							public void run() {
								progressLabel.setVisible(false);
								uploadingInfoPanel.remove(progressLabel);
								uploadingInfoPanel.validate();
								progressLabel2.setVisible(false);
								uploadingInfoPanel2.remove(progressLabel2);
								uploadingInfoPanel2.validate();
							}
						}, 2000);
						chatRoomFrame.removeAnUploadingFile(file);
						if (chatRoomFrame.isFilesUploadingListEmpty()) {
							new Timer().schedule(new TimerTask() {
								public void run() {
									uploadingFilesPanel.setVisible(false);
									uploadingFilesPanel2.setVisible(false);
								}
							}, 2000);
							System.out.println("  upload failed ...");
						}
						break;
					}
				}
				try {
					if (ados != null)
						ados.close();
					if (adis != null)
						adis.close();
					if (asocket != null)
						System.out.println(" clietn socket for upload file "
								+ file.getName() + " closed...");
				} catch (Exception e) {
					e.printStackTrace();
				}
			} catch (Exception ufe) {
				ufe.printStackTrace();
			}
		}
	}

	public void uploadFiles() {
		if (ucConnected) {
			try {
				ados.writeUTF("UploadAFile");
				ados.writeUTF(username);
				ados.flush();
				System.out.println(" uploadAFile username ..");
				fis = new DataInputStream(new BufferedInputStream(
						new FileInputStream(file.getAbsoluteFile())));
				java.util.Date dateNow = new java.util.Date();
				SimpleDateFormat dateFormat = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
				String dateNowStr = dateFormat.format(dateNow);
				String uploadStr = systemInfo + "sysInfomation"
						+ "******* 系统信息 [" + dateNowStr + "]: " + username
						+ " 正在上传文件: " + file.getName() + " *******\n\n";
				joyuClientSocket.sendString(uploadStr);
				ados.writeUTF(file.getName().replace(' ', '_'));
				ados.flush();
				String string = adis.readUTF();
				if (string.equalsIgnoreCase("fileExists")) {
					int result = JOptionPane.showConfirmDialog(chatRoomFrame,
							" " + file.getName() + " 已经存在，是否要覆盖它？");
					if (result == JOptionPane.YES_OPTION) {
						ados.writeUTF("overWrite");
						ados.flush();
						ados.writeLong((long) file.length());
						ados.flush();
						new Thread(new UploadingSend()).start();
						new Thread(new UploadingInfo()).start();
					} else {
						uploadingInfoPanel.remove(progressLabel);
						uploadingInfoPanel2.remove(progressLabel2);
						ados.writeUTF("cancel");
						ados.flush();
						java.util.Date dateNow2 = new java.util.Date();
						SimpleDateFormat dateFormat2 = new SimpleDateFormat(
								"yyyy-MM-dd HH:mm:ss");
						String dateNowStr2 = dateFormat2.format(dateNow2);
						String cancelStr = systemInfo + "sysInfomation"
								+ "******* 系统信息 [" + dateNowStr2 + "]: "
								+ username + " 取消了 " + file.getName()
								+ " 的上传，因为文件已经存在于共享里 *******\n\n";
						joyuClientSocket.sendString(cancelStr);
					}
				} else if (string.equalsIgnoreCase("fileNotExists")) {
					ados.writeLong((long) file.length());
					ados.flush();
					new Thread(new UploadingSend()).start();
					new Thread(new UploadingInfo()).start();
				}
			} catch (Exception ufe) {
				ufe.printStackTrace();
			}
		}
	}
}