package application.GUI;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.swing.ButtonGroup;
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
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;

import util.InstancesManipulation;
import util.PropertiesReader;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instances;
import algorithms.co_training.CoTrainingData;
import experimentSetting.DatasetSettings;

public class DataSettingsDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	
	//Dataset panel
	private JPanel datasetPanel = new JPanel();
	
	private JLabel labelClassAttName = new JLabel("Class attribute name:");
	private JLabel labelIdAttName = new JLabel("ID attribute name:");
	
	
	// load experiment
	private JLabel lblFolder = new JLabel("Folder:");
	private ButtonGroup group = new ButtonGroup();
	private JRadioButton rbLoadExperiment = new JRadioButton("Load experiment");
	private JButton btnLoadData = new JButton("Browse");		
	private JTextField tfLoadDataFolder;
	
	// create experiment
	private JLabel lblDataFiles = new JLabel("Data files:");
	
	private JRadioButton rbCreateACv = new JRadioButton("New experiment");
	
	private JButton btnAddDataFile = new JButton("Add");
	private JButton btnRemoveDataFile = new JButton("Remove");
	private final JLabel lblResultsFolder = new JLabel("Results folder:");
	private final JTextField tfResultsFolder = new JTextField();
	private final JButton btnResultsFolder = new JButton("...");
	private final JLabel lblRandomNumberGenerator = new JLabel("Random number generator seed:");
	private final JPanel panelExperiments = new JPanel();
	
	private java.util.List<Instances> views = new ArrayList<Instances>();
	
	DefaultComboBoxModel<String> cbModelClassAtt = new DefaultComboBoxModel<String>();
	private JComboBox<String> cbClassAtt = new JComboBox<>(cbModelClassAtt);
	
	DefaultComboBoxModel<String> cbModelIdAtt = new DefaultComboBoxModel<String>();
	private final JComboBox<String> cbIdAtt = new JComboBox<String>(cbModelIdAtt);
	
	
	
	StringListTabelModel learningModelsTabeModel = new StringListTabelModel("learning model", true);
	private final JTable tableLearningModels = new JTable(learningModelsTabeModel);
	
	private final JPanel panelLearningModels = new JPanel();
	private final JScrollPane scrollPane_1 = new JScrollPane();
	private final JLabel lblCombinedViewsModel = new JLabel("Combined views model:");
	private final JTextField tfCombinedViewsModel = new JTextField();
	private final JScrollPane spViews = new JScrollPane();
	
	StringListTabelModel viewsTabeModel = new StringListTabelModel("file", false);
	private final JTable tableViews = new JTable(viewsTabeModel);
	private final JButton btnSave = new JButton("Save and close");
	private final JButton btnLoad = new JButton("Load from file");
	private final JFormattedTextField tfRandomSeed;
	private final JCheckBox chckbxUseForAll = new JCheckBox("Use for all views");
	private final JLabel lblNumberOfViews = new JLabel("Number of views:");
	private final JFormattedTextField tfNoViews = new JFormattedTextField();
	private final JButton btnCancel = new JButton("Cancel");
	
	private String saveFileLoc = null;
	private String lastViewFileLoc = "." + File.separator + "data";
	private final JLabel lblWhenTiedClassify = new JLabel("When tied classify as:");
	private final JComboBox<String> cbFirstClass = new JComboBox<String>();
	
	private Attribute getAttribute(String attribute){
		for (Instances view : views){
			for(int attInd=0; attInd <view.numAttributes(); attInd++){
				if (view.attribute(attInd).name().equals(attribute))
					return view.attribute(attInd);
			}
		}
		return null;
	}
	
	private void updateViewModelsTable(int index){
		if(learningModelsTabeModel.getRowCount() < index){
			learningModelsTabeModel.addElement("weka.classifiers.bayes.NaiveBayes");
		}else{
			learningModelsTabeModel.removeElement(index);
		}
			
		tableLearningModels.getColumnModel().getColumn(0).setPreferredWidth(25);
		tableLearningModels.getColumnModel().getColumn(1).setPreferredWidth(294-25);
	}
	
	private java.util.Map<String, Boolean> getViewAtts(Instances view){
		java.util.Map<String, Boolean> attList = new HashMap<String, Boolean>(); 
		for(int attInd=0; attInd <view.numAttributes(); attInd++){
			attList.put(view.attribute(attInd).name(), view.attribute(attInd).isNominal());
		}
		return attList;
	}
	/**
	 * 
	 * @return attributes that exist in all views (class and id) 
	 */
	private java.util.Map<String, Boolean> findCommonAttributes(){
		java.util.Map<String, Boolean> attsInCommon = new HashMap<String, Boolean>(); 
		
		if(views.size() > 0){
			java.util.Map<String, Boolean> attsV1 = getViewAtts(views.get(0)); 
		
			for(String attV1 : attsV1.keySet()){ // key equals index of the view
				boolean common = true;
				for(int j=1; j<views.size(); j++){				
					java.util.Map<String, Boolean> attsV2 = getViewAtts(views.get(j));
					if(!attsV2.containsKey(attV1)){
						common = false;
						break;
					}					
				}
				if(common){
					attsInCommon.put(attV1, attsV1.get(attV1));
				}
			}
			
		}
		return attsInCommon;
	}
	
	private void updateAttributeLists(){
		cbClassAtt.removeAllItems();
		cbIdAtt.removeAllItems();
		cbModelIdAtt.addElement("generate ID");
		
		java.util.Map<String, Boolean> candidates = findCommonAttributes();
		for(String attName : candidates.keySet()){
			boolean isNominal = candidates.get(attName);
			if(isNominal)
				cbModelClassAtt.addElement(attName);
			cbModelIdAtt.addElement(attName);
		}
	}
	
	private void addDataFile(File file){
		try {			
			Instances view = InstancesManipulation.readArff(file.getAbsolutePath(), false);
			view = InstancesManipulation.removeAllInstances(view);
			views.add(view);
			
			viewsTabeModel.addElement(file.getAbsolutePath());

			tableViews.getColumnModel().getColumn(0).setMaxWidth(50);
//			tableViews.getColumnModel().getColumn(1).setPreferredWidth(tableViews.getWidth()-tableViews.getWidth()/4);
			
			lastViewFileLoc = file.getPath();						 						
			updateAttributeLists();			
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(DataSettingsDialog.this, "File \'" + file.getAbsolutePath() + "\' not recognized as an 'Arff data files' file.", "Error reading arff file", JOptionPane.ERROR_MESSAGE);
			e1.printStackTrace();
		}
	}
	
	private void setDataLoaded(boolean isLoaded){
		views.clear();
		learningModelsTabeModel.removeAllElements();
		if(isLoaded){
			tfResultsFolder.setText(tfLoadDataFolder.getText());
			try {
				int noViews = 0;
				while(true){
					File labeledViewFile = new File(tfLoadDataFolder.getText() + File.separator + "fold_0" + File.separator + "labeled_view" + noViews + ".arff");
					if(!labeledViewFile.exists())
						break;
					noViews++;
				}
				CoTrainingData data = new CoTrainingData(tfLoadDataFolder.getText() + File.separator + "fold_0" + File.separator, noViews, false);
				for(Instances view : data.getLabeledData()){
					views.add(view);
					updateViewModelsTable(views.size());
				}
				
			} catch (Exception e1) {
				e1.printStackTrace();
			}			
		}else{
			tfResultsFolder.setText("." + File.separator + "data" + File.separator + "result");
			
			int noViews = Integer.parseInt(tfNoViews.getText());
			for(int i=1; i<=noViews; i++)
				updateViewModelsTable(i);			
			
			for(int i=0; i<viewsTabeModel.getRowCount(); i++){
				String fileName = (String) viewsTabeModel.getValueAt(i, 1);
				try {
					Instances view = InstancesManipulation.readArff(fileName, false);
					view = InstancesManipulation.removeAllInstances(view);					
					views.add(view);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
		updateAttributeLists();
		
		
		btnLoadData.setEnabled(isLoaded);
		tfLoadDataFolder.setEnabled(isLoaded);
		lblFolder.setEnabled(isLoaded);
		
		tableViews.setEnabled(!isLoaded);
		btnAddDataFile.setEnabled(!isLoaded);		
		btnRemoveDataFile.setEnabled(false);
		tableViews.setEnabled(!isLoaded);
		tfResultsFolder.setEditable(false);
		tfResultsFolder.setEnabled(!isLoaded);
		btnResultsFolder.setEnabled(!isLoaded);
		
		tableViews.getSelectionModel().addListSelectionListener(new ListSelectionListener() {			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(tableViews.getSelectedRow() != -1)
					btnRemoveDataFile.setEnabled(true);
				else
					btnRemoveDataFile.setEnabled(false);				
			}
		});
		lblDataFiles.setEnabled(!isLoaded);
	}
	
	private boolean allEntriesCorrect(){
		try{
			if(views.size() == 0)
				throw new Exception("At least one view should be specified.");
						
			File resultsFolder = new File(tfResultsFolder.getText());
//			if(!resultsFolder.isDirectory())
//				throw new Exception("Error saving data properties: value entered as results folder \"" + tfResultsFolder.getText() + "\" is not a valid directory.");
			
			if(!resultsFolder.exists()){
//				System.out.println("Creating directory \"" + resultFolder + "\" for recording results.");
				boolean created = resultsFolder.mkdir();
				if(!created){
					throw new Exception("ERROR: Directory for recording results \"" + resultsFolder + "\" could not be created.");
				}
			}
			
			String classAtt = "";
			if(cbClassAtt.getSelectedIndex() == -1){
				throw new Exception("Error saving data properties: Class attribute must be specified.");
			}else{
				classAtt = cbModelClassAtt.getElementAt(cbClassAtt.getSelectedIndex());
			}
			
			String idAtt = "";
			if(cbIdAtt.getSelectedIndex() == -1){
				throw new Exception("Error saving data properties: ID attribute must be specified.");
			}else{
				idAtt = cbModelIdAtt.getElementAt(cbIdAtt.getSelectedIndex());
			}
			
			if(classAtt.equals(idAtt))
				throw new Exception("Class and ID attribute cannot be identical.");			
			
			try{
				Integer.parseInt(tfRandomSeed.getText().replaceAll(",", ""));	
			}catch(NumberFormatException e){
				throw new Exception("'" + tfRandomSeed.getText().replaceAll(",", "") + "' is not a valid int value.");
			}
			
			if(!chckbxUseForAll.isSelected())
			for(int row=0; row<learningModelsTabeModel.getRowCount(); row++){
				String learningModel = (String) learningModelsTabeModel.getValueAt(row, 1);
				try{
					@SuppressWarnings("unused")
					Classifier cl = (Classifier) PropertiesReader.getObject(learningModel);
				}catch(Exception e){
					throw new Exception("Error setting a learning model for View " + row + ": '" + learningModel + "' is not a valid WEKA classifier.");
				}
			}
			
			try{
				@SuppressWarnings("unused")
				Classifier cl = (Classifier) PropertiesReader.getObject(tfCombinedViewsModel.getText());
			}catch(Exception e){
				throw new Exception("Error setting a combined classifier model: " + tfCombinedViewsModel.getText() + "' is not a valid WEKA classifier.");
			}
			
			return true;
		}catch(Exception e){
			JOptionPane.showMessageDialog(DataSettingsDialog.this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}
	
	private void saveDataPropertiesToFile(String path){
		try{		
			Properties dataPropertiesFile = new Properties();
	
			String dataFiles = "";
			if(rbCreateACv.isSelected()){				
				for(int row=0; row<viewsTabeModel.getRowCount(); row++){
					dataFiles+= "\"" + viewsTabeModel.getValueAt(row, 1) + "\" ";
				}				
			}
			dataPropertiesFile.setProperty("dataFiles", dataFiles.trim());
			
			if(rbLoadExperiment.isSelected()){
				dataPropertiesFile.setProperty("loadPresetExperiment", "true");
			}else
				dataPropertiesFile.setProperty("loadPresetExperiment", "false");
			
			dataPropertiesFile.setProperty("noViews", "2");
			
			dataPropertiesFile.setProperty("resultFolder", tfResultsFolder.getText());
						
			String classAttName = (String) cbClassAtt.getSelectedItem();
			dataPropertiesFile.setProperty("classAttributeName", classAttName);
			
			String idAttName = (String) cbIdAtt.getSelectedItem(); 
			if(idAttName.equals("generate ID"))
				idAttName = "";
			dataPropertiesFile.setProperty("idAttributeName", idAttName);
			
			String classNames = "";
			Attribute classAtt = getAttribute(classAttName);
			String firstClassName = (String) cbFirstClass.getSelectedItem();
			classNames += "\"" + firstClassName + "\" ";
			for(int i=0; i<classAtt.numValues(); i++){	
				if(!classAtt.value(i).equals(firstClassName))
					classNames += "\"" + classAtt.value(i) + "\" ";
			}
			classNames = classNames.trim();			
			dataPropertiesFile.setProperty("classNames", classNames.trim());
			dataPropertiesFile.setProperty("randomGeneratorSeed", tfRandomSeed.getText().replaceAll(",", ""));
			
			String learningModels = "";
			if(!chckbxUseForAll.isSelected())
				for(int row=0; row<learningModelsTabeModel.getRowCount(); row++){
					learningModels += "\""+ (String) learningModelsTabeModel.getValueAt(row, 1) + "\" ";
				}
			else
				learningModels ="\"" + tfCombinedViewsModel.getText() + "\"";
			
			dataPropertiesFile.setProperty("classifiers", learningModels.trim());			
			dataPropertiesFile.setProperty("combinedClassifier", tfCombinedViewsModel.getText());
		
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Calendar cal = Calendar.getInstance();						
			dataPropertiesFile.store(new FileOutputStream(path + File.separator + "data.properties"), dateFormat.format(cal.getTime()));
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void getCurrentSettings() throws Exception{
		if(!DatasetSettings.getInstance().isInitiated()) // no DataSettings specified yet
			return;
		viewsTabeModel.removeAllElements();
		if(DatasetSettings.getInstance().isLoadPresetExperiment()){
			tfLoadDataFolder.setText(DatasetSettings.getInstance().getResultFolder());
			rbLoadExperiment.setSelected(true);
			btnLoadData.setEnabled(true);
			CoTrainingData data = new CoTrainingData(tfLoadDataFolder.getText() + File.separator + "fold_0" + File.separator, DatasetSettings.getInstance().getNoViews(), false);
			for(Instances view : data.getLabeledData()){
				views.add(view);	
				updateAttributeLists();
			}
		}else{
			rbCreateACv.setSelected(true);			
			List<String> fileNames = DatasetSettings.getInstance().getFileNames();
			for(String fileName : fileNames){
				addDataFile(new File(fileName));
			}			
		}

		cbClassAtt.setSelectedItem(DatasetSettings.getInstance().getClassAttributeName());
		String idAttName = DatasetSettings.getInstance().getIdAttributeName();
		if(idAttName == null)
			cbIdAtt.setSelectedItem("generate ID");
		else
			cbIdAtt.setSelectedItem(idAttName);
		
		cbFirstClass.setSelectedItem(DatasetSettings.getInstance().getClassNames().get(0));
		
		tfResultsFolder.setText(DatasetSettings.getInstance().getResultFolder());
		tfRandomSeed.setText(""+DatasetSettings.getInstance().getRandSeed());
		
		tfCombinedViewsModel.setText(DatasetSettings.getInstance().getCombinedClassiffierClassName());
		int noViews = DatasetSettings.getInstance().getNoViews();
		List<String> viewClassificationModels = DatasetSettings.getInstance().getClassifierClassNames();
		if(viewClassificationModels.size() == 1 && viewClassificationModels.get(0).equals(DatasetSettings.getInstance().getCombinedClassiffierClassName())){
			chckbxUseForAll.setSelected(true);
			learningModelsTabeModel.removeAllElements();
			for(int i=0; i<noViews; i++)
				learningModelsTabeModel.addElement(DatasetSettings.getInstance().getCombinedClassiffierClassName());
			learningModelsTabeModel.setVisible(false);
		}else{
			chckbxUseForAll.setSelected(false);
			learningModelsTabeModel.removeAllElements();
			learningModelsTabeModel.setVisible(true);
			for(String model : viewClassificationModels){
				learningModelsTabeModel.addElement(model);
			}
		}
	}
	
	private void loadProperties(String filePath) throws Exception{
		DatasetSettings.getInstance().readProperties(filePath);
		getCurrentSettings();		
	}
	
	
	/**
	 * Create the frame. 
	 * @param owner parent dialogue
	 */
	public DataSettingsDialog(JFrame owner) {
		super(owner);
		setTitle("Dataset settings");
		setResizable(false);
		setModal(true);
		tfCombinedViewsModel.setText("weka.classifiers.bayes.NaiveBayes");
		tfCombinedViewsModel.setBounds(12, 47, 412, 22);
		tfCombinedViewsModel.setColumns(10);
		tfResultsFolder.setBounds(12, 47, 364, 22);
		tfResultsFolder.setColumns(10);
		setBounds(100, 100, 900, 562);
		setLocationRelativeTo(owner);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		tfNoViews.setEnabled(false);
		tfNoViews.setText("2");
		tfNoViews.setBounds(172, 43, 43, 22);
		
		datasetPanel.setBorder(new TitledBorder(null, "Dataset", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		datasetPanel.setBounds(12, 244, 423, 112);
		contentPane.add(datasetPanel);
		datasetPanel.setLayout(null);
		labelClassAttName.setHorizontalAlignment(SwingConstants.RIGHT);
		
		
		labelClassAttName.setBounds(12, 26, 205, 16);
		datasetPanel.add(labelClassAttName);
		labelIdAttName.setHorizontalAlignment(SwingConstants.RIGHT);
		
		
		labelIdAttName.setBounds(20, 55, 197, 16);
		datasetPanel.add(labelIdAttName);
		
		rbLoadExperiment.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setDataLoaded(true);					
			}
		});
		rbLoadExperiment.setBounds(8, 160, 171, 25);
		contentPane.add(rbLoadExperiment);
		
		tfLoadDataFolder = new JTextField();
		tfLoadDataFolder.setEditable(false);
		tfLoadDataFolder.setEnabled(false);
		tfLoadDataFolder.setBounds(81, 194, 675, 22);
		contentPane.add(tfLoadDataFolder);
		tfLoadDataFolder.setColumns(10);
		btnLoadData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setCurrentDirectory(new File("." + File.separator + "data"));
				int returnVal = chooser.showOpenDialog(DataSettingsDialog.this);
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					String resultDirectory = chooser.getSelectedFile().getAbsolutePath(); 
					
					try {
						int noViews = 0;
						while(true){
							File labeledViewFile = new File(resultDirectory + File.separator + "fold_0" + File.separator + "labeled_view" + noViews + ".arff");
							if(!labeledViewFile.exists())
								break;
							noViews++;
						}
						
						CoTrainingData data = new CoTrainingData(resultDirectory + File.separator + "fold_0" + File.separator, noViews, false);
						if(data.getLabeledData().length == 0){
							throw new Exception("No experiment to load in directory: '" + resultDirectory + "'.\nThe selected directory should contain"
									+ " subdirectories fold_0, fold_1,... Each of the subdirectories should contain 4 x <no of views> arff files."
									+ "\nFor example, 2 views: labeled_view0.arff, labeled_view1.arff, unlabeled_view0.arff, unlabeled_view1.arff, test_view0.arff, test_view1.arff and (optionally) pool_view0.arff, pool_view1.arff.");
						}
						
						views.clear();
						learningModelsTabeModel.removeAllElements();
						tfLoadDataFolder.setText(resultDirectory);
						
						for(Instances view : data.getLabeledData()){
							views.add(view);
							updateViewModelsTable(views.size());
						}										
						updateAttributeLists();
						tfResultsFolder.setText(tfLoadDataFolder.getText());
						tfResultsFolder.setEnabled(false);
						btnResultsFolder.setEnabled(false);
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(DataSettingsDialog.this, e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		
		
		btnLoadData.setEnabled(false);
		btnLoadData.setBounds(766, 194, 113, 22);
		contentPane.add(btnLoadData);
		
		
		rbCreateACv.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setDataLoaded(false);
			}
		});
		
		
		
		rbCreateACv.setBounds(12, 9, 203, 25);
		contentPane.add(rbCreateACv);
		
		
		lblFolder.setBounds(35, 194, 56, 16);
		contentPane.add(lblFolder);
		
		
		lblDataFiles.setBounds(36, 71, 131, 16);
		contentPane.add(lblDataFiles);
		
		
		btnAddDataFile.setBounds(771, 87, 113, 22);
		btnAddDataFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {			
				JFileChooser chooser = new JFileChooser();
			
				FileNameExtensionFilter filter = new FileNameExtensionFilter("arff files", "arff", "ARFF");
				chooser.setFileFilter(filter);
				
				chooser.setCurrentDirectory(new File(lastViewFileLoc));
				int returnVal = chooser.showOpenDialog(DataSettingsDialog.this);
				if(returnVal == JFileChooser.APPROVE_OPTION) {								
					addDataFile(chooser.getSelectedFile().getAbsoluteFile());
					if(viewsTabeModel.getRowCount() == 2)
						btnAddDataFile.setEnabled(false);					
					if(chooser.getSelectedFile().getParent() != null)
						tfResultsFolder.setText(chooser.getSelectedFile().getParent() + File.separator + "result");					
				}				
			}
		});
		contentPane.add(btnAddDataFile);
				
		btnRemoveDataFile.setBounds(771, 113, 113, 25);
		btnRemoveDataFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedInd = tableViews.getSelectedRow();
				if(selectedInd != -1){										
					viewsTabeModel.removeElement(selectedInd);
					views.remove(selectedInd);
					
					updateAttributeLists();
					if(selectedInd ==0 && viewsTabeModel.getRowCount() > 0)
						tableViews.setRowSelectionInterval(0, 0); 
					else if (viewsTabeModel.getRowCount() > 0)
						tableViews.setRowSelectionInterval(selectedInd-1, selectedInd-1);										
					
					tableViews.getColumnModel().getColumn(0).setPreferredWidth(35);
					tableViews.getColumnModel().getColumn(1).setPreferredWidth(tableViews.getWidth()-35);
					btnAddDataFile.setEnabled(true);
				}
			}
		});
		contentPane.add(btnRemoveDataFile);
	
		rbCreateACv.setSelected(true);
		lblResultsFolder.setBounds(12, 27, 191, 16);
		
		panelExperiments.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "All experiments", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelExperiments.setBounds(12, 361, 423, 112);
		
		contentPane.add(panelExperiments);
		panelExperiments.setLayout(null);
		
		panelExperiments.add(lblResultsFolder);
		
		panelExperiments.add(tfResultsFolder);
		btnResultsFolder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setCurrentDirectory(new File(lastViewFileLoc));
				int returnVal = chooser.showOpenDialog(DataSettingsDialog.this);
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					String resultDirectory = chooser.getSelectedFile().getAbsolutePath(); 
					tfResultsFolder.setText(resultDirectory);
				}
			}
		});
		btnResultsFolder.setBounds(386, 47, 27, 22);
		panelExperiments.add(btnResultsFolder);
		lblRandomNumberGenerator.setHorizontalAlignment(SwingConstants.LEFT);
		
		lblRandomNumberGenerator.setBounds(12, 78, 257, 16);
		
		panelExperiments.add(lblRandomNumberGenerator);
		
		
		
		setDataLoaded(false);
		
		
		cbClassAtt.setBounds(226, 23, 187, 22);
		datasetPanel.add(cbClassAtt);
		cbIdAtt.setBounds(227, 52, 186, 22);
		cbClassAtt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {		
				int itemCount = cbClassAtt.getItemCount();
				if(itemCount > 0){
					String classAttName = (String) cbClassAtt.getSelectedItem();
					cbFirstClass.removeAllItems();
					Attribute classAtt = getAttribute(classAttName);
					for(int i=0; i<classAtt.numValues(); i++){					
						cbFirstClass.addItem(classAtt.value(i));
					}
				}
			}
		});
		
		datasetPanel.add(cbIdAtt);
		lblWhenTiedClassify.setHorizontalAlignment(SwingConstants.RIGHT);
		lblWhenTiedClassify.setBounds(5, 84, 211, 16);
		
		datasetPanel.add(lblWhenTiedClassify);
		cbFirstClass.setBounds(226, 81, 187, 22);
		
		datasetPanel.add(cbFirstClass);
		panelLearningModels.setBorder(new TitledBorder(null, "Learning models", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelLearningModels.setBounds(445, 244, 434, 229);
		
		contentPane.add(panelLearningModels);
		panelLearningModels.setLayout(null);
		
		scrollPane_1.setBounds(12, 82, 412, 134);
		
		panelLearningModels.add(scrollPane_1);
		
		scrollPane_1.setViewportView(tableLearningModels);
		lblCombinedViewsModel.setBounds(12, 22, 216, 16);
		
		panelLearningModels.add(lblCombinedViewsModel);
		
		panelLearningModels.add(tfCombinedViewsModel);
	
		group.add(rbCreateACv);
		group.add(rbLoadExperiment);
		spViews.setBounds(35, 87, 726, 64);
		
		contentPane.add(spViews);
		
		spViews.setViewportView(tableViews);
			
		tableLearningModels.getColumnModel().getColumn(0).setPreferredWidth(25);
		tableLearningModels.getColumnModel().getColumn(1).setPreferredWidth(294-25);
		tableLearningModels.setRowSelectionAllowed(false);
		tableLearningModels.setColumnSelectionAllowed(false);
		chckbxUseForAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(chckbxUseForAll.isSelected()){
					learningModelsTabeModel.setVisible(false);
				}else{					
					learningModelsTabeModel.setVisible(true);
				}
			}
		});
		chckbxUseForAll.setBounds(234, 18, 190, 25);
		
		panelLearningModels.add(chckbxUseForAll);
			
		tfRandomSeed = new JFormattedTextField(NumberFormat.getIntegerInstance());
		tfRandomSeed.setText("2016");
		
		tfRandomSeed.setBounds(279, 75, 97, 22);
		
		panelExperiments.add(tfRandomSeed);
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(allEntriesCorrect()){				
					JFileChooser chooser = new JFileChooser();
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);		
					chooser.setCurrentDirectory(new File(lastViewFileLoc));
					int returnVal = chooser.showSaveDialog(DataSettingsDialog.this);
					if(returnVal == JFileChooser.APPROVE_OPTION) {								
						saveDataPropertiesToFile(chooser.getSelectedFile().getAbsolutePath());						
						saveFileLoc = chooser.getSelectedFile().getAbsolutePath();
						setVisible(false);
						dispose();
					}
				}				
			}
		});
		btnSave.setBounds(371, 498, 166, 25);
		
		contentPane.add(btnSave);
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();				
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);			
				chooser.setCurrentDirectory(new File(lastViewFileLoc));
				int returnVal = chooser.showOpenDialog(DataSettingsDialog.this);
				if(returnVal == JFileChooser.APPROVE_OPTION) {								
					String filePath = chooser.getSelectedFile().getAbsolutePath() + File.separator + "data.properties";
					try{
						loadProperties(filePath);			
					}catch(Exception ex){
						JOptionPane.showMessageDialog(DataSettingsDialog.this, "Error loading data properties from file '" + filePath + "'.\nReason: " + ex.getMessage(),
								"Error loading properties file", JOptionPane.ERROR_MESSAGE); 
					}
				}
			}
		});
		btnLoad.setBounds(195, 498, 166, 25);
		
		contentPane.add(btnLoad);
		tableLearningModels.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		
		
		tableViews.getColumnModel().getColumn(0).setPreferredWidth(35);
		tableViews.getColumnModel().getColumn(1).setPreferredWidth(571-35);	
		tableViews.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){			
			private static final long serialVersionUID = 1L;
			public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column){
				setEnabled(table == null || table.isEnabled());
				super.getTableCellRendererComponent(table, value, selected, focused, row, column);
				return this;
	    }
		});
		
		
		chckbxUseForAll.setSelected(true);
		lblNumberOfViews.setHorizontalAlignment(SwingConstants.LEFT);
		lblNumberOfViews.setBounds(35, 46, 132, 16);
		
		contentPane.add(lblNumberOfViews);
		
		
		contentPane.add(tfNoViews);
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveFileLoc = null;
				setVisible(false);
				dispose();
			}
		});
		btnCancel.setBounds(547, 498, 166, 25);
		
		contentPane.add(btnCancel);
		learningModelsTabeModel.setVisible(false);
		try{
			getCurrentSettings();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public String showDialog(){		
		setVisible(true);
		if(saveFileLoc != null)
			return saveFileLoc + File.separator + "data.properties";
		else 
			return null;
	}
	
}
