import javax.swing.text.html.ImageView;
import java.awt.*;

/**
 * 
 * @author Shivanie and Tyler
 * @description 
 *       Team class holds all the information for individual teams. Their displayName, info, and ranking.
 *       
 */

public class Team{

  private String fullName;
  private String displayName;
  private String nickname;
  private String info;
  private int ranking;
  private String logoRef;
  public double offensePPG;
  public double defensePPG;
  private ImageView logo;
  
  /**
   * Constructor
   * @param displayName 
   *        The displayName of the team
   * @param info
   * 		A short description of the team
   * @param ranking
   * 		The ranking in the team region from 1 to 16
   */
  public Team(String displayName, String nickname, String info, int ranking,
              double oPPG, double dPPG, String logoRef, String fullname){
    this.displayName = displayName;
    this.nickname = nickname;
    this.info = info;
    this.ranking = ranking;
    offensePPG = oPPG;
    defensePPG = dPPG;
    this.logoRef = logoRef;
    this.fullName = fullname;

  }

  /**
   * 
   * @return displayName the displayName of the team
   */
  public String getDisplayName(){
    return displayName;
  }
  
  /**
   * 
   * @return nickname the mascot of the team
   */
  public String getNickname(){
	  return nickname;
  }
  
  /**
   * 
   * @return info a short description of the team
   */
  public String getInfo(){
    return info;
  }
  
  /**
   * 
   * @return ranking the ranking from 1 - 16
   */
  public int getRanking(){
    return ranking;
  }
  
  /**
   * 
   * @return offensePPG the average points per game for offense
   */
  public double getOffensePPG(){
    return offensePPG;
  }
  
  /**
   * 
   * @return defensePPG
   */
  public double getDefensePPG(){
    return defensePPG;
  }

  /**
   *
   * @return fullName
   */
  public String getFullName() {
    return fullName;
  }

  /**
   * 
   * @param info 
   * 		The short description of the team
   */
  public void setInfo(String info){
    this.info = info;
  }
  
  /**
   * 
   * @param newNickname the new nickname for a team
   */
  public void setNickname(String newNickname){
	  nickname = newNickname;
  }
  
  /**
   * 
   * @param ranking
   * 		The ranking from 1 to 16
   */
  public void setRanking(int ranking){
    this.ranking = ranking;
  }
  
  /**
   * 
   * @param newDefense The new points per game for defense
   */
  public void setDefense(double newDefense){
	  defensePPG = newDefense;
  }
  
  /**
   * 
   * @param newOffense the new points per game for offense.
   */
  public void setOffense(double newOffense){
	  offensePPG =  newOffense;
  }

  public String getLogoRef() {
    return logoRef;
  }
}