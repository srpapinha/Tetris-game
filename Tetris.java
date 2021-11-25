import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Random;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;


public class Tetris extends JPanel {
    //T O L I S
    private final int[][] blockFormsX = {{-1, 0, 0, 1}, {0, 1, 0, 1}, {0, 0, 0, 1}, {0, 0, 0, 0}, {0, 0, 1, 1}};
    private final int[][] blockFormsY = {{0, 0, -1, 0}, {0, 1, 1, 0}, {-2, -1, 0, 0}, {-1, 0, 1, 2}, {1, 0, 0, -1}};

    private boolean isRunning;
    private int width;
    private int height;

    private int blockPerWidth = 12;
    private int blockPerHeight = 16;

    private int blockX;
    private int blockY;

    private int blockSizeX = width / blockPerWidth;
    private int blockSizeY = height / blockPerHeight;

    private int[][] blocks = new int[blockPerHeight][blockPerWidth];

    private Random random = new Random();

    private int formIndex;

    private int[] blockFormX;
    private int[] blockFormY;

    private int points;

    private long second;

    private int frametime = 1000 / 60;

    public Tetris(int width, int height) {
        this.width = width;
        this.height = height;

        blockSizeX = (width / blockPerWidth);
        blockSizeY = (height / blockPerHeight);

        resetBlock();

        setPreferredSize(new Dimension(width, height));
        setFocusable(true);
        addKeyListener(new Key());
    }

    private void update() {
        while(checkLine() != -1) {
            eraseLine(checkLine());
        }
        if(second % (1000 / 2) < frametime) {
            gravity();
        }
        
    }

    public void run() {
        isRunning = true;
        while(true) {
            update();
            repaint();
            try {
                Thread.sleep(frametime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            second += frametime;
            second %= 1000;
        }
    }

    private void reset() {
        blocks = new int[blockPerHeight][blockPerWidth];
        isRunning = true;
        points = 0;
        resetBlock();
    }

    private void eraseLine(int line) {
        if(line == -1) return;
        for(int i = line; i > 0; i--) {
            for(int j = 1; j < blocks[0].length - 1; j++) {
                blocks[i][j] = blocks[i - 1][j];
            }
        }
        points++;
    }

    private int checkLine() {
        boolean isCompleted = false;
        for(int i = 1; i < blocks.length - 1; i++) {
            for(int j = 1; j < blocks[0].length - 1; j++) {
                if(blocks[i][j] == 1) {
                    isCompleted = true;
                } else {
                    isCompleted = false;
                    break;
                }
            }
            if(isCompleted) return i;
        }
        return -1;
    }

    private boolean colision(int x, int y) {
        for(int i = 0; i < 4; i++) {
            if(blocks[y + blockFormY[i]][x + blockFormX[i]] == 1) {
                return true;
            }

            if(y + blockFormY[i] >= blockPerHeight - 1) {
                return true;
            } 

            if(x + blockFormX[i] < 1 || x + blockFormX[i] >= blockPerWidth - 1) {
                return true;
            }
        }
        return false;
    }

    private void gravity() {
        if(colision(blockX, blockY + 1)) {
            storeBlocks();
            resetBlock();
        } else {
            blockY += 1;
        }
    }

    private void rotate() {
        if(formIndex == 1) return;

        int[] copyBlockFormX = blockFormX.clone(),
        copyBlockFormY = blockFormY.clone();

        for (int i = 0; i < 4; i++) {
            int temp = blockFormX[i];
            blockFormX[i] = blockFormY[i] * -1;
            blockFormY[i] = temp;
        }

        if(colision(blockX, blockY)) {
            blockFormX = copyBlockFormX;
            blockFormY = copyBlockFormY;
        }
    }

    private void moveBlock(int x, int y) {
        if(!colision(x, y)) {
            blockX = x;
            blockY = y;
        }
        repaint();
    }

    private void resetBlock() {
        blockX = blockPerWidth / 2;
        blockY = 1;

        formIndex = random.nextInt(5);

        blockFormX = blockFormsX[formIndex].clone();
        blockFormY = blockFormsY[formIndex].clone();
    }

    private void storeBlocks() {
        if(blockY <= 1) {
            isRunning = false;
            return;
        }

        for(int i = 0; i < 4; i++) {
            blocks[blockFormY[i] + blockY][blockFormX[i] + blockX] = 1;
        }
    }

    private void paintGameOver(Graphics g) {
        g.setColor(Color.white);
        g.setFont(new Font(Font.SERIF, Font.BOLD, 30));
        g.drawString("Game Over", width / 2 - g.getFontMetrics().stringWidth("Game Over") / 2,  height / 2);
        g.drawString("Points: " + points, width / 2 - g.getFontMetrics().stringWidth("Points: x") / 2,  height / 2 + height / 10);
        g.drawString("Press SPACE to restart", width / 2 - g.getFontMetrics().stringWidth("Press SPACE to restart") / 2, height - height / 4);
    }

    private void paintWalls(Graphics g) {
        g.setColor(Color.GREEN);
        for(int i = 0; i < blockPerHeight; i++) {
            for(int j = 0; j < blockPerWidth; j++) {
                if(i == 0 || i == blockPerHeight - 1 || j == 0 || j == blockPerWidth - 1) {
                    g.fillRect(blockSizeX * j, blockSizeY * i, blockSizeX - 1, blockSizeY - 1);
                }
            }
        }
    }

    private void paintBlock(Graphics g) {
        g.setColor(Color.red);
        for(int i = 0; i < 4; i++) {
            g.fillRect(blockSizeX * (blockFormX[i] + blockX), blockSizeY * (blockFormY[i] + blockY), blockSizeX - 1, blockSizeY - 1);
        }
    }

    private void paintBlocks(Graphics g) {
        g.setColor(Color.blue);
        for(int i = 0; i < blockPerHeight; i++) {
            for(int j = 0; j < blockPerWidth; j++) {
                if(blocks[i][j] == 1) {
                    g.fillRect(blockSizeX * j, blockSizeY * i, blockSizeX - 1, blockSizeY - 1);
                }
            }
        }
    }

    private void paintBackground(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintBackground(g);
        
        if(isRunning) {
            paintBlocks(g);
            paintBlock(g);
            paintWalls(g);
        } else {
            paintGameOver(g);
        }
    }

    public static void main(String[] args) {
        JFrame window = new JFrame();
        Tetris tetris = new Tetris(600, 800);


        window.add(tetris);
        window.pack();
        window.setVisible(true);
        tetris.run();
    }

    private class Key implements KeyListener {

        @Override
        public void keyTyped(KeyEvent e) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void keyPressed(KeyEvent e) {
            switch(e.getKeyChar()) {
                case 'a':
                    moveBlock(blockX - 1, blockY);
                    break;

                case 'd':
                    moveBlock(blockX + 1, blockY);
                    break;

                case 's':
                    gravity();
                    break;

                case 'w':
                    rotate();
                    break;

                case ' ':
                    reset();
                    break;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {

        }

    }
}