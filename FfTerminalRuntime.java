// Assume everything is in the root package.

import javax.swing.Timer;
import javax.swing.JComponent;
import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class FfTerminalRuntime extends JComponent implements KeyListener {

	public static void main(String[] args) {
		new FfTerminalRuntime().run(FfCompiler.parse(System.in));
	}


	private static class Square {
		public char character = ' ';
		public Color foreground = Color.WHITE, background = Color.BLACK;
	}

	private static Color listToColor(FfRuntime.List list) {
		float r = Float.parseFloat((String) list.get(0));
		float g = Float.parseFloat((String) list.get(1));
		float b = Float.parseFloat((String) list.get(2));
		return new Color(r, g, b);
	}

	private static FfRuntime.List colorToList(Color color) {
		return new FfRuntime.List(color.getRed() / 255.0, color.getGreen() / 255.0, color.getBlue() / 255.0);
	}

	// Originally I had planned on graphics just being 800 x 600.
	// But I think it should actually scale to available size.
	// public static final int PIXEL_WDITH = 800;
	// public static final int PIXEL_HEIGHT = 600;

	public static final int NUMBER_OF_COLUMNS = 80;
	public static final int NUMBER_OF_ROWS = 30;

	private Square[][] squares = new Square[NUMBER_OF_COLUMNS][NUMBER_OF_ROWS];

	private FfRuntime.Function keyPressListener, keyReleaseListener, keyTypedListener;

	public int getFontWidth() { return getWidth() / NUMBER_OF_COLUMNS; } // 10;
	public int getFontHeight() { return getHeight() / NUMBER_OF_ROWS; } // ideally, width/height ~ 3/5
	public int getFontSize() { return (int) (((double) getFontWidth()) * (5.0 / 3.0)); }

	public Font getFont() { return new Font("Monospaced", Font.PLAIN, getFontHeight()); }

	public Object run(Object ast) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 600);
		frame.getContentPane().add(this);
		frame.setVisible(true);
		setFocusable(true);
		setSize(800, 600);

		for (int x = 0; x < NUMBER_OF_COLUMNS; x++)
			for (int y = 0; y < NUMBER_OF_ROWS; y++)
				squares[x][y] = new Square();


		FfRuntime.Scope scope = FfRuntime.declareBuiltins(new FfRuntime.GlobalScope());
		FfRuntime.Dict world = new FfRuntime.Dict();

		addKeyListener(this);

		world.putBuiltin(new FfRuntime.Builtin() {

			public String getName() {
				return "set";
			}

			public Object call(FfRuntime.List args) {
				int x = Integer.parseInt((String) args.get(0));
				int y = Integer.parseInt((String) args.get(1));
				squares[x][y].character = ((String) args.get(2)).charAt(0);
				squares[x][y].foreground = listToColor((FfRuntime.List) args.get(3));
				squares[x][y].background = listToColor((FfRuntime.List) args.get(4));
				repaint();
				return args.get(2);
			}

		});

		world.putBuiltin(new FfRuntime.Builtin() {

			public String getName() {
				return "get";
			}

			public Object call(FfRuntime.List args) {
				int x = Integer.parseInt((String) args.get(0));
				int y = Integer.parseInt((String) args.get(1));
				Square square = squares[x][y];
				return new FfRuntime.List(
						Character.toString(square.character),
						colorToList(square.foreground),
						colorToList(square.background));
			}

		});

		world.putBuiltin(new FfRuntime.Builtin() {

			public String getName() {
				return "onKeyPress";
			}

			public Object call(FfRuntime.List args) {
				keyPressListener = (FfRuntime.Function) args.get(0);
				return args.get(0);
			}

		});

		world.putBuiltin(new FfRuntime.Builtin() {

			public String getName() {
				return "onKeyRelease";
			}

			public Object call(FfRuntime.List args) {
				keyReleaseListener = (FfRuntime.Function) args.get(0);
				return args.get(0);
			}

		});

		world.putBuiltin(new FfRuntime.Builtin() {

			public String getName() {
				return "onKeyTyped";
			}

			public Object call(FfRuntime.List args) {
				keyTypedListener = (FfRuntime.Function) args.get(0);
				return args.get(0);
			}

		});

		world.putBuiltin(new FfRuntime.Builtin() {

			public String getName() {
				return "onTimer";
			}

			public Object call(FfRuntime.List args) {
				final FfRuntime.Function callback = (FfRuntime.Function) args.get(1);
				Timer timer = new Timer(
						Integer.parseInt((String) args.get(0)),
						new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								callback.call(new FfRuntime.List());
							}
						}
				);
				timer.setRepeats(false);
				timer.start();
				return callback;
			}

		});

		scope.declare("world", world);
		return FfRuntime.eval(scope, ast);
	}

	public void paintComponent(Graphics g) {
		g.setFont(getFont());

		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getWidth(), getHeight());

		int w = getFontWidth(), h = getFontHeight();
		for (int x = 0; x < NUMBER_OF_COLUMNS; x++) {
			for (int y = 0; y < NUMBER_OF_ROWS; y++) {
				if (squares[x][y] == null)
					squares[x][y] = new Square();

				int px = x * w, py = y * h;
				Square square = squares[x][y];

				g.setColor(square.background);
				g.fillRect(px, py, w, h);

				g.setColor(square.foreground);
				g.drawString(Character.toString(square.character), px, py + h);
			}
		}
	}

	public void keyPressed(KeyEvent e) {
		if (keyPressListener != null)
			keyPressListener.call(new FfRuntime.List());
	}

	public void keyReleased(KeyEvent e) {
		if (keyReleaseListener != null)
			keyReleaseListener.call(new FfRuntime.List());
	}

	public void keyTyped(KeyEvent e) {
		if (keyTypedListener != null)
			keyTypedListener.call(new FfRuntime.List(Character.toString(e.getKeyChar())));
	}
}

