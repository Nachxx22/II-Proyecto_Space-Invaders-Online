package Objects;

import java.awt.Graphics2D;

public class ClassG extends  InvaderLine implements  Drawable{
    public ClassG(int posX, int posY, int speed, int size, int lvl) {
        super(posX, posY, speed, size, lvl);
        int counter = 0;
       // this.setEn(new ArbolAvl());

        while(counter < size) {
            String sprite = "Invader_1";

            Invader enemy = new Invader(posX, posY, 50, 50, lvl, speed, sprite);
            this.getEn().agregar(enemy);
            counter++;
        }

        this.setLineClass("ArbolAVL");

    }

    /*
    @Override
    public void draw(Graphics2D g) {
        while (this.getEn().tamaño()>0) {
            this.getEn().contiene(2);
        }

    }
     */
    public void draw(Graphics2D g) {
        for (int c = 0; c < this.getEn().tamaño(); c++) {
           // this.getEn().getTree(c).draw(g);
        }
    }

    @Override
    public void update(double delta) {

    }
}
