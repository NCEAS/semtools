/**
 * 
 */
package org.ecoinformatics.sms.plugins.table;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellRenderer;

import org.ecoinformatics.sms.annotation.Observation;

/**
 * @author leinfelder
 * 
 */
public class ObservationCellRenderer extends JLabel implements
		TableCellRenderer {

	public static Map<Object, Color> colorMap = new HashMap<Object, Color>();
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax
	 * .swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		if (value instanceof Observation) {
			//look up the color for this object
			Color c = colorMap.get(value);
			if (c == null) {
				int red = (int) (Math.random( )*256);
				int green = (int)(Math.random( )*256);
				int blue = (int)(Math.random( )*256);
				Color randomColor = new Color(red, green, blue);
				c = randomColor;
				colorMap.put(value, c);
			}
			this.setColor1(c);
		}
		else {
			this.setColor1(Color.white);
		}
		setText((value == null) ? "" : value.toString());
		return this;
	}

	private Color color1;
	private Color color2;

	public ObservationCellRenderer() {
		this(Color.lightGray, Color.white);
	}

	public ObservationCellRenderer(Color c1, Color c2) {
		super();
		this.color1 = c1;
		this.color2 = c2;
	}

	public void setColor1(Color c1) {
		this.color1 = c1;
		repaint();
	}

	public void setColor2(Color c2) {
		this.color2 = c2;
		repaint();
	}

	// Overloaded in order to paint the background
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;

		int w = getWidth();
		int h = getHeight();

		GradientPaint gradient = new GradientPaint(w, 0, color1, w, h, color2,
				true);
		g2.setPaint(gradient);
		g2.fillRect(0, 0, w, h);
		
		super.paintComponent(g);
	}

	public static void main(String[] args) {

		final ObservationCellRenderer pGradient = new ObservationCellRenderer();

		Color[] colors = { createColor("Black", Color.black),
				createColor("Blue", Color.blue),
				createColor("Green", Color.green),
				createColor("yellow", Color.yellow),
				createColor("orange", Color.orange),
				createColor("red", Color.red),
				createColor("white", Color.white) };

		JComboBox c1 = new JComboBox(colors);
		c1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox combo = (JComboBox) e.getSource();
				Color c = (Color) combo.getSelectedItem();
				pGradient.setColor1(c);
			}
		});

		JComboBox c2 = new JComboBox(colors);
		c2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox combo = (JComboBox) e.getSource();
				Color c = (Color) combo.getSelectedItem();
				pGradient.setColor2(c);
			}
		});

		JPanel pColors = new JPanel(new GridLayout(0, 2));
		pColors.add(c1);
		pColors.add(c2);

		pGradient.add(new JButton("A button"));
		pGradient.add(new JTextField("A text field"));

		c1.setSelectedItem(colors[1]);
		c2.setSelectedItem(colors[2]);

		JFrame f = new JFrame("Gradient test");
		f.setSize(300, 200);
		f.getContentPane().add(pColors, BorderLayout.NORTH);
		f.getContentPane().add(pGradient, BorderLayout.CENTER);
		f.setVisible(true);
	}

	private static Color createColor(String name, Color c) {

		final String colorname = name;
		Color color = new Color(c.getRed(), c.getGreen(), c.getBlue()) {
			private String name = colorname;

			public String toString() {
				return name;
			}
		};
		return color;
	}

}
