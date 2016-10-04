/** 	
 * Name: CoTrainingPropertiesDialog.java
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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;

import experimentSetting.CoTrainingSettings;
import experimentSetting.DatasetSettings;
import javax.swing.SwingConstants;



public class CoTrainingPropertiesDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private JFormattedTextField tfK;
	private JRadioButton rdbtnNoIts = new JRadioButton("Specify number of iterations:");
	private JRadioButton rdbtnAllUnlabeled = new JRadioButton("Label all unlabeled instances");
	private final JPanel contentPanel = new JPanel();
	private JFormattedTextField tfPoolSize;
	private JCheckBox chckbxTestEachIteration = new JCheckBox("Test each iteration");
	private String lastSaveFileLoc;
	private String saveFileLoc = null;

	ClassNamesTableModel classNamesTableModel = new ClassNamesTableModel();
	private JTable tableClassNames = new JTable(classNamesTableModel);
	
	private void setClassNames(){
		List<String> classNames = DatasetSettings.getInstance().getClassNames();
		for(String className : classNames){
			classNamesTableModel.addElement(new ClassNameElement(className, 1));
		}
		tableClassNames.getColumnModel().getColumn(0).setPreferredWidth(160);
		tableClassNames.getColumnModel().getColumn(1).setPreferredWidth(tableClassNames.getWidth()-160);
	}
	
	public void setLastSaveFileLoc(String lastSaveFileLoc){
		this.lastSaveFileLoc = lastSaveFileLoc;
	}
	
	private void checkAllEntriesCorrect() throws Exception{
		if(rdbtnNoIts.isSelected())
		try{
			int its = Integer.parseInt(tfK.getText().replace(",", ""));
			if(its<1)
				throw new Exception("There must be at least one iteration of co-training");
		}catch(NumberFormatException e){
			throw new Exception("Error reading number of co-training iterations: '" + tfK.getText().replace(",", "") + "' is not a valid int value.");
		}
		
		try{
			int pool = Integer.parseInt(tfPoolSize.getText().replace(",", ""));
			if(pool<0)
				throw new Exception("The size of the pool must be greater or equal than 0.");
		}catch(NumberFormatException e){
			throw new Exception("Error reading pool size: '" + tfPoolSize.getText().replace(",", "") + "' is not a valid int value.");
		}
		
		int totalGrowthSize = 0;
		for(int i=0; i<classNamesTableModel.getRowCount(); i++){
			try{
				int growthSize = (int) classNamesTableModel.getValueAt(i, 1);
				if(growthSize < 0)
					throw new Exception("Error reading growth size for class " + classNamesTableModel.getValueAt(i, 0) + ": growth size must be greater or equal than 0");
				totalGrowthSize += growthSize;
			}catch(NumberFormatException e){
				throw new Exception("Error reading growth size for class " + classNamesTableModel.getValueAt(i, 0) + ": '" + classNamesTableModel.getValueAt(i, 1) + "' is not a valid int value.");
			}
		}
		if(totalGrowthSize == 0)
			throw new Exception("Error setting growth size: at least 1 example must be labeled in each iteration of co-training.");
		
	}
	
	private void saveCTSettings(String path){
		try{
			Properties ctPropertiesFile = new Properties();
			
			if(rdbtnNoIts.isSelected()){
				ctPropertiesFile.setProperty("labelAllUnlabeledData", "false");
				int its = Integer.parseInt(tfK.getText().replace(",", ""));
				ctPropertiesFile.setProperty("coTrainingIterations", ""+its);
			}else{
				ctPropertiesFile.setProperty("labelAllUnlabeledData", "true");
				ctPropertiesFile.setProperty("coTrainingIterations", "");
			}
			
			int pool = Integer.parseInt(tfPoolSize.getText().replace(",", ""));
			ctPropertiesFile.setProperty("poolSize", ""+pool);
			
			String classNamesSt = "";
			String growthSizeSt = "";
			for(int i=0; i<classNamesTableModel.getRowCount(); i++){
				String className = (String) classNamesTableModel.getValueAt(i, 0);
				classNamesSt += "\"" + className + "\" ";
				int growthSize = (int) classNamesTableModel.getValueAt(i, 1);
				growthSizeSt += growthSize + " ";
			}
			ctPropertiesFile.setProperty("className",  classNamesSt);
			ctPropertiesFile.setProperty("growthSize",  growthSizeSt);
			
			if(chckbxTestEachIteration.isSelected()){
				ctPropertiesFile.setProperty("testEachIteration",  "true");
			}else{
				ctPropertiesFile.setProperty("testEachIteration",  "false");
			}
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Calendar cal = Calendar.getInstance();
			FileOutputStream out = new FileOutputStream(path + File.separator + "co-training.properties");
			ctPropertiesFile.store(out, dateFormat.format(cal.getTime()));
			out.flush();
			out.close();
			
			ctPropertiesFile.store(new FileOutputStream(path + File.separator + "co-training.properties"), dateFormat.format(cal.getTime()));			 
			saveFileLoc = path;
		}catch(Exception e){
			JOptionPane.showMessageDialog(CoTrainingPropertiesDialog.this, "Error saving co-training settings to file '" 
					+ path + File.separator + "co-training.properties" + "'. Reason: " + e.getMessage(), "Error saving co-training settings", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void getCurrentSettings() throws Exception{
		CoTrainingSettings ctSettings = CoTrainingSettings.getInstance();
		if(!ctSettings.isInitiated()){ // CoTraining settings haven't been initialized yet
			return;
		}
		
		if(CoTrainingSettings.getInstance().isLabelAllUnlabeled())
			rdbtnAllUnlabeled.setSelected(true);
		else{
			rdbtnNoIts.setSelected(true);
			tfK.setText("" + CoTrainingSettings.getInstance().getIterations());
		}
		classNamesTableModel.removeAllElements();
		Map<String,Integer> growthSize = CoTrainingSettings.getInstance().getGrowthSize();
		for(String className : growthSize.keySet()){
			classNamesTableModel.addElement(new ClassNameElement(className, growthSize.get(className)));
		}
		if(ctSettings.isTestEachIteration())
			chckbxTestEachIteration.setSelected(true);
		else
			chckbxTestEachIteration.setSelected(false);
	}
	
	private void loadProperties(String filePath) throws Exception{
		CoTrainingSettings.getInstance().readProperties(filePath);
		getCurrentSettings();
	}
	
	/**
	 * Create the dialog.
	 * @param owner parent dialogue
	 */
	public CoTrainingPropertiesDialog(JFrame owner) {
		super(owner, true);
		setTitle("Co-training settings");
		setResizable(false);
		setBounds(100, 100, 409, 322);
		setLocationRelativeTo(owner);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		NumberFormat amountFormat = NumberFormat.getIntegerInstance();
		
		{
			tfK = new JFormattedTextField(amountFormat);
			tfK.setText("20");
			tfK.setBounds(245, 10, 148, 22);
			contentPanel.add(tfK);
		}
		ButtonGroup group = new ButtonGroup();
		{
			rdbtnNoIts.setHorizontalAlignment(SwingConstants.LEFT);
			rdbtnNoIts.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(rdbtnAllUnlabeled.isSelected()){
						tfK.setEnabled(false);
					}else{
						tfK.setEnabled(true);
					}
				}
			});
			
			rdbtnNoIts.setBounds(8, 9, 231, 25);
			contentPanel.add(rdbtnNoIts);
			group.add(rdbtnNoIts);
			rdbtnNoIts.setSelected(true);
		}
		{
			rdbtnAllUnlabeled.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(rdbtnAllUnlabeled.isSelected()){
						tfK.setEnabled(false);
					}else{
						tfK.setEnabled(true);
					}
				}
			});
			
			rdbtnAllUnlabeled.setBounds(8, 39, 385, 25);
			contentPanel.add(rdbtnAllUnlabeled);
			group.add(rdbtnAllUnlabeled);
		}
		{
			JLabel lblPoolSize = new JLabel("Pool size:");
			lblPoolSize.setBounds(18, 73, 70, 16);
			contentPanel.add(lblPoolSize);
		}
		{
			tfPoolSize = new JFormattedTextField(amountFormat);
			tfPoolSize.setText("50");
			tfPoolSize.setBounds(97, 70, 70, 22);
			contentPanel.add(tfPoolSize);
		}
		{
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBounds(8, 124, 385, 92);
			contentPanel.add(scrollPane);
			{
				tableClassNames.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
				tableClassNames.setRowSelectionAllowed(false);
				scrollPane.setViewportView(tableClassNames);
			}
		}
		{
			JLabel lblGrowthSizePer = new JLabel("Growth size per iteration:");
			lblGrowthSizePer.setBounds(8, 100, 328, 16);
			contentPanel.add(lblGrowthSizePer);
		}
		{
			
			chckbxTestEachIteration.setBounds(8, 226, 328, 25);
			contentPanel.add(chckbxTestEachIteration);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("Save and close");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try{
							checkAllEntriesCorrect();
							
							JFileChooser chooser = new JFileChooser();
							chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);		
							chooser.setCurrentDirectory(new File(lastSaveFileLoc));
							int returnVal = chooser.showSaveDialog(CoTrainingPropertiesDialog.this);
							if(returnVal == JFileChooser.APPROVE_OPTION) {								
								saveCTSettings(chooser.getSelectedFile().getAbsolutePath());
							}
							setVisible(false);
							dispose();
						}catch(Exception ex){
							JOptionPane.showMessageDialog(CoTrainingPropertiesDialog.this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
						}
							
					}
				});
				{
					JButton btnLoadFromFile = new JButton("Load from file");
					btnLoadFromFile.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							JFileChooser chooser = new JFileChooser();				
							chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);			
							chooser.setCurrentDirectory(new File(lastSaveFileLoc));
							int returnVal = chooser.showOpenDialog(CoTrainingPropertiesDialog.this);
							if(returnVal == JFileChooser.APPROVE_OPTION) {								
								String filePath = chooser.getSelectedFile().getAbsolutePath() + File.separator + "co-training.properties";
								try{
									loadProperties(filePath);						
								}catch(Exception ex){
									JOptionPane.showMessageDialog(CoTrainingPropertiesDialog.this, "Error loading co-training properties from file '" + filePath + "'.\nReason: " + ex.getMessage(),
											"Error loading co-training settings", JOptionPane.ERROR_MESSAGE); 
								}
							}
						}
					});
					buttonPane.add(btnLoadFromFile);
				}
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						saveFileLoc = null;
						setVisible(false);
						dispose();
					}
				});
				buttonPane.add(cancelButton);
			}
		}
		
		setClassNames();
		try {
			getCurrentSettings();
		} catch (Exception e) {			
			e.printStackTrace();
		}
		
		
	}

	public String showDialog(){
		setVisible(true);
		if(saveFileLoc != null)
			return saveFileLoc + File.separator + "co-training.properties";
		else
			return null;
	}
}
