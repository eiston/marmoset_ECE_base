/**
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Nov 15, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.marmoset.utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.mail.MessagingException;

import org.apache.commons.io.IOUtils;

/**
 * UptimeDaemon
 * Opens a serverSocket on a port (9999 by default) and responds to any request with the
 * results of the linux command 'uptime'.
 * @author jspacco
 */
public class UptimeDaemon
{
    private static void usage() {
        System.err.println("Usage: java UptimeDaemon <output_dir>");
        System.exit(2);
    }
    
    /**
     * @param args
     */
    public static void main(String[] args)
    throws IOException
    {
        // TODO read in the to/from/host/thresholds from the command line or a config file
        if (args.length < 1) {
            usage();
        }
        File outputDir=new File(args[0]);
        if (!outputDir.isDirectory())
            usage();
        
        UptimeDaemon uptimeDaemon;
        if (args.length > 1) {
            int port=Integer.parseInt(args[1]);
            uptimeDaemon=new UptimeDaemon(outputDir, port);
        } else {
            uptimeDaemon=new UptimeDaemon(outputDir);
        }
        uptimeDaemon.run();
    }
    
    private File outputDir;
    
    public UptimeDaemon(File outputDir, int port)
    throws IOException
    {
        this.port=port;
        this.outputDir=outputDir;
        this.serverSocket = new ServerSocket(this.port);
        this.hostname = InetAddress.getLocalHost().getHostAddress();
    }
    
    public UptimeDaemon(File outputDir)
    throws IOException
    {
        // Use default port
        this(outputDir, 9999);
    }
    
    private ServerSocket serverSocket;
    private int port;
    private String hostname;
    private String toEmailAddress=System.getProperty("to_email", "jspacco@cs.umd.edu");
    private String fromEmailAddress=System.getProperty("from_email", "jspacco@umiacs.umd.edu");
    private String host=System.getProperty("stmp", "smtp.cs.umd.edu");
    private long waitTime=30000;
    
    public void run()
    {
        // Listen on a socket for connections and report the current uptime
        Thread t = new Thread() {
            public void run()
            {
                while (true) {
                    try {
                        Socket sock = serverSocket.accept();
                        String uptime=shellCommand("uptime");
                        sock.getOutputStream().write(uptime.getBytes());
                        sock.close();
                    } catch (IOException e) {
                        System.out.println(e);
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        System.out.println(e);
                        e.printStackTrace();
                    }
                }
            }
        };
        t.start();
        
        //if (false)
        new Thread() {
            public void run()
            {
                while (true) {
                    try {
                        String uptime = shellCommand("uptime");
                        //System.out.println(uptime);
                        processLoad(uptime);

                        Thread.sleep(waitTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }
    
    void processLoad(String uptime)
    {
        String[] arr = uptime.split("load average: ");
        String[] arr2 = arr[1].split("\\s*,\\s+");
        Float[] times = new Float[arr2.length];
        for (int ii=0; ii<arr2.length;ii++) {
            times[ii]=Float.parseFloat(arr2[ii]);
        }
        
        String date=dateFormat.format(new Date());

        // According to staff, sendmail shuts off on our linux machines when the load reaches
        // 12.0, so these thresholds need to be well under that to be at all useful.

        double oneMinuteThreshold = 8.0;
        double fiveMinuteThreshold = 6.0;
        double fifteenMinuteThreshold = 5.0;
        /*        
        double oneMinuteThreshold = 0.5;
        double fiveMinuteThreshold = 0.4;
        double fifteenMinuteThreshold = 0.3;
        */

        // 1 minute load average
        if (times[0].floatValue() > oneMinuteThreshold) {
            System.out.println("Sky is falling!");
            // In this case just email me a quick note that the load has spiked
            String message="1 min load on on " +hostname+ " has spiked; uptime: "+uptime;
            emailMyself(message,message);
        }
        // 5 minute load average
        if (times[1].floatValue() > fiveMinuteThreshold) {
            System.out.println("Rain and thunder and...");
            //Definitely make sure any spikes in the last 5 minutes written out to disk
            String filename=outputDir.getAbsolutePath() + "/5min." +date;
            dumpToFile(filename, getDiagnosticInformation());
        }
        // 15 minute load average
        if (times[2].floatValue() > fifteenMinuteThreshold) {
            System.out.println("Chicken little sucked");
            //Any major spikes in the last 15 minutes are probably a terrible problem, and
            // should be logged.  I should also email myself.  Though be careful because
            // sendmail shuts down at a certain load level...
            String filename=outputDir.getAbsolutePath() + "/15min." +date;
            dumpToFile(filename, getDiagnosticInformation());
        }
    }
    
    private static final SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss");
    
    private void emailMyself(String subject, String message)
    {
        try {
            JavaMail.sendMessage(toEmailAddress, fromEmailAddress, host, subject, message);
        } catch (MessagingException ignore) {
            // ignore; this is a best-effort attempt to email myself
            // When the system is having problems, that's when it's least likely to
            // work well enough to send email.  sendmail stops working on our
            // linux machines when the load gets above 12.0 anyway.
        }
    }
    
    private void dumpToFile(String filename, String info)
    {
        FileOutputStream fos=null;
        try {
            fos=new FileOutputStream(filename);
            fos.write(info.getBytes());
            fos.flush();
        } catch (IOException ignore) {
            // ignore; we're trying to dump things on a best-effort basis so if things
            // don't get dumped, that's OK
        } finally {
            IOUtils.closeQuietly(fos);
        }
    }
    
    String getDiagnosticInformation()
    {
        // I'm placing each of these in a separate try/catch block; it's possible that
        // some of them will fail if the system is under load, and I want to get as much
        // data as possible.
        String ps ="";
        try {
            ps= shellCommand("ps -aux");
        } catch (Exception ignore) {
            // ignore
        }
        String vmstat = "";
        try {
            vmstat = shellCommand("vmstat");
        } catch (Exception ignore) {
            // ignore
        }
        String iostat = "";
        try {
            iostat = shellCommand("iostat");
        } catch (Exception ignore) {
            // ignore
        }
        String netstat ="";
        try {
            netstat = shellCommand("netstat");
        } catch (Exception ignore){
            // ignore
        }
        
        return "IP address: " +hostname +"\n"+
            "prompt% ps\n" +ps +
            "prompt% vmstat\n" +vmstat +
            "prompt% iostat\n" +iostat +
            "prompt% netstat\n" +netstat;
    }
    
    private static String shellCommand(String cmd)
    throws InterruptedException, IOException
    {
        return shellCommand(cmd.split("\\s+"));
    }
    
    private static String shellCommand(String[] cmd)
    throws InterruptedException, IOException
    {
        JProcess jproc = new JProcess(cmd);
        int exitCode = jproc.waitFor(0);
        if (exitCode==0) {
            return jproc.getOut().toString() +"\n"+
                jproc.getErr().toString();
        } else {
            return "FUBAR: " +exitCode;
            //throw new IOException("Uptime process exited with non-zero error code: " +exitCode);
        }
    }

    
}
