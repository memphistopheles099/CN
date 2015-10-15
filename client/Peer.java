package client;

public class Peer {
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
