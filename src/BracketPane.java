import javafx.event.EventHandler;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javafx.scene.shape.Polygon;


/**
 * Created by Richard and Ricardo on 5/3/17.
 */
public class BracketPane extends BorderPane {

        /**
         * Reference to the graphical representation of the nodes within the bracket.
         */
        private static ArrayList<BracketNode> nodes;
        /**
         * Used to initiate the paint of the bracket nodes
         */
        private static boolean isTop = true;
        /**
         * Maps the text "buttons" to it's respective grid-pane
         */
        private HashMap<StackPane, Pane> panes;
        /**
         * Reference to the current bracket.
         */
        private Bracket currentBracket;
        /**
         * Reference to active subtree within current bracket.
         */
        //Samuel Hernandez: Made static to keep reference value when resetting Bracket Panes
        private static int displayedSubtree;
        /**
         * Keeps track of whether or not bracket has been finalized.
         */
        private boolean finalized;
        /**
         * Important logical simplification for allowing for code that is easier
         * to maintain.
         */
        private HashMap<BracketNode, Integer> bracketMap = new HashMap<>();
        /**
         * Reverse of the above;
         */
        private HashMap<Integer, BracketNode> nodeMap = new HashMap<>();

        /**
         * @author Samuel Hernandez, 04/04/2021
         * The buttons to choose a bracket (East, West, Full etc.)
         */
        private ArrayList<StackPane> buttons;

        /**
         * @author Samuel Hernandez, 04/04/2021
         * Array to have class access to all roots
         */
        private ArrayList<Root> roots;

        // added attribute to class, no longer exclusive to method
        private TournamentInfo info;

        private GridPane center;
        private GridPane fullPane;

        /**
         * Pane attributes added to class to allow displaying of rounds
         * @editor Ariel Liberzon
         */
        private GridPane topLeftPane;
        private GridPane topCenterPane;
        private GridPane topRightPane;

        /**
         * Initializes the properties needed to construct a bracket.
         * The constructor now includes methods that display the rounds
         * above the corresponding place in the bracket
         *
         * @param currentBracket the current bracket
         * @editor Ariel Liberzon
         */
        public BracketPane(Bracket currentBracket) {
                this.currentBracket = currentBracket;

                bracketMap = new HashMap<>();
                nodeMap = new HashMap<>();
                panes = new HashMap<>();
                nodes = new ArrayList<>();
                roots = new ArrayList<>();
                center = new GridPane();
                buttons = new ArrayList<>();
                buttons.add(customButton("EAST"));
                buttons.add(customButton("WEST"));
                buttons.add(customButton("MIDWEST"));
                buttons.add(customButton("SOUTH"));
                buttons.add(customButton("FULL"));

                setInfo();

                ArrayList<GridPane> gridPanes = new ArrayList<>();
                String[] divisionNames = {"EAST", "WEST", "MIDWEST", "SOUTH", ""};
                for (int m = 0; m < buttons.size() - 1; m++) {
                        roots.add(new Root(3 + m, divisionNames[m], m));
                        panes.put(buttons.get(m), roots.get(m));
                }
                Pane finalPane = createFinalFour();
                fullPane = new GridPane();
                GridPane gp1 = new GridPane();
                gp1.add(roots.get(0), 0, 0);
                gp1.add(roots.get(1), 0, 1);
                GridPane gp2 = new GridPane();
                gp2.add(roots.get(2), 0, 0);
                gp2.add(roots.get(3), 0, 1);
                gp2.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                fullPane.add(gp1, 0, 1);
                fullPane.add(finalPane, 1, 1, 1, 2);
                fullPane.add(gp2, 2, 1);
                fullPane.setAlignment(Pos.CENTER);
                panes.put(buttons.get((buttons.size() - 1)), fullPane);
                finalPane.toBack();

                // Panes that will display rounds
                topLeftPane = new GridPane();
                topCenterPane = new GridPane();
                topRightPane = new GridPane();

                // creates proper labels for the rounds
                createRounds();

                // Initializes the button grid
                GridPane buttonGrid = new GridPane();
                for (int i = 0; i < buttons.size(); i++)
                        buttonGrid.add(buttons.get(i), 0, i);
                buttonGrid.setAlignment(Pos.CENTER);

                // set default center to the button grid
                this.setCenter(buttonGrid);

                //For all buttons set its functionality
                for(int i = 0; i < buttons.size(); i++){
                        StackPane button = buttons.get(i);
                        button.setStyle("-fx-background-color: #8bc4de; -fx-font-family: Futura;");
                        button.setOnMouseEntered(mouseEvent -> {
                                button.setStyle("-fx-background-color: LIGHTGREEN; -fx-font-family: Futura;");
                                button.setEffect(new InnerShadow(10, Color.LIGHTGREEN));
                        });
                        button.setOnMouseExited(mouseEvent -> {
                                button.setStyle("-fx-background-color: #8bc4de; -fx-font-family: Futura;");
                                button.setEffect(null);
                        });

                        //What happens when click happens in button
                        button.setOnMouseClicked(mouseEvent -> {
                                setVisiblePane(buttons.indexOf(button));
                                displayedSubtree=buttons.indexOf(button)==7?0:buttons.indexOf(button)+3;
                                if(buttons.indexOf(button) == 4) {
                                        MarchMadnessGUI.getButton().setDisable(true);
                                }
                                else
                                        MarchMadnessGUI.getButton().setDisable(false);

                        });
                }
        }

