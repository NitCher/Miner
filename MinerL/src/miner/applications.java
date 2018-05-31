package miner;

import javafx.application.Application;
import javafx.application.Platform;

import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.*;

import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Pair;


import java.util.*;

/**
 * Главный класс. Наследует от Application- класс для создания и запуска приложения javafx
 * javafx - это пакет для создания графического интерфейса
 * для javafx не обязательно писать метод Main
 */
public class applications extends Application { //
    //переменные размера окна
    private double widthWindow, heightWindow;
    //поток таймера для подсчета игрового времени
    private Thread threadTimer;
    //поля для вывода времени и количества флагов
    private TextField timerField = new TextField();
    private TextField flagField = new TextField();

    private Point2D coordinate; // координата расположения ячеек(шестигранников)
    private final int size = 15, minRow = 10, minColl = 10, maxRow = 24, maxColl = 30; // размер ячеек, минимальное количество строк и стобцов, максимальное количество строк и сболбцов

    private int row = 10, coll = 10, countBobm = 9, countEmptyCell = 0; // начальные количество столбцов и строк,а  так же количество бомб
    private boolean[][] status;       //массив, содержащий статус ячеек(true-открыта, false - закрыта)
    private int baseX, baseY, offsetX;   //начальные координаты для отображения ячеек. OffsetX - смещение по Х для составления сетки в виде сот
    private Pane boardPane; // панель игрового поля, на ней размещаются все ячейки
    private AnchorPane mainPane;    //панель для размещения жэлементов
    private Scene scene; // сцена - главная панель, на ней размещаются все элементы графического интерфейса
    private Stage window; // объект - графическое окно виндовс
    private MenuBar menu = new MenuBar(); // объект меню (верхняя линия, игра, помощь)
    public static Hexagon[][] hexagons;    //массив с ячейками
    Vector<Pair<Integer, Integer>> shaheed = new Vector<>();// коллекция(массив данных) содержащий коэфициенты ячеек-бомб
    private double width = (Math.sqrt(Math.pow(size, 2) - Math.pow(size / 2, 2))) * 2; // расчет расстояния между ячейками по оси Х (ширина)
    private double height = size * 2; // расстояние по оси Y (высота)
    private int countCheckCells; // количество отмеченных ячеек
    //


    private TimerGame timer = new TimerGame(10);

    /**
     * перезаписанный метод запуска приложения из класса Application. В аргументах принимает окно(форму)
     *
     * @param primaryStage
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception { // стартовый метод
        //задаем начальные размеры окна
        widthWindow = 200;
        heightWindow = 200;
        //сохраняем окно в переменной для более удобного обращения к нему
        window = primaryStage; // определяем окно
        //создаем главную панель, на ней будет размещаться игровая доска, меню и поля вывода информации
        mainPane = new AnchorPane();
        //создаем игровую панель, на ней будут размещаться ячейки
        boardPane = new Pane();
        //создаем и наполняем меню игры
        Menu mGame = new Menu("Игра");
        Menu mHelp = new Menu("Помощь");
        MenuItem itemNewGame = new MenuItem("Новая");
        MenuItem itemExit = new MenuItem("Выход");
        mGame.getItems().addAll(itemNewGame, itemExit);
        menu.getMenus().addAll(mGame, mHelp);
        itemNewGame.setOnAction(event -> {
            createGame("Новая игра");// метод вызова окна для ввода параметров игрового поля, находиться ниже
        });
        itemExit.setOnAction(e -> {
            shutdown();//метод для корректного выхода из приложения
        });
        //добавляем на панель игровое поле
        mainPane.getChildren().add(boardPane);
        //растягиваем меню по ширине окна
        menu.prefWidthProperty().bind(window.widthProperty());
        //добавляем меню
        timerField.setDisable(true);
        flagField.setDisable(true);
        timerField.setFocusTraversable(false);
        flagField.setFocusTraversable(false);
        //панель размещения полей вывода времнеи и количства флагов
        HBox box = new HBox(timerField, flagField);
        //задаем ей отслеживание ширины окна, чтобы панель всегда была по ширине окна
        box.prefWidthProperty().bind(window.widthProperty());
        //задаем прослушку таймре.
        timerField.textProperty().bind(timer.messageProperty());
        //задаем местоположение панели полей на главной панели
        AnchorPane.setBottomAnchor(box, 10.0);
        //добавляем панель полей вывода на главную панель
        mainPane.getChildren().add(box);
        //добавляем меню
        /**
         * getChildren() -метод для получения информации о панели, или объекте отображения  в javafx
         * Через данный метод можно добавлять или удалить элементы с панели или сцены
         */
        mainPane.getChildren().add(menu);
        //делаем окно размерами, указанными в соотвествующих переменных
        window.setWidth(widthWindow);
        window.setHeight(heightWindow);
        //запрещаем изменять окно
        window.setResizable(false);

