package joyu.chat.client;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JButton;

public class RButtonPanel extends JButton {
	private static final long serialVersionUID = 39082560987930759L;
	public static final Color BUTTON_COLOR1 = new Color(180, 230, 147);
	// public static final Color BUTTON_COLOR2 = new Color(180, 230, 147);
	public static final Color BUTTON_COLOR2 = new Color(51, 184, 47);
	public static final Color BUTTON_FOREGROUND_COLOR = Color.black;
	private boolean hover;
	private int style;
	public static final int ROUND_RECT = 0;
	public static final int LEFT_ROUND_RECT = 1;
	public static final int RIGHT_ROUND_RECT = 2;
	public static final int BALL = 3;
	public static final int STAR = 4;

	public RButtonPanel() {
		this(ROUND_RECT);
	}

	public RButtonPanel(int style) {
		this.style = style;
		if (BALL == style) {
			setPreferredSize(new Dimension(42, 42));
		} else if (STAR == style) {
			setPreferredSize(new Dimension(42, 42));
		}
		setFont(new Font("system", Font.PLAIN, 12));
		setBorderPainted(false);
		setForeground(BUTTON_COLOR2);
		setFocusPainted(false);
		setContentAreaFilled(false);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				setForeground(BUTTON_FOREGROUND_COLOR);
				hover = true;
				repaint();
			}

			@Override
			public void mouseExited(MouseEvent e) {
				setForeground(BUTTON_COLOR2);
				hover = false;
				repaint();
			}
		});
	}

	public RButtonPanel(int style, String text) {
		setText(text);
		this.style = style;
		if (BALL == style) {
			setPreferredSize(new Dimension(42, 42));
		} else if (STAR == style) {
			setPreferredSize(new Dimension(42, 42));
		}
		setFont(new Font("system", Font.PLAIN, 12));
		setBorderPainted(false);
		setForeground(BUTTON_COLOR2);
		setFocusPainted(false);
		setContentAreaFilled(false);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				setForeground(BUTTON_FOREGROUND_COLOR);
				hover = true;
				repaint();
			}

			@Override
			public void mouseExited(MouseEvent e) {
				setForeground(BUTTON_COLOR2);
				hover = false;
				repaint();
			}
		});
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g.create();
		int h = getHeight();
		int w = getWidth();
		float tran = 1F;
		if (!hover) {
			tran = 0.3F;
		}
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		GradientPaint p1;
		GradientPaint p2;
		if (getModel().isPressed()) {
			p1 = new GradientPaint(0, 0, new Color(0, 0, 0), 0, h - 1,
					new Color(100, 100, 100));
			p2 = new GradientPaint(0, 1, new Color(0, 0, 0, 50), 0, h - 3,
					new Color(255, 255, 255, 100));
		} else {
			p1 = new GradientPaint(0, 0, new Color(100, 100, 100), 0, h - 1,
					new Color(0, 0, 0));
			p2 = new GradientPaint(0, 1, new Color(255, 255, 255, 100), 0,
					h - 3, new Color(0, 0, 0, 50));
		}
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				tran));
		GradientPaint gp = new GradientPaint(0.0F, 0.0F, BUTTON_COLOR1, 0.0F,
				h, BUTTON_COLOR2, true);
		g2d.setPaint(gp);
		switch (style) {
		case ROUND_RECT: {
			RoundRectangle2D.Float r2d = new RoundRectangle2D.Float(0, 0,
					w - 1, h - 1, 20, 20);
			Shape clip = g2d.getClip();
			g2d.clip(r2d);
			g2d.fillRect(0, 0, w, h);
			g2d.setClip(clip);
			g2d.setPaint(p1);
			g2d.drawRoundRect(0, 0, w - 1, h - 1, 20, 20);
			g2d.setPaint(p2);
			g2d.drawRoundRect(1, 1, w - 3, h - 3, 18, 18);
			break;
		}
		case LEFT_ROUND_RECT: {
			RoundRectangle2D.Float r2d = new RoundRectangle2D.Float(0, 0,
					(w - 1) + 20, h - 1, 20, 20);
			Shape clip = g2d.getClip();
			g2d.clip(r2d);
			g2d.fillRect(0, 0, w, h);
			g2d.setClip(clip);
			g2d.setPaint(p1);
			g2d.drawRoundRect(0, 0, (w - 1) + 20, h - 1, 20, 20);
			g2d.setPaint(p2);
			g2d.drawRoundRect(1, 1, (w - 3) + 20, h - 3, 18, 18);
			g2d.setPaint(p1);
			g2d.drawLine(w - 1, 1, w - 1, h);
			g2d.setPaint(p2);
			g2d.drawLine(w - 2, 2, w - 2, h - 1);
			break;
		}
		case RIGHT_ROUND_RECT: {
			RoundRectangle2D.Float r2d = new RoundRectangle2D.Float(-20, 0,
					(w - 1) + 20, h - 1, 20, 20);
			Shape clip = g2d.getClip();
			g2d.clip(r2d);
			g2d.fillRect(0, 0, w, h);
			g2d.setClip(clip);
			g2d.setPaint(p1);
			g2d.drawRoundRect(-20, 0, (w - 1) + 20, h - 1, 20, 20);
			g2d.setPaint(p2);
			g2d.drawRoundRect(-19, 1, (w - 3) + 20, h - 3, 18, 18);
			g2d.setPaint(p1);
			g2d.drawLine(0, 1, 0, h);
			g2d.setPaint(p2);
			g2d.drawLine(1, 2, 1, h - 1);
			break;
		}
		case BALL: {
			Arc2D.Float a2d = new Arc2D.Float(0, 0, w, h, 0, 360, Arc2D.CHORD);
			Shape clip = g2d.getClip();
			g2d.clip(a2d);
			g2d.fillRect(0, 0, w, h);
			g2d.setClip(clip);
			g2d.setPaint(p1);
			g2d.drawOval(0, 0, w - 1, h - 1);
			g2d.setPaint(p2);
			g2d.drawOval(1, 1, w - 3, h - 3);
			break;
		}
		case STAR: {
			int x = w / 2;
			int y = h / 2;
			int r = w / 2;
			// 计算五个顶点
			Point[] ps = new Point[5];
			for (int i = 0; i <= 4; i++) {
				ps[i] = new Point((int) (x - r
						* Math.sin((i * 72 + 36) * 2 * Math.PI / 360)),
						(int) (y + r
								* Math.cos((i * 72 + 36) * 2 * Math.PI / 360)));
			}
			GeneralPath star = new GeneralPath();
			star.moveTo(ps[3].x, ps[3].y);
			star.lineTo(ps[0].x, ps[0].y);
			star.lineTo(ps[2].x, ps[2].y);
			star.lineTo(ps[4].x, ps[4].y);
			star.lineTo(ps[1].x, ps[1].y);
			star.lineTo(ps[3].x, ps[3].y);
			star.closePath();
			Shape clip = g2d.getClip();
			g2d.clip(star);
			g2d.fillRect(0, 0, w, h);
			g2d.setClip(clip);
			g2d.setPaint(p1);
			g2d.draw(star);
			g2d.setPaint(p2);
			g2d.draw(star);
			break;
		}
		default:
			break;
		}
		g2d.dispose();
		super.paintComponent(g);
	}
}