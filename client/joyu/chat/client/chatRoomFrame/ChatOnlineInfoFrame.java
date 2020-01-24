package joyu.chat.client.chatRoomFrame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.JButton;
import javax.swing.JFrame;
import joyu.chat.client.JoyuClientSocket;
import joyu.chat.client.JoyuTableModel;
import joyu.chat.client.JoyuTableSortShowOnlineInfo;
import joyu.chat.client.RButton;
import joyu.chat.client.UserClientSide;

public class ChatOnlineInfoFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private final String systemInfo;
	private Image logoImage;
	private JoyuClientSocket joyuClientSocket;
	private String[] columnNames = new String[] { "状态", "昵称", "真名", "性别", "邮箱",
			"电话", "地址" };
	private Object[][] data;
	private Iterator it;
	private JoyuTableModel tableModel;
	private JoyuTableSortShowOnlineInfo joyuTable;
	private HashSet<UserClientSide> allUsersSet = new HashSet<UserClientSide>();
	private UserClientSide user;
	private HashSet<String> usersOnlineSet;

	public ChatOnlineInfoFrame(HashSet<String> usersOnlineSet,
			String systemInfo, JoyuClientSocket joyuClientSocket,
			Image logoImage) {
		this.joyuClientSocket = joyuClientSocket;
		this.systemInfo = systemInfo;
		this.usersOnlineSet = usersOnlineSet;
		this.logoImage = logoImage;
		showOnlineInfoFrame();
	}

	private void showOnlineInfoFrame() {
		setIconImage(logoImage);
		getContentPane().setBackground(new Color(180, 230, 147));
		setTitle("查看用户信息");
		final JButton buttonOnlineRefresh = new RButton(0, "刷      新");
		buttonOnlineRefresh.setFont(new Font("system", Font.PLAIN, 12));
		buttonOnlineRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					allUsersSet.clear();
					usersOnlineSet.clear();
					joyuClientSocket.sendString(systemInfo + "getAllUsers");
					joyuClientSocket.sendString(systemInfo + "getUsersOnline");
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		add(buttonOnlineRefresh, BorderLayout.NORTH);
	}

	public void freshOnlineInfo() {
		if (this != null) {
			if (joyuTable != null) {
				joyuTable.invalidate();
				joyuTable.setVisible(false);
			}
			data = new Object[ChatOnlineInfoFrame.this.getAllUsersSet().size()][columnNames.length];
			it = ChatOnlineInfoFrame.this.getAllUsersSet().iterator();
			while (it.hasNext()) {
				for (int i = 0; i < ChatOnlineInfoFrame.this.getAllUsersSet()
						.size(); i++) {
					user = (UserClientSide) it.next();
					String online = "离线";
					if (usersOnlineSet.contains(user.getUsername())) {
						online = "在线";
					}
					data[i][0] = new String(online);
					data[i][1] = new String(user.getUsername());
					data[i][2] = new String(user.getRealname());
					data[i][3] = new String(user.getSex());
					data[i][4] = new String(user.getEmail());
					data[i][5] = new String(user.getPhone());
					data[i][6] = new String(user.getAddress());
				}
			}
			tableModel = new JoyuTableModel(columnNames, data);
			joyuTable = new JoyuTableSortShowOnlineInfo(tableModel);
			joyuTable.setOpaque(true);
			add(joyuTable, BorderLayout.CENTER);
			addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					dispose();
				}
			});
			setVisible(true);
		}
	}

	public synchronized void clearAllUsersSet() {
		allUsersSet.clear();
	}

	public synchronized void addToAllUsersSet(UserClientSide u) {
		allUsersSet.add(u);
	}

	public synchronized HashSet<UserClientSide> getAllUsersSet() {
		return allUsersSet;
	}
}