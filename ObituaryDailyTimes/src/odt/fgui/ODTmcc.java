package odt.fgui;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import odt.ftool.CheckForDups;
import odt.ftool.SearchForDups;
import odt.gui.CustomOutputStream;
import odt.pub.BuildPubFiles;
import odt.pub.UploadOdtFile;
import odt.tool.DirectorySetup;
import odt.tool.ErrorCategory;
import odt.tool.ErrorLog;
import odt.tool.ObitEdit;

import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;
import java.util.Set;
import java.util.Map.Entry;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JTextArea;
import java.awt.TextArea;

import javax.swing.SwingConstants;
import java.awt.Toolkit;

public class ODTmcc {

	private static String fileToCheck;
	private String folderToCheck;
	private JFrame frame;
	private JTextField txtFileToCheck;
	private JTextField txtRecords;
	private JTextField txtErrors;
	private JTextField txtFolder;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ODTmcc window = new ODTmcc();
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
	public ODTmcc() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame("Final Moderator Master Control Center");
		frame.setBounds(100, 100, 603, 479);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setIconImage(Toolkit.getDefaultToolkit().getImage(ODTmcc.class.getResource("/odt/resources/MCC2_32.png")));
		frame.setFont(new Font("Tahoma", Font.PLAIN, 20));

		/* this will center the frame */
		frame.setLocationRelativeTo(null);
		
		/* Make "File to Check" textField get the focus whenever 
		 * frame is activated.
		 */
		frame.addWindowFocusListener(new WindowAdapter() 
		{
		    public void windowGainedFocus(WindowEvent e) 
		    {
				txtFileToCheck.requestFocusInWindow();  
		    }
		});
		
		/* Set up the directory structure */
		DirectorySetup dirSetup = new DirectorySetup();
		
		/* Set up the ODT directory structure.  If it fails, we're done. */
		if ( ! dirSetup.isSetupComplete() ) {			
			return;
		}
		
		JLabel lblFileName = new JLabel("File name:");
		lblFileName.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblFileName.setBounds(10, 32, 119, 14);
		frame.getContentPane().add(lblFileName);
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.setBounds(0, 0, 587, 21);
		frame.getContentPane().add(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmConfig = new JMenuItem("Config");
		mntmConfig.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ConfigGUI configAction = new ConfigGUI();
				configAction.setVisible(true);
			}
		});
		mnFile.add(mntmConfig);
		
		JMenuItem mntmMakeIssue = new JMenuItem("Make Issue");
		mntmMakeIssue.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setFileToCheck(txtFileToCheck.getText());
				IssueGUI makeIssue = new IssueGUI();
				makeIssue.setVisible(true);
			}
		});
		mnFile.add(mntmMakeIssue);

		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});		
		mnFile.add(mntmExit);
		
		txtFileToCheck = new JTextField();		
		txtFileToCheck.setFont(new Font("Tahoma", Font.PLAIN, 12));
		txtFileToCheck.setBounds(11, 48, 126, 20);
		frame.getContentPane().add(txtFileToCheck);
		txtFileToCheck.setColumns(10);
		
	
		txtFileToCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				setFileToCheck(txtFileToCheck.getText());
			}
		});
		
		txtRecords = new JTextField();
		txtRecords.setFont(new Font("Monospaced", Font.PLAIN, 12));
		txtRecords.setBackground(Color.WHITE);
		txtRecords.setEditable(false);
		txtRecords.setBounds(460, 29, 113, 20);
		frame.getContentPane().add(txtRecords);
		txtRecords.setColumns(10);
		
		txtErrors = new JTextField();
		txtErrors.setFont(new Font("Monospaced", Font.PLAIN, 12));
		txtErrors.setBackground(Color.WHITE);
		txtErrors.setEditable(false);
		txtErrors.setBounds(460, 60, 113, 20);
		frame.getContentPane().add(txtErrors);
		txtErrors.setColumns(10);

		JTextArea txtrErrorCategories = new JTextArea();
		txtrErrorCategories.setEditable(false);
		txtrErrorCategories.setBounds(307, 47, 143, 54);
		frame.getContentPane().add(txtrErrorCategories);
		
		JButton btnCheckForErrors = new JButton("Check for errors");
		btnCheckForErrors.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnCheckForErrors.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				fileToCheck = txtFileToCheck.getText();
				ErrorLog obitErrors = new ErrorLog();
				obitErrors = ObitEdit.doObitEdit(fileToCheck, obitErrors);
				
				int numRecords = ErrorLog.getNumRecords(obitErrors);
				
				String msgResults;
				msgResults = String.format("%4d%n Records", numRecords);
				txtRecords.setText(msgResults);
				int numErrors = ErrorLog.getNumErrors(obitErrors);
				msgResults = String.format("%4d%n Errors", numErrors);
				txtErrors.setText(msgResults);
				
				txtrErrorCategories.setText("");
				Set<Entry<ErrorCategory, Integer>> enumSet= obitErrors.errCategoryCounter.entrySet();
				for (Entry<ErrorCategory, Integer> entry:enumSet) {
					msgResults = String.format("%05d %s%n",entry.getValue(),entry.getKey());
					txtrErrorCategories.append(msgResults);
				}			
				
				JOptionPane.showMessageDialog(frame, "Checking complete!");
			}
		});
		
		btnCheckForErrors.setBounds(10, 79, 140, 23);
		frame.getContentPane().add(btnCheckForErrors);
		
		txtFolder = new JTextField();
		txtFolder.setFont(new Font("Tahoma", Font.PLAIN, 13));
		txtFolder.setText("dbfiles");
		txtFolder.setBounds(301, 111, 168, 20);
		frame.getContentPane().add(txtFolder);
		txtFolder.setColumns(10);
		
		JButton btnCheckForDups = new JButton("Check for dups");
		btnCheckForDups.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] args = new String[0];
				fileToCheck = txtFileToCheck.getText();
				folderToCheck = txtFolder.getText();
				CheckForDups.doCheckForDups( fileToCheck, folderToCheck, args );
				JOptionPane.showMessageDialog(frame, "Checking complete!");
			}
		});
		btnCheckForDups.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnCheckForDups.setBounds(11, 113, 139, 23);
		frame.getContentPane().add(btnCheckForDups);
		
		TextArea textArea = new TextArea();
