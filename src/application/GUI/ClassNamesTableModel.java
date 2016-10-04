/** 	
 * Name: ClassNamesTableModel.java
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

public class ClassNamesTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private String[] columnNames = {"class name", "no. labeled"};
	private final LinkedList<ClassNameElement> list;

    public ClassNamesTableModel() {
        list = new LinkedList<ClassNameElement>();
    }

    public void addElement(ClassNameElement e) {
        // Adds the element in the last position in the list
        list.add(e);
        fireTableRowsInserted(list.size()-1, list.size()-1);
    }
    
    public void removeAllElements() {
        list.clear();
        fireTableRowsInserted(list.size()-1, list.size()-1);
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public int getRowCount() {
        return list.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch(columnIndex) {
            case 0: return list.get(rowIndex).getClassName();
            case 1: return list.get(rowIndex).getNoLabeled();
        }
        return null;
    }

	@Override
	public String getColumnName(int column) {		
		return columnNames[column];
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if(columnIndex == 1)
			return true;
		return false;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if(columnIndex != 1)
			return;
		try{
			int val = Integer.parseInt((String) aValue);
			if (val >= 0)
				list.get(rowIndex).setNoLabeled(val);
		}catch(Exception e){
			
		}
	} 
	
	public LinkedList<ClassNameElement> getClassNames(){
		LinkedList<ClassNameElement> res = new LinkedList<ClassNameElement>();
		res.addAll(list);
		return res;
	}
    
	public String getClassNamesAsString(){
		String res = "";
		for(ClassNameElement el : list){
			res += "\"" + el.getClassName() + "\" ";
		}
		return res.trim();
	}
	
	public String getNoLabeledAsString(){
		String res = "";
		for(ClassNameElement el : list){
			res += el.getNoLabeled() + " ";
		}
		return res.trim();
	}

}
