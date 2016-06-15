package application.GUI;

import java.util.LinkedList;

import javax.swing.table.AbstractTableModel;

public class StringListTabelModel extends AbstractTableModel {
	private String[] columnNames = new String[2];
	private final LinkedList<String> list;
	boolean editableSecondColumn;
	private boolean visible = true;

	private static final long serialVersionUID = 1L;

	public StringListTabelModel(String secondColumnName, boolean editableSecondColumn){
		list = new LinkedList<String>();
		columnNames[0] = "view";
		columnNames[1] = secondColumnName;
		this.editableSecondColumn = editableSecondColumn;
	}
	
	public void addElement(String viewModel) {
		// Adds the element in the last position in the list
		list.add(viewModel);
		if(visible)
			fireTableRowsInserted(list.size()-1, list.size()-1);
	}
	
	public void removeAllElements() {
		if(list.size() == 0)
			return;
		int rowCount = list.size();
        list.clear();
        if(visible)
        	fireTableRowsDeleted(0, rowCount);
    }
	
	public void removeLastElement() {
		if(list.size() == 0)
			return;
        list.removeLast();
        if(visible)
        	fireTableRowsDeleted(list.size()-1, list.size()-1);
    }
	
	public void removeElement(int index) {
		if(list.size() < index)
			return;
		list.remove(index);
		if(visible)
			fireTableRowsDeleted(index, index);
    }
	
	@Override
	public int getRowCount() {
		if(visible)
			return list.size();
		else 
			return 0;
	}

	@Override
	public int getColumnCount() {
		 return columnNames.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(visible)
			switch(columnIndex) {
        		case 0: return rowIndex;
        		case 1: return list.get(rowIndex);
			}
		return null;
	}

	@Override
	public String getColumnName(int column) {		
		return columnNames[column];
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if(visible && columnIndex == 1)
			return editableSecondColumn;
		return false;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if(!visible || columnIndex != 1)
			return;
		list.set(rowIndex, (String) aValue);
		fireTableCellUpdated(rowIndex, columnIndex);
	} 
	
	
	
	public void setVisible(boolean visible){
		this.visible = visible;
		fireTableRowsInserted(0, list.size());
	}
}
