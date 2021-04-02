//package marchmadness;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.swing.*;

/**
 *  MarchMadnessGUI
 * 
 * this class contains the buttons the user interacts
 * with and controls the actions of other objects 
 *
 * @author Grant Osborn
 */
public class MarchMadnessGUI extends Application {


    //all the gui elements
    private BorderPane root;
    private ToolBar toolBar;
    private ToolBar btoolBar;
    private Button simulate;
    private Button logout;
    private Button scoreBoardButton;
    private Button viewBracketButton;
    private static Button clearButton;
    private Button resetButton;
    private Button finalizeButton;
    private Button help;
    private Button quit;
    private Button viewMine;
    private  Image img1;
    //allows you to navigate back to division selection screen
    private Button back;
    private  Bracket startingBracket;
    //reference to currently logged in bracket
    private Bracket selectedBracket;
    private Bracket simResultBracket;
    private ArrayList<Bracket> playerBrackets;
    private HashMap<String, Bracket> playerMap;
    private ScoreBoardTable scoreBoard;
    private TableView table;
    private BracketPane bracketPane;
    private GridPane loginP;
    private TournamentInfo teamInfo;
    private final StackPane firstscene = new StackPane();


    @Override
    public void start(Stage primaryStage) {
        //try to load all the files, if there is an error display it
        try{
            teamInfo=new TournamentInfo();
            startingBracket= new Bracket(teamInfo.loadStartingBracket());
            simResultBracket=new Bracket(teamInfo.loadStartingBracket());
        } catch (IOException ex) {
            showError(new Exception("Can't find "+ex.getMessage(),ex),true);
        }
        //deserialize stored brackets
        playerBrackets = loadBrackets();

        playerMap = new HashMap<>();
        addAllToMap();



        //the main layout container
        root = new BorderPane();
        Group root1 = new Group();
        scoreBoard= new ScoreBoardTable();
        table=scoreBoard.start();
        loginP=createLogin();
        CreateToolBars();
        Image img = new Image("ncaa.jpg");
        root.setBackground(new Background(new BackgroundImage(img,BackgroundRepeat.REPEAT,
                BackgroundRepeat.REPEAT,
                BackgroundPosition.CENTER,
                BackgroundSize.DEFAULT)));
        //display login screen
        login();

        setActions();
        root.setTop(toolBar);

        BorderPane.setAlignment(btoolBar, Pos.CENTER);
        root.setBottom(btoolBar);
        Scene scene = new Scene(root);
        primaryStage.setMaximized(true);

        primaryStage.setTitle("March Madness Bracket Simulator");

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }



    /**
     * simulates the tournament  
     * simulation happens only once and
     * after the simulation no more users can login
     */
    private void simulate(){
        //cant login and restart prog after simulate
        simulate.setDisable(true);
        logout.setDisable(false);

        scoreBoardButton.setDisable(false);
        viewBracketButton.setDisable(false);
        viewMine.setDisable(false);


        teamInfo.simulate(simResultBracket);
        for(Bracket b:playerBrackets){
            scoreBoard.addPlayer(b,b.scoreBracket(simResultBracket));
        }

        displayPane(table);
    }

    /**
     * Displays the login screen
     *
     */
    private void login(){
        logout.setDisable(true);
        simulate.setDisable(true);
        scoreBoardButton.setDisable(true);
        viewBracketButton.setDisable(true);
        viewMine.setDisable(true);
        btoolBar.setDisable(true);
        clearButton.setDisable(true);
        displayPane(loginP);
    }
    private void logout() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(" Logout");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to logout?");

        alert.showAndWait();

