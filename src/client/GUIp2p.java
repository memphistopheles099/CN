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
import javax.swing.DropMode;
import javax.swing.JTextArea;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class GUIp2p {

	JFrame frame;
	private Boolean SendbyEnter;
	private String name;
	private String IP;
	private int port;
	
	GUIp2p(String n, String ip, int p){
		name = n;
		IP = ip;
		port = p;
		initialize();
	}
	
	/**
	 * Launch the application.
	 
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUIp2p window = new GUIp2p();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}*/

	/**
	 * Create the application.
	 */
	public GUIp2p() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 484, 358);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle(name);
		SendbyEnter = true;

		JTextArea textAreaMsgShow = new JTextArea();
		textAreaMsgShow.setEditable(false);
		
		JTextArea textAreaMsgType = new JTextArea();
		
		JButton btnSend = new JButton("G\u1EEDi");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				// Send XML message to peer
				
				// Send XML online state to server
				
				String message = textAreaMsgType.getText() ;
				textAreaMsgType.setText("");
				textAreaMsgShow.setText(textAreaMsgShow.getText()+"Me:"+message+"\n");
			}
		});
		
		JButton btnAttache = new JButton("\u0110\u00EDnh k\u00E8m t\u1EC7p");
		
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


class PeerListen extends Thread{
	JTextArea MsgShow;
	String message;
	public PeerListen(JTextArea msgCome){
		MsgShow = msgCome;
	}
	public void run(){
		/*
		 * Extract XML and get message
		 */
		MsgShow.setText(MsgShow.getText() + '\n' + message);
	}
}