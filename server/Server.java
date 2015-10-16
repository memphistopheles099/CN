package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import org.w3c.dom.*;
import javax.xml.parsers.*;

import javax.swing.DefaultListModel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import protocol.Header;

class LiveTime extends Thread{
	String name;
	public LiveTime(String n){
		name = n;
	}
	public void run(){
		try {
			Thread.sleep(60000);
			for(int i = 0; i < Server.accountList.getSize(); i++)
				if (name.equals(Server.accountList.getElementAt(i).getName())){
					Server.accountList.getElementAt(i).setOffline();
					break;
				}
					
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

class StreamHandler extends Thread{
	InputStream is;
	OutputStream os;
	ObjectOutputStream oos;
	StreamHandler(InputStream in, OutputStream out){
		is=in;
		os=out;
		try {
			oos = new ObjectOutputStream(os);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void run(){
		try {
			ObjectInputStream in = new ObjectInputStream(is);
			try {
				while(true){
					Document doc = (Document)in.readObject();
					Element root = doc.getDocumentElement();
				
				// Read MESSAGE
					switch(root.getNodeName()){
					case Header.LOG_IN:{  // Login Event
						String id = root.getElementsByTagName(Header.ID).item(0).getTextContent();
						String pass = root.getElementsByTagName(Header.PASS).item(0).getTextContent();
						Boolean exist = false;
						for(int i = 0; i < Server.accountList.getSize(); i++){
							if (id.equals(Server.accountList.getElementAt(i).getName())&&pass.equals(Server.accountList.getElementAt(i).getPass())){
								exist = true;
								try {
									Document ans = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
									Element res = ans.createElement("RESPONSE");
									res.setTextContent("ACCEPT");
									ans.appendChild(res);
									oos.writeObject(ans);
									System.out.println(Header.LOG_IN+":"+ id);
									Thread countDown = new LiveTime(id);
									Server.manager.addElement(countDown);
									countDown.start();
									//out.close();
								} catch (ParserConfigurationException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								break;
							}
						}
						if(!exist){
							try {
								Document ans = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
								Element res = ans.createElement("RESPONSE");
								res.setTextContent("REJECT");
								ans.appendChild(res);
								ObjectOutputStream out = new ObjectOutputStream(os);
								out.writeObject(ans);
							} catch (ParserConfigurationException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						break;
						} // end case LOGIN
					
					case Header.REQ_LST: {
						
						String nameUser = root.getElementsByTagName(Header.ID).item(0).getTextContent();
						int idx = -1;
						for (int i = 0; i < Server.accountList.size();  i++)
							if (nameUser.equals(Server.accountList.getElementAt(i).getName())){
								idx = i;
								break;
							}
						for(int i = 0; i < Server.manager.getSize(); i++)
							if (nameUser.equals(Server.accountList.getElementAt(i).getName())){
								Server.manager.getElementAt(i).interrupt();
								Server.manager.removeElementAt(i);
								Thread countDown = new LiveTime(nameUser);
								Server.manager.addElement(countDown);;
								countDown.start();
								break;
							}
						Server.accountList.getElementAt(idx).setOnline();
						Server.accountList.getElementAt(idx).setInfo(root.getElementsByTagName(Header.IP).item(0).getTextContent(), Integer.parseInt(root.getElementsByTagName(Header.PORT).item(0).getTextContent())); 
						DefaultListModel<Account> list_req =  Server.getList();
						try {
							Document ansReq = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
							Element listFriend = ansReq.createElement("LIST_FRIEND");
							ansReq.appendChild(listFriend);
							for(int i = 0; i < list_req.getSize(); i++){
								if (list_req.getElementAt(i).getState()&&
										Server.accountList.getElementAt(idx).isFriend(list_req.getElementAt(i).getName())){
									Element buddy = ansReq.createElement("FRIEND");
									Element name = ansReq.createElement(Header.ID);
									name.appendChild(ansReq.createTextNode(list_req.getElementAt(i).getName()));
									buddy.appendChild(name);
									Element ip = ansReq.createElement(Header.IP);
									ip.appendChild(ansReq.createTextNode(list_req.getElementAt(i).getIP()));
									buddy.appendChild(ip);
									Element port = ansReq.createElement(Header.PORT);
									port.appendChild(ansReq.createTextNode(String.valueOf(list_req.getElementAt(i).getPort())));
									buddy.appendChild(port);
									listFriend.appendChild(buddy);
								}
							}
							oos.writeObject(ansReq);
						} catch (ParserConfigurationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						break;
						} // end case REQUEST_LIST
					
					case "REGISTER": {
						String id = root.getElementsByTagName(Header.ID).item(0).getTextContent();
						String pass = root.getElementsByTagName("PASSWORD").item(0).getTextContent();
						Boolean duplicate = false;
						for(int i = 0; i < Server.accountList.getSize(); i++){
							if (id.equals(Server.accountList.getElementAt(i).getName())){
								duplicate = true;
								try {
									Document ans = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
									Element res = ans.createElement("RESPONSE");
									res.setTextContent("REJECT");
									ans.appendChild(res);
									ObjectOutputStream out = new ObjectOutputStream(os);
									out.writeObject(ans);
									out.close();
								} catch (ParserConfigurationException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								break;
							}
						}
						if(!duplicate){
							try {
								Document ans = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
								Element res = ans.createElement("RESPONSE");
								res.setTextContent("ACCEPT");
								ans.appendChild(res);
								ObjectOutputStream out = new ObjectOutputStream(os);
								out.writeObject(ans);
								Server.newAccount(new Account(id, pass));
								//out.close();
							} catch (ParserConfigurationException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						break;
						} // end case REGISTER
					case Header.LOG_OUT : {
						String id = root.getElementsByTagName(Header.ID).item(0).getTextContent();
						for(int i = 0; i < Server.accountList.size(); i++){
							if (id.equals(Server.accountList.getElementAt(i).getName())) {
								Server.accountList.getElementAt(i).setOffline();
								break;
							}
						}
						break;
					}
					case Header.AWK:{
						System.out.println("AWK received");
						String nameUser = root.getTextContent();
						for(int i = 0; i < Server.manager.getSize(); i++)
							if (nameUser.equals(Server.accountList.getElementAt(i).getName())){
								System.out.print("Reset Counting Down");
								Server.manager.getElementAt(i).interrupt();
								Server.manager.removeElementAt(i);
								Thread countDown = new LiveTime(nameUser);
								Server.manager.addElement(countDown);;
								countDown.start();
								break;
							}
						break;
					}
					case Header.FR_REQ:{
						try {
							String me = root.getElementsByTagName(Header.ID).item(0).getTextContent();
							String buddy = root.getElementsByTagName(Header.ID).item(1).getTextContent();
							boolean exist =false;
							for(int i = 0; i < Server.accountList.size(); i++){
								if (buddy.equals(Server.accountList.getElementAt(i).getName())){
									exist =true;
									for(int j = 0; j < Server.accountList.size(); j++){
										if (me.equals(Server.accountList.getElementAt(j).getName())) {
											if(Server.accountList.getElementAt(j).isFriend(buddy)){
												Document res = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
												Element ans = res.createElement(Header.RESPONSE);
												ans.appendChild(res.createTextNode(Header.FR_AL));
												res.appendChild(ans);
												oos.writeObject(res);
											}
											else{
											Server.accountList.getElementAt(j).addFriend(buddy);
											Document res = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
											Element ans = res.createElement(Header.RESPONSE);
											ans.appendChild(res.createTextNode(Header.ACC));
											res.appendChild(ans);
											oos.writeObject(res);
											}
											break;
										}
									}
									break;
								}
							}
							if(!exist){
								Document res = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
								Element ans = res.createElement(Header.RESPONSE);
								ans.appendChild(res.createTextNode(Header.REJ));
								res.appendChild(ans);
								oos.writeObject(res);
							}
							break;
							
						} catch (ParserConfigurationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					} 
				}// end switch
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
}

public class Server {
	public static DefaultListModel<Thread> manager= new DefaultListModel<Thread>();
	public static DefaultListModel<Account> accountList;
	public static void initList(){
		DefaultListModel<String> fl = new DefaultListModel<String>();
		fl.addElement("abcd");
		accountList = new DefaultListModel<Account>();
		accountList.addElement(new Account("abcd","1234",fl));
		fl = new DefaultListModel<String>();
		fl.addElement("abcd");
		fl.addElement("efgh");
		accountList.addElement(new Account("efgh","1234",fl));
	}
	public static void newAccount(Account a){
		accountList.addElement(a);
	}
	public static DefaultListModel<Account> getList(){
		return accountList;
	}
	public static void main(String[] args) throws IOException{
		int port = 6000;
		ServerSocket sSocket= new ServerSocket(port);
		Server.initList();
		while(true){
			try {
				Socket cSocket = sSocket.accept();
				InputStream  inputStream = cSocket.getInputStream();
				OutputStream outputStream = cSocket.getOutputStream();
				StreamHandler s = new StreamHandler(inputStream, outputStream);
				s.start();
			}	
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
