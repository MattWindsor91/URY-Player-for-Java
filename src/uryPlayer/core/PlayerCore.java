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
import javax.sound.sampled.FloatControl;
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
  private volatile float volume;
  
  
  /**
   *  Create a new PlayerCore.
   *  
   *  @param inURL     The stream URL to connect to.
   */
  
  public
  PlayerCore (String inURL)
  {
    super ();
    
    streamURL = inURL;
    thread = null;
    volume = 1;
  }

  
  /**
   * Start the PlayerCore, if it has not already been started.
   * 
   * @return true if the operation succeeded, false otherwise.
   */
  
  public boolean
  start ()
  {
    if (thread != null)
      return false;
    
    thread = new Thread (this);
    thread.start ();
   
    return true;
  }
  
  
  /**
   * Stop the PlayerCore, if it has been started.
   * 
   * @return true if the operation succeeded, false otherwise.
   */
  
  public boolean
  stop ()
  {
    if (thread == null)
      return false;
    
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
        return false;
      }
    
    return true;
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
   * @return  the current stream URL.
   */
  
  public String
  getURL ()
  {
    return streamURL;
  }
 
  
  /**
   * Changes the URL used by the PlayerCore.
   * 
   * This will stop the current player if running.
   * 
   * @return  true if the URL was changed, false otherwise.
   */
  
  public boolean
  setURL (String streamURL)
  {
    if (thread == null)
      stop ();
      
    this.streamURL = streamURL;
    return true;
  }
  
  
  /**
   * Set the volume of the stream, if it is playing.
   * 
   * @param volume The new volume, as a floating-point number from 0 to 1.
   * 
   * @return true if the operation succeeded, false otherwise.
   */
  
  public boolean
  setVolume (float volume)
  {
    this.volume = volume;
    return true;
  }


  /**
   * @return the current volume, as a floating-point number from 0 to 1.
   */
  
  public float
  getVolume ()
  {
    return volume;
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
    float previous_volume = 2;
    
    if (line != null)
      {
        line.open ();
        line.start ();
        int nBytesRead = 0;
        
        // nBytesRead == -1 implies end of file, so terminate when this occurs.
        
        while (nBytesRead != -1 && thread == thisThread)
          {
            if (previous_volume != getVolume ())
              {
                previous_volume = getVolume ();
                updateVolume (line);
              }
            
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
   * Update the volume of the player core.
   * 
   * @param line  The line to change volume on.
   */
  
  private void
  updateVolume (SourceDataLine line)
  {
    FloatControl volCtrl = null;
    
    if (line.isControlSupported (FloatControl.Type.VOLUME))
      {
        volCtrl = (FloatControl) line.getControl (FloatControl.Type.VOLUME);
        volCtrl.setValue (getVolume ());
      }
    else
      {
        volCtrl = (FloatControl) line.getControl (FloatControl.Type.MASTER_GAIN);
        
        // Linear volume = 10 ^ (Gain (dB) / 20)
        
        volCtrl.setValue ((float) (Math.log10 (getVolume ())) * 20);
      }
    System.out.println (volCtrl.getValue ());
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
