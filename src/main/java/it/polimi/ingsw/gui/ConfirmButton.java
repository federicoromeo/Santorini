package it.polimi.ingsw.gui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Default button used to confirm choices
 */
public class ConfirmButton extends JButton {

    public ConfirmButton(String text) {
        super(text);
        setBorderPainted(false);
        setContentAreaFilled(false);
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        try {
            g.drawImage(new ImageIcon(ImageIO.read(getClass().getResource(Pics.BUTTON.getPath()))).getImage(),0,0,this.getWidth(),20,this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
