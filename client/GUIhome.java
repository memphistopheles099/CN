package client;

import java.net.*;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.LayoutStyle.ComponentPlacement;

import server.Account;
import server.Server;

import javax.swing.ListModel;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import protocol.Header;

import javax.swing.JLabel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

class KeepOnline extends Thread{
	public String name;
	private ObjectOutputStream oos;
	public KeepOnline(String id, ObjectOutputStream s){
		name = id;
		oos = s;
	}
	public void run(){
		try {
			while (true){
				Thread.sleep(50000);
				try {
					Document awake = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
					Element awk = awake.createElement(Header.AWK);
					awk.appendChild(awake.createTextNode(name));
					awake.appendChild(awk);
					try {
						System.out.println("Send AWK");
						oos.writeObject(awake);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

class startChat extends Thread{
	private Socket chat;
	private String name;
	private ServerSocket server;
	public startChat(String n, ServerSocket s, Socket c){
		name = n;
		server = s;
		chat = c;
	}
	public void run(){
		try {
			ObjectInputStream in = new ObjectInputStream(chat.getInputStream());
			Boolean created = false;
			while (true){
				try {
					Document message = (Document)in.readObject();
					Element root = message.getDocumentElement();
					switch(root.getNodeName()){
						case Header.CHAT: {
							String nameGuess = root.getElementsByTagName(Header.ID).item(0).getTextContent();
							for (int i = 0; i < GUIhome.windowList.getSize(); i++){
								if (nameGuess.equals(GUIhome.windowList.elementAt(i).name)){
									created = true;
									GUIhome.windowList.elementAt(i).receiveMessage(root.getElementsByTagName(Header.MSG).item(0).getTextContent());
									break;
								}
							}
							if(!created){
								Peer buddy = new Peer(root.getElementsByTagName(Header.ID).item(0).getTextContent(),
										root.getElementsByTagName(Header.IP).item(0).getTextContent(),
										Integer.parseInt(root.getElementsByTagName(Header.PORT).item(0).getTextContent()));
								GUIhome.newWindow(
									new Peer(name,InetAddress.getLocalHost().getHostAddress(),server.getLocalPort()),
									buddy, root.getElementsByTagName("MESSAGE").item(0).getTextContent()
									);
							}
							break;
						}
						case Header.SENDFILE:{
							String name = root.getElementsByTagName(Header.ID).item(0).getTextContent();
							String filename = root.getElementsByTagName(Header.FILENAME).item(0).getTextContent();
							String nameOnly = filename.substring(0, filename.indexOf("."));
							String ext = filename.substring(filename.indexOf("."),filename.length());
							String path = "C:\\Chat4N\\";
							int i=1;
							if(new File(path+nameOnly+ext).exists())
							while (true){
								String temp= nameOnly.concat("_"+String.valueOf(i));
								if(new File(path+temp+ext).exists()) i++;
								else {
									nameOnly=temp;
									break;
								}
							}
							File receivedFile = new File(path+nameOnly+ext);
							receivedFile.getParentFile().mkdirs();
							byte[] data = (byte[])in.readObject();
							FileOutputStream fos = new FileOutputStream(receivedFile);
							fos.write(data);
							fos.close();
							int res = JOptionPane.showConfirmDialog(null, name+" has just sent a file named "+nameOnly+ext+".\nDo you want to keep it?","File Receiving",JOptionPane.YES_NO_OPTION);
							if (res==JOptionPane.NO_OPTION) receivedFile.delete();
							break;
						}
						case Header.CLSWIN: {
							String window = root.getTextContent();
							GUIhome.closeWindow(window);
							System.out.println(GUIhome.windowList.getSize());
						}
						
					}
				}
					catch (ClassNotFoundException e) {
				}
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

class chatWaiter extends Thread{
	ServerSocket server;
	String name;
	public chatWaiter(String n, ServerSocket s){
		server=s;
		name = n;
	}
	public void run(){
		while (true){
			try {
				Socket c = server.accept();
				Thread chat = new startChat(name, server, c);
				chat.start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

public class GUIhome {
	JList<Peer> list;
	DefaultListModel<Peer> buddyList;
	Thread waiter;
	String IP;
	int port;
	public static DefaultListModel<GUIp2p> windowList = new DefaultListModel<GUIp2p>();
	public static void newWindow(Peer me, Peer buddy, String msg){
		GUIp2p chat;
		chat = new GUIp2p(me, buddy);
		chat.frame.setVisible(true);
		chat.receiveMessage(msg);
		GUIhome.windowList.addElement(chat);
	}
	public static void closeWindow(String name){
		for (int i = 0; i < windowList.size(); i++){
			if (windowList.getElementAt(i).name.equals(name))
				windowList.remove(i);
		}
	}
	JFrame frame;
	private Socket csocket=null;
	private ServerSocket ssocket=null;
	String name;
	ObjectOutputStream oos;
	ObjectInputStream ios;
	JButton btnLogout;
	/**
	 * Launch the application.
	 */
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUIhome window = new GUIhome("");
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
	public GUIhome(String n) {
		name = n;
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		try {
			ssocket = new ServerSocket(0);
			IP= InetAddress.getLocalHost().getHostAddress();
			port= ssocket.getLocalPort();
			waiter = new chatWaiter(name, ssocket);
			waiter.start();
			
			csocket = new Socket(InetAddress.getLocalHost(),6000);
			oos = new ObjectOutputStream(csocket.getOutputStream());
			ios = new ObjectInputStream(csocket.getInputStream());
			Thread awake = new KeepOnline(name, oos);
			awake.start();
			frame = new JFrame();
			frame.setTitle("4N+T");
			frame.setBounds(100, 100, 348, 291);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			buddyList= new DefaultListModel<Peer>();
			try {
				Document memberList = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				Element requestList = memberList.createElement("REQUEST_LIST");
				memberList.appendChild(requestList);
				Element name_t = memberList.createElement(Header.ID);
				name_t.appendChild(memberList.createTextNode(name));
				requestList.appendChild(name_t);
				Element ip_t =  memberList.createElement("IP");
				ip_t.appendChild(memberList.createTextNode(String.valueOf(InetAddress.getLocalHost().getHostAddress())));
				requestList.appendChild(ip_t);
				Element port_t = memberList.createElement("PORT");
				port_t.appendChild(memberList.createTextNode(String.valueOf(ssocket.getLocalPort())));
				requestList.appendChild(port_t);
				try {
					oos.writeObject(memberList);
					
					try {
						Document req = (Document)ios.readObject();
						Element listfriend= req.getDocumentElement();
						if (listfriend.getNodeName().equals("LIST_FRIEND")){
							NodeList nodes = listfriend.getElementsByTagName("FRIEND");
							for (int i = 0; i < nodes.getLength(); i++){
								Node n = nodes.item(i);
								if (n.getNodeType()==Node.ELEMENT_NODE){
									Element elem = (Element)n;
									String name = elem.getElementsByTagName(Header.ID).item(0).getTextContent();
									String ip = elem.getElementsByTagName("IP").item(0).getTextContent();
									int port = Integer.parseInt(elem.getElementsByTagName("PORT").item(0).getTextContent());
									buddyList.addElement(new Peer(name, ip, port));
								}
							}
						}
						
					} catch (ClassNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			} catch (ParserConfigurationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
			list= new JList<Peer>(buddyList);
			JButton btnChat = new JButton("Chat");
			btnChat.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int idx = list.getSelectedIndex();
					GUIp2p chatWindow = new GUIp2p(new Peer(name, IP, port),new Peer(buddyList.getElementAt(idx).name,buddyList.getElementAt(idx).IP,buddyList.getElementAt(idx).port));
					GUIhome.windowList.addElement(chatWindow);
					chatWindow.frame.setVisible(true);
				}
			});
			
			
			
			
			
			btnLogout = new JButton("\u0110\u0103ng xu\u1EA5t");
			btnLogout.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
						Element logout = doc.createElement(Header.LOG_OUT);
						doc.appendChild(logout);
						Element id = doc.createElement(Header.ID);
						id.appendChild(doc.createTextNode(name));
						logout.appendChild(id);						
						try {
							oos.writeObject(doc);
							frame.setVisible(false);
							csocket.close();
							
							//close all chat window
						//	for (int i = 0; i < windowList.size(); i++)
						//		windowList.remove(i);
							
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
			});
			

			frame.addWindowListener(new java.awt.event.WindowAdapter(){
				public void windowClosing(java.awt.event.WindowEvent e){
					btnLogout.doClick();
				}
			});
			JLabel lblDanhSchOnline = new JLabel("Danh s\u00E1ch online");
			
			JButton btnRefresh = new JButton("Refresh");
			btnRefresh.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					/*
					 * Yeu cau danh sach moi duoc cap nhat.
					 */
					try {
						Document memberList = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
						Element requestList = memberList.createElement(Header.REQ_LST);
						memberList.appendChild(requestList);
						Element name_t = memberList.createElement(Header.ID);
						name_t.appendChild(memberList.createTextNode(name));
						requestList.appendChild(name_t);
						Element ip_t =  memberList.createElement(Header.IP);
						ip_t.appendChild(memberList.createTextNode(String.valueOf(InetAddress.getLocalHost().getHostAddress())));
						requestList.appendChild(ip_t);
						Element port_t = memberList.createElement(Header.PORT);
						port_t.appendChild(memberList.createTextNode(String.valueOf(ssocket.getLocalPort())));
						requestList.appendChild(port_t);
						try {
							oos.writeObject(memberList);
							try {
								Document req = (Document) ios.readObject();
								Element listfriend = req.getDocumentElement();
								if (listfriend.getNodeName().equals("LIST_FRIEND")) {
									NodeList nodes = listfriend.getElementsByTagName("FRIEND");
									buddyList = new DefaultListModel<Peer>();
									for (int i = 0; i < nodes.getLength(); i++) {
										Node n = nodes.item(i);
										if (n.getNodeType() == Node.ELEMENT_NODE) {
											Element elem = (Element) n;
											String name = elem.getElementsByTagName(Header.ID).item(0).getTextContent();
											String ip = elem.getElementsByTagName(Header.IP).item(0).getTextContent();
											int port = Integer.parseInt(elem.getElementsByTagName(Header.PORT).item(0).getTextContent());
											buddyList.addElement(new Peer(name, ip, port));
										}
									}
									list.setModel(buddyList);
								}
							} catch (ClassNotFoundException e1) {
								e1.printStackTrace();
							}
						} catch(IOException e2) {
							e2.printStackTrace();
						}
					} catch (ParserConfigurationException e) {
						e.printStackTrace();
					} catch (UnknownHostException e1) {
						e1.printStackTrace();
					}
				}
			});
			frame.addWindowListener(new java.awt.event.WindowAdapter(){
				public void windowClosing(java.awt.event.WindowEvent e){
					btnLogout.doClick();
				}
			});
			
			JButton btnAddFriend = new JButton("Th\u00EAm b\u1EA1n");
			btnAddFriend.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					String res = JOptionPane.showInputDialog("Nh\u1EADp t\u00EAn t\u00E0i kho\u1EA3n b\u1EA1n b\u00E8");
					if(!res.equals("")){
						Document addFriend;
						try {
							addFriend = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
							Element frReq = addFriend.createElement(Header.FR_REQ);
							addFriend.appendChild(frReq);
							Element name_t = addFriend.createElement(Header.ID);
							name_t.appendChild(addFriend.createTextNode(name));
							frReq.appendChild(name_t);

							Element name_f = addFriend.createElement(Header.ID);
							name_f.appendChild(addFriend.createTextNode(res));
							frReq.appendChild(name_f);
							oos.writeObject(addFriend);
							try {
								Document sRes = (Document)ios.readObject();
								Element ans = sRes.getDocumentElement();
								switch(ans.getTextContent()){
								case Header.ACC:
									JOptionPane.showMessageDialog(null, "Th\u00EAm b\u1EA1n th\u00E0nh c\u00F4ng");
									break;
								case Header.FR_AL:
									JOptionPane.showMessageDialog(null, "\u0110\u00E3 l\u00E0 b\u1EA1n b\u00E8");
									break;
								case Header.REJ:
									JOptionPane.showMessageDialog(null, "T\u00EAn t\u00E0i kho\u1EA3n kh\u00F4ng t\u1ED3n t\u1EA1i");
									break;
								}
							} catch (ClassNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} catch (ParserConfigurationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}						
					}					
				}
			});
			GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
			groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
					.addGroup(groupLayout.createSequentialGroup()
						.addGap(19)
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
							.addComponent(lblDanhSchOnline)
							.addGroup(groupLayout.createSequentialGroup()
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(list, GroupLayout.PREFERRED_SIZE, 198, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
									.addComponent(btnRefresh, GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
									.addComponent(btnLogout, GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
									.addComponent(btnChat, GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
									.addComponent(btnAddFriend, GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE))))
						.addContainerGap())
			);
			groupLayout.setVerticalGroup(
				groupLayout.createParallelGroup(Alignment.TRAILING)
					.addGroup(groupLayout.createSequentialGroup()
						.addContainerGap(20, Short.MAX_VALUE)
						.addComponent(lblDanhSchOnline)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
							.addGroup(groupLayout.createSequentialGroup()
								.addComponent(btnChat, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(btnAddFriend, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(btnRefresh, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(btnLogout, GroupLayout.PREFERRED_SIZE, 44, GroupLayout.PREFERRED_SIZE))
							.addComponent(list, GroupLayout.PREFERRED_SIZE, 201, GroupLayout.PREFERRED_SIZE))
						.addContainerGap())
			);
			frame.getContentPane().setLayout(groupLayout);
		} catch (UnknownHostException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}
		
}
