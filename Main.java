import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        JFrame window = new JFrame("GrimDungeon 2D - Procedural Rogue-Engine");
        Engine engine = new Engine();

        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.add(engine);
        window.pack(); // Fits window perfectly to canvas dimension (960x640)
        window.setLocationRelativeTo(null); // Center window on screen
        window.setVisible(true);

        engine.start();
    }
}