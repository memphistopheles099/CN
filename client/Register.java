package client;

import java.awt.Component;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.xml.parsers.*;

import java.awt.Font;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.io.*;
import java.net.*;
import server.*;

import protocol.Header;

public class Register {

	public JFrame frame;
	public JFrame parent;
	private JTextField textName;
	private JTextField textPassword;
	private JTextField txtLocalhost;
	private JButton btnAbort;
	
	private Socket cSocket;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Register window = new Register(null);
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
	public Register(JFrame p) {
		parent = p;
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 303, 170);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		JLabel lblNickname = new JLabel("Nickname");
		
		JLabel lblPassword = new JLabel("Password");
		
		JButton btnRegister = new JButton("Register");
		btnRegister.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				/*
				 * Send register_message to server
				 * Using Server.addAccount( Account )
				 */
				String name = textName.getText();
				String password = textPassword.getText();
				if (name.isEmpty() || password.isEmpty())
					JOptionPane.showMessageDialog(null, "Please input your Username and Password");
				else {
					try {
						Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
						Element reg = doc.createElement(Header.REGISTER);
						doc.appendChild(reg);
						Element id = doc.createElement(Header.ID);
						id.appendChild(doc.createTextNode(textName.getText()));
						reg.appendChild(id);
						Element pass = doc.createElement("PASSWORD");
						pass.appendChild(doc.createTextNode(textPassword.getText()));
						reg.appendChild(pass);
						try {
							cSocket = new Socket(InetAddress.getLocalHost(), 6000);
							ObjectOutputStream out = new ObjectOutputStream(cSocket.getOutputStream());
							out.writeObject(doc);
							ObjectInputStream in = new ObjectInputStream(cSocket.getInputStream());
							try {
								Document docin = (Document) in.readObject();
								Element response = docin.getDocumentElement();
								if (response.getNodeName().equals(Header.RESPONSE)) {
									if (response.getTextContent().equals("ACCEPT")) {
										JOptionPane.showMessageDialog(null, "Success");
										frame.setVisible(false);
										if (parent != null)
											parent.setVisible(true);
										else {
											Login login = new Login();
											login.frmLogin.setVisible(true);
										}		
									}
									else 
										JOptionPane.showMessageDialog(null, "Username already exists");
								}
							} catch (ClassNotFoundException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} 
						} catch (UnknownHostException e2) {
						// TODO Auto-generated catch block
							e2.printStackTrace(); 
						} catch (IOException e3) {
						// TODO Auto-generated catch block
							e3.printStackTrace();
						}
					} catch (ParserConfigurationException e4) {
					// TODO Auto-generated catch block
						e4.printStackTrace();
					}
				}
			}
		});
		btnRegister.setFont(new Font("Tahoma", Font.BOLD, 11));
		
		 btnAbort = new JButton("Abort");
		btnAbort.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(parent!=null) parent.setVisible(true);
				frame.dispose();
			}
		});
		
		
		frame.addWindowListener(new java.awt.event.WindowAdapter(){
			public void windowClosing(java.awt.event.WindowEvent e){
				btnAbort.doClick();
			}
		});
		
		JButton btnReset = new JButton("Reset");
		btnReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				textName.setText("");
				textPassword.setText("");
			}
		});
		
		textName = new JTextField();
		textName.setColumns(10);
		
		textPassword = new JTextField();
		textPassword.setColumns(10);
		
		JLabel lblHost = new JLabel("Host");
		
		txtLocalhost = new JTextField();
		txtLocalhost.setText("localhost");
		txtLocalhost.setColumns(10);
		GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(btnRegister)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(btnReset, GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(btnAbort, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(lblNickname)
								.addComponent(lblHost)
								.addComponent(lblPassword))
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(textPassword)
								.addComponent(txtLocalhost, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)
								.addComponent(textName, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE))))
					.addGap(8))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(textName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNickname))
					.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(txtLocalhost, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblHost))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblPassword)
						.addComponent(textPassword, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnRegister)
						.addComponent(btnReset)
						.addComponent(btnAbort))
					.addGap(38))
		);
		frame.getContentPane().setLayout(groupLayout);
	}
}
