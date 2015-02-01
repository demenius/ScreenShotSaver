/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package screenshotsaver;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 *
 * @author Braydon
 */
public class ScreenshotSaver
{

    private static JTextField folderField, fileField;
    private static JLabel timeLeft, screenShotsTaken;
    private static boolean running = false;
    private static int time = 15;
    private static boolean secFocus = true;
    private static JTextField min, sec;

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException
    {
        MyListener l = new MyListener();
        JFrame frame = new JFrame("Screenshot Saver");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new SpringLayout());

        JPanel clockPanel = new JPanel();
        min = new JTextField(new JTextFieldLimit(2), "00", 2);
        min.addFocusListener(new FocusListener()
        {
            @Override
            public void focusGained(FocusEvent e)
            {
                secFocus = false;
                ((JTextField) e.getComponent()).selectAll();
            }

            public void focusLost(FocusEvent e)
            {
            }

        });
        clockPanel.add(min);
        clockPanel.add(new JLabel(":"));
        sec = new JTextField(new JTextFieldLimit(2), "15", 2);
        sec.addFocusListener(new FocusListener()
        {
            @Override
            public void focusGained(FocusEvent e)
            {
                secFocus = true;
                ((JTextField) e.getComponent()).selectAll();
            }

            public void focusLost(FocusEvent e)
            {
            }

        });
        clockPanel.add(sec);

        JButton subButton = new JButton("-");
        subButton.setActionCommand("sub");
        subButton.addActionListener(l);
        JButton addButton = new JButton("+");
        addButton.setActionCommand("add");
        addButton.addActionListener(l);
        Font f = subButton.getFont();

        subButton.setFont(new Font(f.getFontName(), Font.BOLD, (int) (f.getSize() * 1.5)));
        addButton.setFont(new Font(f.getFontName(), Font.BOLD, (int) (f.getSize() * 1.5)));

        clockPanel.add(subButton);
        clockPanel.add(addButton);

        JPanel controlPanel = new JPanel();
        URL play = ScreenshotSaver.class.getResource("images/play.png");
        URL stop = ScreenshotSaver.class.getResource("images/stop.png");

        Image image = ImageIO.read(play);
        ImageIcon playIcon = new ImageIcon(ImageIO.read(play).getScaledInstance(image.getWidth(null) / 2, image.getHeight(null) / 2, Image.SCALE_DEFAULT));
        ImageIcon stopIcon = new ImageIcon(ImageIO.read(stop).getScaledInstance(image.getWidth(null) / 2, image.getHeight(null) / 2, Image.SCALE_DEFAULT));

        
        JButton playButton = new JButton(playIcon);
        playButton.setActionCommand("play");
        playButton.addActionListener(l);
        playButton.setMargin(new Insets(0, 0, 0, 0));
        JButton stopButton = new JButton(stopIcon);
        stopButton.setActionCommand("stop");
        stopButton.addActionListener(l);
        stopButton.setMargin(new Insets(0, 0, 0, 0));

        controlPanel.add(playButton);
        controlPanel.add(stopButton);

        panel.add(new JLabel("Screenshots Taken"));
        screenShotsTaken = new JLabel("0", SwingConstants.CENTER);
        panel.add(screenShotsTaken);
        panel.add(new JSeparator(JSeparator.VERTICAL), BorderLayout.LINE_START);
        panel.add(new JLabel("Next Screenshot"));
        timeLeft = new JLabel("00:00");
        panel.add(timeLeft);

        panel.add(new JLabel("Set Time"));
        panel.add(clockPanel);
        panel.add(new JSeparator(JSeparator.VERTICAL), BorderLayout.LINE_START);
        panel.add(controlPanel);
        panel.add(new JPanel());
        
        panel.add(new JLabel("Set File Name"));
        fileField = new JTextField("screenshot");
        panel.add(fileField);
        panel.add(new JSeparator(JSeparator.VERTICAL), BorderLayout.LINE_START);
        panel.add(new JPanel());
        panel.add(new JPanel());

        JPanel folderPanel = new JPanel();
        folderField = new JTextField(System.getProperty("user.dir") + File.separator);
        folderField.setColumns(30);
        folderPanel.add(folderField);
        JButton browseButton = new JButton("Browse");
        browseButton.setActionCommand("browse");
        browseButton.addActionListener(l);
        folderPanel.add(browseButton);
        JButton openButton = new JButton("Open");
        openButton.setActionCommand("open");
        openButton.addActionListener(l);
        folderPanel.add(openButton);

