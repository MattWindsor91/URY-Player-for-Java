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

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.util.Enumeration;
import java.util.Hashtable;

import uryPlayer.core.PlayerCore;
import uryPlayer.core.URYStreamPlayer;

/**
 * The user interface frame of the standalone Java URY player application.
 * 
 * @author Matt Windsor
 */

public class UserInterface
{
  private static final String ABOUT_MESSAGE = 
  "<html>" +
  "<h1>URY Player for Java</h1> <br>" +
  "Copyright (C) 2010 Matt Windsor, URY Computing <br>" +
  "<br>" +
  "This program is free software: you can redistribute it and/or modify<br>" +
  "it under the terms of the GNU General Public License as published by<br>" +
  "the Free Software Foundation, either version 3 of the License, or<br>" +
  "(at your option) any later version.<br>" +
  "<br>" +
  "This program is distributed in the hope that it will be useful,<br>" +
  "but WITHOUT ANY WARRANTY; without even the implied warranty of<br>" +
  "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the<br>" +
  "GNU General Public License for more details.<br>" +
  "<br>" +
  "You should have received a copy of the GNU General Public License<br>" +
  "along with this program.  If not, see <a href=http://www.gnu.org/licenses/>http://www.gnu.org/licences/</a>.<br>" +
  "</html>";
  
  private static final int VOLUME_MULTIPLIER = 1000; /* Multiplier to map from volume float to volume slider int.*/
  
  private URYStreamPlayer parent;
  
  private JFrame frame;
  
  private JButton onOffButton;
  private JButton webcamButton;
  private JButton aboutButton;
  private JButton exitButton;
  
  private ButtonGroup qualityButtons;
  private JPanel qualityPanel;
  
  private JSlider volumeSlider;
  
  private WebcamFetcher webcam;
  private JLabel webcamImage;
  private Dimension webcamPreviousDimension;
  
  
  /**
   * Constructor for the user interface.
   * @param inParent  The URYStreamPlayer that this interface should control.
   */
  
  
  public
  UserInterface (URYStreamPlayer inParent)
  {
    parent = inParent;
    frame = new JFrame ("URY Java Player");
  }
  
  
  /**
   * Create the user interface.
   */
  
  public void
  createUserInterface ()
  {
    frame.getContentPane ().setLayout (new BoxLayout(frame.getContentPane (), 
                                       BoxLayout.PAGE_AXIS));
    frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
    
    createWebcamView ();
    createQualitySelectors ();
    createVolumeControl ();
    createControlButtons ();

    frame.pack ();
    frame.setVisible (true);
  }


  /**
   * Create the webcam viewer (image label and fetcher thread spawner) and 
   * add it to the content pane.
   */
  
  private void
  createWebcamView ()
  {
    webcamImage = new JLabel ();
    webcamImage.setAlignmentX (Component.CENTER_ALIGNMENT);
    webcamImage.setBorder (BorderFactory.createCompoundBorder (BorderFactory.createEmptyBorder (10, 10, 10, 10), 
                                                               BorderFactory.createLoweredBevelBorder ()));
    
    webcam = new WebcamFetcher (this, false);
    webcamPreviousDimension = null;
    
    frame.getContentPane ().add (webcamImage);
  }

  
  /**
   * Create the array of stream quality selectors and add them to the 
   */

  private void
  createQualitySelectors ()
  {
    Border titledBorder = BorderFactory.createTitledBorder (BorderFactory.createEtchedBorder (),
    "Quality");
    
    qualityButtons = new ButtonGroup ();
    qualityPanel = new JPanel (new GridLayout(3, 1));
    qualityPanel.setBorder (BorderFactory.createCompoundBorder (BorderFactory.createEmptyBorder (0, 10, 10, 10), 
        titledBorder));
    
    createQualitySelector ("High Quality (192KB/s)", KeyEvent.VK_H, PlayerCore.HIGH_STREAM, true);
    createQualitySelector ("Low Quality (96KB/s)", KeyEvent.VK_L, PlayerCore.LOW_STREAM, false);
    createQualitySelector ("Mobile Quality (48KB/s)", KeyEvent.VK_M, PlayerCore.MOBILE_STREAM, false);
    
    frame.getContentPane ().add (qualityPanel);
  }
  
  
  /**
   * Create a quality selector.
   * 
   * @param label       The string to assign to the selector label.
   * @param mnemonic    The mnemonic to assign to the selector label.
   * @param streamURI   The stream that the selector will connect to.
   * @param isSelected  Whether or not the selector should begin selected.
   */
  
