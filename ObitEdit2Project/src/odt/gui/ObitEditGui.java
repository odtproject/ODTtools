package odt.gui;

import odt.tool.DirectorySetup;
import odt.tool.ErrorCategory;
import odt.tool.ErrorField;
import odt.tool.ErrorLog;
import odt.tool.ObitEdit;
import odt.util.LibFiles;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTextArea;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.Font;
import java.awt.Image;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

public class ObitEditGui {

	private String fileToCheck;
	private JFrame frame;
	private JTextField textFieldFileName;
	private JTextField textFieldResults;
	private JTextField textFieldErrors;
	private JTextField textFieldRpt;
	private JTextField textFieldCorrectedFile;
	
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ObitEditGui window = new ObitEditGui();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ObitEditGui() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame("ObitEdit2");
		frame.setForeground(new Color(220, 220, 220));
		frame.setBackground(new Color(220, 220, 220));
		frame.getContentPane().setBackground(new Color(240, 248, 255));
		frame.setBounds(100, 100, 712, 501);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		/* Center the location of the window, based on the size of the screen
		 * and the size of the frame.
		 */
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameDim = frame.getSize();
		frame.setLocation((screenDim.width - frameDim.width) / 2, (screenDim.height - frameDim.height) / 2);
		
		/* Load the icons for the frame */
		loadIcons(frame);
		
		/* Make the font for option panes larger */
		UIManager.put("OptionPane.messageFont", new Font("System", Font.PLAIN, 16));
		UIManager.put("OptionPane.buttonFont", new Font("System", Font.PLAIN, 16));
		
//		frame.setIconImages
//		/*.setIconImage(Toolkit.getDefaultToolkit().getImage("Image7_32.png"));
			
		/* Set up the ODT directory structure.  If it fails, we're done. */
		DirectorySetup dirSetup = new DirectorySetup();
		if ( ! dirSetup.isSetupComplete() ) {	
			return;
		}
		
		/* Get the folder for the file to check */
		String checkFileString = DirectorySetup.getODTSubFolderString("Check");
		if ( checkFileString.equals("Invalid") ) {
			return;
		}
		
