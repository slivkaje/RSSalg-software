package application.GUI;

import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import javax.swing.SwingWorker;

import experimentSetting.CVSettings;
import experimentSetting.CoTrainingSettings;
import experimentSetting.DatasetSettings;
import experimentSetting.ExperimentSettings;
import experimentSetting.GASettings;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JScrollPane;

import java.awt.BorderLayout;

import javax.swing.JTextPane;

import application.StartExperiment;

import javax.swing.border.TitledBorder;

import java.awt.FlowLayout;

public class RSSalgFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JLabel lblDataSettings;
	private JTextField tfDataSettings;
	private JLabel lblCrossvalidationExperimentSettings;
	private JTextField tfCVSettings;
	private JButton btnDataSettings;
	private JButton btnCVSettings;
	private JLabel lblCotrainingProperties;
	private JTextField tfCoTrainingSettings;
	private JButton btnCTSettings;
	private JLabel lblExperimentProperties;
	private JTextField tfExperimentProperties;
	private JButton btnExperimentProperties;
	private JButton btnClose;
	private JButton btnStartExperiment;
	private JButton btnGAProperties;
	private JTextField tfGAProperties;
	private JLabel lblGeneticAlgorithmProperties;
	private JPanel panel;
	private JScrollPane scrollPane;
	private JTextPane textPane;
	private JPanel settingsPanel = new JPanel();
	private JPanel buttonsPanel = new JPanel();
	private JButton btnClear;
	private JButton btnClearSettings;
	private JButton btnLoadAll;
	private MessageConsole mc;
	private SwingWorker worker;
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					RSSalgFrame frame = new RSSalgFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private void startExperiment() throws Exception{
		StartExperiment experimentStarter = new StartExperiment();
		DatasetSettings.getInstance().restartRandom();
		experimentStarter.run();
	}
	
	private boolean canRunExperiment(){
		if (!DatasetSettings.getInstance().isInitiated()){
			JOptionPane.showMessageDialog(RSSalgFrame.this, "Data settings not initialized.", "Error starting experiment", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		if(!DatasetSettings.getInstance().isLoadPresetExperiment() && !CVSettings.getInstance().isInitiated()){
			JOptionPane.showMessageDialog(RSSalgFrame.this, "Cross-validation experiment settings not initialized.", "Error starting experiment", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		if(!ExperimentSettings.getInstance().isInitiated()){
			JOptionPane.showMessageDialog(RSSalgFrame.this, "Experiment settings not initialized.", "Error starting experiment", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		if(!CoTrainingSettings.getInstance().isInitiated()){
			JOptionPane.showMessageDialog(RSSalgFrame.this, "Co-training settings not initialized.", "Error starting experiment", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		if(ExperimentSettings.getInstance().getAlgorithm().getClass().getName().equals("algorithms.RSSalg.RSSalg") && !GASettings.getInstance().isInitiated()){
			JOptionPane.showMessageDialog(RSSalgFrame.this, "GA settings not initialized.", "Error starting experiment", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		   			
		return true;
	}

	/**
	 * Create the frame.
	 */
	public RSSalgFrame() {
		setTitle("RSSalg software");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 556, 446);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		lblDataSettings = new JLabel("Dataset settings:");
		lblDataSettings.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDataSettings.setBounds(48, 13, 108, 16);
		settingsPanel.add(lblDataSettings);
		
		tfDataSettings = new JTextField();
		tfDataSettings.setEditable(false);
		tfDataSettings.setBounds(162, 10, 306, 22);
		settingsPanel.add(tfDataSettings);
		tfDataSettings.setColumns(10);
		
		lblCrossvalidationExperimentSettings = new JLabel("Cross-validation settings:");
		lblCrossvalidationExperimentSettings.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCrossvalidationExperimentSettings.setBounds(6, 44, 150, 16);
		settingsPanel.add(lblCrossvalidationExperimentSettings);
		
		tfCVSettings = new JTextField();
		tfCVSettings.setEditable(false);
		tfCVSettings.setBounds(162, 40, 306, 22);
		settingsPanel.add(tfCVSettings);
		tfCVSettings.setColumns(10);
		
		btnDataSettings = new JButton("...");
		btnDataSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DataSettingsDialog dataSettingsDialog = new DataSettingsDialog(RSSalgFrame.this);
				dataSettingsDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				String dataSettingFileLoc = dataSettingsDialog.showDialog();				
				if(dataSettingFileLoc != null){					
					try {
						DatasetSettings.getInstance().readProperties(dataSettingFileLoc);
						tfDataSettings.setText(dataSettingFileLoc);
						btnCVSettings.setEnabled(true);						
						btnCTSettings.setEnabled(true);
						btnExperimentProperties.setEnabled(true);
						btnGAProperties.setEnabled(true);
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(RSSalgFrame.this, "Error loading data settings from file '" + dataSettingFileLoc + "'. \nReason: " + e1.getMessage(),
								"Error loading propeties", JOptionPane.ERROR_MESSAGE);
					}				
				}else{ // in case that DataSettings were changed by loading the data and then pressing cancel
					if(!tfDataSettings.getText().equals("")){
						try {
							DatasetSettings.getInstance().readProperties(tfDataSettings.getText());
						}catch (Exception e1) {
							JOptionPane.showMessageDialog(RSSalgFrame.this, "Error loading data settings from file '" + tfDataSettings.getText() + "'. \nReason: " + e1.getMessage(),
									"Error loading propeties", JOptionPane.ERROR_MESSAGE);
						}	
					}else
						DatasetSettings.getInstance().claerSettings();
				}
			}
		});
		btnDataSettings.setBounds(480, 9, 35, 25);
		settingsPanel.add(btnDataSettings);
		
		btnCVSettings = new JButton("...");
		btnCVSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try{
					CVSettingsDialog cvSettingsDialog = new CVSettingsDialog(RSSalgFrame.this);
					cvSettingsDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					
					if(tfCVSettings.getText().equals(""))
						cvSettingsDialog.setLastSaveFileLoc(new File(tfDataSettings.getText()).getAbsolutePath());
					else
						cvSettingsDialog.setLastSaveFileLoc(new File(tfCVSettings.getText()).getAbsolutePath());
					
					String cvSettingFileLoc = cvSettingsDialog.showDialog();
					if(cvSettingFileLoc != null){
						CVSettings.getInstance().readProperties(cvSettingFileLoc);
						tfCVSettings.setText(cvSettingFileLoc);					
					}else{ // in case that CVSettings were changed by loading the data and then pressing cancel
						if(!tfCVSettings.getText().equals("")){
							try {
								CVSettings.getInstance().readProperties(tfCVSettings.getText());
							}catch (Exception e1) {
								JOptionPane.showMessageDialog(RSSalgFrame.this, "Error loading cross-validation settings from file '" + tfCVSettings.getText() + "'. \nReason: " + e1.getMessage(),
										"Error loading propeties", JOptionPane.ERROR_MESSAGE);
							}	
						}else{
							CVSettings.getInstance().clear();
						}
					}
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}
		});
		btnCVSettings.setEnabled(false);
		btnCVSettings.setBounds(480, 38, 35, 25);
		settingsPanel.add(btnCVSettings);
		
		lblCotrainingProperties = new JLabel("Co-training properties:");
		lblCotrainingProperties.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCotrainingProperties.setBounds(26, 73, 130, 16);
		settingsPanel.add(lblCotrainingProperties);
		
		tfCoTrainingSettings = new JTextField();
		tfCoTrainingSettings.setEditable(false);
		tfCoTrainingSettings.setBounds(162, 70, 306, 22);
		settingsPanel.add(tfCoTrainingSettings);
		tfCoTrainingSettings.setColumns(10);
		
		btnCTSettings = new JButton("...");
		btnCTSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try{
					CoTrainingPropertiesDialog ctSettingsDialog = new CoTrainingPropertiesDialog(RSSalgFrame.this);
					ctSettingsDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					
					if(!tfCoTrainingSettings.getText().equals(""))
						ctSettingsDialog.setLastSaveFileLoc(new File(tfCoTrainingSettings.getText()).getAbsolutePath());
					else
						ctSettingsDialog.setLastSaveFileLoc(new File(tfDataSettings.getText()).getAbsolutePath());
					
					String ctSettingFileLoc = ctSettingsDialog.showDialog();
					if(ctSettingFileLoc != null){
						CoTrainingSettings.getInstance().readProperties(ctSettingFileLoc);
						tfCoTrainingSettings.setText(ctSettingFileLoc);					
					}else{ // in case that CVSettings were changed by loading the data and then pressing cancel
						if(!tfCoTrainingSettings.getText().equals("")){
							try {
								CoTrainingSettings.getInstance().readProperties(tfCoTrainingSettings.getText());
							}catch (Exception e1) {
								JOptionPane.showMessageDialog(RSSalgFrame.this, "Error loading co-training settings from file '" + tfCoTrainingSettings.getText() + "'. \nReason: " + e1.getMessage(),
										"Error loading propeties", JOptionPane.ERROR_MESSAGE);
							}	
						}else{
							CoTrainingSettings.getInstance().clear();
						}
					}
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}
		});
		btnCTSettings.setEnabled(false);
		btnCTSettings.setBounds(480, 67, 35, 25);
		settingsPanel.add(btnCTSettings);
		
		lblExperimentProperties = new JLabel("Experiment properties:");
		lblExperimentProperties.setHorizontalAlignment(SwingConstants.RIGHT);
		lblExperimentProperties.setBounds(6, 102, 150, 16);
		settingsPanel.add(lblExperimentProperties);
		
		tfExperimentProperties = new JTextField();
		tfExperimentProperties.setEditable(false);
		tfExperimentProperties.setBounds(162, 101, 306, 22);
		settingsPanel.add(tfExperimentProperties);
		tfExperimentProperties.setColumns(10);
		
		btnExperimentProperties = new JButton("...");
		btnExperimentProperties.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try{
					ExperimentSettingsDialog expSettings = new ExperimentSettingsDialog(RSSalgFrame.this);
					expSettings.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					
					if(!tfExperimentProperties.getText().equals(""))
						expSettings.setLastSaveFileLoc(new File(tfExperimentProperties.getText()).getAbsolutePath());
					else
						expSettings.setLastSaveFileLoc(new File(tfDataSettings.getText()).getAbsolutePath());
					
					String expSettingFileLoc = expSettings.showDialog();
					if(expSettingFileLoc != null){
						ExperimentSettings.getInstance().readProperties(expSettingFileLoc);
						tfExperimentProperties.setText(expSettingFileLoc);					
					}else{ // in case that CVSettings were changed by loading the data and then pressing cancel
						if(!tfExperimentProperties.getText().equals("")){
							try {
								ExperimentSettings.getInstance().readProperties(tfExperimentProperties.getText());
							}catch (Exception e1) {
								JOptionPane.showMessageDialog(RSSalgFrame.this, "Error loading experiment settings from file '" + tfExperimentProperties.getText() + "'. \nReason: " + e1.getMessage(),
										"Error loading propeties", JOptionPane.ERROR_MESSAGE);
							}	
						}else{
							ExperimentSettings.getInstance().clear();
						}
					}
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}
		});
		btnExperimentProperties.setEnabled(false);
		btnExperimentProperties.setBounds(480, 100, 35, 25);
		settingsPanel.add(btnExperimentProperties);
		FlowLayout flowLayout = (FlowLayout) buttonsPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		
		btnClear = new JButton("Clear");
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textPane.setText("");
			}
		});
		buttonsPanel.add(btnClear);
		
		btnGAProperties = new JButton("...");
		btnGAProperties.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try{
					GASettingsDialog GADialog = new GASettingsDialog(RSSalgFrame.this);
					GADialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					
					if(tfGAProperties.getText().equals(""))
						GADialog.setLastSaveFileLoc(new File(tfDataSettings.getText()).getAbsolutePath());
					else
						GADialog.setLastSaveFileLoc(new File(tfGAProperties.getText()).getAbsolutePath());
					
					String GASettingFileLoc = GADialog.showDialog();
					if(GASettingFileLoc != null){
						GASettings.getInstance().readProperties(GASettingFileLoc);
						tfGAProperties.setText(GASettingFileLoc);					
					}else{ // in case that CVSettings were changed by loading the data and then pressing cancel
						if(!tfGAProperties.getText().equals("")){
							try {
								GASettings.getInstance().readProperties(tfGAProperties.getText());
							}catch (Exception e1) {
								JOptionPane.showMessageDialog(RSSalgFrame.this, "Error loading GA settings from file '" + tfGAProperties.getText() + "'. Reason:\n" + e1.getMessage(),
										"Error loading propeties", JOptionPane.ERROR_MESSAGE);
							}	
						}else{
							GASettings.getInstance().clear();
						}
					}
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}
		});
		btnGAProperties.setEnabled(false);
		btnGAProperties.setBounds(480, 131, 35, 25);
		settingsPanel.add(btnGAProperties);
		
		tfGAProperties = new JTextField();
		tfGAProperties.setEditable(false);
		tfGAProperties.setColumns(10);
		tfGAProperties.setBounds(162, 133, 306, 22);
		settingsPanel.add(tfGAProperties);
		
		lblGeneticAlgorithmProperties = new JLabel("GA properties:");
		lblGeneticAlgorithmProperties.setHorizontalAlignment(SwingConstants.RIGHT);
		lblGeneticAlgorithmProperties.setBounds(70, 135, 86, 16);
		settingsPanel.add(lblGeneticAlgorithmProperties);
		
		panel = new JPanel();
		panel.setBounds(6, 202, 1011, 193);
		contentPane.add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));
		
		scrollPane = new JScrollPane();
		panel.add(scrollPane, BorderLayout.CENTER);
		
		textPane = new JTextPane();
		scrollPane.setViewportView(textPane);
				
		settingsPanel.setBorder(new TitledBorder(null, "Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		settingsPanel.setBounds(26, 13, 525, 164);
		settingsPanel.setLayout(null);

		JPanel northPanel = new JPanel();
		contentPane.add(northPanel, BorderLayout.NORTH);
		northPanel.setLayout(new FlowLayout());
		northPanel.add(settingsPanel);
		settingsPanel.setPreferredSize(new Dimension(525, 200));
		
		btnClearSettings = new JButton("Clear settings");
		btnClearSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tfDataSettings.setText("");
				DatasetSettings.getInstance().claerSettings();
				btnCVSettings.setEnabled(false);
				btnCTSettings.setEnabled(false);
				btnExperimentProperties.setEnabled(false);
				btnGAProperties.setEnabled(false);
				
				tfCVSettings.setText("");
				CVSettings.getInstance().clear();
				
				tfCoTrainingSettings.setText("");
				CoTrainingSettings.getInstance().clear();
				
				tfExperimentProperties.setText("");
				ExperimentSettings.getInstance().clear();
				
				tfGAProperties.setText("");
				GASettings.getInstance().clear();				
			}
		});
		btnClearSettings.setBounds(389, 169, 130, 25);
		settingsPanel.add(btnClearSettings);
		
		btnStartExperiment = new JButton("Start experiment");
		settingsPanel.add(btnStartExperiment);
		btnStartExperiment.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(canRunExperiment())					
					try {
						if (worker!=null)			       
			                worker.cancel(false);			           
			            worker = new SwingWorker(){			         
			            			            	
							@Override
							protected Integer doInBackground() throws Exception {
								try{
									startExperiment();
								}catch(Exception e){
									Throwable cause = e;
									while(cause.getCause() != null) {
									    cause = cause.getCause();
									}
									
									JOptionPane.showMessageDialog(RSSalgFrame.this, cause.getMessage() , "Error starting experiment", JOptionPane.ERROR_MESSAGE);
								}
								return 0;								
							}			
			            };
			            worker.execute();
					} catch (Exception e1) {
						e1.printStackTrace();						
					}
			}
		});
		btnStartExperiment.setBounds(105, 169, 130, 25);
		
		btnLoadAll = new JButton("Load all");
		btnLoadAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();				
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);			
				chooser.setCurrentDirectory(new File("./data"));
				int returnVal = chooser.showOpenDialog(RSSalgFrame.this);
				if(returnVal == JFileChooser.APPROVE_OPTION) {								
					String filePath = chooser.getSelectedFile().getAbsolutePath();
					try{
						DatasetSettings.getInstance().readProperties(filePath + "/data.properties");	
						tfDataSettings.setText(filePath + "/data.properties"); 
						btnCVSettings.setEnabled(true);
						btnCTSettings.setEnabled(true);
						btnExperimentProperties.setEnabled(true);
						btnGAProperties.setEnabled(true);
						
						try{
							CVSettings.getInstance().readProperties(filePath + "/cv.properties");	
							tfCVSettings.setText(filePath + "/cv.properties"); 
						}catch(Exception ex){
							JOptionPane.showMessageDialog(RSSalgFrame.this, "Error loading cross-validation experiment properties from file '" + filePath + "/cv.properties'.\nReason: " + ex.getMessage(),
									"Error loading CV settings", JOptionPane.ERROR_MESSAGE); 
						}
						try{
							CoTrainingSettings.getInstance().readProperties(filePath + "/co-training.properties");	
							tfCoTrainingSettings.setText(filePath + "/co-training.properties"); 
						}catch(Exception ex){
							JOptionPane.showMessageDialog(RSSalgFrame.this, "Error loading co-training properties from file '" + filePath + "/co-training.properties'.\nReason: " + ex.getMessage(),
									"Error loading co-training settings", JOptionPane.ERROR_MESSAGE); 
						}
						try{
							ExperimentSettings.getInstance().readProperties(filePath + "/Experiment_L.properties");	
							tfExperimentProperties.setText(filePath + "/Experiment_L.properties"); 
						}catch(Exception ex){
							JOptionPane.showMessageDialog(RSSalgFrame.this, "Error loading L experiment properties from file '" + filePath + "/Experiment_L.properties'.\nReason: " + ex.getMessage(),
									"Error loading L experiment settings", JOptionPane.ERROR_MESSAGE); 
						}
						try{
							GASettings.getInstance().readProperties(filePath + "/GA.properties");	
							tfGAProperties.setText(filePath + "/GA.properties"); 
						}catch(Exception ex){
							JOptionPane.showMessageDialog(RSSalgFrame.this, "Error loading genetic algorithm experiment properties from file '" + filePath + "GA.properties'.\nReason: " + ex.getMessage(),
									"Error loading GA settings", JOptionPane.ERROR_MESSAGE); 
						}
					}catch(Exception ex){
						JOptionPane.showMessageDialog(RSSalgFrame.this, "Error loading dataset properties from file '" + filePath + "/data.properties'.\nReason: " + ex.getMessage(),
								"Error loading dataset settings", JOptionPane.ERROR_MESSAGE); 
					}
				}
			}
		});
		btnLoadAll.setBounds(247, 169, 130, 25);
		settingsPanel.add(btnLoadAll);
		
		
		buttonsPanel.setBounds(101, 489, 322, 45);
		contentPane.add(buttonsPanel, BorderLayout.SOUTH);
		
		btnClose = new JButton("Close");
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		});
		btnClose.setBounds(440, 169, 75, 25);
		buttonsPanel.add(btnClose);
		
		setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
		mc = new MessageConsole(textPane);	
		mc.redirectOut(null, null);
		mc.setMessageLines(10000);		
	}

}
