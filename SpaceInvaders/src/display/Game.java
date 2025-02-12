package display;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.*;

import Objects.*;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import Objects.Mouse;

/***
 *Class Game: Extiende a superstatemachine e implementa keylistener y mouselistener, Aqui se define todos los objetos.
 */
public class Game extends SuperStateMachine implements KeyListener, MouseListener {
	private int userID;
	private Socket socket;

	private ReadFromServer rfsRun;
	private	WriteToServer wtsRun;

	private Player player;
	private Player player2;

	private Level level;
	private int levelCounter;
	private int score;
	private BufferedImage bg;
	private Font GMFont = new Font("Impact", Font.PLAIN, 28);
	private Mouse pointer;
	private int setA=0;
	public boolean playM= true;


	/**
	 *metodo Game: Crea un jugador de ciertas dimensiones, llama al sprite y toma en cuenta el nivel, puntuaje, el puntero del mouse.
	 * @param stateMachine
	 */
	public Game(StateMachine stateMachine) {
		super(stateMachine);
		this.userID = userID;

		if (userID == 1){
			player =  new Player(280*3/2-25, 360/16*9*3-55, 60, 60, "Spaceship_1");
			player2 = new Player(280*3/2+25, 360/16*9*3-55, 60, 60, "Spaceship_1");
		} else{
			player = new Player(280*3/2+25, 360/16*9*3-55, 60, 60, "Spaceship_1");
			player2 = new Player(280*3/2-25, 360/16*9*3-55, 60, 60, "Spaceship_1");
		}

		level = new Level(1);
		levelCounter = 1;
		score = 0;
		pointer = new Mouse(stateMachine.getCanva());


		try {
			URL url = this.getClass().getResource("/Sprites/Background.png");
			bg = ImageIO.read(url);
		} catch(IOException e) {e.printStackTrace();}
	}


	/**
	 *Main del proyecto, Aqui se establece el Javaframe
	 * @param args
	 */
	public static void main(String args[]) {
		Display display = new Display();
		display.connectToServer();
		JFrame frame = new JFrame();
		frame.add(display);
		frame.pack();

		frame.setTitle("Space Invaders: Player");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setVisible(true);

		display.setFrame(frame);
		display.start();
	}

	@Override
	public void connectToServer(){
		try{
			socket = new Socket("localhost",1331);
			DataInputStream dis = new DataInputStream(socket.getInputStream());
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

			userID = dis.readInt();

			System.out.println("joined as player "+ userID);

			if (userID == 1){
				System.out.println("waiting for partner...");
			}

			rfsRun = new ReadFromServer(dis);
			wtsRun = new WriteToServer(dos);

			rfsRun.waitForStart();
		}catch (IOException e){
			e.printStackTrace();
		}
	}

	private class ReadFromServer implements Runnable{

		private DataInputStream dataIn;

		public ReadFromServer (DataInputStream in){
			dataIn = in;
			System.out.println("RFS created");
		}

