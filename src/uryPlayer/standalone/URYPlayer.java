/**
 * This file is part of URY Player for Java (Standalone).
 * Copyright (C) 2010 Matt Windsor, URY Computing
 * 
 * URY Player for Java is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the 
 * Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version.
 *
 * URY Player for Java is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General 
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * URY Player for Java.  If not, see <http://www.gnu.org/licences/>.
 */


package uryPlayer.standalone;

import uryPlayer.core.PlayerCore;
import uryPlayer.core.URYStreamPlayer;


/**
 * A standalone streaming program using the URY Java Applet base-code.
 * 
 * @author Matt Windsor
 *
 */

public class URYPlayer implements URYStreamPlayer
{
  boolean running;
  PlayerCore player;
  UserInterface ui;

  public
  URYPlayer ()
  {
    running = false;
    player = null;
    ui = null;
  }

  
  /**
   * The main function of the program.
   * 
   * @param args  The arguments to pass to the URY player.
   */
  
  public static void
  main (String[] args)
  {
    URYPlayer app = new URYPlayer ();
    app.init ();
  }

  
  /**
   * Initialise the URY player.
   */
  
  private void
  init ()
  {
    player = new PlayerCore (PlayerCore.HIGH_STREAM);
    ui = new UserInterface (this);
    ui.createUserInterface ();
  }

  
  /**
   * Start the attached player, if it is running.
   */
  
  public void
  startPlayer ()
  {
    if (player != null)
      player.start ();
  }
  
  
  /**
   * Stop the attached player, if it is running.
   */
  
  public void
  stopPlayer ()
  {
    if (player != null)
      player.stop ();
  }
  
  
  /**
   * Get whether or not the PlayerCore should be running.
   * 
   * @return  true if the player is expected to be running, false otherwise.
   */
  
  public boolean 
  isRunning ()
  {
    if (player != null)
      return player.isRunning ();
    else
      return false;
  }

  
  /**
   * Change the URL of the stream the player is streaming from.
   * 
   * @param newURL  The new stream URL.
   */
    
  public void
  changePlayerURL (String newURL)
  {
    if (player != null)
      player.changeURL (newURL);
  }
}