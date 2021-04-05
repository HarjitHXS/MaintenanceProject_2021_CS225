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
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * MarchMadnessGUI
 *
 * This application provides users the chance to predict the outcome of the NCAA tournament.
 * Users are able to login into the application place their predictions through this simple GUI that allows them to
 * decide which teams will win matches and move on to next rounds.
 * After the user completes a bracket the program is capable of simulating the tournament.
 * At the end the user's prediction gets graded depending on how well the user predicted the outcome of the games.
 * Also, to make it more fun the user is able to see how many points he or she scored
 * and also how many points other users got.
 *
 * This class contains the buttons the user interacts
 * with and controls the actions of other objects.
 *
 * @author Grant Osborn
 * Editors: Ariel Liberzon, Justin Valas, Harjit Singh, Alexander Tang and Samuel Hernandez
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
    private Image img1;
    private Button back;
    private Bracket startingBracket;
    private Bracket selectedBracket;
    private Bracket simResultBracket;
    private ArrayList<Bracket> playerBrackets;
    private HashMap<String, Bracket> playerMap;
    private ScoreBoardTable scoreBoard;
    private TableView table;
    private BracketPane bracketPane;
    private GridPane loginP;
    private TournamentInfo teamInfo;

    /**
     * Method initializes the GUI and initializes basic components for the application to run.
     * @param primaryStage the stage to start
     */
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

        //Harjit Singh: ToolTip and Background Image.
        try {
            Image img = new Image("ncaa.jpg");
            root.setBackground(new Background(new BackgroundImage(img, BackgroundRepeat.REPEAT,
                    BackgroundRepeat.REPEAT,
                    BackgroundPosition.CENTER,
                    BackgroundSize.DEFAULT)));
        }
        catch (Exception e) {
            showError(new Exception("Can't find "+e.getMessage(),e),true);
        }
        simulate.setTooltip(new Tooltip("view the simulate"));
        logout.setTooltip(new Tooltip("Click here to Logout"));
        scoreBoardButton.setTooltip(new Tooltip("Click here to ScoreBoard"));
        viewBracketButton.setTooltip(new Tooltip("Click here to viewBracket"));
        clearButton.setTooltip(new Tooltip("Click here to clear "));
        resetButton.setTooltip(new Tooltip("Click here to reset all Brackets"));
        finalizeButton.setTooltip(new Tooltip("Click here to Finalize"));
        help.setTooltip(new Tooltip("Click here to Help"));
        quit.setTooltip(new Tooltip("Click here to Quit"));
        viewMine.setTooltip(new Tooltip("Click here to see your Bracket"));



        //display login screen

        login();
        setActions();
        root.setTop(toolBar);

        BorderPane.setAlignment(btoolBar, Pos.CENTER);
        root.setBottom(btoolBar);
        Scene scene = new Scene(root);
        primaryStage.setMaximized(true);

        primaryStage.setTitle("March Madness Bracket Simulator");
        try {
            primaryStage.getIcons().add(new Image(this.getClass().getResource("logo.png").toString()));
        }catch (Exception e) {
            showError(new Exception("Can't find "+e.getMessage(),e),true);
        }
        primaryStage.setScene(scene);
        primaryStage.show();
        welcomeMsg();
    }

    /**
     * Launches the GUI
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Creates a pop up window upon startup
     * @author Alex, 04/04/2021
     * @editor by Harjit singh
     */
    private Alert welcomeMsg() {
        Alert alert = new Alert(AlertType.INFORMATION, "Welcome to the March Madness Simulator Game.");
        alert.setTitle("Welcome");
        alert.setHeaderText("March Madness Simulator");
        try {
            // Create the ImageView we want to use for the icon
            ImageView icon = new ImageView("logo.png");

            // The standard Alert icon size is 48x48, so let's resize our icon to match
            icon.setFitHeight(48);
            icon.setFitWidth(48);

            // Set our new ImageView as the alert's icon
            alert.getDialogPane().setGraphic(icon);
        }
        catch (Exception e) {
            showError(new Exception("Can't find "+e.getMessage(),e),true);
        }
        alert.show();
        return alert;
    }



    /**
     * This method simulates the tournament, it also enables the proper buttons and components.
     * It grades the brackets depending on correct predictions.
     */
    private void simulate() {
        //cant login and restart program after simulate
        simulate.setDisable(true);
        logout.setDisable(false);
        quit.setDisable(false);
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
     * Displays the login screen in order to allow users to login to the application
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

    /**
     * @author Harjit Singh, 04/04/2021
     * Displays Alert message to logout
     * Displays the login screen
     */
    private void logout() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure want to Logout?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.CANCEL){
            // ... user chose OK
        }
        // ... user chose CANCEL or closed the dialog
        else
            login();

    }
    /**
     * Method displays the scoreboard. In the score board the users can see how many points they got
     * based on their predictions.
     */
    private void scoreBoard(){
        displayPane(table);
    }

    /**
     * This method is called when the view simulated bracket button is pressed and lets the user see what was
     * the result of the simulated tournament.
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
     * This method is called after logging in to allow user to graphically select a division or the full bracket.
     */
    private void chooseBracket(){
        //login.setDisable(true);
        btoolBar.setDisable(false);
        logout.setDisable(false);
        bracketPane=new BracketPane(selectedBracket);
        displayPane(bracketPane);
    }

    /**
     * Resets the division the user is working in (East, West etc.) clearing all choices in that division.
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
     * Resets entire bracket
     */
    private void reset(){
        if(confirmReset()){
            //horrible hack to reset
            selectedBracket=new Bracket(startingBracket);
            bracketPane=new BracketPane(selectedBracket);
            displayPane(bracketPane);
        }
    }

    /**
     * @author Harjit Singh, 04/04/2021
     * Displays Alert message to Help
     */

    private Alert help(){
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Help");
        alert.setHeaderText(null);
        alert.setContentText("-Click on which team you think would win the match" +
                              "\n-Click clear to undo the last change" +
                              "\n-Click on reset to reset all the brackets" +
                              "\n-Click finalize to submit the brackets" +
                              "\n-Hover over teams to view their information");
        try {
            ImageView icon = new ImageView("logo.png");

            // The standard Alert icon size is 48x48, so let's resize our icon to match
            icon.setFitHeight(48);
            icon.setFitWidth(48);

            // Set our new ImageView as the alert's icon
            alert.getDialogPane().setGraphic(icon);
        }
        catch (Exception e) {
            showError(new Exception("Can't find "+e.getMessage(),e),true);
        }

        alert.show();
        return alert;
    }

    /**
     * @author Harjit Singh, 04/04/2021
     * Displays Alert message to Quit OK or Cancel
     * Ok to exit the window
     * Cancel to stay on Brackets
     */

    private void close(){
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(" Quit ");
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

    /**
     * This method allows the user to finalize the bracket, meaning they are done with their prediction allowing them
     * to simulate the tournament and view their results.
     */
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
            displayPane(bracketPane);

        }
    }


    /**
     * Displays element in the center of the screen
     * @param p must use a subclass of Pane for layout. 
     * to be properly center aligned in  the parent node
     */
    private void displayPane(Node p){
        root.setCenter(p);
        BorderPane.setAlignment(p,Pos.CENTER);
    }

    /**
     * Creates toolBar and buttons.
     * @editor by Harjit Singh
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
        try {
            img1 = new Image("about.png");
            help = new Button("Help", new ImageView(img1));
        } catch (Exception e){
            showError(new Exception("Can't find "+e.getMessage(),e),true);
        }

        quit =new Button("Quit");
        toolBar.getItems().addAll(
                createSpacer(),
                simulate,
                scoreBoardButton,
                viewBracketButton,
                viewMine,
                logout,
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
     * Sets the actions for each button
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
     * Method to allow user to see his/her prediction of the tournament to be able to compare with the simulation
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
     * @return Clear Button
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

    /**
     * Creates a pane with login functionality
     * @return the pane with the login prompt
     */
    private GridPane createLogin(){


        /*
        LoginPane
        @editor by Alexander
         */

        GridPane loginPane = new GridPane();


        loginPane.setAlignment(Pos.CENTER);
        loginPane.setHgap(10);
        loginPane.setVgap(10);
        loginPane.setPadding(new Insets(5, 5, 5, 5));

        Text welcomeMessage = new Text("March Madness Bracket Simulator");
        welcomeMessage.setFill(Color.WHITE);
        loginPane.add(welcomeMessage, 0, 0, 2, 1);

        Label userName = new Label("Username: ");
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

        Label confirmPassword = new Label("Confirm Password:");
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
                    if(playerPass.equals(confirmPasswordField.getText())) {
                        selectedBracket = playerMap.get(username);
                        chooseBracket();
                    }
                    else
                        infoAlert("Passwords do not match! Please try again.");
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
     * Adds all the brackets to the map for login
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
     * Alerts user to the result of their actions in the login pane
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
     * Serializes a bracket
     * @author Tayon Watson 5/5
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
     * De-seralizes a Bracket
     * @param filename of the seralized bracket file
     * @return deserialized bracket
     * @author Tayon Watson 5/5
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
     *  Loads the brackets
     *  @return an array list of brackets
     *  @author Tayon Watson 5/5
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