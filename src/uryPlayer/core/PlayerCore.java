/**
 * This file is part of URY Player for Java.
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

package uryPlayer.core;
import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import javazoom.spi.mpeg.sampled.convert.MpegFormatConversionProvider;
import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;


/**
 * A URYApplet player thread.
 * 
 * This is responsible for the actual playback from the stream.  In general 
 * usage, the PlayerCore will be dispatched by another class.
 * 
 * @author Matt Windsor
 *
 */

public class PlayerCore implements Runnable
{
  /**
   * The prefix for all stream URLs.
   */
  
  public static final String PREFIX = "http://ury.york.ac.uk/audio/";
  
  
  /**
   * The high-quality URY stream URL.
   */
  
  public static final String HIGH_STREAM = PREFIX + "live-high";
  
  
  /**
   * The low-quality URY stream URL.
   */
  
  public static final String LOW_STREAM = PREFIX + "live-low";
  
  
  /**
   * The mobile-quality (ultra-low) URY stream URL.
   */
  
  public static final String MOBILE_STREAM = PREFIX + "live-mobile";

  private String streamURL;
  private volatile Thread thread;
  
  
  /**
   *  Create a new PlayerCore.
   *  
   *  @param inParent  The URYApplet controlling this PlayerCore.
   *  @param inURL     The stream URL to connect to.
   */
  
  public
  PlayerCore (String inURL)
  {
    super ();
    
    streamURL = inURL;
    thread = null;
  }

  
  /**
   * Start the PlayerCore, if it has not already been started.
   */
  
  public void
  start ()
  {
    if (thread == null)
      {      
        thread = new Thread (this);
        thread.start ();
      }
  }
  
  
  /**
   * Stop the PlayerCore, if it has been started.
   */
  
  public void
  stop ()
  {
    if (thread != null)
      { 
        Thread temp = thread;
        thread = null;
        temp.interrupt ();
        
        try
          {
            temp.join ();
          }
        catch (InterruptedException e)
          {
            // TODO Auto-generated catch block
            e.printStackTrace ();
          }
      }
  }
  
  /**
   * The main body of thread execution.
   */
  
  public void
  run ()
  {
    streamPlay (streamURL);
  }

  /**
   * Changes the URL used by the PlayerCore.
   * 
   * This may only be done when the PlayerCore is not running.
   * 
   * @return  true if the URL was changed, false otherwise.
   */
  
  public boolean
  changeURL (String newURL)
  {
    if (thread == null)
      {
        streamURL = newURL;
        return true;
      }
    else
      return false;
  }
  
  /**
   * Play from a stream until the stream terminates or the PlayerCore is 
   * instructed to stop.
   * 
   * @param inURL  The Uniform Resource Locator for the stream to be played.
   */
  
  public void
  streamPlay (String inURL)
  {
    AudioInputStream in = null;
    
    try
      {
        in = new MpegAudioFileReader ().getAudioInputStream (new URL (inURL));
      }
    catch (UnsupportedAudioFileException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace ();
      }
    catch (IOException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace ();
      }
      
    AudioInputStream din = null;
    AudioFormat baseFormat = in.getFormat ();
    AudioFormat decodedFormat = new AudioFormat (AudioFormat.Encoding.PCM_SIGNED,
                                                 baseFormat.getSampleRate (),
                                                 16,
                                                 baseFormat.getChannels (),
                                                 baseFormat.getChannels () * 2,
                                                 baseFormat.getSampleRate (),
                                                 false);
    din = new MpegFormatConversionProvider ().getAudioInputStream (decodedFormat, in);
      
    // Play now.
      
    try
      {
        rawPlay (decodedFormat, din);
      }
    catch (IOException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace ();
      }
    catch (LineUnavailableException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace ();
      }
      
    // Now try to close the stream.
      
    try
      {
        in.close ();
      }
    catch (IOException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace ();
      }
  }

  
  /**
   * Play the decoded stream until the stream terminates or the PlayerCore is 
   * instructed to stop.
   * 
   * @param targetFormat  The target format of the stream.
   * @param din           The input stream.
   * @throws IOException
   * @throws LineUnavailableException
   */
  
  private void
  rawPlay (AudioFormat targetFormat, AudioInputStream din) throws IOException, LineUnavailableException
  {
    Thread thisThread = Thread.currentThread ();
    
    byte[] data = new byte[4096];
    
    SourceDataLine line = getLine (targetFormat);
    
    if (line != null)
      {
        line.start ();
        int nBytesRead = 0;
        
        // nBytesRead == -1 implies end of file, so terminate when this occurs.
        
        while (nBytesRead != -1 && thread == thisThread)
          {
            nBytesRead = din.read (data, 0, data.length);
            
            if (nBytesRead != -1)
              line.write (data, 0, nBytesRead);
          }

        // Stop
        
        //line.drain (); <-- Causes freezes
        line.stop ();
        line.close ();
        din.close ();
      }
  }

  
  /**
   * Get a data line from the sound system on which to play the stream.
   * 
   * @param audioFormat  The desired audio format.
   * @return  A line on which to play the stream.
   * @throws LineUnavailableException
   */
  
  private SourceDataLine
  getLine (AudioFormat audioFormat) throws LineUnavailableException
  {
    SourceDataLine res = null;
    DataLine.Info info = new DataLine.Info (SourceDataLine.class, audioFormat);
    res = (SourceDataLine) AudioSystem.getLine (info);
    res.open (audioFormat);
    return res;
  }
  
  
  /**
   * Get whether or not the PlayerCore should be running.
   * @return  true if the player is expected to be running, false otherwise.
   */
  
  public boolean 
  isRunning ()
  {
    return (thread != null);
  }
}
