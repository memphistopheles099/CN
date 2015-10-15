package server;

public class Account {
	private String name;
	private String password;
	private Boolean online;
	private String IP;
	private int port;
	public Account(String n, String p){
		name=n; password=p; online=false;
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
}