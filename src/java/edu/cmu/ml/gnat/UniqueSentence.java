package edu.cmu.ml.gnat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Convert documents from their sentences (one-per-line; use `geniass` for this)
 * to a list of sentence id codes, with the id-sentence mappings stored in a separate 
 * file. Important for datasets with lots of sentence duplication such as drug 
 * facts ("Consult your doctor before changing dosage" etc).
 * 
 * @author lbing
 * @author mingyanl
 * @author krivard
 */
public class UniqueSentence {

	public static Pattern PTN = Pattern.compile("<.+>");
	public static HashMap<Long, String> sentMap = new HashMap<Long, String>();

	public static void main(String[] args) throws IOException {
		if (args.length != 3) {
			System.out
					.println("SYNTAX:\n\tinputfile outputfile_documents outputfile_sentenceids [ignore_regex=<.+>]");
			System.exit(0);
		}
		if (args.length>3) {
			PTN = Pattern.compile(args[3]);
		}
		getUniqueSentence(args[0], args[1], args[2]);
	}

	public static void getUniqueSentence(String inputfile, String documentfile,
			String sentencecodefile) throws IOException {
		BufferedReader input = new BufferedReader(new FileReader(inputfile));
		BufferedWriter documents = new BufferedWriter(new FileWriter(documentfile));
		BufferedWriter sentencecodes = new BufferedWriter(new FileWriter(sentencecodefile));

		String line = input.readLine();

		while (line != null) {
			if (line.trim().length() == 0) {
				line = input.readLine();
				continue;
			}

			Matcher m = PTN.matcher(line);
			if (m.find()) {
				documents.write(line);
				documents.newLine();
			} else {
				long sentCode = getCode(line);
				if (!sentMap.containsKey(sentCode)) {
					sentMap.put(sentCode, line);
				}
				documents.write(sentCode + "");
				documents.newLine();
			}
			line = input.readLine();
		}

		for (Long key : sentMap.keySet()) {
			sentencecodes.write(key + "");
			sentencecodes.newLine();
			sentencecodes.write(sentMap.get(key));
			sentencecodes.newLine();
		}

		documents.close();
		input.close();
		sentencecodes.close();
	}

	public static long getCode(String sent) {
		String sentFirstHalf = sent.substring(0, sent.length() / 2);
		String sentSecondHalf = sent
				.substring(sent.length() / 2, sent.length());

		long sentCode = sentFirstHalf.hashCode();
		sentCode = (sentCode << Integer.SIZE);
		sentCode += sentSecondHalf.hashCode();
		return sentCode;
	}
}
