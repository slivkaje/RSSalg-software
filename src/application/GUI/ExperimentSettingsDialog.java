package application.GUI;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileView;
import javax.xml.bind.JAXBException;

import classificationResult.measures.MeasureIF;
import algorithms.RSSalg.resultStatistic.ClassifierEnsembleList;
import experimentSetting.DatasetSettings;
import experimentSetting.ExperimentSettings;

public class ExperimentSettingsDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private MeasuresTableModel measuresTableModel = new MeasuresTableModel();
	private JTable tableMeasures = new JTable(measuresTableModel);
	private JTextField tfLoadClassifiersFile;
	private JComboBox<String> cbAlgorithm = new JComboBox<String>();
	private JComboBox<String> cbMeasure = new JComboBox<String>();
	private JComboBox<String> cbMeasureClass = new JComboBox<String>();
	private final JComboBox<String> cbFeatureSplit = new JComboBox<String>();
	private JFormattedTextField tfNoSplits = new JFormattedTextField(NumberFormat.getIntegerInstance());
	private JButton btnBrowse = new JButton("...");
	private JCheckBox chckbxLoadClassifiers = new JCheckBox("Load classifiers");
	private JLabel lblFileName = new JLabel("File name:");
	private JCheckBox chckbxWriteEnlargedTraining = new JCheckBox("Write enlarged training set");
	private JButton btnRemove = new JButton("Remove");
	private String lastSaveFileLoc;
	private String saveFileLoc = null;
	private JCheckBox chckbxWriteClassifiers = new JCheckBox("Write training/test statistics");
	private final JButton btnLoadFromFile = new JButton("Load from file");
	
	private void getCurrentSettings(){
		if(!ExperimentSettings.getInstance().isInitiated()) // Experiment settings haven't been loaded yet
			return;
		
		String algImplClass = ExperimentSettings.getInstance().getAlgorithm().getClass().getName();
		String alg;
		switch (algImplClass) {
			case "algorithms.SupervisedAlgorithm_L":
				alg = "L";
				break;
			case "algorithms.SupervisedAlgorithm_All":
				alg = "All";
				break;
			case "algorithms.co_training.CoTraining":
				alg = "Co-training";
				break;
			case "algorithms.RSSalg.MajorityVote":
				alg = "MV";
				break;
			case "algorithms.RSSalg.RSSalg":
				if(ExperimentSettings.getInstance().getEvaluator().getClass().getName().equals("algorithms.RSSalg.GA.RSSalgCandidateEvaluator"))
					alg = "RSSalg";
				else
					alg = "RSSalg_best";
				break;			
			default:
				alg = "";
				break;
		}
		cbAlgorithm.setSelectedItem(alg);
		
		measuresTableModel.removeAllElements();
		List<MeasureIF> measures = ExperimentSettings.getInstance().getMeasures();
		for(MeasureIF measure : measures){
			String name = measure.getClass().getName();
			if(name.equals("avg"))
				name = "not specified";
			String className = measure.getClassName();
			measuresTableModel.addElementMeasureImplClass(name, className);
		}
		
		if(ExperimentSettings.getInstance().getSplitter() == null){
			if(alg.equals("Co-training"))
				cbFeatureSplit.setSelectedItem("Natural");
			else
				cbFeatureSplit.setSelectedItem("Random");
		}
		
		if(!alg.equals("L") && !alg.equals("All")){
			tfNoSplits.setText("" + ExperimentSettings.getInstance().getNoSplits());
		}
		
		chckbxLoadClassifiers.setSelected(ExperimentSettings.getInstance().isLoadClassifierStatistic());
		if(chckbxLoadClassifiers.isSelected() || alg == "MV")
			tfLoadClassifiersFile.setText(ExperimentSettings.getInstance().getClassifiersFilename());
		else{
			if(!alg.equals("All") && !alg.equals("L"))
				tfNoSplits.setEnabled(true);
		}
		
		chckbxWriteClassifiers.setSelected(ExperimentSettings.getInstance().isWriteClassifiers());
		chckbxWriteEnlargedTraining.setSelected(ExperimentSettings.getInstance().isWriteEnlargedCoTrainingSet());
	}
	
	private void setClassNames(){
		List<String> classNames = DatasetSettings.getInstance().getClassNames();
		for(String className : classNames){
			cbMeasureClass.addItem(className);			
		}		
	} 
	
	public void setLastSaveFileLoc(String lastSaveFileLoc){
		this.lastSaveFileLoc = lastSaveFileLoc;
	}
	
	private String getAlgorithmImplementationClass(){
		String algorithm = (String) cbAlgorithm.getSelectedItem();
		switch(algorithm){
			case "L": 
				return "algorithms.SupervisedAlgorithm_L";
			case "All": 
				return "algorithms.SupervisedAlgorithm_All";
			case "Co-training": 
				return "algorithms.co_training.CoTraining";
			case "MV": 
				return "algorithms.RSSalg.MajorityVote";
			case "RSSalg":
			case "RSSalg_best":				
				return "algorithms.RSSalg.RSSalg";		
			default:
				return null;
		}
	}
	
	private String getCandidateEvaluatorImpl(){
		String algorithm = (String) cbAlgorithm.getSelectedItem();
		switch(algorithm){
			case "RSSalg": return "algorithms.RSSalg.GA.RSSalgCandidateEvaluator";
			case "RSSalg_best": return "algorithms.RSSalg.GA.TestSetAccuracyCandidateEvaluator";
			default: return "";
		}
	}
	
	private String getFeatureSplitImplClass(){
		String split = (String) cbFeatureSplit.getSelectedItem();
		switch (split) {
			case "Random": return "featureSplit.DifferentRandomSplitsSplitter";
			default: return ""; // none or natural
		}
	}
	
	private String getVoterImplClass(){
		String algorithm = (String) cbAlgorithm.getSelectedItem();
		switch (algorithm) {
			case "MV": return "algorithms.RSSalg.voter.MajorityVoter";
			default: return ""; // none or natural
		}
	}
	
	private void checkAllEntriesValid() throws Exception{
		if(measuresTableModel.getRowCount() == 0)
			throw new Exception("Error: Specify at least one measure for experiment validation.");
		if(chckbxLoadClassifiers.isSelected()){
			if (tfLoadClassifiersFile.getText().equals(""))
				throw new Exception("Error loading classifier statitstics: file not specified.");
		}		
		try{
			String split = (String) cbFeatureSplit.getSelectedItem();
			int noSplits = Integer.parseInt(tfNoSplits.getText().replace(",", ""));
			if(!split.equals("None") &&noSplits < 1)
				throw new Exception("Error: there must be at least 1 feature split.");
		}catch(NumberFormatException ex){
			System.out.println("Error specifying number of splits: '" + tfNoSplits.getText().replace(",", "") + "' is not a valid int value.");
		}
	}
	
	private void saveSettings(String path){
		Properties experimentProperties = new Properties();
		
		String algorithm = (String) cbAlgorithm.getSelectedItem();
		String algImplClass = getAlgorithmImplementationClass();
		experimentProperties.setProperty("algorithm", algImplClass);
		
		String measureImplClasses = measuresTableModel.getMeasureImplementationClasses();
		experimentProperties.setProperty("measures", measureImplClasses);
		String classNamesForMeasures = measuresTableModel.getClassNamesForMeasures(); 
		experimentProperties.setProperty("measuresForClass", classNamesForMeasures);
		
		String split = (String) cbFeatureSplit.getSelectedItem();
		String featureSplitImplClass = getFeatureSplitImplClass();
		experimentProperties.setProperty("featureSpliter", featureSplitImplClass);
		
		String noSplits = tfNoSplits.getText().replace(",", ""); 
		experimentProperties.setProperty("noSplits", noSplits);
		if(!algorithm.equals("MV"))
			experimentProperties.setProperty("loadClassifiers", ""+chckbxLoadClassifiers.isSelected());
		else
			experimentProperties.setProperty("loadClassifiers", "false");
		experimentProperties.setProperty("ClassifiersFilename", tfLoadClassifiersFile.getText());		
		experimentProperties.setProperty("writeClassifiers", ""+chckbxWriteClassifiers.isSelected());
		experimentProperties.setProperty("writeEnlargedTrainingSet", ""+chckbxWriteEnlargedTraining.isSelected());
		String candidateEvaluator = getCandidateEvaluatorImpl();
		experimentProperties.setProperty("candidateEvaluator", candidateEvaluator);
		
		String voter = getVoterImplClass();
		experimentProperties.setProperty("voter", voter);
		
		String filename = "experiment_" + algorithm;
		if(algorithm.equals("Co-training"))
			filename += "_" + split;
		filename = path + "/" + filename + ".properties";		
		
		try{
			FileOutputStream out = new FileOutputStream(filename);		
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Calendar cal = Calendar.getInstance();
			experimentProperties.store(out, dateFormat.format(cal.getTime()));
			saveFileLoc = filename;
		}catch(IOException ex){
			String message = "Error saving experiment settings to \n'" + filename + "'";
			JOptionPane.showMessageDialog(ExperimentSettingsDialog.this, message, "Error saving experiment settings", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void loadProperties(String filePath) throws Exception {
		ExperimentSettings.getInstance().readProperties(filePath);
		getCurrentSettings();
	}

	/**
	 * Create the dialog.
	 * @param owner parent dialogue
	 */
	public ExperimentSettingsDialog(JFrame owner) {
		super(owner, true);
		setTitle("Experiment settings");
		setBounds(100, 100, 528, 526);
		setLocationRelativeTo(owner);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		{			
			cbAlgorithm.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String algorithm = (String) cbAlgorithm.getSelectedItem(); 
							
					// load classifiers
					if(algorithm.equals("MV")){
						chckbxLoadClassifiers.setSelected(true);
						chckbxLoadClassifiers.setEnabled(false);
						lblFileName.setEnabled(true);
						btnBrowse.setEnabled(true);						
					}else if(algorithm.contains("RSSalg")){						
						chckbxLoadClassifiers.setSelected(false);
						chckbxLoadClassifiers.setEnabled(true);							
						if(!tfLoadClassifiersFile.getText().equals("")){
							ClassifierEnsembleList cl = new ClassifierEnsembleList();
							try{
								cl.fromXML(DatasetSettings.getInstance().getResultFolder() + "/fold_0/" + tfLoadClassifiersFile.getText());
							}catch(JAXBException ex){
								ex.printStackTrace();
							}
							tfNoSplits.setText("" + cl.getEnsembles().size());
						}
						lblFileName.setEnabled(true);
						btnBrowse.setEnabled(false);						
					}else{						
						chckbxLoadClassifiers.setSelected(false);
						chckbxLoadClassifiers.setEnabled(false);
						lblFileName.setEnabled(false);
						btnBrowse.setEnabled(false);
						tfLoadClassifiersFile.setText("");
					}
					
					// Feature split					
					if(algorithm.equals("L")||algorithm.equals("All")){				
						cbFeatureSplit.setModel(new DefaultComboBoxModel<String>(new String[] {"None"}));
						tfNoSplits.setEnabled(false);
						tfNoSplits.setText("0");
					}else if(algorithm.equals("Co-training")){
						cbFeatureSplit.setModel(new DefaultComboBoxModel<String>(new String[] {"Random", "Natural"}));
						cbFeatureSplit.setEnabled(true);
						tfNoSplits.setEnabled(true); // Random is selected
						tfNoSplits.setText("1");
					}else{ 
						cbFeatureSplit.setModel(new DefaultComboBoxModel<String>(new String[] {"Random"}));		
						cbFeatureSplit.setEnabled(false);
						if(chckbxLoadClassifiers.isSelected()){							
//							tfNoSplits.setText("");
							tfNoSplits.setEnabled(false);
						}else{
							tfNoSplits.setText("1");
							tfNoSplits.setEnabled(true);
						}												
					}
					
					// write enlarged training set
					if(algorithm.equals("Co-training")||algorithm.contains("RSSalg")){
						chckbxWriteEnlargedTraining.setEnabled(true);
					}else{
						chckbxWriteEnlargedTraining.setSelected(false);
						chckbxWriteEnlargedTraining.setEnabled(false);
					}					
				}
			});
			cbAlgorithm.setModel(new DefaultComboBoxModel<String>(new String[] {"L", "All", "Co-training", "MV", "RSSalg", "RSSalg_best"}));
			cbAlgorithm.setBounds(80, 13, 99, 22);
			contentPanel.add(cbAlgorithm);
		}
		{
			JLabel lblAlgorithm = new JLabel("Algorithm:");
			lblAlgorithm.setBounds(12, 16, 77, 16);
			contentPanel.add(lblAlgorithm);
		}
		{
			JPanel panelMeasures = new JPanel();
			panelMeasures.setBorder(new TitledBorder(null, "Mesures", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			panelMeasures.setBounds(12, 48, 486, 206);
			contentPanel.add(panelMeasures);
			panelMeasures.setLayout(null);
			{
				JScrollPane scrollPane = new JScrollPane();
				scrollPane.setBounds(10, 23, 464, 104);
				panelMeasures.add(scrollPane);
				{	
					tableMeasures.getSelectionModel().addListSelectionListener(new ListSelectionListener() {			
						@Override
						public void valueChanged(ListSelectionEvent e) {
							if(tableMeasures.getSelectedRow() != -1)
								btnRemove.setEnabled(true);
							else
								btnRemove.setEnabled(false);				
						}
					});
					scrollPane.setViewportView(tableMeasures);
				}
			}
			{
				JLabel label = new JLabel("Measure:");
				label.setBounds(10, 128, 66, 16);
				panelMeasures.add(label);
			}
			{				
				cbMeasure.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						String measure = (String) cbMeasure.getSelectedItem();
						if(measure.equals("Accuracy")){
							cbMeasureClass.setSelectedItem("not specified");
							cbMeasureClass.setEnabled(false);							
						}else{
							cbMeasureClass.setEnabled(true);
						}
					}
				});
				cbMeasure.setModel(new DefaultComboBoxModel<String>(new String[] {"Accuracy", "F1-measure", "Precision", "Recall"}));
				cbMeasure.setBounds(10, 146, 112, 22);
				panelMeasures.add(cbMeasure);
			}
			{
				JLabel lblForClass = new JLabel("For class:");
				lblForClass.setBounds(134, 128, 56, 16);
				panelMeasures.add(lblForClass);
			}
			{				
				cbMeasureClass.setEnabled(false);
				cbMeasureClass.setModel(new DefaultComboBoxModel<String>(new String[] {"not specified"}));
				cbMeasureClass.setBounds(134, 146, 143, 22);
				panelMeasures.add(cbMeasureClass);
			}
			{
				JButton btnAdd = new JButton("Add");
				btnAdd.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						measuresTableModel.addElement((String) cbMeasure.getSelectedItem(), (String) cbMeasureClass.getSelectedItem());
					}
				});
				btnAdd.setBounds(289, 145, 89, 25);
				panelMeasures.add(btnAdd);
			}
			{				
				btnRemove.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						int selectedIndex = tableMeasures.getSelectedRow();
						if(selectedIndex!=-1){
							measuresTableModel.removeElement(selectedIndex);
							if(measuresTableModel.getRowCount() > 0){
								if(selectedIndex == 0)
									tableMeasures.setRowSelectionInterval(0, 0);
								else
									tableMeasures.setRowSelectionInterval(selectedIndex-1, selectedIndex-1);
							}							
						}
					}
				});
				btnRemove.setEnabled(false);
				btnRemove.setBounds(384, 145, 90, 25);
				panelMeasures.add(btnRemove);
			}
		}
		{
			JLabel lblFeatureSplit = new JLabel("Feature split:");
			lblFeatureSplit.setBounds(12, 262, 84, 16);
			contentPanel.add(lblFeatureSplit);
		}
		{
			cbFeatureSplit.setEnabled(false);
			cbFeatureSplit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String split = (String) cbFeatureSplit.getSelectedItem();
					if(split.equals("Random")){
						tfNoSplits.setEnabled(true);
					}else{
						tfNoSplits.setText("1");
						tfNoSplits.setEnabled(false);
					}
						
				}
			});
			cbFeatureSplit.setModel(new DefaultComboBoxModel<String>(new String[] {"None"}));
			cbFeatureSplit.setBounds(12, 284, 105, 22);
			contentPanel.add(cbFeatureSplit);
		}
		{			
			chckbxLoadClassifiers.setEnabled(false);
			chckbxLoadClassifiers.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(chckbxLoadClassifiers.isSelected()){
						btnBrowse.setEnabled(true);
						cbFeatureSplit.setSelectedItem("Random");
						cbFeatureSplit.setEnabled(false);
						tfNoSplits.setText("");
						tfNoSplits.setEnabled(false);
						
						if(!tfLoadClassifiersFile.getText().equals("")){
							ClassifierEnsembleList cl = new ClassifierEnsembleList();
							try{
								cl.fromXML(DatasetSettings.getInstance().getResultFolder() + "/fold_0/" + tfLoadClassifiersFile.getText());
							}catch(JAXBException ex){
								ex.printStackTrace();
							}
							tfNoSplits.setText("" + cl.getEnsembles().size());
						}
					}else{
						btnBrowse.setEnabled(false);
						tfNoSplits.setText("1");
						tfNoSplits.setEnabled(true);
					}
				}
			});
			chckbxLoadClassifiers.setBounds(12, 325, 131, 25);
			contentPanel.add(chckbxLoadClassifiers);
		}
		{			
			chckbxWriteClassifiers.setSelected(true);
			chckbxWriteClassifiers.setBounds(12, 380, 191, 25);
			contentPanel.add(chckbxWriteClassifiers);
		}
		{			
			chckbxWriteEnlargedTraining.setEnabled(false);
			chckbxWriteEnlargedTraining.setBounds(12, 410, 191, 25);
			contentPanel.add(chckbxWriteEnlargedTraining);
		}
		{
			JLabel lblNumberOfSplits = new JLabel("Number of splits:");
			lblNumberOfSplits.setBounds(129, 267, 105, 16);
			contentPanel.add(lblNumberOfSplits);
		}
		{			
			tfNoSplits.setEnabled(false);
			tfNoSplits.setText("0");
			tfNoSplits.setBounds(129, 284, 105, 22);
			contentPanel.add(tfNoSplits);
		}
		{
			lblFileName.setEnabled(false);
			
			lblFileName.setBounds(36, 355, 87, 16);
			contentPanel.add(lblFileName);
		}
		{
			tfLoadClassifiersFile = new JTextField();
			tfLoadClassifiersFile.setEditable(false);
			tfLoadClassifiersFile.setBounds(103, 353, 348, 22);
			contentPanel.add(tfLoadClassifiersFile);
			tfLoadClassifiersFile.setColumns(10);
		}
		{			
			btnBrowse.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					final File resultsDir = new File(DatasetSettings.getInstance().getResultFolder() + "/fold_0/");
					if(!resultsDir.exists()){
						JOptionPane.showMessageDialog(ExperimentSettingsDialog.this, "Error: classifiers file must be located in the results folder (specified in"
								+ "data settings). Current setting of results folder\n'" + DatasetSettings.getInstance().getResultFolder() + "'\ndoes not exist or does not contain subfolder 'fold_0'.\n"
										+ "Run another experiment first in order to create a classifier statistic for loading.",
								"Error", JOptionPane.ERROR_MESSAGE);								
						return;
					}										
					JFileChooser chooser = new JFileChooser(resultsDir);
					chooser.setFileView(new FileView() {
						@Override
						public Boolean isTraversable(File f) {
							return resultsDir.equals(f);
						}
					});
					
					
					chooser.setFileFilter(new FileFilter() {
						
						@Override
						public String getDescription() {
							String algorithm = (String) cbAlgorithm.getSelectedItem();
							if(algorithm.equals("MV")){
								return "Classifier test statistic";
							}else
								return "Classifier train statistic";						
						}
						
						@Override
						public boolean accept(File f) {
							String lowercaseName = f.getName().toLowerCase();
							if(lowercaseName.endsWith(".xml")){
								String algorithm = (String) cbAlgorithm.getSelectedItem();
								if(algorithm.equals("MV")){
									// accept only test statistic
									if (lowercaseName.startsWith("classifiers_test_"))
										return true;
									else 
										return false;
								}else{
									// accept only train statistic
									if (lowercaseName.contains("_test_"))
										return false;
									else 
										return true;
								}
							}
							return false;
						}
					});
					
					
//					FileNameExtensionFilter filter = new FileNameExtensionFilter("xml files", "xml", "XML");
//					chooser.setFileFilter((FileFilter) fileNameFilter);
					int returnVal = chooser.showOpenDialog(ExperimentSettingsDialog.this);
					if(returnVal == JFileChooser.APPROVE_OPTION) {
						try{
							ClassifierEnsembleList cl = new ClassifierEnsembleList();
							cl.fromXML(chooser.getSelectedFile().getAbsolutePath());
							tfLoadClassifiersFile.setText(chooser.getSelectedFile().getName());
							tfNoSplits.setText("" + cl.getEnsembles().size());
						}catch(JAXBException ex){
							JOptionPane.showMessageDialog(ExperimentSettingsDialog.this, "Error: '" + chooser.getSelectedFile().getAbsolutePath() + "' is not a valid "
									+ "training classifier statistics file: " + ex.getMessage(), "Error loading classifiers file", JOptionPane.ERROR_MESSAGE); 
						}							
					}
				}
			});
			btnBrowse.setEnabled(false);
			btnBrowse.setBounds(462, 351, 36, 25);
			contentPanel.add(btnBrowse);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton btnSave = new JButton("Save and close");
				btnSave.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try{
							checkAllEntriesValid();
							
							JFileChooser chooser = new JFileChooser();
							chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);		
							chooser.setCurrentDirectory(new File(lastSaveFileLoc));
							int returnVal = chooser.showSaveDialog(ExperimentSettingsDialog.this);
							if(returnVal == JFileChooser.APPROVE_OPTION) {			
								try{
									saveSettings(chooser.getSelectedFile().getAbsolutePath());
									setVisible(false);
									dispose();
								}catch(Exception ex){
									JOptionPane.showMessageDialog(ExperimentSettingsDialog.this, "Error saving experiment settings to: '" + 
											chooser.getSelectedFile().getAbsolutePath() + "' reason: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
								}								
							}
						}catch(Exception ex){
							JOptionPane.showMessageDialog(ExperimentSettingsDialog.this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				});
				{
					btnLoadFromFile.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							JFileChooser chooser = new JFileChooser();											
							FileNameExtensionFilter filter = new FileNameExtensionFilter("properties files", "properties");
							chooser.setFileFilter(filter);							
							chooser.setCurrentDirectory(new File(lastSaveFileLoc));
							int returnVal = chooser.showOpenDialog(ExperimentSettingsDialog.this);
							if(returnVal == JFileChooser.APPROVE_OPTION) {								
								String filePath = chooser.getSelectedFile().getAbsolutePath();
								try{
									loadProperties(filePath);						
								}catch(Exception ex){
									JOptionPane.showMessageDialog(ExperimentSettingsDialog.this, "Error loading experiment properties from file '" + filePath + "'.\nReason: " + ex.getMessage(),
											"Error loading experiment settings", JOptionPane.ERROR_MESSAGE); 
								}
							}
						}
					});
					buttonPane.add(btnLoadFromFile);
				}
				btnSave.setActionCommand("OK");
				buttonPane.add(btnSave);
				getRootPane().setDefaultButton(btnSave);
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
		setClassNames();
		getCurrentSettings();
	}
	
	public String showDialog(){
		setVisible(true);
		if(saveFileLoc != null)
			return saveFileLoc;
		else
			return null;
	}
}
