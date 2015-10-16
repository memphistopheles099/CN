package client;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;
import javax.swing.SwingConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import protocol.Header;

import javax.swing.DropMode;
import javax.swing.JTextArea;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.ObjectOutputStream;

class chatListener extends Thread{
	//public chatListener()
}
class FileTransfer extends Thread{
	private ObjectOutputStream oos;
	private File file;
	private String hostname;
	public FileTransfer(ObjectOutputStream s, File f, String n){
		oos = s;
		file = f;
		hostname = n;
	}
	public void run() {
		try {
			try {
				try{
					FileInputStream fis = new FileInputStream(file);
					byte[] data = new byte[(int)file.length()];
					fis.read(data);
					fis.close();
					
					Document fileSending = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
					Element sendFile = fileSending.createElement(Header.SENDFILE);
					fileSending.appendChild(sendFile);
					Element name = fileSending.createElement(Header.ID);
					name.appendChild(fileSending.createTextNode(hostname));
					sendFile.appendChild(name);
					Element filename = fileSending.createElement(Header.FILENAME);
					filename.appendChild(fileSending.createTextNode(file.getName()));
					sendFile.appendChild(filename);
					oos.writeObject(fileSending);
					oos.writeObject(data);
				}
				catch (FileNotFoundException e){
					JOptionPane.showMessageDialog(null, "File kh\u00F4ng t\u1ED3n t\u1EA1i");
				}
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
public class GUIp2p {

	JFrame frame;
	private Boolean SendbyEnter;
	String name;
	private String IP;
	private Socket toPeer;
	private int port;
	private Peer host;
	private JTextArea textAreaMsgShow;
	public JTextArea textAreaMsgType;
	public JCheckBox chckbxSendbyEnter;
	public JButton btnSend;
	public String message;
	ObjectOutputStream out;
	GUIp2p(Peer h, Peer buddy){
		name = buddy.name;
		IP = buddy.IP;
		port = buddy.port;
		host = h;
		try {
			toPeer = new Socket(IP,port);
			out = new ObjectOutputStream(toPeer.getOutputStream());
			initialize();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUIp2p window = new GUIp2p(null,null);
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Initialize the contents of the frame.
	 */
	public void receiveMessage(String msg){
		textAreaMsgShow.setText(textAreaMsgShow.getText()+name+": "+msg+"\n");
	}
	private void initialize(){
			frame = new JFrame();
			frame.setResizable(false);
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame.setBounds(100, 100, 484, 358);
			frame.setTitle(name);
			SendbyEnter = true;
	
			textAreaMsgShow = new JTextArea();
			textAreaMsgShow.setEditable(false);
			
			textAreaMsgType = new JTextArea();
			
			btnSend = new JButton("G\u1EEDi");
			btnSend.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					/*
					 * 
					 * 
					 *  Send a message 
					 *  user toPeer to send  
					 *  
					 *  
					 */
					try {
						Document msg = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
						Element chat = msg.createElement(Header.CHAT);
						msg.appendChild(chat);
						Element chatName = msg.createElement(Header.ID);
						chatName.appendChild(msg.createTextNode(host.name));
						chat.appendChild(chatName);
						Element chatIP = msg.createElement("IP");
						chatIP.appendChild(msg.createTextNode(host.IP));
						chat.appendChild(chatIP);
						Element chatPort = msg.createElement("PORT");
						chatPort.appendChild(msg.createTextNode(String.valueOf(host.port)));
						chat.appendChild(chatPort);
						Element mes = msg.createElement("MESSAGE");
						mes.appendChild(msg.createTextNode(textAreaMsgType.getText()));
						chat.appendChild(mes);
						try {
							out.writeObject(msg);
							System.out.println(host+" send: "+textAreaMsgType.getText());
							textAreaMsgShow.setText(textAreaMsgShow.getText()+"Me: "+textAreaMsgType.getText()+"\n");
							textAreaMsgType.setText("");
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					} catch (ParserConfigurationException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			});
			
			JButton btnAttache = new JButton("\u0110\u00EDnh k\u00E8m t\u1EC7p");
			btnAttache.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					/*
					 * Send a file 
					 *
					 */
					JFileChooser choose = new JFileChooser();
					choose.setCurrentDirectory(new File(System.getProperty("user.home")));
					int res = choose.showOpenDialog(null);
					if (res==JFileChooser.APPROVE_OPTION){
						Thread sendFile = new FileTransfer(out, choose.getSelectedFile(), host.name);
						sendFile.start();
					}
				}
			});
			
			chckbxSendbyEnter = new JCheckBox("Nh\u1EA5n Enter \u0111\u1EC3 g\u1EEDi");
			chckbxSendbyEnter.setSelected(true);
			chckbxSendbyEnter.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					if (chckbxSendbyEnter.isSelected()) {
						btnSend.setEnabled(false);
						SendbyEnter = true;
					}
					else {
						btnSend.setEnabled(true);
						SendbyEnter = false;
					}
				}
			});
	
			textAreaMsgType.addKeyListener(new KeyAdapter() {
				@Override
				public void keyTyped(KeyEvent arg0) {
					if( arg0.getKeyChar()=='\n') {
						if(SendbyEnter) btnSend.doClick();
					}
				}
			});
			frame.addWindowListener(new java.awt.event.WindowAdapter(){
				public void windowClosing(java.awt.event.WindowEvent e){
					/*
					 * 
					 *  xoa khoi windowList trong GUIhome
					 *  
					 */
					try {
						Document msgclose1 = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
						Element chat = msgclose1.createElement(Header.CHAT);
						msgclose1.appendChild(chat);
						Element chatName = msgclose1.createElement(Header.ID);
						chatName.appendChild(msgclose1.createTextNode(host.name));
						chat.appendChild(chatName);
						Element chatIP = msgclose1.createElement("IP");
						chatIP.appendChild(msgclose1.createTextNode(host.IP));
						chat.appendChild(chatIP);
						Element chatPort = msgclose1.createElement("PORT");
						chatPort.appendChild(msgclose1.createTextNode(String.valueOf(host.port)));
						chat.appendChild(chatPort);
						Element mes = msgclose1.createElement("MESSAGE");
						mes.appendChild(msgclose1.createTextNode("HAS JUST CLOSED CHAT WINDOW!"));
						chat.appendChild(mes);
						Document msgclose2;
					
						msgclose2 = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
						Element cls = msgclose2.createElement(Header.CLSWIN);
						cls.appendChild(msgclose2.createTextNode(name));
						msgclose2.appendChild(cls);
						System.out.println(GUIhome.windowList.size());
						try {
							Socket selfCon=new Socket(host.IP, host.port);
							ObjectOutputStream os = new ObjectOutputStream(selfCon.getOutputStream());
							os.writeObject(msgclose2);
							selfCon.close();
							os.close();
							out.writeObject(msgclose1);
						} catch (UnknownHostException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					} catch (ParserConfigurationException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					try {
						toPeer.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			});
			GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
			groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
					.addGroup(groupLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
							.addComponent(chckbxSendbyEnter)
							.addComponent(textAreaMsgShow, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
							.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
								.addComponent(textAreaMsgType, GroupLayout.PREFERRED_SIZE, 332, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
									.addComponent(btnSend, GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
									.addComponent(btnAttache, GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE))))
						.addContainerGap())
			);
			groupLayout.setVerticalGroup(
				groupLayout.createParallelGroup(Alignment.TRAILING)
					.addGroup(Alignment.LEADING, groupLayout.createSequentialGroup()
						.addContainerGap()
						.addComponent(textAreaMsgShow, GroupLayout.PREFERRED_SIZE, 209, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(chckbxSendbyEnter)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
							.addGroup(groupLayout.createSequentialGroup()
								.addComponent(btnAttache)
								.addGap(4)
								.addComponent(btnSend))
							.addComponent(textAreaMsgType, GroupLayout.PREFERRED_SIZE, 53, GroupLayout.PREFERRED_SIZE))
						.addContainerGap(88, Short.MAX_VALUE))
			);
			frame.getContentPane().setLayout(groupLayout);
	}
}