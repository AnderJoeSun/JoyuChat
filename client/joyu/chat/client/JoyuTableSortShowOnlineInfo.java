package joyu.chat.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

public class JoyuTableSortShowOnlineInfo extends JPanel {
	private static final long serialVersionUID = 1L;

	public JoyuTableSortShowOnlineInfo(final JoyuTableModel tableModel) {
		super(new GridLayout(1, 0));
		JTable table = new JTable(tableModel) {
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
						tip = (String) getValueAt(rowIndex, colIndex);
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
		table.setPreferredScrollableViewportSize(new Dimension(200, 500));
		table.setFillsViewportHeight(true);
		table.setAutoCreateRowSorter(true);
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
		// table.setShowVerticalLines(false);
		// table.setShowHorizontalLines(false);
		// table.getTableHeader().setReorderingAllowed(false);
		// table.getTableHeader().setResizingAllowed(true);
		// TableColumn column = table.getColumnModel().getColumn(0);
		// column.setPreferredWidth(30);
		table.setBackground(new Color(204, 232, 207));
		table.setBorder(null);
		JScrollPane scrollPane = new JScrollPane(table);
		add(scrollPane);
	}
}