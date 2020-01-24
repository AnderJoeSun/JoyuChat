package joyu.chat.client;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import joyu.chat.client.chatRoomFrame.ChatRoomFrame;
import joyu.chat.client.chatRoomFrame.UploadPanel;

public class JoyuTableSortShareFiles extends UploadPanel {
	private static final long serialVersionUID = 1L;
	private JTable table;

	public JoyuTableSortShareFiles(final JoyuTableModel tableModel,
			String username, ChatRoomFrame chatRoomFrame,
			JoyuClientSocket joyuClientSocket, String systemInfo,
			String SayuServerIP, String SayuServerport) {
		super(username, chatRoomFrame, joyuClientSocket, systemInfo,
				new GridLayout(1, 0), SayuServerIP, SayuServerport);
		table = new JTable(tableModel) {
			private static final long serialVersionUID = 1L;

			public String getToolTipText(MouseEvent e) {
				String tip = null;
				java.awt.Point p = e.getPoint();
				int rowIndex = rowAtPoint(p);
				int colIndex = columnAtPoint(p);
				if ((colIndex < tableModel.getColumnCount())
						&& (colIndex != -1) && (rowIndex != -1)
						&& (rowIndex < tableModel.getRowCount())) {
					int realColumnIndex = convertColumnIndexToModel(colIndex);
					int realRowIndex = convertRowIndexToModel(rowIndex);
					if ((realColumnIndex == colIndex)
							&& (realRowIndex == rowIndex)) {
						tip = getValueAt(rowIndex, colIndex).toString();
					} else {
						tip = super.getToolTipText(e);
					}
					return tip;
				}
				return null;
			}

			protected JTableHeader createDefaultTableHeader() {
				return new JTableHeader(columnModel) {
					private static final long serialVersionUID = 1L;

					public String getToolTipText(MouseEvent e) {
						java.awt.Point p = e.getPoint();
						int index = columnModel.getColumnIndexAtX(p.x);
						int realIndex = columnModel.getColumn(index)
								.getModelIndex();
						return tableModel.getColumnName(realIndex);
					}
				};
			}
		};
		table.setFillsViewportHeight(true);
		table.setAutoCreateRowSorter(true);
		table.setShowVerticalLines(true);
		table.setShowHorizontalLines(false);
		table.getTableHeader().setReorderingAllowed(false);
		table.getTableHeader().setResizingAllowed(true);
		for (int i = 0; i < 7; i++) {
			table.getColumnModel().getColumn(i)
					.setCellRenderer(new DefaultTableCellRenderer() { // 重写
																		// setValue
																		// 方法
								private static final long serialVersionUID = 1L;

								public void setValue(Object value) {
									this.setHorizontalAlignment(SwingConstants.CENTER);// 居中
									super.setValue(value);
								}
							});
		}
		table.getColumnModel().getColumn(0).setPreferredWidth(180);
		table.getColumnModel().getColumn(1).setPreferredWidth(70);
		table.getColumnModel().getColumn(2).setPreferredWidth(70);
		table.getColumnModel().getColumn(2)
				.setCellRenderer(new DefaultTableCellRenderer() { // 重写 setValue
																	// 方法
							private static final long serialVersionUID = 1L;

							public void setValue(Object value) {
								this.setHorizontalAlignment(SwingConstants.RIGHT);// 居右
								super.setValue(value);
							}
						});
		table.getColumnModel().getColumn(3).setPreferredWidth(70);
		table.getColumnModel().getColumn(4).setPreferredWidth(160);
		table.getColumnModel().getColumn(5).setPreferredWidth(160);
		table.getColumnModel().getColumn(6).setPreferredWidth(80);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setBackground(new Color(204, 232, 207));
		table.setBorder(null);
		JScrollPane b = new JScrollPane(table);
		add(b);
	}

	public JTable getTable() {
		return table;
	}
}