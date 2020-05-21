package com.github.usdn.motes.standalone;

import com.github.usdn.util.NodeAddress;
import org.apache.commons.cli.*;

import java.net.InetSocketAddress;

public final class Loader{
    public static void main(final String[] args) {

        Options options = new Options();

        options.addOption("net id",true,"connection with sink");



        // create the parser
        CommandLineParser parser = new BasicParser();
        try {
            CommandLine line = parser.parse(options, args);
            Thread th;

            byte cmdNet = (byte) Integer.parseInt(line.getOptionValue("n"));
            NodeAddress cmdAddress = new NodeAddress(Integer.parseInt(
                    line.getOptionValue("a")));
            int cmdPort = Integer.parseInt(line.getOptionValue("p"));
            String cmdTopo = line.getOptionValue("t");

            String cmdLevel;

            if (!line.hasOption("l")) {
                cmdLevel = "SEVERE";
            } else {
                cmdLevel = line.getOptionValue("l");
            }

            if (line.hasOption("c")) {

                if (!line.hasOption("sd")) {
                    throw new ParseException("-sd option missing");
                }
                if (!line.hasOption("sp")) {
                    throw new ParseException("-sp option missing");
                }
                if (!line.hasOption("sm")) {
                    throw new ParseException("-sm option missing");
                }

                String cmdSDpid = line.getOptionValue("sd");
                String cmdSMac = line.getOptionValue("sm");
                long cmdSPort = Long.parseLong(line.getOptionValue("sp"));
                String[] ipport = line.getOptionValue("c").split(":");
                th = new Thread(new Sink(cmdNet, cmdAddress, cmdPort,
                        new InetSocketAddress(ipport[0], Integer
                                .parseInt(ipport[1])), cmdTopo, cmdLevel,
                        cmdSDpid, cmdSMac, cmdSPort));
            } else {
                th = new Thread(new Mote(cmdNet, cmdAddress, cmdPort, cmdTopo,
                        cmdLevel));
            }
            th.start();
            th.join();
        } catch (InterruptedException | ParseException ex) {
            System.out.println("Parsing failed.  Reason: " + ex.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("sdn-wise-data -n id -a address -p port"
                            + " -t filename [-l level] [-sd dpid -sm mac -sp port]",
                    options);
        }
    }
}

