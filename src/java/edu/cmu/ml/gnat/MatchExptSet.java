package edu.cmu.ml.gnat;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;
import com.wcohen.ss.DistanceLearnerFactory;
import com.wcohen.ss.api.StringDistanceLearner;
import com.wcohen.ss.expt.Blocker;
import com.wcohen.ss.expt.MatchData;
import com.wcohen.ss.expt.MatchData.InputFormatException;
import com.wcohen.ss.expt.MatchExpt;


public class MatchExptSet {
	Blocker blocker;
	StringDistanceLearner learner;
	BufferedReader read;
	String filename;
	String[] commands;
	
	public MatchExptSet(String[] argv) {
		try {
			blocker = (Blocker)Class.forName(MatchExpt.BLOCKER_PACKAGE+argv[0]).newInstance();
		    learner = DistanceLearnerFactory.build( argv[1] );
		    filename = argv[2];
		    read = new BufferedReader(new FileReader(filename));
		    commands = Arrays.copyOfRange(argv, 3, argv.length);
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
	    		System.out.println("# "+batchid);
                expt.dumpResultsAsStrings(System.out);
            } else if (c.equals("-dumpIds")) {
	    		System.out.println("# "+batchid);
                expt.dumpResultsAsIds(System.out);
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
