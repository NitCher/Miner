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


public class applications extends Application {
    private double widthWindow, heightWindow;
    private Thread threadTimer;
    private TextField timerField = new TextField();
    private TextField flagField = new TextField();

    private Point2D coordinate;
    private final int size = 15, minRow=10,minColl=10, maxRow=24, maxColl=30;

    private int row = 10, coll = 10, countBobm = 9, countEmptyCell=0;
    private boolean[][] status;
    private int baseX, baseY, offsetX;
    private Pane boardPane;
    private AnchorPane mainPane;
    private Scene scene;
    private Stage window;
    private MenuBar menu = new MenuBar();
    public static Hexagon[][] hexagons;
    Vector<Pair<Integer, Integer>> shaheed = new Vector<>();
    private double width = (Math.sqrt(Math.pow(size, 2) - Math.pow(size / 2, 2))) * 2;
    private double height = size * 2;
    private int countCheckCells;

    private TimerGame timer = new TimerGame(10);


    @Override
    public void start(Stage primaryStage) throws Exception {
        widthWindow = 200; heightWindow = 200;
        window = primaryStage;
        mainPane = new AnchorPane();
        boardPane = new Pane();

        Menu mGame = new Menu("Игра");
        Menu mHelp = new Menu("Помощь");
        MenuItem itemNewGame = new MenuItem("Новая");
        MenuItem itemExit = new MenuItem("Выход");
        mGame.getItems().addAll(itemNewGame, itemExit);
        menu.getMenus().addAll(mGame, mHelp);
        itemNewGame.setOnAction(event -> {
            createGame("Новая игра");
        });
        itemExit.setOnAction(e -> {
            shutdown();
        });

        mainPane.getChildren().add(boardPane);
        menu.prefWidthProperty().bind(window.widthProperty());
        timerField.setDisable(true);
        flagField.setDisable(true);
        timerField.setFocusTraversable(false);
        flagField.setFocusTraversable(false);
        HBox box = new HBox(timerField, flagField);
        box.prefWidthProperty().bind(window.widthProperty());
        timerField.textProperty().bind(timer.messageProperty());
        AnchorPane.setBottomAnchor(box, 10.0);
        mainPane.getChildren().add(box);
        mainPane.getChildren().add(menu);
        window.setWidth(widthWindow);
        window.setHeight(heightWindow);
        window.setResizable(false);

        scene = new Scene(mainPane);

        window.setTitle("Сапер");
        window.setScene(scene);
        window.setOnCloseRequest((WindowEvent e) -> {
            shutdown();
        });
        window.show();

    }

    private void shutdown() {
        Platform.exit();
        System.exit(0);
    }

