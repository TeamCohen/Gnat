package edu.cmu.ml.gnat.label;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;

import com.wcohen.ss.api.StringWrapper;
import com.wcohen.ss.lookup.SoftDictionary;
import com.wcohen.ss.lookup.SoftTFIDFDictionary;

public class NormalizeLabels {
	SoftDictionary dict = new SoftDictionary();
	public NormalizeLabels(String ontofile) throws IOException {
		LineNumberReader r = new LineNumberReader(new FileReader(ontofile));
		String line;
		// first read one non-comment line; this is the header
		for (int i=0; (line = r.readLine()) != null && i<1; ) {
			if (line.startsWith("#")) continue;
			i++;
		}
		// now read the rest of the file, one record per line
		while((line = r.readLine()) != null ) {
			line = line.trim();
			int n = line.indexOf('\t');
			n = n<0 ? line.length() : n;
			String key = line.substring(0,n);
			dict.put(key, key);
		}
		//dict.freeze();
		System.out.println("Imported "+r.getLineNumber()+" categories");
		r.close();
	}
	
	public void normalize(String labelfile) throws IOException {
		int n = labelfile.lastIndexOf('.'); n = n<0 ? labelfile.length() : n;
		String outfile = labelfile.substring(0, n) + ".norm" + labelfile.substring(n);
		LineNumberReader r = new LineNumberReader(new FileReader(labelfile));
		BufferedWriter w = new BufferedWriter(new FileWriter(outfile));
		for (String line; (line=r.readLine()) != null;) {
			if (line.startsWith("#")) continue;
			line = line.trim();
			String[] parts = line.split("\t");
			w.write(parts[0]);
			for (int i=1; i<parts.length; i++) {
//				int numresults = dict.lookup(0, parts[i]);
//				if (numresults == 0) {
//					System.out.println("No matches on "+labelfile+" line "+r.getLineNumber()+": "+parts[i]);
//					continue; // TODO: complain
//				}
//				w.write("\t"+dict.getValue(0));
				parts[i] = parts[i].trim();
				if (parts[i].length()<1) continue;
				StringWrapper result = ((StringWrapper)dict.lookup(parts[i]));
				if (result != null)
					w.write("\t"+result.unwrap());
				else System.out.println("No matches on "+labelfile+" line "+r.getLineNumber()+": "+parts[i]);
			}
			w.write("\n");
		}
		r.close();w.close();
	}

	public static void main(String[] args) throws IOException {
		if (args.length < 2) {
			System.out.println("Usage:\n\tontology.tsv labelfile-1.tsv [... labelfile-n.tsv]\n");
			System.out.println("Output:\n\tlabelfile-1.norm.tsv [... labelfile-n.norm.tsv]\n");
			System.out.println("Ontology format: tsv with first field the category name\n");
			System.out.println("Label format: tsv with fields:\n\tentity cat1 [... catN]");
			System.exit(1);
		}
		NormalizeLabels norm = new NormalizeLabels(args[0]);
		for (int i=1; i<args.length; i++) {
			norm.normalize(args[i]);
		}
	}

}