  private void
  createQualitySelector (String label, int mnemonic, final String streamURI, boolean isSelected)
  {
    JRadioButton qualityButton = new JRadioButton (label);
    qualityButton.setMnemonic (mnemonic);
    qualityButton.setSelected (isSelected);
    qualityButton.addActionListener (new ActionListener ()
    {
      @Override
      public void
      actionPerformed (ActionEvent event)
      {
        parent.setStream (streamURI);
      }
    });
  
    qualityButtons.add (qualityButton);
    qualityPanel.add (qualityButton);
  }
  
  
  /**
   * Create the volume slider.
   */
  
  private void
  createVolumeControl ()
  {
    Border titledBorder = BorderFactory.createTitledBorder (BorderFactory.createEtchedBorder (),
    "Volume");
    
    volumeSlider = new JSlider (JSlider.HORIZONTAL, 0, VOLUME_MULTIPLIER, VOLUME_MULTIPLIER);
    volumeSlider.setBorder (BorderFactory.createCompoundBorder (BorderFactory.createEmptyBorder (0, 10, 10, 10), 
        titledBorder));
   
    
    // Make labels for the ends.
    
    Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel> ();
    
    labelTable.put (new Integer(0), new JLabel("0%"));
    labelTable.put (new Integer(VOLUME_MULTIPLIER), new JLabel("100%"));
    
    volumeSlider.setLabelTable (labelTable);
    volumeSlider.setPaintLabels (true);
    
    
    // Change listener.
    
    volumeSlider.addChangeListener (new ChangeListener ()
    {

      @Override
      public void
      stateChanged (ChangeEvent e)
      {
        parent.setVolume (Integer.valueOf (volumeSlider.getValue ()).floatValue () / VOLUME_MULTIPLIER);
      }
      
    });
    
    frame.getContentPane ().add (volumeSlider);
  }
  
  
  /**
   * Create the deck of control buttons.
   */
  
  private void
  createControlButtons ()
  {
    // On/off button
    
    onOffButton = new JButton ("Start Player");
    onOffButton.addActionListener (new ActionListener ()
    {
      @Override
      public void
      actionPerformed (ActionEvent arg0)
      {
        if (parent.isRunning () == false)
          startPlayer ();
        else
          stopPlayer ();          
      }
    });
    
    
    // Webcam button
    
    webcamButton = new JButton ("Start Webcam");
    webcamButton.addActionListener (new ActionListener ()
    {
      @Override
      public void
      actionPerformed (ActionEvent arg0)
      {
        if (webcam.isRunning () == false)
          startWebcam ();
        else
          stopWebcam ();          
      }
    });
    
    
    // About button
    
    aboutButton = new JButton ("About");
    aboutButton.addActionListener (new ActionListener ()
    {
      @Override
      public void
      actionPerformed (ActionEvent arg0)
      {
        JOptionPane.showMessageDialog (frame,
                                       ABOUT_MESSAGE, 
                                       "About", JOptionPane.PLAIN_MESSAGE);
      }
    });
    
    
    // Exit button
    
    exitButton = new JButton ("Exit");
    exitButton.addActionListener (new ActionListener ()
    {
      @Override
      public void
      actionPerformed (ActionEvent arg0)
      {
        System.exit (0);
      }
    });
    
    
    // Positioning
    
    JPanel controlButtonPanel = new JPanel (new GridLayout (2, 2));
    
    controlButtonPanel.add (onOffButton);
    controlButtonPanel.add (webcamButton);
    controlButtonPanel.add (aboutButton);
    controlButtonPanel.add (exitButton);
    
    controlButtonPanel.setBorder (BorderFactory.createEmptyBorder (0, 10, 10, 10));
    
    frame.getContentPane ().add (controlButtonPanel);
  } 

  
  /**
   * Sets the enabled/disabled state of the interface controls that are 
   * affected by the change of player-core state.
   * 
   * @param state  The new state of the controls.
   */
  