    private void buildBoard() {

        threadTimer = new Thread(timer);
        baseX = 0;
        baseY = 40;
        boardPane.setDisable(false);

        hexagons = new Hexagon[row][coll];


        Random rnd = new Random();
        status = new boolean[row][coll];
        for (int i = 0; i < row; i++) {
            offsetX = baseX;
            for (int j = 0; j < coll; j++) {
                status[i][j] = false;

                coordinate = new Point2D(baseX, baseY);
                hexagons[i][j] = new Hexagon(coordinate, size);
                if (j % 2 == 0) {
                    offsetX = baseX;
                    baseX += size * 0.9;
                } else
                    baseX = offsetX;
                baseY += horizontal(size) + (0.66 * size);

            }
            baseY = 40;
            baseX += vertical(size) + size / 3;


        }

        int x,y,countSetBomb = 0;
        while (countSetBomb < countBobm) {
            do {
               x = rnd.nextInt(row);
               y = rnd.nextInt(coll);
            } while (hexagons[x][y].isHasBomb());
            hexagons[x][y].setHasBomb(true);
            shaheed.add(new Pair<>(x, y));
            countSetBomb++;
        }

        for (y = 0; y < coll; y++) {
            for (x = 0; x < row; x++) {

                long bombs = getNeighbors(x, y).stream().filter(t -> t.isHasBomb()).count();
                if (bombs > 0) {
                    hexagons[x][y].setCountBombAround(bombs);
                }
            }
        }
        for (Hexagon[] line : hexagons) {
            for (Hexagon test : line) {
                boardPane.getChildren().add(test);
            }

        }
        threadTimer.start();
        widthWindow = (row * width) + size*3 ;
        heightWindow =(coll * (height-size/3))+size*6;
        window.setWidth(widthWindow);
        window.setHeight(heightWindow);
        boardPane.setOnMouseClicked(e -> {
            for (int i = 0; i < row; i++)
                for (int j = 0; j < coll; j++)
                    if (((e.getX() >= hexagons[i][j].getTranslateX()) && (e.getX() <= hexagons[i][j].getTranslateX() + width)) &&
                            ((e.getY() >= hexagons[i][j].getTranslateY()) && (e.getY() <= hexagons[i][j].getTranslateY() + height))) {
                        if (e.getButton().equals(MouseButton.SECONDARY)) {
                            if (countCheckCells!=0&& !hexagons[i][j].check) {
                                if(hexagons[i][j].isOpen()) return;
                                hexagons[i][j].setStatusImage(new Image("flag.jpg"));
                                Platform.runLater(() -> flagField.setText(String.valueOf(--countCheckCells)));
                            }else {if (countCheckCells == 10) return;
                                    if (!hexagons[i][j].check & countCheckCells == 0) return;


                                hexagons[i][j].setStatusImage(null);
                                Platform.runLater(() -> flagField.setText(String.valueOf(++countCheckCells)));
                            }

                        } else {
                            if (hexagons[i][j].isHasBomb()) {
                                for (Pair<Integer, Integer> id : shaheed) {
                                   try {
                                       hexagons[id.getKey()][id.getValue()].cellBang();
                                   }catch (ArrayIndexOutOfBoundsException ex){
                                   }
                                   }
                                boardPane.setDisable(true);
                                threadTimer.interrupt();
                                try {
                                    threadTimer.join();
                                } catch (InterruptedException e1) {
                                    e1.printStackTrace();
                                }
                                threadTimer = null;
                                createGame("Повторить?");
                                return;
                            } else {
                                if (hexagons[i][j].check) return;
                                receiveClick(i, j);
                                if (pro()){
                                    for (Pair<Integer, Integer> id : shaheed) {
                                        hexagons[id.getKey()][id.getValue()].cellBang();
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
    }


    private void showWin(){
        ButtonType buttonTypeOk = new ButtonType("Повторить", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeCansel = new ButtonType("Нет", ButtonBar.ButtonData.OK_DONE);
        Alert winner =new Alert(Alert.AlertType.CONFIRMATION, "Желаете ли вы начать новую игру??",buttonTypeOk,buttonTypeCansel);

        winner.setTitle("Выигрыш!!!");
        winner.setHeaderText("Вы выиграли в игре за время: " + timerField.getText()+"\n");


        Optional<ButtonType> option = winner.showAndWait();

                    if (option.get() ==buttonTypeOk) {
                        createGame("Начать новую игру?");
                    }


    }
    private void createGame(String title) {
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


                coll = (Integer.parseInt(rowField.getText())< maxRow)?
                        (Integer.parseInt(rowField.getText())>minRow)?
                                Integer.parseInt(rowField.getText()):minRow : maxRow  ;

                row = (Integer.parseInt(collField.getText())< maxColl)?
                        (Integer.parseInt(collField.getText())>minColl)?
                                Integer.parseInt(collField.getText()):minColl : maxColl  ;



                int maxBomb = row * coll -5;
                int minBomb = 3;
                countBobm = (Integer.parseInt(bombField.getText())< row * coll)?
                        (Integer.parseInt(bombField.getText())>minBomb)?
                                Integer.parseInt(bombField.getText()):minBomb : maxBomb  ;
                countCheckCells = countBobm;
                countEmptyCell = row*coll - countBobm;
                System.out.println(countEmptyCell);
                flagField.setText(String.valueOf(countCheckCells));
                hexagons = null;

                System.out.println(row + " " + coll + "    " + countBobm);
                Platform.runLater(() -> {
                    boardPane.getChildren().clear();
                    buildBoard();


                });
            }
            if (param.equals(buttonCancel)) {

            }

            return null;
        });
        inputDialog.showAndWait();
    }


    private int receiveClick(int x, int y) {
        int cell_x = x;
        int cell_y = y;

        int result = hexagons[cell_x][cell_y].openCell();

        if (hexagons[cell_x][cell_y].getBombAround() != 0) {
            status[cell_x][cell_y]=true;
            return 0;
        }
        status[cell_x][cell_y]=true;

        if (result == 1) {

            try {
                receiveClick(x + 1, y);
            } catch (ArrayIndexOutOfBoundsException e) {

            }
            try {
                receiveClick(x - 1, y);

            } catch (ArrayIndexOutOfBoundsException e) {
            }
            try {
                receiveClick(x, y + 1);
            } catch (ArrayIndexOutOfBoundsException e) {
            }
            try {
                receiveClick(x, y - 1);
            } catch (ArrayIndexOutOfBoundsException e) {
            }
            return 0;
        }


        return result;
    }


    private double horizontal(int size) {
        return Math.sqrt(3) / 2 * size;
    }

    private double vertical(int size) {
        double rez = 0.75 * size * 2;

        return rez;
    }


    private boolean pro() {
        int i = 0;
        for (boolean[] line : status) {
            for (boolean b : line) {
                if (b) {
                    i++;
                }
            }

        }
        if(i==countEmptyCell) return true;
        return false;
    }

}



