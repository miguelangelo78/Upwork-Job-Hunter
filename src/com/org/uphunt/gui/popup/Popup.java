package com.org.uphunt.gui.popup;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JWindow;

public class Popup {
	private JWindow pop_window;
	
	private final int POP_WIDTH = 250;
	private final int POP_HEIGHT = 80;
	
	public Popup(){}
	
	public void show(String title, String msg){
		pop_window = new JWindow();
		pop_window.setSize(POP_WIDTH, POP_HEIGHT);
		pop_window.setLayout(new GridBagLayout());
		pop_window.setAlwaysOnTop(true);
		pop_window.setType(javax.swing.JFrame.Type.POPUP);
		
		Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();
		Insets toolHeight = Toolkit.getDefaultToolkit().getScreenInsets(pop_window.getGraphicsConfiguration());
		pop_window.setLocation(scrSize.width - pop_window.getWidth(), scrSize.height - toolHeight.bottom - pop_window.getHeight());
		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1.f;
		constraints.weighty = 1.f;
		constraints.insets = new Insets(5,5,5,5);
		constraints.fill = GridBagConstraints.BOTH;
		
		JLabel headingLabel = new JLabel(title);
		headingLabel.setOpaque(false);
		
		pop_window.add(headingLabel, constraints);
		
		constraints.gridx++;
		constraints.weightx = 0f;
		constraints.weighty = 0f;
		constraints.fill = GridBagConstraints.NONE;
		constraints.anchor = GridBagConstraints.NORTH;
	
		JButton closeBtn = new JButton(new AbstractAction("x") {
			private static final long serialVersionUID = -3544746641935009971L;

			public void actionPerformed(ActionEvent e) {
				pop_window.dispose();
			}
		});
		closeBtn.setMargin(new Insets(1,4,1,4));
		closeBtn.setFocusable(true);
		
		pop_window.add(closeBtn, constraints);
		
		constraints.gridx = 0;
		constraints.gridy++;
		constraints.weightx = 1.0f;
		constraints.weighty = 1.0f;
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.fill = GridBagConstraints.BOTH;
		
		JLabel messageLabel = new JLabel("<HtMl>"+msg);
		
		pop_window.add(messageLabel, constraints);
		pop_window.setVisible(true);
		
		new Thread(){
			public void run(){
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				pop_window.dispose();
			}
		}.start();
	}
}
