/*
 * (swing1.1beta3)
 */
 
package jp.gr.java_conf.tame.swing.table;
 
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


/**
 * @version 1.0 11/22/98
 */
class TextPreviewLabel extends JLabel {
  private String sampleText = "  Sample Text  Sample Text  ";
  boolean isForgroundSelection;
  
  public TextPreviewLabel() {
    this(Color.black, Color.white, true);
  }
  
  public TextPreviewLabel(Color fore, Color back, boolean isForgroundSelection) {
    setOpaque(true);
    setForeground(fore);
    setBackground(back);
    this.isForgroundSelection = isForgroundSelection;
    setText(sampleText);
  }
  
  public void setForeground(Color col) {
    if (isForgroundSelection) {
      super.setForeground(col);
    } else {
      super.setBackground(col);
    }
  }
  
}


class ColorChooserDialog extends JDialog {
    private Color initialColor;
    private Color retColor;
    private JColorChooser chooserPane;

    public ColorChooserDialog(Component c, String title,
                          final    JColorChooser chooserPane) {
        super(JOptionPane.getFrameForComponent(c), title, true);
        setResizable(false);

        this.chooserPane = chooserPane;

	String okString = UIManager.getString("ColorChooser.okText");
	String cancelString = UIManager.getString("ColorChooser.cancelText");
	String resetString = UIManager.getString("ColorChooser.resetText");

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(chooserPane, BorderLayout.CENTER);

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton okButton = new JButton(okString);
	getRootPane().setDefaultButton(okButton);
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                retColor = chooserPane.getColor();
                setVisible(false);
            }
        });
        buttonPane.add(okButton);

        JButton cancelButton = new JButton(cancelString);
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                retColor = null;
                setVisible(false);
            }
        });
        buttonPane.add(cancelButton);

        JButton resetButton = new JButton(resetString);
        resetButton.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               chooserPane.setColor(initialColor);
           }
        });
        buttonPane.add(resetButton);
        contentPane.add(buttonPane, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(c);
        addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent e) {
            setVisible(false);
          }
        });
    }
    
  public Color getColor() {
    return retColor;
  }

}


public class TextColorChooser extends JColorChooser {
 
  public TextColorChooser(Color target, Color reference, boolean isForgroundSelection) {
    super(target);
    if (isForgroundSelection) {
      setPreviewPanel(new TextPreviewLabel(target, reference,isForgroundSelection));
    } else {
      setPreviewPanel(new TextPreviewLabel(reference,target, isForgroundSelection));
    }
    updateUI();
  }
  
  public Color showDialog(Component component, String title) {
    ColorChooserDialog dialog = new ColorChooserDialog(component, title, this);
    dialog.setVisible(true);
    Color col = dialog.getColor();
    dialog.dispose();
    return col;
  }
  
}