//		textArea.setFont(new Font("Lucida Console", Font.BOLD, 18));
		textArea.setBounds(10, 255, 567, 176);
		frame.getContentPane().add(textArea);
		
		PrintStream printStream = new PrintStream(new CustomOutputStream(textArea));
		System.setOut(printStream);
		System.setErr(printStream);

		
		JLabel lblFolderToCheck = new JLabel("Folder for checking:");
		lblFolderToCheck.setHorizontalAlignment(SwingConstants.RIGHT);
		lblFolderToCheck.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblFolderToCheck.setBounds(165, 111, 119, 21);
		frame.getContentPane().add(lblFolderToCheck);
		
		JLabel lblErrorsByCategory = new JLabel("Errors by category");
		lblErrorsByCategory.setBounds(310, 33, 119, 14);
		frame.getContentPane().add(lblErrorsByCategory);
		
		JButton btnBuildPubFiles = new JButton("Build PubFiles");
		btnBuildPubFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String[] args = new String[0];
				BuildPubFiles.doBuildPubFiles(args);
				
				String message = "Files are ready! Would you like to upload them to the website?";
				String title = "Build complete";
				int reply = JOptionPane.showConfirmDialog(frame, message, title, JOptionPane.YES_NO_OPTION);
				
				if ( reply == JOptionPane.YES_OPTION ) {
					BuildPubFiles.uploadPubFiles(dirSetup);
				}
			}
		});
		btnBuildPubFiles.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnBuildPubFiles.setBounds(433, 180, 140, 27);
		frame.getContentPane().add(btnBuildPubFiles);
		
		JButton btnSearchForDups = new JButton("Search for Dups");
		btnSearchForDups.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String[] args = new String[0];
				folderToCheck = txtFolder.getText();
				SearchForDups.doSearchForDups(folderToCheck, args);
				JOptionPane.showMessageDialog(frame, "Searching complete!");
			}
		});
		btnSearchForDups.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnSearchForDups.setBounds(307, 142, 143, 27);
		frame.getContentPane().add(btnSearchForDups);
		
		JLabel lblSystemOutput = new JLabel("System output:");
		lblSystemOutput.setBounds(31, 235, 119, 14);
		frame.getContentPane().add(lblSystemOutput);
		
		JButton btnUploadPubfiles = new JButton("Upload PubFiles");
		btnUploadPubfiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) 
			{
				BuildPubFiles.uploadPubFiles(dirSetup);
			}
		});
		btnUploadPubfiles.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnUploadPubfiles.setBounds(433, 211, 139, 25);
		frame.getContentPane().add(btnUploadPubfiles);
		
		JButton btnUploadLibfiles = new JButton("Upload LibFiles");
		btnUploadLibfiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) 
			{
				UploadOdtFile.uploadLibFiles(dirSetup);
			}
		});
		btnUploadLibfiles.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnUploadLibfiles.setBounds(303, 182, 126, 23);
		frame.getContentPane().add(btnUploadLibfiles);

		
	}

	public static String getFileToCheck() {
		return fileToCheck;
	}

	public void setFileToCheck(String fileToCheck) {
		ODTmcc.fileToCheck = fileToCheck;
	}
}
