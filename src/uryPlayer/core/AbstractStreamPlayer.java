/**
 * 
 */
package uryPlayer.core;


/**
 * A partial implementation of the URYStreamPlayer interface.
 * 
 * The stream player hosts a PlayerCore, which is responsible for the actual
 * stream playback, and provides an interface to let the PlayerCore know when 
 * to stop playing.
 * 
 * @author Matt Windsor
 */

public abstract class AbstractStreamPlayer implements URYStreamPlayer
{
  private PlayerCore player;
  
  /**
   * Creates a new URY stream player.
   */
  
  public
  AbstractStreamPlayer ()
  {
    player = new PlayerCore (PlayerCore.HIGH_STREAM);
  }
  
  
  /**
   * @see uryPlayer.core.URYStreamPlayer#isRunning()
   */
  
  @Override
  public boolean 
  isRunning ()
  {
    if (player != null)
      return player.isRunning ();
    else
      return false;
  }  

  
  /**
   * @see uryPlayer.core.URYStreamPlayer#startPlayer()
   */
  
  @Override
  public boolean
  startPlayer ()
  {
    if (player == null)
      return false;
    else
      {
        player.start ();
        return true;
      }
  }

  
  /**
   * @see uryPlayer.core.URYStreamPlayer#stopPlayer()
   */
  
  @Override
  public boolean
  stopPlayer ()
  {
    if (player == null)
      return false;
    else
      {
        player.stop ();
        return true;
      }
  }

  
  /**
   * @see uryPlayer.core.URYStreamPlayer#getVolume()
   */
  
  @Override
  public float
  getVolume ()
  {
    return player.getVolume ();
  }

  
  /**
   * @see uryPlayer.core.URYStreamPlayer#setVolume(double)
   */
  
  @Override
  public boolean
  setVolume (float volume)
  {
    if (player == null)
      return false;
    else
      return player.setVolume (volume);
  }

  
  /**
   * @see uryPlayer.core.URYStreamPlayer#getStream()
   */
  
  @Override
  public String
  getStream ()
  {
    if (player != null)
      return player.getURL ();
    else
      return null;
  }

  
  /**
   * @see uryPlayer.core.URYStreamPlayer#setStream(java.lang.String)
   */
  
  @Override
  public boolean
  setStream (String newURL)
  {
    if (player != null)
      return player.setURL (newURL);
    else
      return false;
  }

}