		public void waitForStart(){
			try{
				String start = dataIn.readUTF();
				System.out.println("Message from server: "+start);

				Thread readThread = new Thread(rfsRun);
				Thread writeThread = new Thread(wtsRun);
				readThread.start();
				writeThread.start();
			}catch(IOException e){
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			try{
				while(true){
					if (player2 != null){
						player2.setPosX(dataIn.readInt());
						player2.setPosY(dataIn.readInt());
					}
				}
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}

	private class WriteToServer implements Runnable{

		private DataOutputStream dataOut;

		public WriteToServer (DataOutputStream out){
			dataOut = out;
			System.out.println("WTS created");
		}

		@Override
		public void run() {
			try{
				while (true){
					if (player != null){



						dataOut.writeInt(player.getPosX());
						dataOut.writeInt(player.getPosY());
						dataOut.flush();
					}
					Thread.sleep(25);
				}
			} catch(IOException | InterruptedException e){
				e.printStackTrace();
			}
		}
	}

	/**
	 *Metodo reset: Reestablece el jugador a su lugar original y contador nivel, tambien el score en 0.
	 */
	public void reset() {
		level = new Level(1);
		player =  new Player(280*3/2-25, 360/16*9*3-55, 60, 60, "Spaceship_1");
		levelCounter = 1;
		score = 0;
	}
	/**
	 *Metodo gameOver: Para la maquina de estado, y crea una pantalla de gameover con el score obtenido on diferente musica.
	 * @param g
	 */
	public void gameOver(Graphics2D g) {
		this.getStateMachine().StopM();
		g.setColor(Color.black);
		g.fillRect(0, 0, 350*3+10, 200*3+10);
		BufferedImage go = null;
		try {
			URL url = this.getClass().getResource("/Sprites/GameOver.png");
			go = ImageIO.read(url);
		} catch(IOException e) {e.printStackTrace();}
		g.drawImage(go, (280*3+10)/2-300, 50, go.getWidth(), go.getHeight(), null);
		g.setFont(GMFont);
		g.setColor(Color.white);
		g.drawString(Integer.toString(score), (280*3+10)/2-g.getFontMetrics().stringWidth(Integer.toString(score))/2, 500);
		if (setA==0){
			++setA;
			Audio loss = new Audio("SpaceInvaders/Tracks/GameOver.wav");

		}

	}
	/**
	 *Metodo showInfo: Demuestra la informacion del jugador en la pantalla de juego, cantidad de vida restante y la linea de clase que esta.
	 * @param g
	 */
	public void showInfo(Graphics2D g) {
		g.setFont(GMFont);
		g.setColor(Color.white);
		g.drawString(level.getCurrent().getLineClass(), 5, 40);
		g.drawString(Integer.toString(score), 230*3+30, 80);

		BufferedImage heart = null;
		try {
			URL url = this.getClass().getResource("/Sprites/Health.png");
			heart = ImageIO.read(url);
		} catch(IOException e) {e.printStackTrace();}
		for(int i = 0; i < player.getLifes(); i++) {
			g.drawImage(heart, 220*3+30+25*i, 20, 20, 20, null);
		}
	}

	/**
	 *Metodo Getplayer
	 * @return player
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 *Metodo getlevel
	 * @return level
	 */
	public Level getLevel() {
		return level;
	}

	/**
	 *Metodo draw: cuando hay gameover se demuestra info de player y level.
	 * @param g
	 */
	@Override
	public void draw(Graphics2D g) {

			if(!this.player.isAlive() | !this.player2.isAlive()) {
				this.gameOver(g);

				return;
			}

			g.drawImage(bg, 0, 0, 280*3+10, 200*3+10, null);
			g.setColor(Color.gray);
			g.fillRect(280*3+10, 0, 350*3-280*3, 200*3+10);
			this.showInfo(g);
			player.draw(g);
			player2.draw(g);
			level.draw(g);
		}


	/**
	 *Metodo update: Actualiza la pantalla(Destruir enemigos, Destruir balas cuando tocan fuera de pantalla, Obtener colisiones entre jugador y enemigos).
	 * @param delta
	 */
	@Override
	public void update(double delta) {
		if(!player.isAlive()) {
			return;
		}
		if(level.getSpeed() == 4) {
			++levelCounter;
			level = new Level(levelCounter);
		}
		// Destruir enemigos
		for(int i = 0; i < level.getCurrent().getEnemies().size(); i++)  {
			int b = 0;
			if(level.getCurrent().getEnemies().get(i).getPosY() >= 600 - level.getCurrent().getEnemies().get(i).getHeight()) {

				while(player.isAlive()) {
					player.loseLife();

				}

				return;
			}
			while(b < player.getBullets().size()) {
				if(player.getBullets().get(b).isColliding(level.getCurrent().getEnemies().get(i))) {
					player.getBullets().remove(b);
					if(level.getCurrent().getEnemies().get(i).destroy()) {
						Audio exp = new Audio("SpaceInvaders/Tracks/explosion.wav");
						if(level.getCurrent().getEnemies().get(i).isBoss()) {
							this.score += 500;
						} else {
							this.score += 100;

						}
						level.getCurrent().getEnemies().remove(i);
						if(level.getCurrent().getEnemies().size() == 0) {
							level.nextLine();
							level.update(delta);
						}
						--i;
					}
				} else {
					b++;
				}
			}
		}
		level.getCurrent().arrangeLine();



		// Destruir balas cuando tocan fuera de pantalla
		for(int i = 0; i < player.getBullets().size(); i++) {
			if(player.getBullets().get(i).getPosY() < -50) {
				player.getBullets().remove(i);
				--i;
			}
		}



		// Obtener colisiones entre jugador y enemigos
		for(int i = 0; i < level.getCurrent().getEnemies().size(); i++) {
			if(level.getCurrent().getEnemies().get(i).isColliding(player)) {
				if(level.getCurrent().getEnemies().get(i).isBoss()) {
					this.score += 250;
				} else {
					this.score += 50;
				}
				level.getCurrent().getEnemies().remove(i);
				if(level.getCurrent().getEnemies().size() == 0) {
					level.nextLine();
					level.update(delta);
				}
				player.loseLife();
			}
		}
		// Update

		pointer.updatePosition();
		player.SetPosX(pointer.getMouseX());
		player.update(delta);
		level.update(delta);
	}

	/**
	 *Metodo init(Crea canvas con mouselistener y keylistener).
	 * @param canvas
	 */
	@Override
	public void init(Canvas canvas) {
		canvas.addMouseListener(player);
		canvas.addMouseListener(this);
		canvas.addKeyListener(this);
	}

	/**
	 * Metodo keyPressed: Obtiene cuando se toca la tecla escape, lo cual reinicia el juego
	 * @param e
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		if(key == KeyEvent.VK_ESCAPE) {
			this.reset();
			this.getStateMachine().setState((byte) 0);
		}
	}


	@Override
	public void keyReleased(KeyEvent e) {}

	@Override
	public void keyTyped(KeyEvent e) {}


	@Override
	public void mouseClicked(MouseEvent e) {
	}


	@Override
	public void mousePressed(MouseEvent e) {

	}

	@Override
	public void mouseReleased(MouseEvent e) {

	}


	@Override
	public void mouseEntered(MouseEvent e) {
	}


	@Override
	public void mouseExited(MouseEvent e) {
	}

}
