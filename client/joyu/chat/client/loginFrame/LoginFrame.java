package joyu.chat.client.loginFrame;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import joyu.chat.client.RButton;

public class LoginFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private AboutJoyuFrame aboutJoyuFrame;
	private Image logoImage;
	private JLabel labelBackground;
	private JLabel labelUsername;
	private boolean mainFrameActivated = false;
	boolean mainFrameDisposed = false;
	private boolean byFrameActivated = false;
	private JLabel labelPassword;
	private JLabel labelJoyuChatRoomName;
	private Thread myDock;
	private boolean myDockStoppable = false;
	private JLabel labelDongHua;
	private JButton buttonRegister;
	private JButton buttonLogin;
	private JButton buttonAdmin;
	private JButton buttonAboutJoyu;
	private ImageIcon iconDongHua;
	private ImageIcon iconJoyuChatRoomName;
	private ImageIcon loginFrameBackground;
	private String registURL;
	private String adminURL;
	private TextField textFieldUsername = new TextField();
	private TextField textFieldPassword = new TextField();

	public LoginFrame(Image logoImage, String registURL, String adminURL,
			AboutJoyuFrame aboutJoyuFrame) {
		this.logoImage = logoImage;
		this.registURL = registURL;
		this.adminURL = adminURL;
		this.aboutJoyuFrame = aboutJoyuFrame;
		showLoginFrame();
	}

	public void showLoginFrame() {
		setIconImage(logoImage);
		iconJoyuChatRoomName = new ImageIcon(System.getProperty("user.dir")
				+ "\\resources\\loginFrameBar.JPG");
		labelJoyuChatRoomName = new JLabel(iconJoyuChatRoomName);
		labelJoyuChatRoomName.setBounds(0, 0,
				iconJoyuChatRoomName.getIconWidth(),
				iconJoyuChatRoomName.getIconHeight());
		labelUsername = new JLabel();
		labelUsername.setText("用户呢称：");
		labelUsername.setFont(new Font("system", Font.PLAIN, 13));
		labelUsername.setBounds(new Rectangle(30, 69, 69, 25));
		labelPassword = new JLabel();
		labelPassword.setText("用户密码：");
		labelPassword.setFont(new Font("system", Font.PLAIN, 13));
		labelPassword.setBounds(new Rectangle(30, 97, 69, 25));
		iconDongHua = new ImageIcon(System.getProperty("user.dir")
				+ "\\resources\\donghua.gif");
		labelDongHua = new JLabel(iconDongHua);
		labelDongHua.setBounds(new Rectangle(8, 129, 215, 84));
		textFieldUsername.setBounds(new Rectangle(97, 69, 118, 25));
		textFieldUsername.setBackground(new Color(205, 255, 205));
		textFieldPassword.setBounds(new Rectangle(97, 97, 118, 25));
		textFieldPassword.setBackground(new Color(205, 255, 205));
		textFieldPassword.setEchoChar('*');
		textFieldUsername.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent ke1) {
				if (ke1.getKeyCode() == KeyEvent.VK_ENTER) {
					String str = textFieldUsername.getText().trim();
					if (str.isEmpty()) {
						JOptionPane.showMessageDialog(LoginFrame.this,
								"用户昵称不能为空,或不能全为空格哦!", "内容格式不正确",
								JOptionPane.WARNING_MESSAGE);
						textFieldUsername.requestFocus();
					} else {
						textFieldPassword.requestFocus();
					}
				}
			}
		});
		buttonRegister = new RButton(0);
		buttonRegister.setBounds(new Rectangle(230, 69, 80, 25));
		buttonRegister.setText("注  册");
		buttonRegister.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Process p = Runtime.getRuntime().exec(
							"cmd.exe   /c   start   " + registURL);
					p.waitFor();
				} catch (Exception ex) {
					System.out.println(ex.getMessage());
				}
			}
		});
		buttonAdmin = new RButton(0);
		buttonAdmin.setBounds(new Rectangle(230, 97, 80, 25));
		buttonAdmin.setText("管理员");
		buttonAdmin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Process p = Runtime.getRuntime().exec(
							"cmd.exe   /c   start   " + adminURL);
					p.waitFor();
				} catch (Exception ex) {
					System.out.println(ex.getMessage());
				}
			}
		});
		buttonAboutJoyu = new RButton(0);
		buttonAboutJoyu.setBounds(new Rectangle(230, 150, 80, 25));
		buttonAboutJoyu.setText("联系我");
		buttonAboutJoyu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				aboutJoyuFrame.setBounds((getBounds().x + getBounds().width),
						getBounds().y, 239, 251);
				RButton buttonClose = aboutJoyuFrame.getRButton();
				buttonClose.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						myDock = null;
						aboutJoyuFrame.dispose();
					}
				});
				aboutJoyuFrame.addWindowListener(new WindowAdapter() {
					public void windowActivated(WindowEvent e) {
						byFrameActivated = true;
					}

					public void windowDeactivated(WindowEvent e) {
						byFrameActivated = false;
					}
				});
				if (myDock == null) {
					myDock = new Thread(new MyDock());
					myDock.start();
				}
				if (!aboutJoyuFrame.isVisible()) {
					aboutJoyuFrame.setVisible(true);
				}
			}
		});
		buttonLogin = new RButton(0);
		buttonLogin.setBounds(new Rectangle(230, 185, 80, 25));
		buttonLogin.setText("登  录");
		setLayout(null);
		setSize(330, 251);
		setTitle("欢迎登陆鱼鱼聊天室...");
		add(labelPassword);
		add(labelUsername);
		add(labelDongHua);
		add(labelJoyuChatRoomName);
		add(textFieldUsername);
		add(textFieldPassword);
		add(buttonRegister);
		add(buttonLogin);
		add(buttonAdmin);
		add(buttonAboutJoyu);
		// 设置登陆窗口背景图片
		loginFrameBackground = new ImageIcon(System.getProperty("user.dir")
				+ "\\resources\\loginFrameBackground.JPG");
		labelBackground = new JLabel(loginFrameBackground);
		getLayeredPane().add(labelBackground, new Integer(Integer.MIN_VALUE));
		labelBackground.setBounds(0, 0, loginFrameBackground.getIconWidth(),
				loginFrameBackground.getIconHeight());
		setLocation(340, 263);
		setResizable(false);
		setAlwaysOnTop(true);
		addWindowListener(new WindowAdapter() {
			public void windowActivated(WindowEvent e) {
				mainFrameActivated = true;
			}

			public void windowDeactivated(WindowEvent e) {
				mainFrameActivated = false;
			}
		});
		setVisible(true);
		add(labelBackground);
	}

	private class MyDock implements Runnable {
		public void run() {
			boolean joined = true;
			while (!myDockStoppable && LoginFrame.this.isVisible()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if (!myDockStoppable && byFrameActivated) {
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
						joined = true;
					} else {
						joined = false;
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							// e.printStackTrace();
						}
					}
				}
				if (!myDockStoppable && mainFrameActivated && joined) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						// e.printStackTrace();
					}
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
			}
		}
	}

	public JButton getButtonLogin() {
		return buttonLogin;
	}

	public String getUsernameInput() {
		return textFieldUsername.getText().trim();
	}

	public String getPasswordInput() {
		return textFieldPassword.getText().trim();
	}

	public void setMyDockStoppable(boolean myDockStoppable) {
		this.myDockStoppable = myDockStoppable;
	}

	public TextField getTextFieldPassword() {
		return textFieldPassword;
	}
	// public LoginFrame() { showLoginFrame(); }
	// public static void main(String[] agrs) { new LoginFrame(); }
}