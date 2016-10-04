/** 	
 * Name: StringListTabelModel.java
 * 
 * Author: Jelena Slivka <slivkaje AT uns DOT ac DOT rs>
 * 
 * Copyright: (c) 2016 Jelena Slivka <slivkaje AT uns DOT ac DOT rs>
 * 
 * This file is a part of RSSalg software, a flexible, highly configurable tool for experimenting 
 * with co-training based techniques. RSSalg Software encompasses the implementation of 
 * co-training and RSSalg, a co-training based technique that can be applied to single-view 
 * datasets published in the paper: 
 * 
 * Slivka, J., Kovacevic, A. and Konjovic, Z., 2013. 
 * Combining Co-Training with Ensemble Learning for Application on Single-View Natural 
 * Language Datasets. Acta Polytechnica Hungarica, 10(2).
 *   
 * RSSalg software is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RSSalg software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/
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