  private void
  setControlsEnabled (boolean state)
  {
    
    Enumeration<AbstractButton> buttons = qualityButtons.getElements ();
    
    while (buttons.hasMoreElements ())
      {
        buttons.nextElement ().setEnabled (state);
      }

    onOffButton.setEnabled (state);
  }

  
  /**
   * Starts the player tracked by this user interface, updating the UI state to reflect this.
   */
    
  public void
  startPlayer ()
  {
    setControlsEnabled (false);
    
    SwingWorker<Void, Void> worker = new SwingWorker<Void, Void> () 
    {
      protected Void
      doInBackground ()
      {
        parent.startPlayer ();
        
        return null;
      }
      
      public void
      done ()
      {
        // Only re-enable the on/off button.
        // This is so people don't try to change the URL during playback, 
        // which at the time of writing does not work.
        
        onOffButton.setText ("Stop Player");
        onOffButton.setEnabled (true);
      }
    };
    
    worker.execute ();  
  } 
  

  /**
   * Stops the player tracked by this user interface, updating the UI state to reflect this.
   */
  
  public void
  stopPlayer ()
  {
    setControlsEnabled (false);
    
    // Stop the player in a separate thread to ensure UI responsiveness.
    
    SwingWorker<Void, Void> worker = new SwingWorker<Void, Void> () 
    {
      protected Void
      doInBackground ()
      {
        parent.stopPlayer ();
        
        return null;
      }
      
      public void
      done ()
      {
        onOffButton.setText ("Start Player");
        setControlsEnabled (true);
      }
    };
    
    worker.execute (); 
  }
  
  
  /**
   * Starts the webcam fetcher, updating the UI state to reflect this.
   */
    
  public void
  startWebcam ()
  {
    webcamButton.setEnabled (false);
    
    SwingWorker<Void, Void> worker = new SwingWorker<Void, Void> () 
    {
      protected Void
      doInBackground ()
      {
        webcam.start ();
        
        return null;
      }
      
      public void
      done ()
      {
        webcamButton.setText ("Stop Webcam");
        webcamButton.setEnabled (true);
      }
    };
    
    worker.execute ();  
  } 
  

  /**
   * Stops the webcam fetcher, updating the UI state to reflect this.
   */
  
  public void
  stopWebcam ()
  {
    webcamButton.setEnabled (false);
    
    // Stop the player in a separate thread to ensure UI responsiveness.
    
    SwingWorker<Void, Void> worker = new SwingWorker<Void, Void> () 
    {
      protected Void
      doInBackground ()
      {
        webcam.stop ();
        
        return null;
      }
      
      public void
      done ()
      {
        webcamButton.setText ("Start Webcam");
        webcamButton.setEnabled (true);
        frame.validate ();
        frame.pack ();
      }
    };
    
    worker.execute (); 
  }

  
  /**
   * Update the webcam image.
   * 
   * @param imageIcon  The new image to use.
   */

  public void
  setWebcamImage (ImageIcon imageIcon)
  {
    webcamImage.setIcon (imageIcon);
    //webcamImage.setPreferredSize (new Dimension (imageIcon.getIconWidth (), imageIcon.getIconHeight ()));
    
    // If the size has changed, redo the layout.
    
    if (webcamPreviousDimension == null
        || imageIcon.getIconWidth () != webcamPreviousDimension.width
        || imageIcon.getIconHeight () != webcamPreviousDimension.height)
      {
        webcamImage.setAlignmentX (Component.CENTER_ALIGNMENT);
        frame.getContentPane ().setLayout (new BoxLayout(frame.getContentPane (), BoxLayout.PAGE_AXIS));
        frame.validate ();
        frame.pack ();
        
        webcamPreviousDimension = new Dimension (imageIcon.getIconWidth (), imageIcon.getIconHeight ());
      }
  }
}
