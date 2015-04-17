// Assume everything is in the root package.

import javax.swing.JPanel;
import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

public class FfTerminalRuntime extends JPanel {

	public static final int PIXEL_WDITH = 800;
	public static final int PIXEL_HEIGHT = 600;

	public static final int FONT_WIDTH = 10;
	public static final int FONT_HEIGHT = 20; // ideally, width/height ~ 3/5

	public static final int NUMBER_OF_COLUMNS = 80;
	public static final int NUMBER_OF_ROWS = 30;

	public static void main(String[] args) {
		new FfTerminalRuntime().run(FfCompiler.parse(System.in));
	}

	private Font font;
	private char[][] buffer;

	public void run(FfRuntime.Dict code) {
		JFrame frame = new JFrame("Hello World Swing");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.getContentPane().add(this);

		setPreferredSize(new Dimension(800, 600));

		buffer = new char[NUMBER_OF_COLUMNS][NUMBER_OF_ROWS];
		for (int x = 0; x < NUMBER_OF_COLUMNS; x++)
			for (int y = 0; y < NUMBER_OF_ROWS; y++)
				buffer[x][y] = ' ';

		FfRuntime.Scope scope = FfRuntime.declareBuiltins(new FfRuntime.GlobalScope());
		FfRuntime.eval(scope, code);

		font = new Font("Monospaced", Font.PLAIN, FONT_HEIGHT);

		frame.pack();
		frame.setVisible(true);
	}

	public void paintComponent(Graphics g) {
		g.setFont(font);
		for (int x = 0; x < NUMBER_OF_COLUMNS; x++)
			for (int y = 0; y < NUMBER_OF_ROWS; y++)
				printChar(g, buffer[x][y], x, y);
	}

	public static void printChar(Graphics g, char c, int x, int y) {
		g.drawString(Character.toString(c), x * FONT_WIDTH, (y+1) * FONT_HEIGHT);
	}

}
