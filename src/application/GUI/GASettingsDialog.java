package application.GUI;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
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
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import experimentSetting.DatasetSettings;
import experimentSetting.GASettings;

public class GASettingsDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private final JPanel contentPanel = new JPanel();
	
	private final JComboBox<String> cbMeasure = new JComboBox<String>();
	private final JComboBox<String> cbClass = new JComboBox<String>();
	
	private final JCheckBox cbLog = new JCheckBox("Keep log");
	private final JCheckBox chckbxElitism = new JCheckBox("Elitism");
	
	private final JButton btnLoadFromFile = new JButton("Load from file");
	private final JButton btnSave = new JButton("Save and close");
	private final JButton btnCancel = new JButton("Cancel");
	
	private final JFormattedTextField tfGenerationSize = new JFormattedTextField(NumberFormat.getIntegerInstance());
	private final JFormattedTextField tfIts = new JFormattedTextField(NumberFormat.getIntegerInstance());
	private final JFormattedTextField tfNoImproval = new JFormattedTextField(NumberFormat.getIntegerInstance());
	private final JFormattedTextField tfTesting = new JFormattedTextField(NumberFormat.getPercentInstance());
	private final JFormattedTextField tfCrossover = new JFormattedTextField(DecimalFormat.getNumberInstance());
	private final JFormattedTextField tfMutation = new JFormattedTextField(DecimalFormat.getNumberInstance());

	private String saveFileLoc;
	private String lastSaveFileLoc;
	
	public void setLastSaveFileLoc(String lastSaveFileLoc) {
		this.lastSaveFileLoc = lastSaveFileLoc;
	}

	private boolean allEntriesCorrect(){
		try{
			try{
				int genSize = Integer.parseInt(tfGenerationSize.getText().replace(",", ""));
				if(genSize < 2){
					tfGenerationSize.grabFocus();
					throw new Exception("The generation size in GA should be at least 2.");
				}
			}catch(NumberFormatException e){
				tfGenerationSize.grabFocus();
				throw new Exception("The generation size " + tfGenerationSize.getText().replace(",", "")+" is not a valid int value.");
			}
			
			try{
				int itNo = Integer.parseInt(tfIts.getText().replace(",", ""));
				if(itNo < 1){
					tfIts.grabFocus();
					throw new Exception("The number of iterations in GA should be at least 1.");
				}
			}catch(NumberFormatException e){
				tfIts.grabFocus();
				throw new Exception("The iteration number " + tfIts.getText().replace(",", "")+" is not a valid int value.");
			}
			
			try{
				int noImprovalGen = Integer.parseInt(tfNoImproval.getText().replace(",", ""));
				if(noImprovalGen < 0 && noImprovalGen != -1){
					tfNoImproval.grabFocus();
					throw new Exception("The number of generations without improval should be positive or -1 (ignore).");
				}
			}catch(NumberFormatException e){
				tfNoImproval.grabFocus();
				throw new Exception("The number of generations without improval " + tfNoImproval.getText().replace(",", "")+" is not a valid int value.");
			}
			
			try{
				double crossoverTS = Double.parseDouble(tfCrossover.getText().replace(",", ""));
				if(crossoverTS <= 0 || crossoverTS >= 1){
					tfCrossover.grabFocus();
					throw new Exception("The crossover threshold in GA should be in the interval (0, 1).");
				}
			}catch (NumberFormatException e) {
				tfCrossover.grabFocus();
				throw new Exception("The crossover threshold " + tfCrossover.getText().replace(",", "") + " is not a valid double value.");
			}
			
			try{
				double mutationTS = Double.parseDouble(tfMutation.getText().replace(",", ""));
				if(mutationTS <= 0 || mutationTS >= 1){
					tfMutation.grabFocus();
					throw new Exception("The mutation threshold in GA should be in the interval (0, 1).");
				}
			}catch (NumberFormatException e) {
				tfMutation.grabFocus();
				throw new Exception("The mutation threshold " + tfMutation.getText().replace(",", "") + " is not a valid double value.");
			}
			
			try{
				double testingTS = Double.parseDouble(tfTesting.getText().substring(0, tfTesting.getText().length()-1).replace(",", ""));
				testingTS = testingTS/100;			
				if(testingTS < 0 || testingTS > 1){
					tfTesting.grabFocus();
					throw new Exception("The testing threshold should be between 0 and 100 %.");
				}
			}catch(NumberFormatException e){
				tfTesting.grabFocus();
				throw new Exception("The testing threshold " + tfTesting.getText().substring(0, tfTesting.getText().length()-1).replace(",", "")+" is not a valid int value.");
			}

			return true;
		}catch(Exception e){
			JOptionPane.showMessageDialog(GASettingsDialog.this, e.getMessage(), "Error saving GA settings", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}
	
	private void setMeasureClassComboBox(){
		List<String> classNames = DatasetSettings.getInstance().getClassNames();
		for(String className : classNames){
			cbClass.addItem(className);
		}
		if(cbMeasure.getSelectedItem().equals("Accuracy"))
			cbClass.setEnabled(false);
	}
	
	private String getMeasureImplementationClass(String measure){
		switch (measure) {
		case "Accuracy": return "classificationResult.measures.AccuracyMeasure";
		case "F1-measure": return "classificationResult.measures.F1Measure";
		case "Precision": return "classificationResult.measures.Precision";
		case "Recall": return "classificationResult.measures.Recall";
		default: return null;
		}
	}
	
	private String getMeasureFromImplClass(String measure){
		switch (measure) {
		case "classificationResult.measures.AccuracyMeasure": return "Accuracy";
		case "classificationResult.measures.F1Measure": return "F1-measure";
		case "classificationResult.measures.Precision": return "Precision";
		case "classificationResult.measures.Recall": return "Recall";
		default: return null;
		}
	}
	
	private void getCurrentSettings(){
		if(!GASettings.getInstance().isInitiated()) // no settings yet
			return;
		
		tfGenerationSize.setText(""+GASettings.getInstance().getGenerationSize());
		tfIts.setText(""+GASettings.getInstance().getIterationNo());
		tfNoImproval.setText(""+GASettings.getInstance().getNoImprovalgenerations());
		tfCrossover.setText(""+GASettings.getInstance().getCrossoverTS());
		tfMutation.setText(""+GASettings.getInstance().getMutationTS());
		double testingTS = GASettings.getInstance().getTestingTS();
		testingTS = testingTS*100;
		tfTesting.setText("" + testingTS + "%");
		chckbxElitism.setSelected(GASettings.getInstance().isElitism());
		cbMeasure.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(cbMeasure.getSelectedItem().equals("Accuracy")){
					cbClass.setEnabled(false);
					cbClass.setSelectedItem("not specified");
				}else
					cbClass.setEnabled(true);
			}
		});
		cbMeasure.setSelectedItem(getMeasureFromImplClass(GASettings.getInstance().getOptMeasure().getClass().getName()));
		if(cbMeasure.getSelectedItem().equals("Accuracy")){
			cbClass.setSelectedItem("not specified");
			cbClass.setEnabled(false);
		}else{		
			String className = GASettings.getInstance().getOptMeasure().getClassName();
			if(className == null || className.equals("avg"))
				className = "not specified";
			cbClass.setSelectedItem(className);
			cbClass.setEnabled(true);
		}
		cbLog.setSelected(GASettings.getInstance().isLogGA());
	}
	
	private void save(String path) throws FileNotFoundException, IOException{
		Properties gaProperties = new Properties();	
		
		int genSize = Integer.parseInt(tfGenerationSize.getText().replace(",", ""));
		gaProperties.setProperty("generationSize", ""+genSize);
		
		int itNo = Integer.parseInt(tfIts.getText().replace(",", ""));
		gaProperties.setProperty("iterations", "" + itNo);
		
		int noImprovalGen = Integer.parseInt(tfNoImproval.getText().replace(",", ""));
		gaProperties.setProperty("noImprovalGenerations", "" + noImprovalGen);
		
		double crossoverTS = Double.parseDouble(tfCrossover.getText().replace(",", ""));
		gaProperties.setProperty("crossoverTS", "" + crossoverTS);
		
		double mutationTS = Double.parseDouble(tfMutation.getText().replace(",", ""));
		gaProperties.setProperty("mutationTS", "" + mutationTS);
		
		double testingTS = Double.parseDouble(tfTesting.getText().substring(0, tfTesting.getText().length()-1).replace(",", ""));
		testingTS = testingTS/100;
		gaProperties.setProperty("testingTS", "" + testingTS);
		
		boolean elitism = chckbxElitism.isSelected();
		gaProperties.setProperty("elitism", "" + elitism);
		
		boolean keepLog = cbLog.isSelected();
		gaProperties.setProperty("logGA", "" + keepLog);
		
		String measure = (String) cbMeasure.getSelectedItem();
		String measureAlg = getMeasureImplementationClass(measure);
		gaProperties.setProperty("optimizationMeasure", measureAlg);
		
		String measureClass = (String) cbClass.getSelectedItem();
		if(measureClass.equals("not specified"))
			measureClass = "avg";
		gaProperties.setProperty("optimizationMeasureClass", measureClass);
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		gaProperties.store(new FileOutputStream(path + File.separator + "GA.properties"), dateFormat.format(cal.getTime()));
	}
	
	private void loadProperties(String filePath) throws Exception{
		GASettings.getInstance().readProperties(filePath);
		getCurrentSettings();	
	}

	/**
	 * Create the dialog.
	 * @param owner parent dialogue
	 */
	public GASettingsDialog(JFrame owner) {
		super(owner);
		setTitle("Genetic algorithm settings");
		setResizable(false);
		setModal(true);
		setBounds(100, 100, 421, 403);
		setLocationRelativeTo(owner);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		{
			JLabel lblOptimizationMeasureClass = new JLabel("Optimization measure class:");
			lblOptimizationMeasureClass.setHorizontalAlignment(SwingConstants.RIGHT);
			lblOptimizationMeasureClass.setBounds(10, 262, 251, 16);
			contentPanel.add(lblOptimizationMeasureClass);
		}
		{
			JLabel lblOptimizationMeasure = new JLabel("Optimization measure:");
			lblOptimizationMeasure.setHorizontalAlignment(SwingConstants.RIGHT);
			lblOptimizationMeasure.setBounds(10, 230, 251, 16);
			contentPanel.add(lblOptimizationMeasure);
		}
		{			
			cbMeasure.setModel(new DefaultComboBoxModel<String>(new String[] {"Accuracy", "F1-measure", "Precision", "Recall"}));
			cbMeasure.setBounds(271, 227, 134, 22);
			contentPanel.add(cbMeasure);
		}
		{			
			cbClass.setModel(new DefaultComboBoxModel<String>(new String[] {"not specified"}));
			cbClass.setBounds(271, 259, 134, 22);
			contentPanel.add(cbClass);
		}
		{
			JLabel lblGenerationSize = new JLabel("Generation size:");
			lblGenerationSize.setHorizontalAlignment(SwingConstants.RIGHT);
			lblGenerationSize.setBounds(10, 11, 251, 16);
			contentPanel.add(lblGenerationSize);
		}
		{			
			tfGenerationSize.setText("50");
			tfGenerationSize.setBounds(271, 8, 134, 22);
			contentPanel.add(tfGenerationSize);
		}
		{
			JLabel lblNumberOfIterations = new JLabel("Maximal number of iterations:");
			lblNumberOfIterations.setHorizontalAlignment(SwingConstants.RIGHT);
			lblNumberOfIterations.setBounds(10, 45, 251, 16);
			contentPanel.add(lblNumberOfIterations);
		}
		{			
			tfIts.setText("50");
			tfIts.setBounds(271, 42, 134, 22);
			contentPanel.add(tfIts);
		}
		{
			JLabel lblGenerationsWithoutImproval = new JLabel("Generations without improval:");
			lblGenerationsWithoutImproval.setHorizontalAlignment(SwingConstants.RIGHT);
			lblGenerationsWithoutImproval.setBounds(10, 78, 251, 16);
			contentPanel.add(lblGenerationsWithoutImproval);
		}
		{			
			tfNoImproval.setText("5");
			tfNoImproval.setBounds(271, 75, 134, 22);
			contentPanel.add(tfNoImproval);
		}
		{
			JLabel lblCrossoverProbability = new JLabel("Crossover threshold:");
			lblCrossoverProbability.setHorizontalAlignment(SwingConstants.RIGHT);
			lblCrossoverProbability.setBounds(10, 111, 251, 16);
			contentPanel.add(lblCrossoverProbability);
		}
		{			
			tfCrossover.setText("0.3");
			tfCrossover.setBounds(271, 108, 134, 22);
			contentPanel.add(tfCrossover);
		}
		{
			JLabel lblMutationThreshold = new JLabel("Mutation threshold:");
			lblMutationThreshold.setHorizontalAlignment(SwingConstants.RIGHT);
			lblMutationThreshold.setBounds(10, 144, 251, 16);
			contentPanel.add(lblMutationThreshold);
		}
		{			
			tfMutation.setText("0.02");
			tfMutation.setBounds(271, 141, 134, 22);
			contentPanel.add(tfMutation);
		}
		{			
			chckbxElitism.setSelected(true);
			chckbxElitism.setBounds(271, 200, 113, 25);
			contentPanel.add(chckbxElitism);
		}
		{
			JLabel lblTestingThreshold = new JLabel("Testing threshold:");
			lblTestingThreshold.setHorizontalAlignment(SwingConstants.RIGHT);
			lblTestingThreshold.setBounds(10, 174, 251, 16);
			contentPanel.add(lblTestingThreshold);
		}
		{			
			tfTesting.setText("20%");
			tfTesting.setBounds(271, 171, 134, 22);
			contentPanel.add(tfTesting);
		}
		{			
			cbLog.setBounds(271, 293, 113, 25);
			contentPanel.add(cbLog);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{				
				btnLoadFromFile.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						JFileChooser chooser = new JFileChooser();				
						chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);			
						chooser.setCurrentDirectory(new File(lastSaveFileLoc));
						int returnVal = chooser.showOpenDialog(GASettingsDialog.this);
						if(returnVal == JFileChooser.APPROVE_OPTION) {								
							String filePath = chooser.getSelectedFile().getAbsolutePath() + File.separator + "GA.properties";
							try{
								loadProperties(filePath);						
							}catch(Exception ex){
								ex.printStackTrace();
								JOptionPane.showMessageDialog(GASettingsDialog.this, "Error loading genetic algorithm properties from file '" + filePath + "'. Reason:\n" + ex.getMessage(),
										"Error loading GA properties", JOptionPane.ERROR_MESSAGE); 
							}
						}
					}
				});
				buttonPane.add(btnLoadFromFile);
			}
			{				
				btnSave.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(allEntriesCorrect()){
							JFileChooser chooser = new JFileChooser();
							chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);		
							chooser.setCurrentDirectory(new File(lastSaveFileLoc));
							int returnVal = chooser.showSaveDialog(GASettingsDialog.this);
							if(returnVal == JFileChooser.APPROVE_OPTION) {
								try{
									save(chooser.getSelectedFile().getAbsolutePath());
									saveFileLoc = chooser.getSelectedFile().getAbsolutePath() + File.separator + "GA.properties";
									setVisible(false);
									dispose();
								}catch(Exception ex){
									JOptionPane.showMessageDialog(GASettingsDialog.this, "Error saving GA properties. Reason:\n" + ex.getMessage(), 
											"Error saving GA settings", JOptionPane.ERROR_MESSAGE);
								}
								
							}							
						}
					}
				});
				btnSave.setActionCommand("OK");
				buttonPane.add(btnSave);
				getRootPane().setDefaultButton(btnSave);
			}
			{				
				btnCancel.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						saveFileLoc = null;
						setVisible(false);
						dispose();
					}
				});
				btnCancel.setActionCommand("Cancel");
				buttonPane.add(btnCancel);
			}
		}
		setMeasureClassComboBox();
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
