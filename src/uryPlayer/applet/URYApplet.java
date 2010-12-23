/**
 * This file is part of URY Player for Java (Applet).
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

package uryPlayer.applet;
import java.applet.Applet;
import java.awt.HeadlessException;

import uryPlayer.core.PlayerCore;
import uryPlayer.core.URYStreamPlayer;


/**
 * A simple applet for streaming from the University Radio York MP3 streams.
 * 
 * This applet does not provide a user interface - it is expected that any 
 * web pages using this applet will provide their own UI in JavaScript etc.
 * 
 * @author Matt Windsor
 *
 */

public class URYApplet extends Applet implements URYStreamPlayer
{ 
  /**
   * Version string of some sort.
   */
  
  private static final long serialVersionUID = -3710033878028709433L;

  PlayerCore player;
  
  
  /**
   * Creates a new URY player applet.
   * 
   * @throws HeadlessException
   */
  
  public
  URYApplet () throws HeadlessException
  {
    player = new PlayerCore (PlayerCore.HIGH_STREAM);
  }

  
  /**
   * Applet start function.
   */
  
  public void
  init ()
  {
    startPlayer ();
  }
  
  
  /**
   * Applet stop function.
   */
  
  public void
  stop ()
  {
    stopPlayer ();
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
   * @param newURL  The new stream URL.
   */
    
  public void
  changePlayerURL (String newURL)
  {
    if (player != null)
      player.changeURL (newURL);
  }
}
