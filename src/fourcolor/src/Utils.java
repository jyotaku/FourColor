package fourcolor.src;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.io.Files;

/**
 * 問題1
 * ファイル名を処理するためのUtil
 * @author jyotaku
 * @since 2016.5.25
 *
 */
public class Utils {
	
	private final static String ANSWER_FOLDER_NAME = "answer";
	private final static String ANSWER_FILE_NAME_SUFFIX = "_answer";
	private final static String ANSWER_FILE_EXTENSION = ".txt";

	public static String createAnswerFolder(String inputFileName){
		String answerFolder = getAnswerFloder(inputFileName);
		File newdir = new File(answerFolder);
		newdir.mkdir();
		return answerFolder;
	}
	
	public static String getAnswerFloder(String inputFileName){
		Path outputFilePath = Paths.get(inputFileName);
		Path parent = outputFilePath.getParent();
		String anwserFolderPath = parent.toString() 
				+ File.separator 
				+ ANSWER_FOLDER_NAME;
		return anwserFolderPath;
	}
	
	public static String getOutputFileName(String answerFolderPath, String inputFileName) {
		String answerFileName = answerFolderPath
				+ File.separator 
				+ Files.getNameWithoutExtension(inputFileName)
				+ ANSWER_FILE_NAME_SUFFIX
				+ ANSWER_FILE_EXTENSION;

		return answerFileName; 
	}
}