        /**
         * Clears the entries of a team future wins
         * @param treeNum
         */
        private void clearAbove(int treeNum) {
                int nextTreeNum = (treeNum - 1) / 2;
                if (!nodeMap.get(nextTreeNum).getName().isEmpty()) {
                        nodeMap.get(nextTreeNum).setName("");
                        clearAbove(nextTreeNum);
                }
        }

        /**
         * Clear.
         */
        public void clear(){
            clearSubtree(displayedSubtree);
        }

        /**
         * Handles clicked events for BracketNode objects
         */
        private EventHandler<MouseEvent> clicked = mouseEvent -> {
                //conditional added by matt 5/7 to differentiate between left and right mouse click
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                        BracketNode n = (BracketNode) mouseEvent.getSource();
                        int treeNum = bracketMap.get(n);
                        int nextTreeNum = (treeNum - 1) / 2;
                        if (!nodeMap.get(nextTreeNum).getName().equals(n.getName())) {
                                currentBracket.removeAbove((nextTreeNum));
                                clearAbove(treeNum);
                                nodeMap.get((bracketMap.get(n) - 1) / 2).setName(n.getName());
                                currentBracket.moveTeamUp(treeNum);
                        }
                }
                //added by matt 5/7, shows the teams info if you right click
                /*
                else if (mouseEvent.getButton().equals(MouseButton.SECONDARY)) {
                        String text = "";
                        String logoRef = "";
                        BracketNode n = (BracketNode) mouseEvent.getSource();
                        int treeNum = bracketMap.get(n);
                        String displayName = currentBracket.getBracket().get(treeNum);
                        if (info != null) {
                                Team t = info.getTeam(displayName);
                                logoRef = t.getLogoRef();
                                //by Tyler - added the last two pieces of info to the pop up window
                                text += "Team: " + t.getFullName() + " | Ranking: " + t.getRanking()
                                        + "\nMascot: " + t.getNickname() + "\nInfo: " + t.getInfo()
                                        + "\nAverage Offensive PPG: " + t.getOffensePPG()
                                        + "\nAverage Defensive PPG: "+ t.getDefensePPG();
                        } else {//if for some reason TournamentInfo is not working, it
                                // will display info not found
                                text += "Info for " + displayName + "not found";
                        }

                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, text, ButtonType.CLOSE);
                        alert.setTitle("Team Information");
                        alert.setHeaderText(null);

                        alert.setGraphic(new ImageView(this.getClass().getResource("Icons/"+logoRef).toString()));

                        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                        alert.showAndWait();
                }*/
        };
        /**
         * Handles mouseEntered events for BracketNode objects
         * @editor Ariel Liberzon, Harjit Singh
         * Built on top of the existing event handler method code to allow for a tooltip
         * Text output is based on the original secondary mouse click event handler code
         * In addition to text, every team will have its logo displayed in the tooltip
         */
        private EventHandler<MouseEvent> enter = mouseEvent -> {
                BracketNode tmp = (BracketNode) mouseEvent.getSource();
                tmp.setStyle("-fx-background-color: lightgreen;");
                tmp.setEffect(new InnerShadow(10, Color.LIGHTGREEN));
                if (!tmp.displayName.equals("")) {
                        Team t = info.getTeam(tmp.displayName);
                        Tooltip tooltip = new Tooltip();
                        tooltip.install(tmp, tooltip);
                        String logoRef = t.getLogoRef();
                        String text = "Team: " + t.getFullName() + " | Ranking: " + t.getRanking()
                                + "\nMascot: " + t.getNickname() + "\nInfo: " + t.getInfo()
                                + "\nAverage Offensive PPG: " + t.getOffensePPG()
                                + "\nAverage Defensive PPG: "+ t.getDefensePPG();
                        tooltip.setText(text);
                        try {
                                tooltip.setGraphic(new ImageView(this.getClass().getResource("Icons/" + logoRef).toString()));
                        } catch (Exception e) {
                                tooltip.setText(text + "\nMissing Logo!");
                        }
                }
        };

        /**
         * Handles mouseExited events for BracketNode objects
         */
        private EventHandler<MouseEvent> exit = mouseEvent -> {
                BracketNode tmp = (BracketNode) mouseEvent.getSource();
                tmp.setStyle(null);
                tmp.setEffect(null);

        };

        /**
         * Gets full pane.
         *
         * @return the full pane
         */
        public GridPane getFullPane() {
                return fullPane;
        }

        private void setInfo() {
                try {
                        info = new TournamentInfo();
                }
                catch (IOException e) {
                        info = null;
                }
        }

        /**
         * Method adds labels to now what round teams are on, when making selection in individual sections
         * of the bracket.
         *
         * @param index the section to add the labels to.
         * @author Samuel Hernandez (Based on Ariel Liberzon's code)
         */
        public void roundsForDivsions(int index){
                if(index >= 0 && index < 4) {
                        GridPane top = new GridPane();
                        String[] roundArr = {"ROUND1", "ROUND 2", "SWEET 16", "ELITE 8", "FINAL FOUR"};
                        String style = " -fx-font: 15px Futura; -fx-background-color: lightgreen;" +
                                "-fx-text-fill: #18284a; -fx-alignment:center;";

                        for (int i = 0; i < roundArr.length; i++) {
                                Label label = new Label(roundArr[i]);
                                if (i == 0)
                                        label.setMinWidth(120.0);
                                else
                                        label.setMinWidth(100.0);
                                label.setAlignment(Pos.CENTER);
                                top.add(label, i, 0);
                        }

                        //Add to top of the pane
                        top.setStyle(style);
                        top.setLayoutY(-20);
                        roots.get(index).getChildren().remove(top);             //If already there will delete it
                        roots.get(index).getChildren().add(top);
                }
        }

        /**
         * This method creates rounds labels in the 3 added panes.
         * Based on the pane, a string array is used to correctly place each
         * round name in its intended place using widths that correspond to the
         * width of the bracket node below
         * Round names are based on the NCAA March Madness names
         * @author Ariel Liberzon
         */
        private void createRounds() {
                String[] roundArr = {"ROUND1", "ROUND 2", "SWEET 16", "ELITE 8", "FINAL FOUR"};

                for (int i = 0; i < roundArr.length; i++) {
                        Label label = new Label(roundArr[i]);
                        if (i == 0)
                                label.setMinWidth(120.0);
                        else
                                label.setMinWidth(100.0);
                        label.setAlignment(Pos.CENTER);
                        topLeftPane.add(label, i, 0);
                }

                topCenterPane.add(new Label("CHAMPIONSHIP"), 0, 0);
                for (int i = 0; i < roundArr.length; i++) {
                        Label label = new Label(roundArr[roundArr.length - 1 - i]);
                        label.setTextAlignment(TextAlignment.CENTER);
                        if (i == roundArr.length - 1)
                                label.setMinWidth(120.0);
                        else
                                label.setMinWidth(100.0);
                        label.setAlignment(Pos.CENTER);
                        topRightPane.add(label, i, 0);
                }

                fullPane.add(topLeftPane, 0,0);
                fullPane.add(topCenterPane, 1,0);
                fullPane.add(topRightPane, 2,0);

                String style = " -fx-font-family: Futura; -fx-background-color: lightgreen;" +
                        "-fx-text-fill: #18284a; -fx-alignment:center;";
                topLeftPane.setStyle(style);
                topCenterPane.setStyle(style);
                topRightPane.setStyle(style);
        }

        /**
         * Method sets the visible pane on command (Either of the 4 little or the full pane)
         *
         * @param index the pane to set visible
         * @author Samuel Hernandez
         */
        public void setVisiblePane(int index){
                setCenter(null);
                center.add(new ScrollPane(panes.get(buttons.get(index))), 0, 0);
                center.setAlignment(Pos.CENTER);
                setCenter(center);
                roundsForDivsions(index);                       //Add round labels
        }

        /**
         * Gets displayed subtree.
         *
         * @return the displayed subtree
         */
        public int getDisplayedSubtree() {
                return displayedSubtree;
        }

        /**
         * Method to add the triangle in the screen
         * Triangle appears when full bracket is visible only. It is centered to the screen.
         * @author Samuel Hernandez, Harjit Singh, Ariel Liberzon
         * @param finalPane the pane to add the triangles to
         */
        private StackPane createTriangle(Pane finalPane) {
                StackPane stackPane = new StackPane();
                Polygon triangle1 = new Polygon();                              //Create triangles
                Polygon triangle2 = new Polygon();
                triangle1.getPoints().addAll(new Double[]{
                        finalPane.getMinWidth()/ 2.0, 150.0,
                        finalPane.getMinWidth(), 0.0,
                        0.0, 0.0}
                );
                triangle1.setFill(Color.rgb(139, 196, 222));
                triangle2.getPoints().addAll(new Double[]{
                        finalPane.getMinWidth()/ 2.0, 140.0,
                        finalPane.getMinWidth() - 10.0, 0.0,
                        10.0, 0.0}
                );
                triangle2.setFill(Color.rgb(24, 40, 74));

                Label label1 = new Label("\n\n2017 NCAA TOURNAMENT");           //Create text
                Label label2 = new Label("\n\n\nBRACKET");
                String style = " -fx-font-family: Futura; -fx-text-fill: #ffffff; -fx-font-scale: 16;";
                label1.setStyle(style);
                label2.setStyle(style);
                label1.setAlignment(Pos.TOP_CENTER);
                label2.setAlignment(Pos.TOP_CENTER);

                stackPane.getChildren().addAll(triangle1, triangle2 ,label1, label2);
                stackPane.setAlignment(Pos.TOP_CENTER);
                return stackPane;
        }


        /**
         * Helpful method to retrieve our magical numbers
         *
         * @param root the root node (3,4,5,6)
         * @param pos  the position in the tree (8 (16) , 4 (8) , 2 (4) , 1 (2))
         * @return The list representing the valid values.
         */
        public ArrayList<Integer> helper(int root, int pos) {
                ArrayList<Integer> positions = new ArrayList<>();
                int base = 0;
                int tmp = (root * 2) + 1;
                if (pos == 8) base = 3;
                else if (pos == 4) base = 2;
                else if (pos == 2) base = 1;
                for (int i = 0; i < base; i++) tmp = (tmp * 2) + 1;
                for (int j = 0; j < pos * 2; j++) positions.add(tmp + j);
                return positions; //                while ((tmp = ((location * 2) + 1)) <= 127) ;
        }

        /**
         * Sets the current bracket to,
         *
         * @param target The bracket to replace currentBracket
         */
        public void setBracket(Bracket target) {
                currentBracket = target;
        }

        /**
         * Clears the sub tree from,
         *
         * @param position The position to clear after
         */
        public void clearSubtree(int position) {
                currentBracket.resetSubtree(position);
        }

        /**
         * Resets the bracket-display
         */
        public void resetBracket() {
                currentBracket.resetSubtree(0);
        }

        /**
         * Requests a message from current bracket to tell if the bracket
         * has been completed.
         *
         * @return True if completed, false otherwise.
         */
        public boolean isComplete() {
                return currentBracket.isComplete();
        }

        /**
         * Is finalized boolean.
         *
         * @return true if the current-bracket is complete and the value of finalized is also true.
         */
        public boolean isFinalized() {
                return currentBracket.isComplete() && finalized;
        }

        /**
         * Sets finalized.
         *
         * @param isFinalized The value to set finalized to.
         */
        public void setFinalized(boolean isFinalized) {
                finalized = isFinalized && currentBracket.isComplete();
        }

        /**
         * Returns a custom "Button" with specified
         *
         * @param name The name of the button
         * @return pane The stack-pane "button"
         */
        private StackPane customButton(String name) {
                StackPane pane = new StackPane();
                Rectangle r = new Rectangle(100, 50, Color.TRANSPARENT);
                Text t = new Text(name);
                t.setTextAlignment(TextAlignment.CENTER);
                pane.getChildren().addAll(r, t);
                pane.setStyle("-fx-background-color: #8bc4de;");
                return pane;
        }

        /**
         * Create final four pane,
         * the center of the 'Full' bracket.
         * @edited by Justin Valas
         *
         * @return the pane
         */
        public Pane createFinalFour() {
                Pane finalPane = new Pane();
                finalPane.setMinWidth(400.0);
                //Creates the middle three Bracket Nodes:    -JV
                BracketNode nodeFinal0 = new BracketNode("", 175, 272, 70, 20);
                BracketNode nodeFinal1 = new BracketNode("", 70, 380, 75, 20);
                BracketNode nodeFinal2 = new BracketNode("", 260, 480, 80, 20);
                nodeFinal0.setName(currentBracket.getBracket().get(0));
                nodeFinal1.setName(currentBracket.getBracket().get(1));
                nodeFinal2.setName(currentBracket.getBracket().get(2));

                nodeFinal0.setOnMouseClicked(clicked);
                nodeFinal0.setOnMouseEntered(enter);
                nodeFinal0.setOnMouseExited(exit);

                nodeFinal1.setOnMouseClicked(clicked);
                nodeFinal1.setOnMouseEntered(enter);
                nodeFinal1.setOnMouseExited(exit);

                nodeFinal2.setOnMouseClicked(clicked);
                nodeFinal2.setOnMouseEntered(enter);
                nodeFinal2.setOnMouseExited(exit);

                finalPane.getChildren().add(nodeFinal0);
                finalPane.getChildren().add(nodeFinal1);
                finalPane.getChildren().add(nodeFinal2);

                //Creates the box that contains the champion bracket node:    -JV
                Rectangle rect = new Rectangle(100,260,200,40);
                rect.setFill(Color.TRANSPARENT);
                rect.setStroke(Color.BLACK);
                rect.setStrokeWidth(2);
                finalPane.getChildren().add(rect);

                //Creates the label for the champion box:    -JV
                Label lblChamp = new Label("National Champion");
                lblChamp.setStyle("-fx-font: 20px 'Futura'; -fx-text-fill: #16284f");
                lblChamp.setLayoutX(115);
                lblChamp.setLayoutY(305);
                finalPane.getChildren().add(lblChamp);

                //Creates lines for the two finalists nodes:    -JV
                Line line1 = new Line(0,220,0,620);
                Line line2 = new Line(400,220,400,620);
                Line line3 = new Line(0,400,200,400);
                Line line4 = new Line(200,500,400,500);
                finalPane.getChildren().addAll(line1,line2,line3,line4);

                finalPane.getChildren().add(createTriangle(finalPane));

                bracketMap.put(nodeFinal1, 1);
                bracketMap.put(nodeFinal2, 2);
                bracketMap.put(nodeFinal0, 0);
                nodeMap.put(1, nodeFinal1);
                nodeMap.put(2, nodeFinal2);
                nodeMap.put(0, nodeFinal0);
                return finalPane;
        }

        /**
         * Creates the graphical representation of a subtree.
         * Note, this is a vague model. TODO: MAKE MODULAR
         */
        private class Root extends Pane {

                private int location;
                //Samuel Hernandez. Holds the division name (East, West, etc.)
                private Label division;
                //Samuel Hernandez. Holds the number of the root.
                private int number;

                /**
                 * Instantiates a new Root.
                 *
                 * @param location     the location
                 * @param divisionName the division name
                 * @param number       the number
                 */
                public Root(int location, String divisionName, int number) {
                        this.location = location;
                        this.number = number;
                        this.setTranslateY(20);
                        createVertices(420, 200, 100, 20, 0, 0);
                        createVertices(320, 119, 100, 200, 1, 0);
                        createVertices(220, 60, 100, 100, 2, 200);
                        createVertices(120, 35, 100, 50, 4, 100);
                        createVertices(20, 25, 100, 25, 8, 50);
                        for (BracketNode n : nodes) {
                                n.setOnMouseClicked(clicked);
                                n.setOnMouseEntered(enter);
                                n.setOnMouseExited(exit);
                        }
                        division = new Label(divisionName);
                        setTextAndSpace();
                }

                /**
                 * Method sets the name of the division and also ads extra space in the bottom to be fully displayed
                 * in scroll pane.
                 * @author Samuel Hernandez
                 */
                private void setTextAndSpace(){
                        division.setLayoutX(300);
                        division.setLayoutY(190);
                        division.setStyle("-fx-font: 20px futura; -fx-text-fill: #16284f");
                        division.setFont(new Font(20));
                        getChildren().add(division);

                        //Add extra space to visualize complete in scroll panel
                        if(number == 1) {
                                Line l = new Line();
                                l.setStartY(420);
                                l.setStartX(0);
                                l.setEndY(420);
                                l.setEndX(0);
                                getChildren().add(l);
                        }
                }

                /**
                 * The secret sauce... well not really,
                 * Creates 3 lines in appropriate location unless it is the last line.
                 * Adds these lines and "BracketNodes" to the Pane of this inner class
                 */
                private void createVertices(int iX, int iY, int iXO, int iYO, int num, int increment) {
                        int y = iY;
                        if (num == 0 && increment == 0) {
                                BracketNode last = new BracketNode("", iX, y - 20, iXO, 20);
                                nodes.add(last);
                                getChildren().addAll(new Line(iX, iY, iX + iXO, iY), last);
                                last.setName(currentBracket.getBracket().get(location));
                                bracketMap.put(last, location);
                                nodeMap.put(location, last);
                        } else {
                                ArrayList<BracketNode> aNodeList = new ArrayList<>();
                                for (int i = 0; i < num; i++) {
                                        Point2D tl = new Point2D(iX, y);
                                        Point2D tr = new Point2D(iX + iXO, y);
                                        Point2D bl = new Point2D(iX, y + iYO);
                                        Point2D br = new Point2D(iX + iXO, y + iYO);
                                        BracketNode nTop = new BracketNode("", iX, y - 20, iXO, 20);
                                        aNodeList.add(nTop);
                                        nodes.add(nTop);
                                        BracketNode nBottom = new BracketNode("", iX, y + (iYO - 20), iXO, 20);
                                        aNodeList.add(nBottom);
                                        nodes.add(nBottom);
                                        Line top = new Line(tl.getX(), tl.getY(), tr.getX(), tr.getY());
                                        Line bottom = new Line(bl.getX(), bl.getY(), br.getX(), br.getY());
                                        Line right = new Line(tr.getX(), tr.getY(), br.getX(), br.getY());
                                        getChildren().addAll(top, bottom, right, nTop, nBottom);
                                        isTop = !isTop;
                                        y += increment;
                                }
                                ArrayList<Integer> tmpHelp = helper(location, num);
                                for (int j = 0; j < aNodeList.size(); j++) {
                                        aNodeList.get(j).setName(currentBracket.getBracket().get(tmpHelp.get(j)));
                                        bracketMap.put(aNodeList.get(j), tmpHelp.get(j));
                                        nodeMap.put(tmpHelp.get(j), aNodeList.get(j));
                                }
                        }

                }
        }

        /**
         * The BracketNode model for the Graphical display of the "Bracket"
         */
        private class BracketNode extends Pane {
                private String displayName;
                private Rectangle rect;
                private Label name;

                /**
                 * Creates a BracketNode with,
                 *
                 * @param displayName The name if any
                 * @param x           The starting x location
                 * @param y           The starting y location
                 * @param rX          The width of the rectangle to fill pane
                 * @param rY          The height of the rectanglej
                 */
                public BracketNode(String displayName, int x, int y, int rX, int rY) {
                        this.setLayoutX(x);
                        this.setLayoutY(y);
                        this.setMaxSize(rX, rY);
                        this.displayName = displayName;
                        rect = new Rectangle(rX, rY);
                        rect.setFill(Color.TRANSPARENT);
                        name = new Label(displayName);
                        name.setTranslateX(5);
                        name.setStyle("-fx-font-family: Futura; -fx-text-fill: #16284f");
                        getChildren().addAll(name, rect);
                }

                /**
                 * Gets name.
                 *
                 * @return displayName The teams name.
                 */
                public String getName() {
                        return displayName;
                }

                /**
                 * Sets name.
                 *
                 * @param displayName The name to assign to the node.
                 */
                public void setName(String displayName) {
                        this.displayName = displayName;
                        name.setText(displayName);
                }
        }
}
