package odt.fgui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import odt.ftool.IssueConfig;
import odt.ftool.ObitIssue;
import odt.tool.DirectorySetup;

import java.awt.Toolkit;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JButton;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class IssueGUI extends JFrame {

	/**
	 * This class is for a window for generating a new issue
	 * of the Obituary Daily Times.
	 */
	private static final long serialVersionUID = -6326487831118735776L;
	private JPanel ctpIssueGen;
	private JTextField txtFileName;
	private JTextField txtVolume;
	private JLabel lblIssue;
	private JTextField txtIssueNum;
	private JButton btnCreateIssue;
	private JTextField txtYear;
	private JLabel lblYear;
	private JButton btnUpdateIssueInfo;
	private JButton btnDistributeDatabaseFile;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) 
	{
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					IssueGUI frame = new IssueGUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public IssueGUI() 
	{
		initComponents();
		createEvents();		
	}
	
	/* This method initializes all of the components */
	private void initComponents() 
	{
		setTitle("ODT Issue Generator");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 358, 281);
		setIconImage(Toolkit.getDefaultToolkit().getImage(IssueGUI.class.getResource("/odt/resources/MCC2_32.png")));
		/* center the frame */
		setLocationRelativeTo(null);
		
		/* Read the volume and issue from the issue configuration file. */
		IssueConfig.readIssueConfig();
		
		/* Initialize some things */
		DirectorySetup.setMCCFolder();
		ObitIssue.setIssueFileName("");
		
		ctpIssueGen = new JPanel();
		ctpIssueGen.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(ctpIssueGen);
		
		JLabel lblInputFile = new JLabel("Input File: ");
		lblInputFile.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblInputFile.setHorizontalAlignment(SwingConstants.RIGHT);
		
		txtFileName = new JTextField();
		txtFileName.setEditable(false);
		txtFileName.setColumns(20);
		txtFileName.setText(ODTmcc.getFileToCheck());
		
		JLabel lblVolume = new JLabel("Volume: ");
		lblVolume.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblVolume.setHorizontalAlignment(SwingConstants.RIGHT);
		
		txtVolume = new JTextField();
		txtVolume.setColumns(5);
		txtVolume.setText(IssueConfig.getIssueVol());
		
		lblIssue = new JLabel("Issue: ");
		lblIssue.setFont(new Font("Tahoma", Font.PLAIN, 14));
		
		txtIssueNum = new JTextField();
		txtIssueNum.setColumns(5);
		txtIssueNum.setText(IssueConfig.getIssueNum());
		
		txtYear = new JTextField();
		txtYear.setColumns(5);
		txtYear.setText(IssueConfig.getIssueYear());
		
		btnCreateIssue = new JButton("Create Issue");
		btnCreateIssue.setFont(new Font("Tahoma", Font.PLAIN, 14));
	
		
		lblYear = new JLabel("Year:");
		lblYear.setHorizontalAlignment(SwingConstants.CENTER);
		lblYear.setFont(new Font("Tahoma", Font.PLAIN, 14));
		
		btnUpdateIssueInfo = new JButton("Update Issue Info");
		btnUpdateIssueInfo.setFont(new Font("Tahoma", Font.PLAIN, 14));
		
		btnDistributeDatabaseFile = new JButton("Distribute database file");
		btnDistributeDatabaseFile.setFont(new Font("Tahoma", Font.PLAIN, 14));
		
		GroupLayout gl_ctpIssueGen = new GroupLayout(ctpIssueGen);
		gl_ctpIssueGen.setHorizontalGroup(
			gl_ctpIssueGen.createParallelGroup(Alignment.TRAILING)
				.addGroup(Alignment.LEADING, gl_ctpIssueGen.createSequentialGroup()
					.addGap(20)
					.addGroup(gl_ctpIssueGen.createParallelGroup(Alignment.TRAILING)
						.addComponent(lblVolume)
						.addComponent(lblInputFile, GroupLayout.PREFERRED_SIZE, 73, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_ctpIssueGen.createSequentialGroup()
							.addGroup(gl_ctpIssueGen.createParallelGroup(Alignment.TRAILING)
								.addComponent(lblYear, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblIssue))
							.addPreferredGap(ComponentPlacement.RELATED)))
					.addGroup(gl_ctpIssueGen.createParallelGroup(Alignment.LEADING)
						.addComponent(btnUpdateIssueInfo)
						.addComponent(txtVolume, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(txtIssueNum, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(txtYear, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_ctpIssueGen.createSequentialGroup()
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_ctpIssueGen.createParallelGroup(Alignment.LEADING)
								.addComponent(txtFileName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(btnDistributeDatabaseFile, GroupLayout.PREFERRED_SIZE, 184, GroupLayout.PREFERRED_SIZE)
								.addComponent(btnCreateIssue, GroupLayout.PREFERRED_SIZE, 128, GroupLayout.PREFERRED_SIZE))))
					.addContainerGap(55, Short.MAX_VALUE))
		);
		gl_ctpIssueGen.setVerticalGroup(
			gl_ctpIssueGen.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_ctpIssueGen.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_ctpIssueGen.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblInputFile, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
						.addComponent(txtFileName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnCreateIssue)
					.addGap(4)
					.addComponent(btnDistributeDatabaseFile, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_ctpIssueGen.createParallelGroup(Alignment.BASELINE)
						.addComponent(txtVolume, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblVolume))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_ctpIssueGen.createParallelGroup(Alignment.BASELINE)
						.addComponent(txtIssueNum, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblIssue))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_ctpIssueGen.createParallelGroup(Alignment.BASELINE)
						.addComponent(txtYear, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblYear))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(btnUpdateIssueInfo)
					.addContainerGap(95, Short.MAX_VALUE))
		);
		ctpIssueGen.setLayout(gl_ctpIssueGen);
		
	} /* end of initComponents() */

	/* This method creates all of the events */
	private void createEvents() 
	{
		/* listener for the button to create the ODT issue */
		btnCreateIssue.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				String[] args = new String[0];
				
				/* If creating the issue is successful, then update the issue
				 * in the config file.
				 */
				if ( ObitIssue.doObitIssue( ODTmcc.getFileToCheck(), args ) )
				{
					/* read the volume and issue number from the file */
					IssueConfig.readIssueConfig();
			
					/* Update the issue number in the box */
					txtIssueNum.setText(IssueConfig.getIssueNum());
					
					JOptionPane.showMessageDialog( null, "Email and database files are ready!\nTurn off word wrap!");
				}
				else
				{
					JOptionPane.showMessageDialog(null, "Failed to create issue", "Create issue message", JOptionPane.ERROR_MESSAGE);
				}	
			}
		});
		
		btnUpdateIssueInfo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				String volume = txtVolume.getText();
				String issue = txtIssueNum.getText();
				String year = txtYear.getText();
		
				/* Store the configuration information in the file.
				 * If it doesn't work, let the user know.
				 */
				if ( IssueConfig.storeIssConfig(volume, issue, year) ) 
				{
					JOptionPane.showMessageDialog(null, "Configuration saved!");
				}
				else 
				{
					JOptionPane.showMessageDialog(null, "Failed to save configuration", "Configuration message", JOptionPane.ERROR_MESSAGE);
				}				
			}

		});
		
		btnDistributeDatabaseFile.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent arg0) 
			{
				/* Distribute the database to the website and the appropriate
				 * folders on the moderator's machine.
				 */
				ObitIssue.distribDatabase();
				
			} /* end of action performed */
			
		}); /* end of distribute database button */


	} /* end of createEvents() */
}
