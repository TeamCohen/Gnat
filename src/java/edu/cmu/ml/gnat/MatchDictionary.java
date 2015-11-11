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
		System.err.println("\t help                                                - This message");
		System.err.println("\t save dictfile aliasFile1 aliasFile2 ...             - Load entity aliases into a dictionary");
		System.err.println("\tquery dictfile resultsFile queryFile1 queryFile2 ... - Run queries against a dictionary and save results");
		
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
	public void query(String[] args) throws FileNotFoundException, IOException {
		if (args.length<4) {
			System.err.println("Expected at least 3 file arguments");
			this.usage();
			System.exit(1);
		}
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
				int n = dict.lookup(0.1, line);
				for (int k=0; k<n; k++) {
					if (k>10) break;
					results.write(String.valueOf(in.getLineNumber()));
					results.write("\t");
					results.write(line);
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
		if (args.length == 0) args = new String[] {"help"};
		switch(CMD.valueOf(args[0])) {
		case help: d.usage(); break;
		case save: d.save(args); break;
		case query: d.query(args); break;
		}
	}
}
