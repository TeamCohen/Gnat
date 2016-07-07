package edu.cmu.ml.gnat;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import org.apache.log4j.Logger;

import com.wcohen.ss.DistanceLearnerFactory;
import com.wcohen.ss.api.StringDistanceLearner;
import com.wcohen.ss.expt.Blocker;
import com.wcohen.ss.expt.MatchData;
import com.wcohen.ss.expt.MatchData.InputFormatException;
import com.wcohen.ss.expt.MatchExpt;


public class MatchExptSet {
	Logger log = Logger.getLogger(MatchExptSet.class);
	Blocker blocker;
	StringDistanceLearner learner;
	BufferedReader read;
	String filename;
	PrintStream write;
	String[] commands;
	
	public MatchExptSet(String[] argv) {
		try {
			if (argv.length < 5) {
				System.err.println("Usage:\n\tblocker learner matchdatafile outputfile commands...");
				System.exit(1);
			}
			blocker = (Blocker)Class.forName(MatchExpt.BLOCKER_PACKAGE+argv[0]).newInstance();
		    learner = DistanceLearnerFactory.build( argv[1] );
		    filename = argv[2];
		    read = new BufferedReader(new FileReader(filename));
		    String outfile = argv[3];
		    if ("-".equals(outfile)) write = System.out;
		    else write = new PrintStream(new FileOutputStream(outfile));
		    commands = Arrays.copyOfRange(argv, 4, argv.length);
		    for (String c:commands) { if (!MatchExpt.commandSupported(c)) throw new RuntimeException("illegal command "+c); }
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void finishBatch(MatchData data, String batchid) throws IOException {
	    MatchExpt expt = new MatchExpt(data,learner,blocker);
	    for (String c : commands) {
	    	if (c.equals("-dump")) {
	    		write.println("# "+batchid);
                expt.dumpResultsAsStrings(write);
            } else if (c.equals("-dumpIds")) {
	    		write.println("# "+batchid);
                expt.dumpResultsAsIds(write);
            }
	    }
	}
	
	public void run() {
		try {
		    int linenum=0;
		    String batchid="";
		    MatchData data = null;
		    for (String line; (line = read.readLine()) != null; linenum++) {
		    	String[] parts = line.split("\t");
		    	if (parts.length < 4) throw new InputFormatException(filename,linenum,"MatchData file needs four fields: batchid, source, entityid, string. only "+parts.length+" fields found: "+line);
		    	if (!batchid.equals(parts[0])) { // then we're in a new batch
		    		if (!batchid.equals("")) { // then we have to finish the last batch
		    			finishBatch(data,batchid);
		    		}
		    		data = new MatchData();
		    		batchid = parts[0];
		    	}
		    	// MatchData has been set up; now add the instance:
		    	data.addInstance(parts[1],parts[2],parts[3]);
		    	if (log.isDebugEnabled()) log.debug("added "+parts[1]+"/"+parts[2]+"/"+parts[3]);
		    }
			finishBatch(data,batchid);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] argv) {
	    MatchExptSet set = new MatchExptSet(argv);
	    set.run();
	}

}
