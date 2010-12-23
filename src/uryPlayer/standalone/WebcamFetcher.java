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

import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import javax.swing.ImageIcon;


/**
 * A threaded class for periodically fetching the URY webcam feed and 
 * updating an image label with its content.
 * 
 * @author Matt Windsor
 */

public class WebcamFetcher implements Runnable
{
  /**
   * The URI pointing to the URY logo (for display when the webcam is off).
   */
  
  public static final String URY_LOGO_URI = "images/ury.jpg";
  
  
  /**
   * The URI pointing to the webcam stream.
   */
  
  public static final String WEBCAM_URI = "http://ury.york.ac.uk/webcam/getcam.php";
 
  
  /**
   * The delay between webcam retrievals, in milliseconds.
   */
  
  public static final int WEBCAM_DELAY = 2000;
  
  
  private UserInterface master;
  private volatile Thread thread;
  
  
  /**
   * Create a new WebcamFetcher to handle the webcam image of the given
   * player user interface.
   * 
   * @param master  The UserInterface whose image should be updated.
   * @param isRunning  Whether or not the 
   */
  
  public
  WebcamFetcher (UserInterface master, boolean isRunning)
  {
    this.master = master;
    
    thread = null;
    
    if (isRunning == true)
      start ();
    else
      stop ();
  }
 
  
  /**
   * @return  whether or not the fetcher is running.
   */
  
  public boolean
  isRunning ()
  {
    return (thread != null);
  }

  
  /**
   * Start the WebcamFetcher, if it has not already been started.
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
   * Stop the WebcamFetcher, if it has been started.
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
    
    // Replace the webcam image with the URY logo.
    
    URL imageURL = getClass ().getResource (URY_LOGO_URI);
    
    if (imageURL != null)
      master.setWebcamImage (new ImageIcon (imageURL));
  }

  
  /**
   * The actual executed body of the webcam fetcher.
   * 
   * @see java.lang.Runnable#run()
   */
  
  @Override
  public void
  run ()
  {    
    URL webcamURL = null;
    Thread thisThread = Thread.currentThread ();
    
    try
      {
        webcamURL = new URL (WEBCAM_URI);
      }
    catch (MalformedURLException e1)
      {
        master.stopWebcam ();
      }    
    
    
    /* Keep getting an image, waiting for WEBCAM_DELAY ms, until the 
       webcam fetcher is closed (ie, the thread field changes). */
    
    while (thread == thisThread)
      {
        Image image = getImage (webcamURL);
    
        
        // The image may be null if the get operation failed.
        
        if (image != null)
          master.setWebcamImage (new ImageIcon (image));
        
        try
          {
            Thread.sleep (WEBCAM_DELAY);
          }
        catch (InterruptedException e)
          {
            /* This is normal.
             (It occurs when the webcam is switched off during sleeping). */
          }
      }
  }
  
  
  /**
   * Retrieve a webcam image from the given URL.
   * 
   * Note: this function will return null if there was an error retrieving 
   * a webcam image - code calling this should handle this by not updating 
   * the webcam.
   * 
   * @param webcamURL  The URL of the webcam image.
   * @return  the Image taken from the webcam, or null in case of error.
   */
  
  private Image
  getImage (URL webcamURL)
  {
    URLConnection webcamConn = null;
    BufferedInputStream in = null;
    Image result = null;    
    
    try
      {
        webcamConn = webcamURL.openConnection ();
        in = new BufferedInputStream(webcamConn.getInputStream ());
      }
    catch (IOException e1)
      {
        // This should cause the webcam to simply not update after failure.
        in = null;
      }
    
    if (in != null)
      {    
        ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream ();

        try
          {
            int c = in.read ();
            while (c != -1)
              {
                byteArrayOut.write (c);
                c = in.read ();
              }
          }
        catch (IOException e1)
          {
            // Get rid of byteArrayOut in order to warn the next code block of the error.
            byteArrayOut = null;
          }

        if (byteArrayOut != null)
          result = Toolkit.getDefaultToolkit ().createImage (byteArrayOut.toByteArray ());       
      }
    
    // If an error came up, this will still be null.
    return result;
  }
}
