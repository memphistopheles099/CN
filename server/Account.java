package server;

import javax.swing.DefaultListModel;

public class Account {
	private String name;
	private String password;
	private Boolean online;
	private String IP;
	private int port;
	private DefaultListModel<String> friendList;
	public Account(String n, String p){
		name=n; password=p; online=false;
		friendList = new DefaultListModel<String>();
	}
	public Account(String n, String p, DefaultListModel<String> list){
		name=n; password=p; online=false; friendList = list;
	}
	public void setInfo(String ip, int p){
		IP = ip;
		port = p;
	}
	public String getIP(){
		return IP;
	}
	public int getPort(){
		return port;
	}
	public String getName(){
		return name;
	}
	public String getPass(){
		return password;
	}
	public Boolean getState(){
		return online;
	}
	public void setOffline(){
		online=false;
	}
	public void setOnline(){
		online=true;
	}
	public String toString(){
		return name+"<"+"online"+">";
	}
	public boolean isFriend(String name){
		for (int i =0; i<friendList.size();i++)
			if (friendList.getElementAt(i).equals(name)) return true;
		return false;
	}
	public void addFriend(String name){
		friendList.addElement(name);
	}
}