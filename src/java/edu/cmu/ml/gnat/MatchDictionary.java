package edu.cmu.ml.gnat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;

import com.wcohen.ss.lookup.SoftTFIDFDictionary;

/**
 * Commandline interface to a secondstring SoftTFIDFDictionary for storing and lookups.
 * 
 * Alias files are tab-separated:
 *   <entityid> <surface-form-1> <surface-form-2> ...
 *  
 * Query files are tab-separated:
 *   <queryid> <query-string>
 *   
 * Results files are tab-separated:
 *   <queryid> <entityid> <matchscore>
 * 
 * @author krivard
 *
 */
public class MatchDictionary {
	public enum CMD {
		save,
		query,
		help
	};
	public void usage() {
		System.err.println("Usage:");
		System.err.println("\t   help                                                - This message");
		System.err.println("\t   save dictfile aliasFile1 aliasFile2 ...             - Load entity aliases into a dictionary");
		System.err.println("\t  query dictfile resultsFile queryFile1 queryFile2 ... - Run queries against a dictionary and save results");
		System.err.println("\tquery+N dictfile resultsFile queryFile1 queryFile2 ... - Run queries against a dictionary and save top N results");
		
	}
	public void save(String[] args) throws FileNotFoundException, IOException {
		if (args.length<3) {
			System.err.println("Expected at least 2 file arguments");
			this.usage();
			System.exit(1);
		}
		File saveFile = new File(args[1]);
		SoftTFIDFDictionary dict = new SoftTFIDFDictionary();
		for (int i=2; i<args.length; i++) {
			System.err.println("Reading aliases from file "+args[i]+"...");
			dict.loadAliases(new File(args[i]));
		}
		dict.freeze();
		dict.saveAs(saveFile);
	}
	public void query(String[] args, String opt) throws FileNotFoundException, IOException {
		if (args.length<4) {
			System.err.println("Expected at least 3 file arguments");
			this.usage();
			System.exit(1);
		}
		int top = -1;
		if (opt != null) top = Integer.parseInt(opt);
		System.err.println("Loading dictionary from file "+args[1]+"...");
		SoftTFIDFDictionary dict = SoftTFIDFDictionary.restore(new File(args[1]));
		BufferedWriter results = new BufferedWriter(new FileWriter(args[2]));
		
		long last,now;
		for (int i=3; i<args.length; i++) {
			System.err.println("Reading queries from file "+args[i]+"...");
			last = System.currentTimeMillis();
			LineNumberReader in = new LineNumberReader(new FileReader(args[i]));
			for (String line; (line = in.readLine()) != null;) {
				now = System.currentTimeMillis();
				if (now-last > 5000) {
					System.err.println("query "+in.getLineNumber()+"...");
					last=now;
				}
				line = line.trim();
				if (line.startsWith("#")) continue;
				int sep = line.indexOf("\t");
				String query = line.substring(sep+1);
				String qid = line.substring(0,sep);
				int n = dict.lookup(0.001, query);
				for (int k=0; k<n; k++) {
					if (top>0 && k>top) break;
					results.write(qid);
					results.write("\t");
					results.write((String) dict.getValue(k));
					results.write("\t");
					results.write(String.valueOf(dict.getScore(k)));
					results.write("\n");
				}
			}
			in.close();
		}
		results.close();
	}
	public static final void main(String[] args) throws FileNotFoundException, IOException {
		MatchDictionary d = new MatchDictionary();
		String cmd = args.length == 0 ? "help" : args[0];
		String opt = null;
		if (cmd.indexOf("+")>0) {
			int i = cmd.indexOf("+");
			opt = cmd.substring(i+1);
			cmd = cmd.substring(0,i);
		}
		switch(CMD.valueOf(cmd)) {
		case help: d.usage(); break;
		case save: d.save(args); break;
		case query: d.query(args,opt); break;
		}
	}
}
