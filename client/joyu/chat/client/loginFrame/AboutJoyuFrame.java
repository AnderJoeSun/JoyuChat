package joyu.chat.client.loginFrame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import joyu.chat.client.RButton;
import joyu.chat.client.RButtonPanel;
import joyu.chat.client.RButtonPanelPain;

public class AboutJoyuFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private RButton buttonClose;

	public AboutJoyuFrame(final String JoyuQQURL) {
		setUndecorated(true);
		RButtonPanel aboutJoyuButtonPanel = new RButtonPanel(0);
		aboutJoyuButtonPanel.setLayout(new BorderLayout());
		RButtonPanelPain buttonAboutJoyuNorth = new RButtonPanelPain(0,
				"鱼鱼 聊 天 室 作 者 信 息");
		buttonAboutJoyuNorth.setForeground(new Color(51, 184, 47));
		buttonAboutJoyuNorth.setFont(new Font("system", Font.PLAIN, 15));
		RButtonPanel buttonAboutJoyuSubPanel = new RButtonPanel(0);
		buttonAboutJoyuSubPanel.setLayout(new BorderLayout());
		final JTextArea aboutJoyuArea = new JTextArea();
		aboutJoyuArea.setBackground(new Color(180, 230, 147));
		aboutJoyuArea.setForeground(new Color(51, 184, 47));
		final String infoOfJoyu = "\n 姓名: Joy\n 年龄: 24\n 昵称: Joy\n 职称: Java工程师\n Q Q: 999333\n 手机: 12345678900\n 邮箱: abcqq.com";
		aboutJoyuArea.setOpaque(false);
		aboutJoyuArea.setText(infoOfJoyu);
		aboutJoyuArea.setFont(new Font("system", Font.PLAIN, 13));
		aboutJoyuArea.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				aboutJoyuArea.setEditable(false);
				aboutJoyuArea.setBackground(new Color(180, 230, 147));
			}

			public void focusLost(FocusEvent e) {
			}
		});
		buttonAboutJoyuSubPanel.add(aboutJoyuArea, BorderLayout.NORTH);
		buttonAboutJoyuSubPanel.add(aboutJoyuArea);
		aboutJoyuButtonPanel.add(buttonAboutJoyuSubPanel);
		aboutJoyuButtonPanel.add(buttonAboutJoyuNorth, BorderLayout.NORTH);
		JPanel closeButtonPanel = new JPanel(new BorderLayout());
		closeButtonPanel.setBackground(new Color(180, 230, 147));
		closeButtonPanel.setOpaque(false);
		JButton buttonMoreInfo = new RButton(1, "   更   多... ");
		buttonMoreInfo.setFont(new Font("system", Font.PLAIN, 13));
		buttonMoreInfo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					Process p = Runtime.getRuntime().exec(
							"cmd.exe /c start " + JoyuQQURL);
					p.waitFor();
				} catch (Exception ex) {
					System.out.println(ex.getMessage());
				}
			}
		});
		buttonClose = new RButton(2, "    关    闭   ");
		buttonClose.setFont(new Font("system", Font.PLAIN, 13));
		buttonClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				dispose();
			}
		});
		closeButtonPanel.add(buttonMoreInfo, BorderLayout.WEST);
		closeButtonPanel.add(buttonClose, BorderLayout.EAST);
		aboutJoyuButtonPanel.add(closeButtonPanel, BorderLayout.SOUTH);
		aboutJoyuButtonPanel.setOpaque(false);
		setContentPane(aboutJoyuButtonPanel);
		setAlwaysOnTop(true);
		pack();
	}

	public RButton getRButton() {
		return buttonClose;
	}
}