package joyu.chat.client.chatRoomFrame;

import java.awt.TextArea;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;
import joyu.chat.client.FileBeanClientSide;
import joyu.chat.client.JoyuClientSocket;

public class UploadTextArea extends TextArea implements DropTargetListener {
	private static final long serialVersionUID = 1L;
	private String username;
	private JoyuClientSocket joyuClientSocket;
	private String systemInfo;
	private ChatRoomFrame chatRoomFrame;
	private String SayuServerIP;
	private String SayuServerport;

	public UploadTextArea(String text, int rows, int columns, int scrollbars,
			String username, ChatRoomFrame chatRoomFrame,
			JoyuClientSocket joyuClientSocket, String systemInfo,
			String SayuServerIP, String SayuServerport) {
		super(text, rows, columns, scrollbars);
		this.username = username;
		this.chatRoomFrame = chatRoomFrame;
		this.joyuClientSocket = joyuClientSocket;
		this.systemInfo = systemInfo;
		this.SayuServerIP = SayuServerIP;
		this.SayuServerport = SayuServerport;
		new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
	}

	public void dragEnter(DropTargetDragEvent dtde) {
	}

	public void dragOver(DropTargetDragEvent dtde) {
	}

	public void dropActionChanged(DropTargetDragEvent dtde) {
	}

	public void dragExit(DropTargetEvent dte) {
	}

	public void drop(DropTargetDropEvent dtde) {
		try {
			if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
				List list = (List) (dtde.getTransferable()
						.getTransferData(DataFlavor.javaFileListFlavor));
				Iterator iterator = list.iterator();
				List<String> filesUploadableNamesList = new ArrayList<String>();
				List<String> filesUploadablePathlist = new ArrayList<String>();
				List<String> filesUnUploadableNameslist = new ArrayList<String>();
				while (iterator.hasNext()) {
					File f = (File) iterator.next();
					if (f != null) {
						boolean uploadAble = true;
						Iterator filesForShareIt = chatRoomFrame
								.getFilesForShareSet().iterator();
						while (filesForShareIt.hasNext()) {
							FileBeanClientSide afilebean = (FileBeanClientSide) (filesForShareIt
									.next());
							if ((f.getName().replace(" ", "_"))
									.equalsIgnoreCase(afilebean.getFileName())
									&& (!(afilebean.getFileFrom()
											.equalsIgnoreCase(username)))) {
								uploadAble = false;
								break;
							}
						}
						if (uploadAble) {
							if (filesUploadableNamesList.size() < 1) {
								filesUploadableNamesList.add(f.getName());
							} else {
								filesUploadableNamesList.add("\n     "
										+ f.getName());
							}
							filesUploadablePathlist.add(f.getAbsolutePath());
						} else {
							if (filesUnUploadableNameslist.size() < 1) {
								filesUnUploadableNameslist.add(f.getName());
							} else {
								filesUnUploadableNameslist.add("\n     "
										+ f.getName());
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
									"操作有误", JOptionPane.WARNING_MESSAGE);
				}
				if (!filesUploadablePathlist.isEmpty()) {
					int result = JOptionPane.showConfirmDialog(chatRoomFrame,
							"您将上传以下文件：\n     " + filesUploadableNamesList
									+ "，\n是否继续？");
					if (result == JOptionPane.YES_OPTION) {
						Iterator iterator2 = filesUploadablePathlist.iterator();
						while (iterator2.hasNext()) {
							File f = new File((String) iterator2.next());
							if (f.length() == 0) {
								JOptionPane.showMessageDialog(chatRoomFrame,
										"您上传的文件为空，请选择其它文件吧！", "内容有误",
										JOptionPane.WARNING_MESSAGE);
							} else if (f.length() > 1000000000) {
								JOptionPane.showMessageDialog(chatRoomFrame,
										"您上传的文件大于1GB，超过上限，请尝试压缩文件后再试吧！",
										"文件太大", JOptionPane.WARNING_MESSAGE);
							} else {
								UploadFileThread uploadFileThread = new UploadFileThread(
										username, chatRoomFrame,
										joyuClientSocket, f, systemInfo,
										SayuServerIP, SayuServerport);
								uploadFileThread.run();
							}
						}
						dtde.dropComplete(true);
					}
				}
			} else {
				dtde.rejectDrop();
			}
		} catch (UnsupportedFlavorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}