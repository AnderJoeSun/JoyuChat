package joyu.chat.client.chatRoomFrame;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import joyu.chat.client.JoyuClientSocket;

public class DownloadFileThreadForSearchFrame implements Runnable {
	private Socket asocket = null;
	private DataOutputStream ados = null;
	private DataInputStream adis = null;
	private boolean ucConnected = false;
	private String username;
	private JFrame searchFilesForShareFrame;
	private String filename;
	private String savePath;
	private int colIndex;
	private int rowIndex;
	private JoyuClientSocket joyuClientSocket;
	private ChatRoomFrame chatRoomFrame;
	private final String systemInfo;
	private String SayuServerIP;
	private String SayuServerport;

	public DownloadFileThreadForSearchFrame(ChatRoomFrame chatRoomFrame,
			String username, JFrame searchFilesForShareFrame, String filename,
			int rowIndex, int colIndex, JoyuClientSocket joyuClientSocket,
			String systemInfo, String SayuServerIP, String SayuServerport) {
		this.username = username;
		this.searchFilesForShareFrame = searchFilesForShareFrame;
		this.chatRoomFrame = chatRoomFrame;
		this.colIndex = colIndex;
		this.rowIndex = rowIndex;
		this.filename = filename;
		this.joyuClientSocket = joyuClientSocket;
		this.systemInfo = systemInfo;
		this.SayuServerIP = SayuServerIP;
		this.SayuServerport = SayuServerport;
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
				+ username + "\\Download\\");
		if (!f3.exists()) {
			f3.mkdir();
		}
		savePath = System.getProperty("user.dir") + "\\users\\" + username
				+ "\\Download\\";
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

	public void run() {
		connect();
		if (ucConnected) {
			downloadFiles();
		}
	}

	public void downloadFiles() {
		JTable table = chatRoomFrame.getJoyuSearchFilesFrame().getTable();
		if (ucConnected) {
			try {
				ados.writeUTF("DownloadAFile");
				ados.writeUTF(username);
				ados.writeUTF(filename);
				ados.flush();
				String stra = adis.readUTF();
				if (stra.equalsIgnoreCase("fileNotFound")) {
					JOptionPane.showMessageDialog(searchFilesForShareFrame,
							"目标文件已经不存在了，请刷新后再试...", "下载失败",
							JOptionPane.WARNING_MESSAGE);
					joyuClientSocket.sendString(systemInfo
							+ "getAllFilesForShare");
				} else if (stra.equalsIgnoreCase("fileFound")) {
					File saveFolder = new File(savePath);
					if (!saveFolder.exists()) {
						saveFolder.mkdir();
					}
					File file = new File(savePath + filename);
					if (file.exists()) {
						int result = JOptionPane.showConfirmDialog(
								searchFilesForShareFrame, " " + file.getName()
										+ " 已经存在于： " + (savePath + filename)
										+ " ，是否要继续从服务器下载选中文件并覆盖它？");
						if (result == JOptionPane.YES_OPTION) {
							file.delete();
							connect();
							if (ucConnected) {
								downloadFiles();
							}
						}
					} else {
						Long filelength = adis.readLong();
						savePath = savePath + filename;
						FileOutputStream fileOut = new FileOutputStream(
								savePath);
						System.out.println("文件名为:" + filename + "\n");
						int bufferSize = 8192;
						byte[] buf = new byte[bufferSize];
						int passedlen = 0;
						int count = 0;
						while (true) {
							count++;
							int readed = 0;
							if (adis != null) {
								readed = adis.read(buf);
							}
							fileOut.write(buf, 0, readed);
							passedlen = passedlen + readed;
							if (count % 50 == 0) {
								table.setValueAt(
										(passedlen * 100L / filelength + "%"),
										rowIndex, colIndex);
								table.validate();
								table.repaint();
								searchFilesForShareFrame.validate();
							}
							if (passedlen >= filelength) {
								System.out.println("接收完成，文件存为" + savePath
										+ "\n");
								table.setValueAt("100%", rowIndex, colIndex);
								table.repaint();
								table.validate();
								searchFilesForShareFrame.validate();
								break;
							}
						}
						fileOut.flush();
						fileOut.close();
						ados.writeUTF("downloadedSuccess");
						ados.flush();
						System.out.println("uploadedSuccess...");
						JOptionPane.showMessageDialog(searchFilesForShareFrame,
								"下载成功!文件已保存为：" + savePath + " ，请注意查收...",
								"下载完毕!", JOptionPane.WARNING_MESSAGE);
					}
				}
			} catch (Exception e) {
				try {
					ados.writeUTF("downloadedFailed");
					ados.flush();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				System.out.println("下载失败" + "\n");
			}
		}
		try {
			if (ados != null)
				ados.close();
			if (adis != null)
				adis.close();
			if (asocket != null)
				asocket.close();
			System.out.println("client socket for download " + filename
					+ " closed...");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}