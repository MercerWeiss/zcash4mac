/************************************************************************************************
 *  _________          _     ____          _           __        __    _ _      _   _   _ ___
 * |__  / ___|__ _ ___| |__ / ___|_      _(_)_ __   __ \ \      / /_ _| | | ___| |_| | | |_ _|
 *   / / |   / _` / __| '_ \\___ \ \ /\ / / | '_ \ / _` \ \ /\ / / _` | | |/ _ \ __| | | || |
 *  / /| |__| (_| \__ \ | | |___) \ V  V /| | | | | (_| |\ V  V / (_| | | |  __/ |_| |_| || |
 * /____\____\__,_|___/_| |_|____/ \_/\_/ |_|_| |_|\__, | \_/\_/ \__,_|_|_|\___|\__|\___/|___|
 *                                                 |___/
 *
 * Copyright (c) 2016 Ivan Vaklinov <ivan@vaklinov.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 **********************************************************************************/
package com.vaklinov.zcashui;


import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.vaklinov.zcashui.ZCashClientCaller.WalletCallException;
import com.vaklinov.zcashui.ZCashInstallationObserver.InstallationDetectionException;

/**
 * Main ZCash Window.
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
public class ZCashUI
    extends JFrame
{
    public static final String NAME,VERSION;
    static {
        String name = System.getProperty("wallet.name","@name@");
        if ("@name@".equals(name))
            name = "ZCash Swing Wallet UI";
        NAME = name;
        String version = System.getProperty("wallet.version","@version@");
        if ("@version@".equals(version))
            version = "Custom Version";
        VERSION = version;
     
        // this is a hack to get the settings dir created before logging
        // subsystem is initialized.
        try {
            OSUtil.getSettingsDirectory();
        } catch (IOException bad) {
            throw new RuntimeException(bad);
        }
    }
    
    private static final Logger LOG = Logger.getLogger(ZCashUI.class.getName());
    
    private ZCashInstallationObserver installationObserver;
    private ZCashClientCaller clientCaller;
    private StatusUpdateErrorReporter errorReporter;

    private WalletOperations walletOps;

    private JMenuItem menuItemExit;
    private JMenuItem menuItemAbout;
    private JMenuItem menuItemEncrypt;
    private JMenuItem menuItemBackup;
    private JMenuItem menuItemExportKeys;
    private JMenuItem menuItemImportKeys;
    private JMenuItem menuItemShowPrivateKey;
    private JMenuItem menuItemImportSinglePrivateKey;

    private DashboardPanel dashboard;
    private AddressesPanel addresses;
    private SendCashPanel  sendPanel;
    private AddressBookPanel bookPanel;

    public ZCashUI(ZCashClientCaller clientCaller)
        throws IOException, InterruptedException, WalletCallException
    {
        super(NAME + " "+VERSION);
        ClassLoader cl = this.getClass().getClassLoader();

        this.setIconImage(new ImageIcon(cl.getResource("images/"+NAME+"-246x246.png")).getImage());

        Container contentPane = this.getContentPane();

        errorReporter = new StatusUpdateErrorReporter(this);
        installationObserver = new ZCashInstallationObserver(OSUtil.getProgramDirectory());
        this.clientCaller = clientCaller;

        // Build content
        JTabbedPane tabs = new JTabbedPane();
        Font oldTabFont = tabs.getFont();
        Font newTabFont  = new Font(oldTabFont.getName(), Font.BOLD | Font.ITALIC, oldTabFont.getSize() * 57 / 50);
        tabs.setFont(newTabFont);
        tabs.addTab("Overview ",
        		    new ImageIcon(cl.getResource("images/overview.png")),
        		    dashboard = new DashboardPanel(this, installationObserver, clientCaller, errorReporter));
        tabs.addTab("Own addresses ",
        		    new ImageIcon(cl.getResource("images/address-book.png")),
        		    addresses = new AddressesPanel(clientCaller, errorReporter));
        tabs.addTab("Send cash ",
        		    new ImageIcon(cl.getResource("images/send.png")),
        		    sendPanel = new SendCashPanel(clientCaller, errorReporter));
        tabs.addTab("Address book ",
                new ImageIcon(cl.getResource("images/address-book.png")),
                bookPanel = new AddressBookPanel(sendPanel,tabs));
        contentPane.add(tabs);

        this.walletOps = new WalletOperations(
            	this, tabs, dashboard, addresses, sendPanel, installationObserver, clientCaller, errorReporter);

        this.setSize(new Dimension(870, 427));

        // Build menu
        JMenuBar mb = new JMenuBar();
        JMenu file = new JMenu("Main");
        file.setMnemonic(KeyEvent.VK_M);
        int accelaratorKeyMask = Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask();
	        file.add(menuItemAbout = new JMenuItem("About...", KeyEvent.VK_A));
        menuItemAbout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, accelaratorKeyMask));
        file.addSeparator();
        file.add(menuItemExit = new JMenuItem("Quit", KeyEvent.VK_Q));
        menuItemExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, accelaratorKeyMask));
        mb.add(file);

        JMenu wallet = new JMenu("Wallet");
        wallet.setMnemonic(KeyEvent.VK_W);
        wallet.add(menuItemBackup = new JMenuItem("Backup...", KeyEvent.VK_B));
        menuItemBackup.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, accelaratorKeyMask));
        wallet.add(menuItemEncrypt = new JMenuItem("Encrypt...", KeyEvent.VK_E));
        menuItemEncrypt.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, accelaratorKeyMask));
        wallet.add(menuItemExportKeys = new JMenuItem("Export private keys...", KeyEvent.VK_K));
        menuItemExportKeys.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, accelaratorKeyMask));
        wallet.add(menuItemImportKeys = new JMenuItem("Import private keys...", KeyEvent.VK_I));
        menuItemImportKeys.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, accelaratorKeyMask));
        wallet.add(menuItemShowPrivateKey = new JMenuItem("Show private key...", KeyEvent.VK_P));
        menuItemShowPrivateKey.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, accelaratorKeyMask));
        wallet.add(menuItemImportSinglePrivateKey = new JMenuItem("Import one private key...", KeyEvent.VK_N));
        menuItemImportSinglePrivateKey.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, accelaratorKeyMask));
        
        mb.add(wallet);

        // TODO: Temporarily disable encryption until further notice - Oct 24 2016
        menuItemEncrypt.setEnabled(false);
                        
        this.setJMenuBar(mb);

        // Add listeners etc.
        menuItemExit.addActionListener(
            new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    ZCashUI.this.exitProgram();
                }
            }
        );

        menuItemAbout.addActionListener(
            new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	try
                	{
                		AboutDialog ad = new AboutDialog(ZCashUI.this);
                		ad.setVisible(true);
                	} catch (UnsupportedEncodingException uee)
                	{
                		LOG.log(Level.WARNING, "", uee);
                		ZCashUI.this.errorReporter.reportError(uee);
                	}
                }
            }
        );

        menuItemBackup.addActionListener(   
        	new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    ZCashUI.this.walletOps.backupWallet();
                }
            }
        );
        
        menuItemEncrypt.addActionListener(
            new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    ZCashUI.this.walletOps.encryptWallet();
                }
            }
        );

        menuItemExportKeys.addActionListener(   
            new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    ZCashUI.this.walletOps.exportWalletPrivateKeys();
                }
            }
       );
        
       menuItemImportKeys.addActionListener(   
            new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    ZCashUI.this.walletOps.importWalletPrivateKeys();
                }
            }
       );
       
       menuItemShowPrivateKey.addActionListener(   
            new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    ZCashUI.this.walletOps.showPrivateKey();
                }
            }
       );
       
       menuItemImportSinglePrivateKey.addActionListener( (e) -> {
                   ZCashUI.this.walletOps.importSinglePrivateKey();
               }
       );

        // Close operation
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                ZCashUI.this.exitProgram();
            }
        });

        // Show initial message
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                try
                {
                    String userDir = OSUtil.getSettingsDirectory();
                    File warningFlagFile = new File(userDir + "/initialInfoShown.flag");
                    if (warningFlagFile.exists())
                    {
                        return;
                    } else
                    {
                        warningFlagFile.createNewFile();
                    }

                } catch (IOException ioe)
                {
                    /* TODO: report exceptions to the user */
                    LOG.log(Level.WARNING, "", ioe);
                }

                JOptionPane.showMessageDialog(
                    ZCashUI.this.getRootPane().getParent(),
                    "The ZClassic GUI Wallet is currently considered experimental. Use of this software\n" +
                    "comes at your own risk! Be sure to read the list of known issues and limitations\n" +
                    "at this page: \n" +
                    "https://github.com/vaklinov/zcash-swing-wallet-ui#known-issues-and-limitations\n\n" +
                    "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR\n" +
                    "IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,\n" +
                    "FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE\n" +
                    "AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER\n" +
                    "LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,\n" +
                    "OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN\n" +
                    "THE SOFTWARE.\n\n" +
                    "(This message will be shown only once)",
                    "Disclaimer", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    public void exitProgram()
    {
        LOG.info("Exiting ...");

        this.dashboard.stopThreadsAndTimers();

        ZCashUI.this.setVisible(false);
        ZCashUI.this.dispose();

        System.exit(0);
    }

    public static void main(String argv[])
        throws IOException
    {
        try
        {
            LOG.info("Starting "+NAME+" "+VERSION+"...");
            LOG.info("OS: " + System.getProperty("os.name") + " = " + OSUtil.getOSType());
            LOG.info("Current directory: " + new File(".").getCanonicalPath());
            LOG.info("Class path: " + System.getProperty("java.class.path"));
            LOG.info("Environment PATH: " + System.getenv("PATH"));

            ////////////////////////////////////////////////////////////
            if (OSUtil.getOSType() != OSUtil.OS_TYPE.MAC_OS) {
                for (LookAndFeelInfo ui : UIManager.getInstalledLookAndFeels())
                {
                    LOG.info("Available look and feel: " + ui.getName() + " " + ui.getClassName());
                    if (ui.getName().equals("Nimbus"))
                    {
                        UIManager.setLookAndFeel(ui.getClassName());
                        break;
                    }
                }
            } else 
                System.setProperty("apple.laf.useScreenMenuBar", "true");

            /////////////////////////////////////////////////////
            ZCashClientCaller clientCaller = new ZCashClientCaller(OSUtil.getProgramDirectory());
            StartupProgressDialog startupBar = new StartupProgressDialog(clientCaller);
            startupBar.setVisible(true);
            startupBar.waitForStartup();
            ZCashUI ui = new ZCashUI(clientCaller);
            ui.setVisible(true);

        } catch (InstallationDetectionException ide)
        {
            LOG.log(Level.WARNING,"ide",ide);
            JOptionPane.showMessageDialog(
                null,
                "This program was started in directory: " + OSUtil.getProgramDirectory() + "\n" +
                ide.getMessage() + "\n" +
                "See the log file for more detailed error information!",
                "Installation error",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } catch (WalletCallException wce)
        {
            LOG.log(Level.WARNING, "wce", wce);

            if ((wce.getMessage().indexOf("{\"code\":-28,\"message\":\"Verifying blocks")      != -1)  ||
            	(wce.getMessage().indexOf("{\"code\":-28,\"message\":\"Rescanning")            != -1)  ||
            	(wce.getMessage().indexOf("{\"code\":-28,\"message\":\"Loading wallet")        != -1)  ||
            	(wce.getMessage().indexOf("{\"code\":-28,\"message\":\"Activating best chain") != -1)  ||
            	(wce.getMessage().indexOf("{\"code\":-28,\"message\":\"Loading addresses")     != -1))
            {
                JOptionPane.showMessageDialog(
                        null,
                        "It appears that zcashd has been started but is not ready to accept wallet\n" +
                        "connections. It is still loading the wallet and blockchain. Please try to \n" +
                        "start the GUI wallet later...",
                        "Wallet communication error",
                        JOptionPane.ERROR_MESSAGE);
            } else
            {
                JOptionPane.showMessageDialog(
                    null,
                    "There was a problem communicating with the ZCash daemon/wallet. \n" +
                    "Please ensure that the ZCash server zcashd is started (e.g. via \n" + 
                    "command  \"zcashd --daemon\"). Error message is: \n" +
                     wce.getMessage() +
                    "See the console output for more detailed error information!",
                    "Wallet communication error",
                    JOptionPane.ERROR_MESSAGE);
            }

            System.exit(2);
        } catch (Exception e)
        {
            LOG.log(Level.WARNING, "Generic exception", e);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            e.printStackTrace(ps);
            ps.flush();
            String stackTrace = new String(baos.toByteArray());
            JOptionPane.showMessageDialog(
                null,
                "A general unexpected critical error has occurred: \n" + e.getMessage() + "\n" +
                stackTrace,
                "Error",
                JOptionPane.ERROR_MESSAGE);
            System.exit(3);
        }
    }
}
