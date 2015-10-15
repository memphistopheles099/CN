package client;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;
import javax.swing.SwingConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.DropMode;
import javax.swing.JTextArea;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

class chatListener extends Thread{
	//public chatListener()
}

public class GUIp2p {

	JFrame frame;
	private Boolean SendbyEnter;
	String name;
	private String IP;
	private Socket toPeer;
	private Peer host;
	private int port;
	private JTextArea textAreaMsgShow;
	ObjectOutputStream out;
	GUIp2p(Peer h, Peer buddy){
		name = buddy.name;
		IP = buddy.IP;
		port = buddy.port;
		host = h;
		try {
			toPeer = new Socket(IP,port);
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
					GUIp2p window = new GUIp2p(null);
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public GUIp2p(Socket s) throws UnknownHostException, IOException {
		toPeer = s;
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	public void receiveMessage(String msg){
		textAreaMsgShow.setText(textAreaMsgShow.getText()+name+": "+msg+"\n");
	}
	private void initialize(){
		try {
			out = new ObjectOutputStream(toPeer.getOutputStream());
			toPeer = new Socket(IP,port);
			frame = new JFrame();
			frame.setBounds(100, 100, 484, 358);
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame.setTitle(name);
			SendbyEnter = true;
	
			textAreaMsgShow = new JTextArea();
			textAreaMsgShow.setEditable(false);
			
			JTextArea textAreaMsgType = new JTextArea();
			
			JButton btnSend = new JButton("G\u1EEDi");
			btnSend.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					/*
					 * Gui messages
					 */
					try {
						Document msg = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
						Element chat = msg.createElement("SESSION_CHAT_ME");
						msg.appendChild(chat);
						Element chatName = msg.createElement("NAME");
						chatName.appendChild(msg.createTextNode(host.name));
						chat.appendChild(chatName);
						System.out.println(host.name);
						Element chatIP = msg.createElement("IP");
						chatIP.appendChild(msg.createTextNode(host.IP));
						chat.appendChild(chatIP);
						System.out.println(host.IP);
						Element chatPort = msg.createElement("PORT");
						chatPort.appendChild(msg.createTextNode(String.valueOf(host.port)));
						chat.appendChild(chatPort);
						Element mes = msg.createElement("MESSAGE");
						mes.appendChild(msg.createTextNode(textAreaMsgType.getText()));
						chat.appendChild(mes);
						System.out.println(host.port);
						try {
							out.writeObject(msg);
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
					 * 
					 * Send a file 
					 * 
					 */
				}
			});
			
			JCheckBox chckbxSendbyEnter = new JCheckBox("Nh\u1EA5n Enter \u0111\u1EC3 g\u1EEDi");
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
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}