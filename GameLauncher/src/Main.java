import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }

        JFrame frame = new JFrame("Launcher");
        frame.setLocationRelativeTo(null);
        frame.setSize(800, 600);
        frame.setLayout(null);
        frame.setVisible(true);

        int startBtnWidth = 200;
        JButton start = new JButton("Start");
        start.setBounds(frame.getWidth() / 2 - startBtnWidth / 2, frame.getHeight() - startBtnWidth / 3 * 2, startBtnWidth, startBtnWidth / 3);
        frame.getContentPane().add(start);
    }

}