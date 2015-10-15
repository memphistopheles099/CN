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



class StreamHandler extends Thread{
	InputStream is;
	OutputStream os;
	StreamHandler(InputStream in, OutputStream out){
		is=in;
		os=out;
	}
	public void run(){
		try {
			ObjectInputStream in = new ObjectInputStream(is);
			try {
				DefaultListModel<Account> list = Server.getList();
				Document doc = (Document)in.readObject();
				Element root = doc.getDocumentElement();
				
				// Read MESSAGE
				switch(root.getNodeName()){
				case Header.LOG_IN:{  // Login Event
					String id = root.getElementsByTagName(Header.ID).item(0).getTextContent();
					String pass = root.getElementsByTagName("PASSWORD").item(0).getTextContent();
					Boolean exist = false;
					for(int i = 0; i < list.getSize(); i++){
						if (id.equals(list.getElementAt(i).getName())&&pass.equals(list.getElementAt(i).getPass())){
							exist = true;
							try {
								Document ans = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
								Element res = ans.createElement("RESPONSE");
								res.setTextContent("ACCEPT");
								ans.appendChild(res);
								ObjectOutputStream out = new ObjectOutputStream(os);
								out.writeObject(ans);
								System.out.println(Header.LOG_IN+":"+ id);
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
				
				case "REQUEST_LIST": {
					int idx = -1;
					for (int i = 0; i < list.size();  i++)
						if (root.getElementsByTagName(Header.ID).item(0).getTextContent().equals(list.getElementAt(i).getName())){
							idx = i;
							break;
						}
					list.getElementAt(idx).setOnline();
					list.getElementAt(idx).setInfo(root.getElementsByTagName(Header.IP).item(0).getTextContent(), Integer.parseInt(root.getElementsByTagName(Header.PORT).item(0).getTextContent())); 
					DefaultListModel<Account> list_req =  Server.getList();
					try {
						Document ansReq = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
						Element listFriend = ansReq.createElement("LIST_FRIEND");
						ansReq.appendChild(listFriend);
						for(int i = 0; i < list_req.getSize(); i++){
							if (list_req.getElementAt(i).getState()){
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
						ObjectOutputStream out = new ObjectOutputStream(os);
						out.writeObject(ansReq);
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
					for(int i = 0; i < list.getSize(); i++){
						if (id.equals(list.getElementAt(i).getName())){
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
				case "LOGOUT" : {
					String id = root.getElementsByTagName(Header.ID).item(0).getTextContent();
					for(int i = 0; i < list.size(); i++){
						if (id.equals(list.getElementAt(i).getName())) {
							list.getElementAt(i).setOffline();
							break;
						}
					}
					break;
				}
				} // end switch
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
	public static DefaultListModel<Account> accountList;
	public static void initList(){
		accountList = new DefaultListModel<Account>();
		accountList.addElement(new Account("abcd","1234"));
		accountList.addElement(new Account("efgh","1234"));
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
