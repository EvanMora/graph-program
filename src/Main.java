//dark falt 
import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
public class Main {
public static void main(String[] args) {
    try {
        UIManager.setLookAndFeel(new FlatDarkLaf());
        } 
        catch (Exception e) {
        System.out.println("No se pudo cargar FlatLaf");
        }
        JFrame frame = new JFrame("Prueba FlatLaf");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JButton button = new JButton("Hello world");
        frame.add(button);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
 }