        SpringUtilities.makeCompactGrid(panel, //parent
                3, 5,
                3, 3, //initX, initY
                3, 3); //xPad, yPad

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.getContentPane().add(folderPanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);
        sec.selectAll();
        sec.requestFocus();

        /*

         
         */
    }

    private static void changeTimeLeft(long difference)
    {
        long display, extra;
        display = time - difference;
        if (display > 59)
        {
            extra = display / 60;
            display = display - extra * 60;
        } else
        {
            extra = 0;
        }
        timeLeft.setText((extra < 10 ? "0" : "") + extra + ":" + (display < 10 ? "0" : "") + display);
    }

    private static class Capturer implements Runnable
    {

        @Override
        public void run()
        {
            System.out.println("start");
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Rectangle screenRectangle = new Rectangle(screenSize);
            Robot robot;
            try
            {
                robot = new Robot();
            } catch (AWTException ex)
            {
                System.out.println("Fail");
                return;
            }

            long timeNow, timeBefore = System.nanoTime();

            int fileNumber = 0;

            long difference;
            time = Integer.parseInt(min.getText()) * 60 + Integer.parseInt(sec.getText());
            changeTimeLeft(0);
            while (running)
            {
                timeNow = System.nanoTime();
                difference = TimeUnit.NANOSECONDS.toSeconds(timeNow - timeBefore);
                if (difference <= time)
                {
                    if (difference > 0.750)
                    {
                        changeTimeLeft(difference);
                    }
                    continue;
                }
                timeBefore = timeNow;
                String number = String.format("%10d", fileNumber).replace(' ', '0');
                final String fileName = folderField.getText() + fileField.getText() + "_" + number + ".jpg";
                fileNumber++;
                screenShotsTaken.setText(""+fileNumber);
                time = Integer.parseInt(min.getText()) * 60 + Integer.parseInt(sec.getText());
                changeTimeLeft(0);

                new Thread(() ->
                {
                    BufferedImage image = robot.createScreenCapture(screenRectangle);
                    try
                    {
                        ImageIO.write(image, "jpg", new File(fileName));
                    } catch (IOException ex)
                    {
                        Logger.getLogger(ScreenshotSaver.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }).start();

            }
        }

    }

    private static void changeTime(int v)
    {
        int val;
        if (secFocus)
        {
            val = Integer.parseInt(sec.getText());

            if ((val < 59 && v > 0) || (val > 0 && v < 0))
            {
                sec.setText("" + (val + v));
            } else
            {
                int val2 = Integer.parseInt(min.getText());

                if (val >= 59 && val2 < 99)
                {
                    sec.setText("0");
                } else if (val >= 0 && val2 > 0)
                {
                    sec.setText("59");
                }

                if ((val2 < 99 && v > 0) || (val2 > 0 && v < 0))
                {
                    min.setText("" + (val2 + v));
                }
            }
        } else
        {
            val = Integer.parseInt(min.getText());
            if ((val < 99 && v > 0) || (val > 0 && v < 0))
            {
                min.setText("" + (val + v));
            }
        }
    }

    private static class MyListener implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent e)
        {
            String action = e.getActionCommand();
            System.out.println(action);

            switch (action)
            {
                case "sub":
                    changeTime(-1);
                    break;
                case "add":
                    changeTime(1);
                    break;
                case "play":
                    running = true;
                    new Thread(new Capturer()).start();
                    break;
                case "stop":
                    running = false;
                    changeTimeLeft(time);
                    break;
                case "browse":
                    JFileChooser chooser = new JFileChooser();
                    chooser.setDialogTitle("Select target directory");
                    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    int returnVal = chooser.showOpenDialog(null);
                    if (returnVal == JFileChooser.APPROVE_OPTION)
                    {
                        folderField.setText(chooser.getSelectedFile().getAbsolutePath() + File.separator);
                    }
                    break;
                case "open":
                    try
                    {
                        Runtime.getRuntime().exec("explorer.exe /open," + folderField.getText());
                    } catch (IOException ex)
                    {
                        Logger.getLogger(ScreenshotSaver.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
            }
        }

    }

    private static class JTextFieldLimit extends PlainDocument
    {

        private int limit;

        JTextFieldLimit(int limit)
        {
            super();
            this.limit = limit;
        }

        JTextFieldLimit(int limit, boolean upper)
        {
            super();
            this.limit = limit;
        }

        @Override
        public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException
        {
            if (str == null)
            {
                return;
            }
            
            if(!(str.matches("[0-9]") || str.matches("[0-9][0-9]")))
            {
                str = "00";
            }

            if ((getLength() + str.length()) <= limit)
            {
                super.insertString(offset, str, attr);
            }
        }
    }
}