		/* If the user would like to get newer versions of the lib files,
		 * then find what lib files there are and see if the ones on the website
		 * are newer.  If so, download the newer ones if the user wants to.
		 */
		int answer;
		answer = JOptionPane.showConfirmDialog(null, "Would you like to check for newer lib files?", "ObitEdit Option", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		
		if ( answer == JOptionPane.YES_OPTION ) {			

			/* We want to see if any of the lib files have newer versions on the
			 * website.
			 */
			LibFiles libFiles = new LibFiles();
			
			/* Get a list of the files in the lib folder */
			libFiles.libFilesPathList = LibFiles.listSourceFiles(DirectorySetup.getLibFilesPath());
			
			try {
				Iterator<Path> fileIterator = libFiles.libFilesPathList.iterator();
				Path libFilePath;
				long libFileAge, webFileAge;
				String optionMessage, libFileName, fileMessage;
				optionMessage = "\nNewer version available on website\nWould you like to download it now?";
				
				/* loop through the files */
				while ( fileIterator.hasNext() )  {
					
					/* get the file name */
					libFilePath = fileIterator.next();
					
					/* Get the age of the file */
					libFileAge = LibFiles.getFileAge(libFilePath);
					
					/* Get the age of the file on the website */
					libFileName = libFilePath.getFileName().toString();
					webFileAge = LibFiles.getWebAge(libFileName);
					
					/* If the web file exists and is newer than the lib file
					 * get the newer file if the user is okay with it.
					 */
					if ( webFileAge >= 0 && libFileAge > webFileAge ) {
						fileMessage = "File " + libFileName;

						answer = JOptionPane.showConfirmDialog(null, fileMessage + optionMessage , "ObituaryFiler Option", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
						
						if ( answer == JOptionPane.YES_OPTION ) {
							if ( LibFiles.downloadWebFile(libFilePath.toString(), libFileName) ) {
								JOptionPane.showMessageDialog(null, "Download complete");
							}
							else {
								JOptionPane.showMessageDialog(null, "Download failed","Download message", JOptionPane.ERROR_MESSAGE);
							}
						}
						
					} /* end of if the web file is newer than the existing file */
					
				} /* end of while there is another file */

			} /* end of try lib file check */ 
			
			catch (Exception e) {			
				e.printStackTrace();
			}
	
		} /* end of yes check for newer lib files */
		
		/* Get the version number for this */
		String versionNumber = ObitEditGui.class.getPackage().getImplementationVersion();
		
		JMenuItem mntmAbout = new JMenuItem("About");
		mntmAbout.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		mntmAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				String versionMessage = "ObitEdit2 version " + versionNumber + "\n\nCopyright \u00a9 Alice Ramsay\n\n";
				JOptionPane.showMessageDialog(frame, versionMessage);
			}
		});
		
		
		JLabel lblFileToCheck = new JLabel("File to check:");
		lblFileToCheck.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblFileToCheck.setBackground(Color.WHITE);
		lblFileToCheck.setHorizontalAlignment(SwingConstants.RIGHT);
		lblFileToCheck.setBounds(10, 32, 91, 21);
		frame.getContentPane().add(lblFileToCheck);		
		

		textFieldFileName = new JTextField(20);
		textFieldFileName.setToolTipText("Enter file name");
		textFieldFileName.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileToCheck = textFieldFileName.getText();	
				try {
					Path checkFilePath = Paths.get(checkFileString, fileToCheck);
					if ( ! checkFilePath.toFile().exists() ) {
						System.err.println("File not found: " + fileToCheck);
					}
				}
				catch ( Exception ie ) {
					String errMsg = "Invalid file name!\n";
					errMsg = errMsg + "Put file in folder\n";
					errMsg = errMsg + checkFileString;
					errMsg = errMsg + "\nEnter just file name in box";
					JOptionPane.showMessageDialog(frame, errMsg);
				}
				
			}
		});
		textFieldFileName.setBounds(111, 33, 134, 21);
		frame.getContentPane().add(textFieldFileName);
		textFieldFileName.setColumns(10);
		
		textFieldResults = new JTextField();
		textFieldResults.setFont(new Font("Courier New", Font.BOLD, 12));
		textFieldResults.setBackground(Color.WHITE);
		textFieldResults.setEditable(false);
		textFieldResults.setBounds(40, 69, 104, 23);
		frame.getContentPane().add(textFieldResults);
		textFieldResults.setColumns(10);
		
		textFieldErrors = new JTextField();
		textFieldErrors.setFont(new Font("Courier New", Font.BOLD, 12));
		textFieldErrors.setBackground(Color.WHITE);
		textFieldErrors.setEditable(false);
		textFieldErrors.setBounds(40, 93, 104, 22);
		frame.getContentPane().add(textFieldErrors);
		textFieldErrors.setColumns(10);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(358, 93, 144, 127);
		frame.getContentPane().add(scrollPane);
		
		JTextArea textArea = new JTextArea();
		scrollPane.setViewportView(textArea);

		
		TextArea textAreaErr = new TextArea();
		textAreaErr.setBounds(32, 357, 483, 86);
		frame.getContentPane().add(textAreaErr);

		PrintStream printStream = new PrintStream(new CustomOutputStream(textAreaErr));
		/* Send the error output to the system error messages area. */
		System.setErr(printStream);
		
		JTextArea textAreaCategories = new JTextArea();
		textAreaCategories.setFont(new Font("Courier New", Font.PLAIN, 12));
		textAreaCategories.setEditable(false);
		textAreaCategories.setBounds(32, 144, 134, 72);
		frame.getContentPane().add(textAreaCategories);
		
		JTextArea textAreaFieldErrors = new JTextArea();
		textAreaFieldErrors.setFont(new Font("Courier New", Font.PLAIN, 12));
		textAreaFieldErrors.setEditable(false);
		textAreaFieldErrors.setBounds(189, 96, 134, 120);
		frame.getContentPane().add(textAreaFieldErrors);

		textFieldRpt = new JTextField();
		textFieldRpt.setBackground(Color.WHITE);
		textFieldRpt.setEditable(false);
		textFieldRpt.setBounds(32, 306, 400, 20);
		frame.getContentPane().add(textFieldRpt);
		textFieldRpt.setColumns(10);
		
		JRadioButton rdbtnSuppressWarnings = new JRadioButton("Suppress warnings");
		rdbtnSuppressWarnings.setBackground(new Color(204, 204, 255));
		rdbtnSuppressWarnings.setFont(new Font("Tahoma", Font.BOLD, 13));
		rdbtnSuppressWarnings.setBounds(521, 32, 169, 23);
		frame.getContentPane().add(rdbtnSuppressWarnings);
		
		JRadioButton rdbtnSuppressSpaceErrors = new JRadioButton("Suppress space errs");
		rdbtnSuppressSpaceErrors.setBackground(new Color(204, 204, 255));
		rdbtnSuppressSpaceErrors.setFont(new Font("Tahoma", Font.BOLD, 13));
		rdbtnSuppressSpaceErrors.setBounds(521, 67, 169, 23);
		frame.getContentPane().add(rdbtnSuppressSpaceErrors);

		JButton btnCheckFile = new JButton("Check file");
		btnCheckFile.setFont(new Font("Tahoma", Font.BOLD, 13));
		btnCheckFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				fileToCheck = textFieldFileName.getText();

				try {
					Path checkFilePath = Paths.get(checkFileString, fileToCheck);
					if ( ! checkFilePath.toFile().exists() ) {
						JOptionPane.showMessageDialog(frame, "File not found: " + checkFilePath.toString());
					}
					else {
						
						/* initialize the error reporting object */
						ErrorLog obitErrors = new ErrorLog();

						/* set the operating flags per the radio buttons */
						obitErrors.suppressFlags.setSuppressSpacingCorr(rdbtnSuppressSpaceErrors.isSelected());
						obitErrors.suppressFlags.setSuppressWarnings(rdbtnSuppressWarnings.isSelected());

						/* run the file checks */
						obitErrors = ObitEdit.doObitEdit(fileToCheck, obitErrors);

						/* how many records were there? */
						int numRecords = ErrorLog.getNumRecords(obitErrors);
						
						/* update the display per the results */
						String msgResults;
						msgResults = String.format("%4d%n Records", numRecords);
						textFieldResults.setText(msgResults);
						
						int numErrors = ErrorLog.getNumErrors(obitErrors);
						msgResults = String.format("%4d%n Errors", numErrors);
						textFieldErrors.setText(msgResults);				

						textAreaCategories.setText("");
						Set<Entry<ErrorCategory, Integer>> enumSet= obitErrors.errCategoryCounter.entrySet();
						for (Entry<ErrorCategory, Integer> entry:enumSet) {
							msgResults = String.format("%05d %s%n",entry.getValue(),entry.getKey());
							textAreaCategories.append(msgResults);
						}	
						
						/* add on the duplicate count */
						int dupCount = obitErrors.getDupCount();
						if ( dupCount > 0 ) {
							msgResults = String.format("%05d DUPLICATES%n",dupCount);
							textAreaCategories.append(msgResults);
						}

						textAreaFieldErrors.setText("");
						Set<Entry<ErrorField, Integer>> enumSet2= obitErrors.errFieldCount.entrySet();
						for (Entry<ErrorField, Integer> entry:enumSet2) {
							msgResults = String.format("%05d %s%n",entry.getValue(),entry.getKey());
							textAreaFieldErrors.append(msgResults);
						}			

						textArea.setText("");
						Set<Entry<String, Integer>> hashSet= obitErrors.tagErrsMap.entrySet();
						for (Entry<String, Integer> entry:hashSet) {
							msgResults = String.format("%05d %s%n",entry.getValue(),entry.getKey());
							textArea.append(msgResults);
						}
						
						textFieldRpt.setText(obitErrors.rptFileName);
						textFieldCorrectedFile.setText(obitErrors.correctedFileName);
						
						JOptionPane.showMessageDialog(frame, "Checking complete!");
						
					} /* end of else the file exists */
				}
				catch ( Exception ie ) {
					String errMsg = "Invalid file name!\n";
					errMsg = errMsg + "Put file in folder\n";
					errMsg = errMsg + checkFileString;
					errMsg = errMsg + "\nEnter just file name in box";
					JOptionPane.showMessageDialog(frame, errMsg);
				}
				
			} /* end of Check file button listener */
		});
		btnCheckFile.setBounds(275, 31, 116, 23);
		frame.getContentPane().add(btnCheckFile);
		
		JLabel lblErrorCategories = new JLabel("Errors by category:");
		lblErrorCategories.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblErrorCategories.setBounds(27, 124, 139, 14);
		frame.getContentPane().add(lblErrorCategories);
		
		JLabel lblErrorsByField = new JLabel("Error totals by field:");
		lblErrorsByField.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblErrorsByField.setBounds(189, 71, 139, 14);
		frame.getContentPane().add(lblErrorsByField);
		
		JLabel lblErrorByTagname = new JLabel("Error totals by tagname:");
		lblErrorByTagname.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblErrorByTagname.setBounds(348, 71, 176, 14);
		frame.getContentPane().add(lblErrorByTagname);
		
		JLabel lblReportFileLocation = new JLabel("Location of report file:");
		lblReportFileLocation.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblReportFileLocation.setBounds(32, 281, 185, 14);
		frame.getContentPane().add(lblReportFileLocation);
		
		textFieldCorrectedFile = new JTextField();
		textFieldCorrectedFile.setEditable(false);
		textFieldCorrectedFile.setColumns(10);
		textFieldCorrectedFile.setBackground(Color.WHITE);
		textFieldCorrectedFile.setBounds(32, 250, 400, 20);
		frame.getContentPane().add(textFieldCorrectedFile);
		
		JLabel label = new JLabel("Location of corrected file:");
		label.setFont(new Font("Tahoma", Font.BOLD, 13));
		label.setBounds(32, 225, 213, 14);
		frame.getContentPane().add(label);		
		
		JLabel lblErrorMessages = new JLabel("System error messages:");
		lblErrorMessages.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblErrorMessages.setBounds(32, 337, 185, 14);
		frame.getContentPane().add(lblErrorMessages);
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.setBounds(0, 0, 696, 21);
		frame.getContentPane().add(menuBar);
		
		JMenu mnFile = new JMenu("File");
		mnFile.setFont(new Font("Segoe UI", Font.BOLD, 13));
		menuBar.add(mnFile);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		mnFile.add(mntmExit);
		
		JMenu mnHelp = new JMenu("Help");
		mnHelp.setFont(new Font("Segoe UI", Font.BOLD, 13));
		menuBar.add(mnHelp);
				
		JMenuItem mntmObiteditHelp = new JMenuItem("ObitEdit2 Help");
		mntmObiteditHelp.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		mntmObiteditHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String helpMessage = "To use ObitEdit2, just put the file to be checked in the folder\n";
				helpMessage = helpMessage + checkFileString + "\nThen click on the";
				helpMessage = helpMessage + "\"Check file\" button.\nThe error totals ";
				helpMessage = helpMessage + "will appear in the boxes.\nA more detailed ";
				helpMessage = helpMessage + "report about the errors will be in the report ";
				helpMessage = helpMessage + "file listed.\nThe corrected file is listed in ";
				helpMessage = helpMessage + "the box.";
				JOptionPane.showMessageDialog(frame, helpMessage);
			}
		});
		mnHelp.add(mntmObiteditHelp);
		
		JMenuItem mntmUpdatingLibFiles = new JMenuItem("Updating Lib files");
		mntmUpdatingLibFiles.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		mntmUpdatingLibFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String helpMessage = "To update tagname and pubs files, just put the new files in the folder\n";
				helpMessage = helpMessage + DirectorySetup.libFilesPath.toString();

				JOptionPane.showMessageDialog(frame, helpMessage);
			}
		});
		mnHelp.add(mntmUpdatingLibFiles);
		mnHelp.add(mntmAbout);			
		
		JLabel lblPutFileIn = new JLabel("See Help for guidance");
		lblPutFileIn.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblPutFileIn.setBounds(20, 55, 176, 14);
		frame.getContentPane().add(lblPutFileIn);
		
		
		
	} /* end of initialize */
	
	private void loadIcons(JFrame f) {
		ArrayList<Image> icons = new ArrayList<>();
		Image img;

		img = new ImageIcon(this.getClass().getResource("/odt_logo16.png")).getImage();
		icons.add(img);
		img = new ImageIcon(this.getClass().getResource("/odt_logo24.png")).getImage();
		icons.add(img);
		img = new ImageIcon(this.getClass().getResource("/odt_logo32.png")).getImage();
		icons.add(img);
		img = new ImageIcon(this.getClass().getResource("/odt_logo48.png")).getImage();
		icons.add(img);
		img = new ImageIcon(this.getClass().getResource("/odt_logo256.png")).getImage();
		icons.add(img);
		f.setIconImages(icons);
		
	} 
} /* end of class */
