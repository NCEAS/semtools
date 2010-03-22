/**
 * 
 */
package org.ecoinformatics.sms.plugins.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * @author leinfelder
 * 
 */
public class OntologyCellRenderer extends JLabel implements
		TableCellRenderer {

	public static Map<Object, Color> colorMap = 
		Collections.synchronizedMap(new HashMap<Object, Color>());
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax
	 * .swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		
		
		if (value != null) {
			//look up the color for this Observation
			Color c = colorMap.get(value);
			if (c == null) {
				int red = (int) (Math.random( )*256);
				int green = (int)(Math.random( )*256);
				int blue = (int)(Math.random( )*256);
				int alpha = 100;
				Color randomColor = new Color(red, green, blue, alpha);
				c = randomColor;
				colorMap.put(value, c);
			}
			this.setColor1(c);
		}
		else {
			this.setColor1(Color.white);
		}
		
		if (isSelected) {
			setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		}
		setToolTipText((value == null) ? "" : value.toString());
		setText((value == null) ? "" : value.toString());
		return this;
	}

	private Color color1;
	private Color color2;

	public OntologyCellRenderer() {
		this(Color.lightGray, Color.white);
	}

	public OntologyCellRenderer(Color c1, Color c2) {
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

		//GradientPaint gradient = new GradientPaint(0, 0, color1, w, h, color2, true);
		GradientPaint gradient = new GradientPaint(w, 0, color1, w, h, color2, true);

		g2.setPaint(gradient);
		g2.fillRect(0, 0, w, h);
		
		super.paintComponent(g);
	}
}
