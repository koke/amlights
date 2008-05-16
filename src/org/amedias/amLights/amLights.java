package org.amedias.amLights;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.util.*;
import java.io.*;

public class amLights extends MIDlet implements CommandListener {
  private Command exitCommand,playCommand;
  private Display display;
  private SSCanvas screen;

  public amLights() {
    // Obtenemos el objeto Display del midlet.
    display = Display.getDisplay(this);

    //  Creamos el comando Salir.
    exitCommand = new Command("Salir",Command.SCREEN,2);
    playCommand = new Command("Jugar",Command.CANCEL,2);

    // Creamos la pantalla principal
    screen=new SSCanvas();

    // Añadimos el comando Salir e indicamos que clase lo manejará
    screen.addCommand(playCommand);
    screen.addCommand(exitCommand);
    screen.setCommandListener(this);
  } 

  public void startApp() throws MIDletStateChangeException {
    // Seleccionamos la pantalla a mostrar
    display.setCurrent(screen); 
  }

  public void pauseApp() {
  }

  public void destroyApp(boolean incondicional) {
  }

  public void commandAction(Command c, Displayable s) {
    // Salir
    if (c == exitCommand) {
      if (screen.isPlaying()) {
        screen.quitGame();
      } else {
        destroyApp(false);
        notifyDestroyed();
      }
    }

    if (c == playCommand && !screen.isPlaying()) {
      // Play!!!
      new Thread(screen).start();
    }
  }
}

class SSCanvas extends Canvas implements Runnable {
  // Board size: 5x5
  private static int bsize = 5;

  // Margin size (in px)
  private static int margin = 10;

  // Board size in pixels
  private int bwidth = 0;

  // Cell status array
  private boolean[] cellStatus = new boolean[bsize*bsize];

  // Cursor position
  private int position;

  // Cell width
  private int step;
  
  // Top margin
  private int top;

  // Left margin
  private int left;

  private boolean playing;

  public void SSCanvas() {
  }

  void quitGame() {
    playing = false;
  }

  boolean isPlaying() {
    return playing;
  }

  public void setPosition(int pos) {
    position = pos;
  }

  public int getPosition() {
    return position;
  }

  public int getPositionX() {
    return position % bsize;
  }

  public int getPositionY() {
    return position / bsize;
  }

  public boolean getStatus(int x, int y) {
    return cellStatus[y*bsize+x];
  }

  public void toggleCell(int x, int y) {
    cellStatus[y*bsize+x] = ! cellStatus[y*bsize+x];
  }

  public void togglePosition() {
    int x = getPositionX();
    int y = getPositionY();

    toggleCell(x, y);
    if (x > 0)
      toggleCell(x-1, y);
    if (y > 0)
      toggleCell(x, y-1);
    if (x < bsize-1)
      toggleCell(x+1, y);
    if (y < bsize-1)
      toggleCell(x, y+1);
  }

  public void start() {
    playing = true;
    
    if (getWidth() > getHeight()) {
      bwidth = (getHeight() - margin * 2);
    } else {
      bwidth = (getWidth() - margin * 2);
    }

    step = bwidth / bsize;
    top = (getHeight() - bwidth) / 2;
    left = (getWidth() - bwidth) / 2;

    Random generator = new Random();

    // Initialize cells with random data
    for (int i = 0; i < bsize*bsize; i++) {
      cellStatus[i] = (0 == (generator.nextInt() % 2));
    }

    // Initialize cursor position
    position = 0;
  }

  public void run() {
    start();

    while (playing) {
      repaint();
      serviceRepaints();
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        System.out.println(e.toString());
      }
    }

    // Repintamos la pantalla
    // para mostrar pantalla de presentación
    repaint();
    serviceRepaints();
  }

  public void paint(Graphics g) {
    if (playing) {
      // Paint the checker
      drawChecker(g);

      // And now, the cells
      for (int x = 0; x < bsize; x++) {
        for (int y = 0; y < bsize; y++) {
          drawCell(g,x,y);
        }
      }

      drawPosition(g);
    } else {
      try {
        Image img = Image.createImage("/amLights_splash.gif");
        g.drawImage (img, 0, 0, Graphics.TOP | Graphics.LEFT);
      } catch (java.io.IOException err) {
        // ignore the image loading failure the application can recover.
      }
    }
  }

  public void drawChecker(Graphics g) {
    g.setColor(0,0,0);
    g.fillRect (0, 0, getWidth(), getHeight());
    g.setColor(255,0,255);
    g.setColor(10,200,100);

    g.drawLine (left, top, left, top + 20);
    for (int i = 0; i <= bsize; i++) {
      g.drawLine(left + step * i, top, left + step * i, top + bwidth);
      g.drawLine(left, top + step * i, left + bwidth, top + step * i);
    }
  }

  public void drawCell(Graphics g, int x, int y) {
    if (getStatus(x,y)) {
      g.setColor(0,255,255);
    } else {
      g.setColor(255,255,255);
    }

    g.fillRect(
        left + x * step + 1,
        top + y * step + 1,
        step - 2,
        step - 2
        );
  }

  public void drawPosition(Graphics g) {
    int x = getPositionX();
    int y = getPositionY();

    g.setColor(255,255,0);

    g.drawRect(
        left + x * step + 1,
        top + y * step + 1,
        step - 2,
        step - 2
        );
  }

  public void keyPressed(int keyCode) {
    int action=getGameAction(keyCode);

    switch (action) {
      case FIRE:
        togglePosition();
        break;
      case LEFT:
        move(-1,0);
        break;
      case RIGHT:
        move(1,0);
        break;
      case UP:
        move(0,-1);
        break;
      case DOWN:
        move(0,1);
        break;
    }
  }

  public void move(int deltaX, int deltaY) {
    int x = getPositionX();
    int y = getPositionY();

    int tempX, tempY;

    tempX = x + deltaX;
    tempY = y + deltaY;

    if ((tempX < 0) || (tempX >= bsize))
      tempX = x;

    if ((tempY < 0) || (tempY >= bsize))
      tempY = y;

    position = tempY * bsize + tempX;
  }
}
