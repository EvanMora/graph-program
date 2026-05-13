//swing 
import javax.swing.*;
//utilidades
import javax.swing.SwingUtilities;
//flat
import com.formdev.flatlaf.FlatDarkLaf;
//main
public class Main {
    public static void main(String[] args){
        //setup de flat 
    FlatDarkLaf.setup();
        //try catch 
    try{
        //manager
    UIManager.setLookAndFeel(new FlatDarkLaf());
    }
        //stacktracker para ver el error si falla 
    catch (Exception e) {
    e.printStackTrace(); 
    }   
        //invoca mas tarde la ventana principal 
    // SwingUtilities.invokeLater(VentanaPrincipal::new);
    } 
}