        //создаем сцену на основе панели с элементами. Сцена - аналог JPanel из swing
        scene = new Scene(mainPane);
        // задаем название окна

        window.setTitle("Сапер");
        //устанавливаем сцену на окно
        window.setScene(scene);
        //действие окна при нажатии на крестик, завершить приложение
        window.setOnCloseRequest((WindowEvent e) -> {
            shutdown();
        });
        //показать окно, запустить приложение
        window.show();

    }

    private void shutdown() {
        Platform.exit();//выйти из javafx
        System.exit(0);//завершить процесс
    }

    private void buildBoard() {//метод создания игрового поля
        //объявляем поток timer
        threadTimer = new Thread(timer);
        baseX = 0;//начальные координаты для отрисовки ячеек
        baseY = 40;
        boardPane.setDisable(false);//запрет на взаимодействие пользователем с окном

        hexagons = new Hexagon[row][coll];//создаем массив ячеек казанных размеров


        Random rnd = new Random();//рандом для генерации бомб
        status = new boolean[row][coll]; // объявляем массив статуса ячеек
        //циклы для отрисовки элементов на панели
        for (int i = 0; i < row; i++) {
            offsetX = baseX;//сохраняем в сдвиге текущуюю координату
            for (int j = 0; j < coll; j++) {
                status[i][j] = false;

                coordinate = new Point2D(baseX, baseY); // создаем точку с началом координат
                hexagons[i][j] = new Hexagon(coordinate, size); // создаем ячейку
                if (j % 2 == 0) {//если это каждый второй ряд
                    offsetX = baseX; // соххраняем текущий Х
                    baseX += size * 0.9; // сдвигаем икс на некоторое расстояние
                } else //иначе
                    baseX = offsetX;//берем текущий икс
                baseY += horizontal(size) + (0.66 * size);  // отрисовывем следующую строку на высоту ячейку

            }
            baseY = 40;
            baseX += vertical(size) + size / 3; // +1/3 * size // смещение по х-су для рисования следующей ячейки


        }

        int x, y, countSetBomb = 0;// генерация бомб
        while (countSetBomb < countBobm) {// пока количество установленных бомб меньше заданного коичества
            do {// на рандоме выбираем координаты
                x = rnd.nextInt(row);
                y = rnd.nextInt(coll);
            } while (hexagons[x][y].isHasBomb());// и повторяем это пока не окажется пустая ячейка
            hexagons[x][y].setHasBomb(true); // заносим в пустую ячейку бомбу
            shaheed.add(new Pair<>(x, y)); // заносим координаты в массив бомб
            countSetBomb++;//прибавляем к установленным бомбам
        }

        for (y = 0; y < coll; y++) {
            for (x = 0; x < row; x++) {
                /**
                 * Создаем переменную о количестве бомб рядом с ячейков
                 * вызываем метод обхода ячеек в потоке, указываем фильтр по состоянию бомбы
                 */
                long bombs = getNeighbors(x, y).stream().filter(t -> t.isHasBomb()).count();
                if (bombs > 0) {
                    hexagons[x][y].setCountBombAround(bombs); // передаем ячейке количество бомб
                }
            }
        }
        for (Hexagon[] line : hexagons) {
            for (Hexagon test : line) {
                boardPane.getChildren().add(test); // отображаем ячейки на игровом поле
            }

        }
        threadTimer.start();// запускаем поток таймера
        widthWindow = (row * width) + size * 3; // перерасчитываем размеры окна в соответсвии с новым полем
        heightWindow = (coll * (height - size / 3)) + size * 6;
        window.setWidth(widthWindow);//устанавливаем новые размеры окна
        window.setHeight(heightWindow);
        boardPane.setOnMouseClicked(e -> {//добавляем действие по щелчку мыши
            for (int i = 0; i < row; i++)
                for (int j = 0; j < coll; j++)
                    //проверяем, совпадают ли координаты нажатия с координатами ячейки
                    if (((e.getX() >= hexagons[i][j].getTranslateX()) && (e.getX() <= hexagons[i][j].getTranslateX() + width)) &&
                            ((e.getY() >= hexagons[i][j].getTranslateY()) && (e.getY() <= hexagons[i][j].getTranslateY() + height))) {
                        if (e.getButton().equals(MouseButton.SECONDARY)) {//если нажали правую кнопку мыши
                            if (countCheckCells != 0 && !hexagons[i][j].check) { // проверяем, если количество флагов не ноль и ячейка не проверена
                                if (hexagons[i][j].isOpen()) return;
                                hexagons[i][j].setStatusImage(new Image("flag.jpg"));//то устанавливаем флаг на ячейку
                                Platform.runLater(() -> flagField.setText(String.valueOf(--countCheckCells)));//выводим в поле флагов их количество
                            } else {
                                if (countCheckCells == 10)
                                    return;// иначе если количество ячеек равно 10 или нулю, то выходим, ничего не делаем
                                if (!hexagons[i][j].check & countCheckCells == 0) return;


                                hexagons[i][j].setStatusImage(null); // иначе убираем флаг
                                Platform.runLater(() -> flagField.setText(String.valueOf(++countCheckCells))); // повышаем количество флагов и выводим на экран
                            }
                            // устанавливаем флаг на ячейку
                        } else {//иначе надатие левой кнопки мыши
                            if (hexagons[i][j].isHasBomb()) {// если это бомба
                                for (Pair<Integer, Integer> id : shaheed) { // пройтись по всем бомбам
                                    try {
                                        hexagons[id.getKey()][id.getValue()].cellBang(); // и подорвать их
                                    } catch (ArrayIndexOutOfBoundsException ex) {//игнорируем если выходим за пределы массива
                                    }
                                }
                                boardPane.setDisable(true);//запрещаем взаимодействоватьс  полем
                                threadTimer.interrupt();//посылаем сигнал о завершению работы потоку таймера
                                try {
                                    threadTimer.join();//ожидаем завершение потока таймера
                                } catch (InterruptedException e1) {
                                    e1.printStackTrace();
                                }
                                threadTimer = null;//обнуляем объект потока
                                createGame("Повторить?");//вывод окна о запросе новой игры
                                return;
                            } else {
                                if (hexagons[i][j].check)
                                    return;//если ячейка проверена, то есть поставлен флаг, то ничего не делаем
                                receiveClick(i, j);// иначе пройтись по всем ячейкам не бомбам
                                if (pro()) {//вызываем метод проверки состояния поля, если вернет true, то все пустые ячейки открыты
                                    for (Pair<Integer, Integer> id : shaheed) { // пройтись по всем бомбам
                                        hexagons[id.getKey()][id.getValue()].cellBang(); // и подорвать их
                                        boardPane.setDisable(true);
                                        try {
                                            threadTimer.interrupt();
                                            threadTimer = null;
                                        } catch (NullPointerException e1) {
                                        }
                                    }
                                    showWin();
                                }
                            }
                        }
                    }

        });

    }

    private List<Hexagon> getNeighbors(int x, int y) {
        List<Hexagon> neighbors = new ArrayList<>();

        // ttt
        // tXt
        // ttt

        int[] points = new int[]{
                -1, -1,
                -1, 0,
                -1, 1,
                0, -1,
                0, 1,
                1, -1,
                1, 0,
                1, 1
        };

        for (int i = 0; i < points.length; i++) {
            int dx = points[i];
            int dy = points[++i];

            int newX = x + dx;
            int newY = y + dy;

            if (newX >= 0 && newX < row
                    && newY >= 0 && newY < coll) {
                neighbors.add(hexagons[newX][newY]);
            }
        }

        return neighbors;
    }// подсчет количества рядом стоящих бомб


    private void showWin() {//метод вывда диалогового окна победы
        ButtonType buttonTypeOk = new ButtonType("Повторить", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeCansel = new ButtonType("Нет", ButtonBar.ButtonData.OK_DONE);
        Alert winner = new Alert(Alert.AlertType.CONFIRMATION, "Желаете ли вы начать новую игру??", buttonTypeOk, buttonTypeCansel);

        winner.setTitle("Выигрыш!!!");
        winner.setHeaderText("Вы выиграли в игре за время: " + timerField.getText() + "\n");


        Optional<ButtonType> option = winner.showAndWait();//запускаем окно и результат нажатия кнопок окна записываем в переменную

        if (option.get() == buttonTypeOk) {//если была нажата клавиша Повторить
            createGame("Начать новую игру?");//запросить новые параметры игры
        }


    }

    private void createGame(String title) {//метод запроса на создание поля
        ButtonType buttonTypeOk = new ButtonType("Да", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonCancel = new ButtonType("Нет", ButtonBar.ButtonData.CANCEL_CLOSE);
        Dialog<Pair<Pair<String, String>, String>> inputDialog = new Dialog<>();

        inputDialog.setTitle(title);
        inputDialog.setHeaderText("Введите количество ячеек и количество бомб");
        inputDialog.setResizable(true);
        Label label1 = new Label("Строк: ");
        Label label2 = new Label("Столбцов: ");
        Label label3 = new Label("Бомб: ");

        TextField rowField = new TextField("10");
        TextField collField = new TextField("10");
        TextField bombField = new TextField("10");
        GridPane grid = new GridPane();
        grid.add(label1, 1, 1);
        grid.add(rowField, 2, 1);
        grid.add(label2, 1, 2);
        grid.add(collField, 2, 2);
        grid.add(label3, 1, 3);
        grid.add(bombField, 2, 3);

        inputDialog.getDialogPane().setContent(grid);


        inputDialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
        inputDialog.getDialogPane().getButtonTypes().add(buttonCancel);
        inputDialog.setResultConverter(param -> {

            if (param.equals(buttonTypeOk)) {

                /**
                 * ВД = введенное значение
                 *
                 * данная конструкция равна условию
                 * Если ВД меньше максимального значения, то Если введенное значение больше минимального, то ВД, иначе минимальное,, иначе максимальное
                 */
                coll = (Integer.parseInt(rowField.getText()) < maxRow) ?
                        (Integer.parseInt(rowField.getText()) > minRow) ?
                                Integer.parseInt(rowField.getText()) : minRow : maxRow;

                row = (Integer.parseInt(collField.getText()) < maxColl) ?
                        (Integer.parseInt(collField.getText()) > minColl) ?
                                Integer.parseInt(collField.getText()) : minColl : maxColl;


                int maxBomb = row * coll - 5;
                int minBomb = 3;
                countBobm = (Integer.parseInt(bombField.getText()) < row * coll) ?
                        (Integer.parseInt(bombField.getText()) > minBomb) ?
                                Integer.parseInt(bombField.getText()) : minBomb : maxBomb;
                countCheckCells = countBobm;
                countEmptyCell = row * coll - countBobm;
                System.out.println(countEmptyCell);
                flagField.setText(String.valueOf(countCheckCells));
                hexagons = null;

                System.out.println(row + " " + coll + "    " + countBobm);
                Platform.runLater(() -> {
                    boardPane.getChildren().clear();//очищаем поле от старых элементов
                    buildBoard();


                });
            }
            if (param.equals(buttonCancel)) {
            //если нажали cancel, то просто закрываем окно
            }


            return null;
        });
        inputDialog.showAndWait();//запустили окно запроса и ждем пока нажмут кнопку


    }


    private int receiveClick(int x, int y) {//рекурсивный объход ячеек
        int cell_x = x;
        int cell_y = y;

        int result = hexagons[cell_x][cell_y].openCell();//открываем ячейку

        if (hexagons[cell_x][cell_y].getBombAround() != 0) {//если вокруг ячейки нет бомб, то вернем 0
            status[cell_x][cell_y] = true;
            return 0;
        }
        status[cell_x][cell_y] = true;//записываем состояние ячейки как открытую

        if (result == 1) {//если 1, значит ячейка пуста и надо проверить соседние

            ///Делаем вид, что тыкнули в клетки
            ///Сверху, снизу, справа и слева
            ///Игнорируем выхождение за границы поля
            try {
                receiveClick(x + 1, y);

            } catch (ArrayIndexOutOfBoundsException e) {
                //ignore
            }
            try {
                receiveClick(x - 1, y);

            } catch (ArrayIndexOutOfBoundsException e) {
                //ignore
            }
            try {
                receiveClick(x, y + 1);

            } catch (ArrayIndexOutOfBoundsException e) {
                //ignore
            }
            try {
                receiveClick(x, y - 1);

            } catch (ArrayIndexOutOfBoundsException e) {
                //ignore
            }

            return 0;
        }


        return result;
    }


    private double horizontal(int size) {
        return Math.sqrt(3) / 2 * size;
    }//расчитываем горизонтальное расстояние от центров ячеек

    private double vertical(int size) {
        double rez = 0.75 * size * 2;

        return rez;
    } // расчитываем ветикальное расстояние от центра ячеек


    private boolean pro() {//расчитываем количество открытых клетов
        int i = 0;
        for (boolean[] line : status) {
            for (boolean b : line) {
                if (b) {
                    i++;
                }
            }

        }
        if (i == countEmptyCell) return true;
        return false;
    }

}



