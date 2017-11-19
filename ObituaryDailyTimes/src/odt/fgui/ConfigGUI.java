package odt.fgui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import odt.pub.ConfigFile;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import java.awt.Font;

import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Toolkit;

public class ConfigGUI extends JFrame {

	/**
	 * This class is for setting up the FTP configuration information.
	 */
	private static final long serialVersionUID = 8030456321241888531L;
	private JPanel contentPane;
	private JTextField txtfldUsername;
	private JLabel lblPassword;
	private JPasswordField passwordField;
	private JTextField txtfldHostname;
	ConfigFile configuration;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ConfigGUI frame = new ConfigGUI();
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
	public ConfigGUI() {
		setTitle("FTP Configuration");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 216);
		setIconImage(Toolkit.getDefaultToolkit().getImage(ConfigGUI.class.getResource("/odt/resources/MCC2_32.png")));

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		/* center the frame */
		setLocationRelativeTo(null);
		
		JLabel lblFtpHostname = new JLabel("FTP Hostname:");
		lblFtpHostname.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblFtpHostname.setHorizontalAlignment(SwingConstants.RIGHT);
		lblFtpHostname.setBounds(45, 26, 114, 24);
		contentPane.add(lblFtpHostname);
		
		txtfldHostname = new JTextField();
		txtfldHostname.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtfldHostname.setBounds(169, 30, 207, 24);
		contentPane.add(txtfldHostname);
		txtfldHostname.setColumns(10);

		JLabel lblFtpUsername = new JLabel("FTP Username:");
		lblFtpUsername.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblFtpUsername.setHorizontalAlignment(SwingConstants.RIGHT);
		lblFtpUsername.setBounds(45, 61, 114, 24);
		contentPane.add(lblFtpUsername);
		
		txtfldUsername = new JTextField();
		txtfldUsername.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtfldUsername.setBounds(169, 64, 207, 20);
		contentPane.add(txtfldUsername);
		txtfldUsername.setColumns(10);
		
		lblPassword = new JLabel("Password:");
		lblPassword.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPassword.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblPassword.setBounds(73, 96, 86, 24);
		contentPane.add(lblPassword);
		
		passwordField = new JPasswordField();
		passwordField.setFont(new Font("Tahoma", Font.PLAIN, 14));
		passwordField.setBounds(169, 95, 207, 20);
		contentPane.add(passwordField);
		
		/* Get the current configuration, if there is one.
		 * And display the current values if found.
		 * Otherwise, blank out the fields.
		 */
		configuration = new ConfigFile();
		
		if ( ConfigFile.readConfig() ) {
			txtfldHostname.setText(ConfigFile.getFTPhost());
			txtfldUsername.setText(ConfigFile.getFTPuser());
			passwordField.setText(ConfigFile.getFTPpasswd());
		}
		else {
			txtfldHostname.setText("");
			txtfldUsername.setText("");
			passwordField.setText("");			
		}
		
		JButton btnSaveConfiguration = new JButton("Save Configuration");
		btnSaveConfiguration.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String hostname = txtfldHostname.getText();
				String username = txtfldUsername.getText();
				char pw[] = passwordField.getPassword();
				
				/* Convert the string of characters into a string */
				String passwd = String.valueOf(pw);				
			
				/* Store the configuration information in the file.
				 * If it doesn't work, let the user know.
				 */
				if ( ConfigFile.storeConfig(hostname, username, passwd) ) {
					JOptionPane.showMessageDialog(null, "Configuration saved!");
				}
				else {
					JOptionPane.showMessageDialog(null, "Failed to save configuration", "Configuration message", JOptionPane.ERROR_MESSAGE);
				}
				
			}
		});
		btnSaveConfiguration.setBackground(new Color(255, 255, 255));
		btnSaveConfiguration.setFont(new Font("Tahoma", Font.BOLD, 14));
		btnSaveConfiguration.setBounds(100, 143, 207, 23);
		contentPane.add(btnSaveConfiguration);
		
	}
}