        logout.setDisable(true);
        simulate.setDisable(true);
        scoreBoardButton.setDisable(true);
        viewBracketButton.setDisable(true);
        viewMine.setDisable(true);
        clearButton.setDisable(true);
        btoolBar.setDisable(true);
        displayPane(loginP);
    }

    /**
     * Displays the score board
     *
     */
    private void scoreBoard(){
        displayPane(table);
    }

    /**
     * Displays Simulated Bracket
     *
     */
    private void viewBracket(){
        bracketPane=new BracketPane(simResultBracket);
        GridPane full = bracketPane.getFullPane();
        //full.setAlignment(Pos.CENTER);
        full.setDisable(true);
        displayPane(new ScrollPane(full));;
    }

    /**
     * allows user to choose bracket
     *
     */
    private void chooseBracket(){
        //login.setDisable(true);
        btoolBar.setDisable(false);
        logout.setDisable(false);
        bracketPane=new BracketPane(selectedBracket);
        displayPane(bracketPane);

    }
    /**
     * resets current selected sub tree
     * for final4 reset Ro2 and winner
     */
    private void clear(){
        int visible = bracketPane.getDisplayedSubtree() - 3;
        bracketPane.clear();
        bracketPane=new BracketPane(selectedBracket);
        displayPane(bracketPane);

        //Samuel Hernandez: Added functionality to not be kicked out of division when clearing.
        bracketPane.setVisiblePane(visible);

    }

    /**
     * resets entire bracket
     */
    private void reset(){
        if(confirmReset()){
            //horrible hack to reset
            selectedBracket=new Bracket(startingBracket);
            bracketPane=new BracketPane(selectedBracket);
            displayPane(bracketPane);
        }
    }

    private void help(){
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(" Help");
        alert.setHeaderText(null);
        alert.setContentText("User can select each bracket and choose the team " + "\n"
                + "\n" + " click clear to undo the last change "+ "\n" +
                "\n"+" click on reset to reset all the brackets "+
                "\n"+" click finalize to submit the brackets ");

        alert.showAndWait();
    }


    private void close(){
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure want to Exit?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.CANCEL){
            // ... user chose OK
        }
            // ... user chose CANCEL or closed the dialog
        else
            System.exit(0);
    }

    private void finalizeBracket(){
        if(bracketPane.isComplete()){
            btoolBar.setDisable(true);
            bracketPane.setDisable(true);
            logout.setDisable(false);
            simulate.setDisable(false);
            //save the bracket along with account info
            seralizeBracket(selectedBracket);

        }else{
            infoAlert("You can only finalize a bracket once it has been completed.");
            //go back to bracket section selection screen
            // bracketPane=new BracketPane(selectedBracket);
            displayPane(bracketPane);

        }
        //bracketPane=new BracketPane(selectedBracket);



    }


    /**
     * displays element in the center of the screen
     *
     * @param p must use a subclass of Pane for layout. 
     * to be properly center aligned in  the parent node
     */
    private void displayPane(Node p){
        root.setCenter(p);
        BorderPane.setAlignment(p,Pos.CENTER);
    }

    /**
     * Creates toolBar and buttons.
     * adds buttons to the toolbar and saves global references to them
     */
    private void CreateToolBars(){
        toolBar  = new ToolBar();
        btoolBar  = new ToolBar();
        logout=new Button("Logout");
        simulate=new Button("Simulate");
        scoreBoardButton=new Button("ScoreBoard");
        viewBracketButton= new Button("View Simulated Bracket");
        viewMine= new Button("View My Bracket");
        clearButton=new Button("Clear");
        resetButton=new Button("Reset");
        finalizeButton=new Button("Finalize");
        back=new Button("Choose Division");
        img1 = new Image("about.png");
        help =new Button("Help",new ImageView(img1));
        quit =new Button("Quit");
        toolBar.getItems().addAll(
                createSpacer(),
                logout,
                simulate,
                scoreBoardButton,
                viewBracketButton,
                viewMine,
                createSpacer(),
                help
        );
        btoolBar.getItems().addAll(
                createSpacer(),
                clearButton,
                resetButton,
                finalizeButton,
                back,
                createSpacer(),
                quit
        );
    }

    /**
     * sets the actions for each button
     */
    private void setActions(){
        logout.setOnAction(e->logout());
        simulate.setOnAction(e->simulate());
        scoreBoardButton.setOnAction(e->scoreBoard());
        viewMine.setOnAction(e -> viewMine());
        viewBracketButton.setOnAction(e->viewBracket());
        clearButton.setOnAction(e->clear());
        resetButton.setOnAction(e->reset());
        finalizeButton.setOnAction(e->finalizeBracket());
        help.setOnAction(e->help());
        quit.setOnAction(e->close());
        back.setOnAction(e->{
            bracketPane=new BracketPane(selectedBracket);
            displayPane(bracketPane);
            clearButton.setDisable(true);
        });
    }

    /**
     * Method to allow user to see his/her prediction of the tournament and not only the results
     * @author Samuel Hernandez
     */
    private void viewMine(){
        bracketPane = new BracketPane(selectedBracket);
        GridPane full = bracketPane.getFullPane();
        full.setAlignment(Pos.CENTER);
        full.setDisable(true);
        displayPane(new ScrollPane(full));
    }

    /**
     * Gets clear button so that bracket pane can enable or disable the clear button depending on what
     * section the user chooses.
     * @author Samuel Hernandez
     */
    public static Button getButton(){
        return clearButton;
    }

    /**
     * Creates a spacer for centering buttons in a ToolBar
     */
    private Pane createSpacer(){
        Pane spacer = new Pane();
        HBox.setHgrow(spacer,Priority.SOMETIMES);
        return spacer;
    }


    private GridPane createLogin(){


        /*
        LoginPane
        Modified by Alexander
         */

        GridPane loginPane = new GridPane();


        loginPane.setAlignment(Pos.CENTER);
        loginPane.setHgap(10);
        loginPane.setVgap(10);
        loginPane.setPadding(new Insets(5, 5, 5, 5));

        Text welcomeMessage = new Text("March Madness Login Welcome");
        welcomeMessage.setFill(Color.WHITE);
        loginPane.add(welcomeMessage, 0, 0, 2, 1);

        Label userName = new Label("User Name: ");
        userName.setTextFill(Color.WHITE);
        loginPane.add(userName, 0, 1);

        TextField enterUser = new TextField();
        enterUser.setPromptText("Username");
        loginPane.add(enterUser, 1, 1);

        Label password = new Label("Password: ");
        password.setTextFill(Color.WHITE);
        loginPane.add(password, 0, 2);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        loginPane.add(passwordField, 1, 2);

        Label confirmPassword = new Label("Confirm Password: ");
        confirmPassword.setTextFill(Color.WHITE);
        loginPane.add(confirmPassword, 0, 3);

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");
        loginPane.add(confirmPasswordField, 1, 3);

        Button signButton = new Button("Sign in");
        loginPane.add(signButton, 1, 4);
        signButton.setDefaultButton(true);//added by matt 5/7, lets you use sign in button by pressing enter

        Label message = new Label();
        loginPane.add(message, 1, 5);


        signButton.setOnAction(event -> {

            // the username user entered
            String username = enterUser.getText().toLowerCase();
            // the password user enter
            String playerPass = passwordField.getText();


            //Check to see if user already exists
            if (playerMap.get(username) != null) {
                //check password of user

                Bracket tmpBracket = this.playerMap.get(username);

                String password1 = tmpBracket.getPassword();

                if (Objects.equals(password1, playerPass)) {
                    // load bracket
                    selectedBracket=playerMap.get(username);
                    chooseBracket();
                }else{
                    infoAlert("The password you have entered is incorrect!");
                }
                //Create a new user using the information provided
            } else {
                //check for empty fields
                if(!username.equals("")&&!playerPass.equals("")){
                    //Only create the new user if the confirm password input field matches the password field
                    if(playerPass.equals(confirmPasswordField.getText())) {
                        //create new bracket
                        Bracket tmpPlayerBracket = new Bracket(startingBracket, username);
                        playerBrackets.add(tmpPlayerBracket);
                        tmpPlayerBracket.setPassword(playerPass);

                        playerMap.put(username, tmpPlayerBracket);
                        selectedBracket = tmpPlayerBracket;
                        //alert user that an account has been created
                        infoAlert("No user with the Username \"" + username + "\" exists. A new account has been created.");
                        chooseBracket();
                    }
                    else{
                        infoAlert("Please confirm your password in the confirm password text field.");
                    }

                }
            }
        });

        return loginPane;
    }

    /**
     * addAllToMap
     * adds all the brackets to the map for login
     */
    private void addAllToMap(){
        for(Bracket b:playerBrackets){
            playerMap.put(b.getPlayerName(), b);
        }
    }

    /**
     * The Exception handler
     * Displays a error message to the user
     * and if the error is bad enough closes the program
     * @param fatal true if the program should exit. false otherwise
     */
    private void showError(Exception e,boolean fatal){
        String msg=e.getMessage();
        if(fatal){
            msg=msg+" \n\nthe program will now close";
            //e.printStackTrace();
        }
        Alert alert = new Alert(AlertType.ERROR,msg);
        alert.setResizable(true);
        alert.getDialogPane().setMinWidth(420);
        alert.setTitle("Error");
        alert.setHeaderText("something went wrong");
        alert.showAndWait();
        if(fatal){
            System.exit(666);
        }
    }

    /**
     * alerts user to the result of their actions in the login pane
     * @param msg the message to be displayed to the user
     */
    private void infoAlert(String msg){
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("March Madness Bracket Simulator");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * Prompts the user to confirm that they want
     * to clear all predictions from their bracket
     * @return true if the yes button clicked, false otherwise
     */
    private boolean confirmReset(){
        Alert alert = new Alert(AlertType.CONFIRMATION,
                "Are you sure you want to reset the ENTIRE bracket?",
                ButtonType.YES,  ButtonType.CANCEL);
        alert.setTitle("March Madness Bracket Simulator");
        alert.setHeaderText(null);
        alert.showAndWait();
        return alert.getResult()==ButtonType.YES;
    }


    /**
     * Tayon Watson 5/5
     * seralizedBracket
     * @param B The bracket the is going to be seralized
     */
    private void seralizeBracket(Bracket B){
        FileOutputStream outStream = null;
        ObjectOutputStream out = null;
        try
        {
            outStream = new FileOutputStream(B.getPlayerName()+".ser");
            out = new ObjectOutputStream(outStream);
            out.writeObject(B);
            out.close();
        }
        catch(IOException e)
        {
            // Grant osborn 5/6 hopefully this never happens
            showError(new Exception("Error saving bracket \n"+e.getMessage(),e),false);
        }
    }
    /**
     * Tayon Watson 5/5
     * deseralizedBracket
     * @param filename of the seralized bracket file
     * @return deserialized bracket
     */
    private Bracket deseralizeBracket(String filename){
        Bracket bracket = null;
        FileInputStream inStream = null;
        ObjectInputStream in = null;
        try
        {
            inStream = new FileInputStream(filename);
            in = new ObjectInputStream(inStream);
            bracket = (Bracket) in.readObject();
            in.close();
        }catch (IOException | ClassNotFoundException e) {
            // Grant osborn 5/6 hopefully this never happens either
            showError(new Exception("Error loading bracket \n"+e.getMessage(),e),false);
        }
        return bracket;
    }

    /**
     * Tayon Watson 5/5
     * deseralizedBracket
     * @return deserialized bracket
     */
    private ArrayList<Bracket> loadBrackets()
    {
        ArrayList<Bracket> list=new ArrayList<Bracket>();
        File dir = new File(".");
        for (final File fileEntry : dir.listFiles()){
            String fileName = fileEntry.getName();
            String extension = fileName.substring(fileName.lastIndexOf(".")+1);

            if (extension.equals("ser")){
                list.add(deseralizeBracket(fileName));
            }
        }
        return list;
    }

}