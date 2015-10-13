package client;

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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

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

import javax.swing.JLabel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

class startChat extends Thread{
	private Socket chat;
	public startChat(Socket c){
		chat = c;
	}
	public void run(){
		try {
			ObjectInputStream in = new ObjectInputStream(chat.getInputStream());
			Boolean created = false;
			while (true){
				try {
					Document message = (Document)in.readObject();
					Element root = message.getElementById("SESSION_CHAT_ME");
					String name = ((Element)root.getElementsByTagName("NAME").item(0)).getTextContent();
					if(created)
						for (int i = 0; i < GUIhome.windowList.getSize(); i++){
							if (name.equals(GUIhome.windowList.elementAt(i).name)){
								GUIhome.windowList.elementAt(i).receiveMessage(root.getElementsByTagName("MESSAGE").item(0).getTextContent());
								break;
							}
						}
					else{
						GUIhome.newWindow(root.getElementsByTagName("NAME").item(0).getTextContent(),
								root.getElementsByTagName("IP").item(0).getTextContent(),
								Integer.parseInt(root.getElementsByTagName("PORT").item(0).getTextContent()),
								root.getElementsByTagName("SESSION_CHAT_ME").item(0).getTextContent());
					}
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
	public chatWaiter(ServerSocket s){
		server=s;
	}
	public void run(){
		while (true){
			try {
				Socket c = server.accept();
				Thread chat = new startChat(c);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}


class Peer {
	String name;
	String IP;
	int port;
	Peer(String n, String i, int p){
		name = n;
		IP = i;
		port = p;
	}
	public String toString(){
		return name+"<"+IP+":"+port+">";
	}
}

public class GUIhome {
	public static DefaultListModel<GUIp2p> windowList = new DefaultListModel<GUIp2p>();
	public static void newWindow(String name, String ip, int port, String msg){
		GUIp2p chat;
		chat = new GUIp2p(name, ip, port);
		chat.frame.setVisible(true);
		chat.receiveMessage(msg);
		windowList.addElement(chat);
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
	Thread waiter;
	String name;
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
		frame.addWindowListener(new java.awt.event.WindowAdapter(){
			public void windowClosing(java.awt.event.WindowEvent e){
				btnLogout.doClick();
			}
		});
		try {
			ssocket = new ServerSocket(0);
			waiter = new chatWaiter(ssocket);
			waiter.start();
			csocket = new Socket(InetAddress.getLocalHost(),6000);
			frame = new JFrame();
			frame.setTitle("4N+T");
			frame.setBounds(100, 100, 348, 291);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			DefaultListModel<Peer> buddyList= new DefaultListModel<Peer>();
			try {
				Document memberList = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				Element requestList = memberList.createElement("REQUEST_LIST");
				memberList.appendChild(requestList);
				Element name_t = memberList.createElement("NAME");
				name_t.appendChild(memberList.createTextNode(name));
				requestList.appendChild(name_t);
				Element ip_t =  memberList.createElement("IP");
				ip_t.appendChild(memberList.createTextNode(String.valueOf(InetAddress.getLocalHost().getHostAddress())));
				requestList.appendChild(ip_t);
				Element port_t = memberList.createElement("PORT");
				port_t.appendChild(memberList.createTextNode(String.valueOf(ssocket.getLocalPort())));
				requestList.appendChild(port_t);
				try {
					ObjectOutputStream out = new ObjectOutputStream(csocket.getOutputStream());
					out.writeObject(memberList);
					ObjectInputStream in = new ObjectInputStream(csocket.getInputStream());
					try {
						Document req = (Document)in.readObject();
						Element listfriend= req.getDocumentElement();
						if (listfriend.getNodeName().equals("LIST_FRIEND")){
							NodeList nodes = listfriend.getElementsByTagName("FRIEND");
							for (int i = 0; i < nodes.getLength(); i++){
								Node n = nodes.item(i);
								if (n.getNodeType()==Node.ELEMENT_NODE){
									Element elem = (Element)n;
									String name = elem.getElementsByTagName("NAME").item(0).getTextContent();
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
			
			
			JList<Peer> list= new JList<Peer>(buddyList);
			JButton btnChat = new JButton("Chat");
			btnChat.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int idx = list.getSelectedIndex();
					GUIp2p chatWindow;
					chatWindow = new GUIp2p(buddyList.getElementAt(idx).name,buddyList.getElementAt(idx).IP,buddyList.getElementAt(idx).port);
					chatWindow.frame.setVisible(true);
				}
			});
			
			
			
			
			
			btnLogout = new JButton("\u0110\u0103ng xu\u1EA5t");
			btnLogout.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					waiter.stop();
					/*
					 * 
					 * Gui message LOG OUT cho server.
					 * 
					 */
				}
			});
			
			
			JLabel lblDanhSchOnline = new JLabel("Danh s\u00E1ch online");
			
			JButton btnRefresh = new JButton("Refresh");
			btnRefresh.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					/*
					 * 
					 * Yeu cau danh sach moi duoc cap nhat.
					 * 
					 */
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
								.addPreferredGap(ComponentPlacement.UNRELATED)
								.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
									.addComponent(btnLogout, GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE)
									.addComponent(btnRefresh, GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE)
									.addComponent(btnChat, GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE))))
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
								.addComponent(btnChat, GroupLayout.PREFERRED_SIZE, 54, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(ComponentPlacement.UNRELATED)
								.addComponent(btnRefresh, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addPreferredGap(ComponentPlacement.UNRELATED)
								.addComponent(btnLogout, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)
								.addGap(7))
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
