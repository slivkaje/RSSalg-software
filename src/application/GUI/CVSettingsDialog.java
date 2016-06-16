package application.GUI;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import util.InstancesManipulation;
import algorithms.co_training.CoTrainingData;
import experimentSetting.CVSettings;
import experimentSetting.DatasetSettings;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class CVSettingsDialog extends JDialog {	
	private static final long serialVersionUID = 1L;
	private final JFormattedTextField tfNoFolds = new JFormattedTextField(NumberFormat.getIntegerInstance());
	private final JFormattedTextField tfNoUnlabeled = new JFormattedTextField(NumberFormat.getIntegerInstance());
	private final JFormattedTextField tfNoTest = new JFormattedTextField(NumberFormat.getIntegerInstance());
	private final JCheckBox chckbxRemoveLabels = new JCheckBox("Remove labels");
	
	private final ClassNamesTableModel classNamesTableModel = new ClassNamesTableModel();
	private final JTable tableClassNames = new JTable(classNamesTableModel);
	private final JPanel contentPanel = new JPanel();
	private final JButton okButton = new JButton("Save and close");
	
	private String saveFileLoc;
	private String lastSaveFileLoc;
	private JButton btnLoadFromFile = new JButton("Load from file");
	
	private int getNoFolds(){
		int count = 0;
		String ResultsFolderLoc = DatasetSettings.getInstance().getResultFolder();
		while(true){
			File fold = new File(ResultsFolderLoc + "/fold_" + count);
			if(fold.exists())
				count++;
			else
				break;
		}		
		return count;
	}
	
	public void setLastSaveFileLoc(String lastSaveFileLoc) {
		this.lastSaveFileLoc = lastSaveFileLoc;
	}

	private void init() throws Exception{		
		List<String> classNames = DatasetSettings.getInstance().getClassNames();		
		boolean loadExperiment = DatasetSettings.getInstance().isLoadPresetExperiment();
		
		if(!loadExperiment){
			for(int i=0; i<classNames.size(); i++)
				classNamesTableModel.addElement(new ClassNameElement(classNames.get(i), 1));
		}else{			
			String ResultsFolderLoc = DatasetSettings.getInstance().getResultFolder();			
			CoTrainingData data = new CoTrainingData(ResultsFolderLoc + "/fold_0/", DatasetSettings.getInstance().getNoViews(), false);
			
			for(int i=0; i<classNames.size(); i++){
				String classAttName = DatasetSettings.getInstance().getClassAttributeName();
				int numLabeled = InstancesManipulation.getNumberOfInstancesWithAttValue(data.getLabeledData()[0], classAttName, classNames.get(i));
				classNamesTableModel.addElement(new ClassNameElement(classNames.get(i), numLabeled));
			}					
			tableClassNames.setEnabled(false);
			
			tfNoFolds.setEnabled(false);
			tfNoFolds.setText(""+getNoFolds());
			
			tfNoUnlabeled.setEnabled(false);
			tfNoUnlabeled.setText("");
			
			tfNoTest.setEnabled(false);
			tfNoTest.setText("");
			chckbxRemoveLabels.setEnabled(false);
			okButton.setEnabled(false);
			btnLoadFromFile.setEnabled(false);
		}	
		
		tableClassNames.getColumnModel().getColumn(0).setPreferredWidth(210);
		tableClassNames.getColumnModel().getColumn(1).setPreferredWidth(294-210);
	}

	private boolean allEntriesCorrect(){
		try{
			int noFolds = 0;
			try{
				noFolds = Integer.parseInt(tfNoFolds.getText());
				if(noFolds < 1)
					throw new Exception("Error setting number of folds for cross-validation: there must be at least one fold.");			
			}catch(NumberFormatException ex){
				throw new Exception("Error setting number of folds for cross-validation: value '" + Integer.parseInt(tfNoFolds.getText()) + "' is not a valid int value.");
			}
			
			int noUnlabeled = 0;
			try{
				noUnlabeled = Integer.parseInt(tfNoUnlabeled.getText().replace(",", ""));
				if(noUnlabeled < 1)
					throw new Exception("Error setting number of folds for unlabeled data: there must be at least one fold.");
					
			}catch(NumberFormatException ex){
				throw new Exception("Error setting number of unlabeled folds: value '" + Integer.parseInt(tfNoUnlabeled.getText().replace(",", "")) + "' is not a valid int value.");
			}
			
			int noTest = 0;
			try{
				noTest = Integer.parseInt(tfNoTest.getText().replace(",", ""));
				if(noTest < 1)
					throw new Exception("Error setting number of folds for test data: there must be at least one fold.");
					
			}catch(NumberFormatException ex){
				throw new Exception("Error setting number of test folds: value '" + Integer.parseInt(tfNoTest.getText().replace(",", "")) + "' is not a valid int value.");
			}
			
			if (noUnlabeled + noTest != noFolds)
				throw new Exception("Error setting the number of folds for cross-validation: number of folds for unlabeled "
						+ "data and number of folds for test data must add up to the total number of folds (" + noFolds + ")");
			
			for(int row=0; row<classNamesTableModel.getRowCount(); row++){			
				int noLabeled = (int) classNamesTableModel.getValueAt(row, 1);
				if(noLabeled <= 0)
					throw new Exception("Error setting number of labeled examples for class \'" + classNamesTableModel.getValueAt(row, 0) + "\': "
								+ "there must be at least one labeled instance.");
			}
				
			return true;
		}catch(Exception e){
			JOptionPane.showMessageDialog(CVSettingsDialog.this, e.getMessage(), "Error saving CV settings", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}
	
	private void saveCVPropertiesToFile(String path) throws FileNotFoundException, IOException{
		Properties cvPropertiesFile = new Properties();		
		cvPropertiesFile.setProperty("noFolds", ""+Integer.parseInt(tfNoFolds.getText()));
		cvPropertiesFile.setProperty("noUnlabeled", ""+Integer.parseInt(tfNoUnlabeled.getText().replace(",", "")));
		cvPropertiesFile.setProperty("noTest", ""+Integer.parseInt(tfNoTest.getText().replace(",", "")));
		
		if(chckbxRemoveLabels.isSelected()){
			cvPropertiesFile.setProperty("removeLabels", "true");
		}else
			cvPropertiesFile.setProperty("removeLabels", "false");
		
		cvPropertiesFile.setProperty("className", classNamesTableModel.getClassNamesAsString());
		cvPropertiesFile.setProperty("noLabeled", classNamesTableModel.getNoLabeledAsString());
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		cvPropertiesFile.store(new FileOutputStream(path + "/cv.properties"), dateFormat.format(cal.getTime()));
	}
	
	private void getCurrentSettings() throws Exception{
		if(!CVSettings.getInstance().isInitiated()){ // no current CV settings, init by DataSettings
			init();
		}else{
			classNamesTableModel.removeAllElements();
			Map<String, Integer> noLabeled = CVSettings.getInstance().getNoLabeled();
			for(String className : noLabeled.keySet()){
				classNamesTableModel.addElement(new ClassNameElement(className, noLabeled.get(className)));
			}
			tfNoUnlabeled.setText(""+CVSettings.getInstance().getNoFoldsUnlabeled());
			tfNoTest.setText(""+CVSettings.getInstance().getNoFoldsTest());
			tfNoFolds.setText(""+CVSettings.getInstance().getNoFolds());
			chckbxRemoveLabels.setSelected(CVSettings.getInstance().isRemoveLabelsFromUnlabeled());
		}
	}
	
	private void loadProperties(String filePath) throws Exception{
		CVSettings.getInstance().readProperties(filePath);
		getCurrentSettings();	
	}
	
	/**
	 * Create the dialog.
	 * @param owner parent dialogue
	 * @throws Exception if loading a preset experiment and co-training data cannot be loaded from the specified folder (missing or corrupt files)
	 */
	public CVSettingsDialog(JFrame owner) throws Exception {
		super(owner);
		setTitle("Cross-validation settings");
		setResizable(false);
		setModal(true);
		setBounds(100, 100, 351, 307);
		setLocationRelativeTo(owner);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		{
			JLabel label = new JLabel("Number of folds:");
			label.setHorizontalAlignment(SwingConstants.RIGHT);
			label.setBounds(22, 200, 101, 16);
			contentPanel.add(label);
		}
		{
			tfNoFolds.setText("10");
			tfNoFolds.setEnabled(false);
			tfNoFolds.setBounds(131, 197, 50, 22);
			contentPanel.add(tfNoFolds);
		}
		{
			JLabel label = new JLabel("Unlabeled folds:");
			label.setHorizontalAlignment(SwingConstants.RIGHT);
			label.setBounds(22, 146, 101, 16);
			contentPanel.add(label);
		}
		{
			tfNoUnlabeled.setText("6");
			tfNoUnlabeled.setBounds(131, 143, 50, 22);
			contentPanel.add(tfNoUnlabeled);
			tfNoUnlabeled.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							int noUnlabeled = Integer.parseInt(tfNoUnlabeled.getText().replace(",", ""));
							int noTest = Integer.parseInt(tfNoTest.getText().replace(",", ""));
							tfNoFolds.setText("" + (noTest + noUnlabeled));
						}
					});
					super.focusLost(e);
				}
			});
		}
		{
			JLabel label = new JLabel("Test folds:");
			label.setHorizontalAlignment(SwingConstants.RIGHT);
			label.setBounds(57, 172, 66, 16);
			contentPanel.add(label);
		}
		{			
			tfNoTest.setText("4");
			tfNoTest.setBounds(131, 170, 50, 22);
			contentPanel.add(tfNoTest);
			tfNoTest.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							int noUnlabeled = Integer.parseInt(tfNoUnlabeled.getText().replace(",", ""));
							int noTest = Integer.parseInt(tfNoTest.getText().replace(",", ""));
							tfNoFolds.setText("" + (noTest + noUnlabeled));
						}
					});
					super.focusLost(e);
				}
			});
		}
		{
			chckbxRemoveLabels.setBounds(189, 142, 113, 25);
			contentPanel.add(chckbxRemoveLabels);
		}
		{
			JPanel panel = new JPanel();
			panel.setLayout(null);
			panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Labeled instances", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			panel.setBounds(12, 13, 318, 124);
			contentPanel.add(panel);
			{
				JScrollPane scrollPane = new JScrollPane();
				scrollPane.setBounds(12, 24, 294, 87);
				panel.add(scrollPane);
				{					
					scrollPane.setViewportView(tableClassNames);
				}
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{			
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(allEntriesCorrect()){				
							JFileChooser chooser = new JFileChooser();
							chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);		
							chooser.setCurrentDirectory(new File(lastSaveFileLoc));
							int returnVal = chooser.showSaveDialog(CVSettingsDialog.this);
							if(returnVal == JFileChooser.APPROVE_OPTION) {	
								try{
									saveCVPropertiesToFile(chooser.getSelectedFile().getAbsolutePath());
									saveFileLoc = chooser.getSelectedFile().getAbsolutePath();
									setVisible(false);
									dispose();
								}catch(Exception ex){
									JOptionPane.showMessageDialog(CVSettingsDialog.this, "Error saving cross-validation settings to file '" 
											+ chooser.getSelectedFile().getAbsolutePath() + "'. Reason: " + ex.getMessage(), "Error saving CV settings", JOptionPane.ERROR_MESSAGE);
								}								
							}
						}	
					}
				});
				{
					
					btnLoadFromFile.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							JFileChooser chooser = new JFileChooser();				
							chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);			
							chooser.setCurrentDirectory(new File(lastSaveFileLoc));
							int returnVal = chooser.showOpenDialog(CVSettingsDialog.this);
							if(returnVal == JFileChooser.APPROVE_OPTION) {								
								String filePath = chooser.getSelectedFile().getAbsolutePath() + "/cv.properties";
								try{
									loadProperties(filePath);						
								}catch(Exception ex){
									JOptionPane.showMessageDialog(CVSettingsDialog.this, "Error loading cross-validation properties from file '" + filePath + "'.\nReason: " + ex.getMessage(),
											"Error loading CV settings", JOptionPane.ERROR_MESSAGE); 
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
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
				
		tableClassNames.setRowSelectionAllowed(false);
		tableClassNames.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		tableClassNames.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){			
			private static final long serialVersionUID = 1L;
			public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column){
				setEnabled(table == null || table.isEnabled());
				super.getTableCellRendererComponent(table, value, selected, focused, row, column);
				return this;
	    }
		});
		
		getCurrentSettings();
	}

	public String showDialog(){
		setVisible(true);
		if(saveFileLoc != null)
			return saveFileLoc + "\\cv.properties";
		else
			return null;
	}
}
