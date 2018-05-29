package miner;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import javafx.scene.text.Text;

public class Hexagon extends StackPane {

    private boolean isOpen = false;
    private boolean hasBomb;
    private Text countBombsAround = new Text(" ");
    private Polyline hexagon;
    private Point2D origin;
    private Image image;
    private ImageView statusImage;
    public boolean check = false;
    private long bombAround;

    public Hexagon(Point2D center, int size) {


        origin =center;
//основной элемент для создания шестиугольников
        hexagon = new Polyline(
                calc_hexagon(center, size, 0).getX(), calc_hexagon(center, size, 0).getY(),
                calc_hexagon(center, size, 1).getX(), calc_hexagon(center, size, 1).getY(),
                calc_hexagon(center, size, 2).getX(), calc_hexagon(center, size, 2).getY(),
                calc_hexagon(center, size, 3).getX(), calc_hexagon(center, size, 3).getY(),
                calc_hexagon(center, size, 4).getX(), calc_hexagon(center, size, 4).getY(),
                calc_hexagon(center, size, 5).getX(), calc_hexagon(center, size, 5).getY(),
                calc_hexagon(center, size, 0).getX(), calc_hexagon(center, size, 0).getY()
        );
        hexagon.setStroke(Color.RED);//указываем каким цветом отображать границы ячейки
        hexagon.setFill(Color.LIGHTGRAY);//указываем каким цветом заливать ячейку
        getChildren().add(hexagon);//добавляем ячейку
        setTranslateX(center.getX());//по указанным координатам
        setTranslateY(center.getY());
        getChildren().add(countBombsAround);//добавляем под ячейку количество рядом находящихся бомб

        countBombsAround.setVisible(false);//скрываем запис о количестве

    }


    public void setCountBombAround(long bombAround){//метод объявления количества бомб рядом
        this.bombAround = bombAround;
        this.countBombsAround.setText(String.valueOf(bombAround));
    }

    public int openCell() {//метод открытия ячейки
        if (!isOpen) {//если ячейка не открыта

          if (hasBomb) {//если ячейка бомба, то вернуть -1, для обработки в другом классе

            return -1;
        } else {//иначе открыть ячейку
              isOpen = true;
              countBombsAround.setVisible(true);//сделать видимым запись
              hexagon.setFill(null);//убрать заливку
              return 1;//вернуть 1 для обработки в другом классе
          }
        }
        return 0;
    }



    private Point2D calc_hexagon(Point2D center, int size, int i){ // расчет точек, для рисования шестигранника
        double pointX=center.getX()+size/2, pointY=center.getY()+size/2; // начальные точки
        double angle_def = 60* i+30;//формула подсчета градуса для сторон
        double angle = Math.PI / 180 * angle_def;//расет координат точек уклов шестигранника
        pointX+=size*Math.cos(angle);
        pointY+=size*Math.sin(angle);
        return new Point2D(pointX,pointY);
    }

    public boolean isHasBomb() {
        return hasBomb;
    }//возвращает флаг бомбы

    public boolean isOpen() {
        return isOpen;
    }//возвращает флаг открытия


    public void cellBang() {//метод подрыва ячейки
        hexagon.setFill(null);//убрать заливку
        if (image==null){ return;}//если иконки нет, то вернуться

        if (!check) {//если не проверена, то есть флаг не стоит
            ImageView bomb = new ImageView(image);//добавить иконку бомбы
            getChildren().add(bomb);//
            bomb.setVisible(true);//показать иконку бомбы
            return;
        }else {//иначе если флаг стоит
            statusImage = new ImageView(new Image("cancel_bomb.jpg")); // то добавить иконку разминированной бомбы
            getChildren().add(statusImage);//
            statusImage.setVisible(true);//показать ее
            check = false;//обнулить флаг
            return;
        }


    }

    public void setStatusImage(Image stat){ // метод установки иконки флага
        if(stat!=null) {//если флаг передает иконку
            statusImage = new ImageView(stat);///добавляем иконку
            getChildren().add(statusImage);
            statusImage.setVisible(true);//показываем ее
            check = true;//делаем статус проверено
        }else{
try {
    statusImage.setVisible(false);//иначе убираем иконку
    check = false;
}catch (NullPointerException e){

}
        }
    }
public long getBombAround(){
        return bombAround;
}//возвращает количество бомб рядом с ячейкой

public void setHasBomb(boolean active){//устанавливает ячейку как бомбу
    hasBomb = active;

        image = new Image("bomb.jpg");//добавляем иконку бомбы


}
}