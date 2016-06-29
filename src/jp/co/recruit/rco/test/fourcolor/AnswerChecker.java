package jp.co.recruit.rco.test.fourcolor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import fourcolor.src.FourColor;
import fourcolor.src.Utils;

/**
 * 問題1
 * 得られた解答ファイルの内容をチェックする
 * 
 * @author jyotaku
 * @since 2016.5.25
 *
 */
public class AnswerChecker {

	public static void main(String[] args) {

		String dir = System.getProperty("user.dir");
		String path = dir + File.separator + "testData";

		// Create answer file
		File file = new File(path);
		File[] files = file.listFiles();
		for (int i = 0; i < files.length; i++) {
			try {
				File questionFile = files[i];
				if (questionFile.isDirectory() || ".DS_Store".equals(questionFile.getName())) {
					continue;
				}
				String inputFileName = questionFile.getName();

				System.out.println(inputFileName);
				
				FourColor fouColorTheorem = new FourColor(questionFile.getPath());
				fouColorTheorem.run();

				// check answer
				System.out.println("---- " + inputFileName + " Check Start ----");
				CheckResult rlt = checkAnswerFile(questionFile.getPath());

				switch (rlt) {
				case GOOD_JOB:
					System.out.println("Good Job");
					break;
				case SAME_BLOCK_DIFF_COLOR:
					System.out.println("Incorrect Answer: Same block different color");
					break;
				case NEIGHBOR_BLOCK_SAME_COLOR:
					System.out.println("Incorrect Answer: Neighbor block same color");
					break;
				default:
					break;
				}

				System.out.println("---- " + inputFileName + " ----");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static CheckResult checkAnswerFile(String inputFileName) {
		Map<String, String> answerMap = Maps.newHashMap();

		List<char[]> questionCharList = getFileCharList(inputFileName);
		String answerFolder = Utils.getAnswerFloder(inputFileName);
		String answerFileName = Utils.getOutputFileName(answerFolder, inputFileName);
		List<char[]> answerCharList = getFileCharList(answerFileName);

		for (int j = 0; j < questionCharList.size(); j++) {
			char[] charArray = questionCharList.get(j);
			for (int i = 0; i < charArray.length; i++) {
				String q = String.valueOf(charArray[i]);
				String a = String.valueOf(answerCharList.get(j)[i]);

				// 同じブロックに同じ色
				String otherAnswer = answerMap.get(q);
				if (Strings.isNullOrEmpty(otherAnswer)) {
					answerMap.put(q, a);
				} else {
					if (!otherAnswer.equals(a)) {
						return CheckResult.SAME_BLOCK_DIFF_COLOR;
					}
				}

				// 隣接ブロックに異なる色
				Coordinate qCoordinate = new Coordinate(i, j);
				List<Coordinate> neighborList = qCoordinate.getNeighborCoordinateList(charArray.length,
						questionCharList.size());
				boolean isWrong = neighborList.stream().anyMatch(coordinate -> {
				
					String qNeighbor = String.valueOf(questionCharList.get(coordinate.getY())[coordinate.getX()]);
					
					String aNeighbor = String.valueOf(answerCharList.get(coordinate.getY())[coordinate.getX()]);
					
					return !qNeighbor.equals(q) && aNeighbor.equals(a);
				});
				
				if (isWrong) {
					return CheckResult.NEIGHBOR_BLOCK_SAME_COLOR;
				}
			}
		}

		return CheckResult.GOOD_JOB;
	}

	private static List<char[]> getFileCharList(String inputFileName) {
		try (FileReader fr = new FileReader(inputFileName); BufferedReader br = new BufferedReader(fr);) {
			List<char[]> charArrayList = br.lines().map(line -> line.toCharArray()).collect(Collectors.toList());
			return charArrayList;
		} catch (IOException e) {
			System.out.println(e);
		}
		return Lists.newArrayList();
	}
}
