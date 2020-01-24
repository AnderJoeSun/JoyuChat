package joyu.chat.client.loggingFrame;

import java.awt.Color;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class LoggingFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	public LoggingFrame(Image logoImage) {
		setTitle("请稍候，登陆中...");
		setIconImage(logoImage);
		ImageIcon img = new ImageIcon(System.getProperty("user.dir")
				+ "\\resources\\loginning.gif");
		setLocation(350, 50);
		setSize(img.getIconWidth() + 100, img.getIconHeight() + 300);
		JPanel contentPanel = (JPanel) getContentPane();
		contentPanel.setLayout(null);
		contentPanel.setBackground(Color.white);
		JLabel loginningPicture = new JLabel(img);
		loginningPicture.setBounds(50, 110, img.getIconWidth(),
				img.getIconHeight());
		contentPanel.add(loginningPicture);
		String loginningText = "Logging. . .";
		JLabel labelLoginningText = new JLabel(loginningText);
		labelLoginningText.setBounds(180, 373, 100, 80);
		labelLoginningText.setVisible(true);
		contentPanel.add(labelLoginningText);
		setResizable(false);
	}
}