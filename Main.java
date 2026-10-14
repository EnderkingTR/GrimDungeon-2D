import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        JFrame window = new JFrame("GrimDungeon 2D - Procedural Rogue-Engine");
        Engine engine = new Engine();

        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.add(engine);
        window.pack(); // Pencereyi Canvas boyutuna (960x640) tam oturtur
        window.setLocationRelativeTo(null); // Ekranın ortasında açar
        window.setVisible(true);

        engine.start();
    }
}