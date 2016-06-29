package fourcolor.src;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Strings;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

/**
 *  問題1
 * 
 * @author jyotaku
 * @since 2016.5.25
 *
 */
public class FourColor {

	private final static List<String> COLOR_MARK = Lists.newArrayList("+", "-", "*", "/");
	private final String inputFileName;

	// 解を探索するためのデータモデル
	private final SetMultimap<String, String> searchModelMap = LinkedHashMultimap.create();
	// 解を保持するマップ
	private final Map<String, String> answerMap = Maps.newHashMap();
	// 入力のテキストファイルの元データを保持するリスト
	private List<char[]> charArrayList;

	public FourColor(String inputfileName) {
		this.inputFileName = inputfileName;
	}

	/**
	 * メインの処理関数
	 */
	public void run() {
		if (!fileExists()) {
			return;
		}

		createSearchModelMapFromTextfile();

		boolean searchResult = searchAnswer();

		printoutResult(searchResult);
	}

	/*
	 * ファイルの存在チェック
	 */
	private boolean fileExists() {
		File file = new File(inputFileName);
		boolean result = file.exists();
		if (!result) {
			System.out.println("There is no such file '" + inputFileName + "'.");
		}
		return result;
	}

	/*
	 * テキストファイルから探索用モデルを作成する
	 */
	private void createSearchModelMapFromTextfile() {

		try (FileReader fr = new FileReader(inputFileName); BufferedReader br = new BufferedReader(fr);) {

			charArrayList = br.lines().map(line -> line.toCharArray()).collect(Collectors.toList());
			int loopSize = charArrayList.size();

			if (isSpcialCaseCheck(loopSize)) {
				return;
			}
			;

			// 上下左右を見て、探索モデルを作成する
			for (int i = 0; i < loopSize; i++) {
				char[] lastArray = (i - 1 >= 0) ? charArrayList.get(i - 1) : null;
				char[] nextArray = (i + 1 < loopSize) ? charArrayList.get(i + 1) : null;
				char[] currentArray = charArrayList.get(i);
				int innerLoopSize = currentArray.length;
				for (int j = 0; j < innerLoopSize; j++) {
					String current = String.valueOf(currentArray[j]);
					String left = j > 0 ? String.valueOf(currentArray[j - 1]) : null;
					String right = j + 1 < innerLoopSize ? String.valueOf(currentArray[j + 1]) : null;
					String top = Objects.isNull(lastArray) ? null : String.valueOf(lastArray[j]);
					String bottom = Objects.isNull(nextArray) ? null : String.valueOf(nextArray[j]);

					Set<String> neighborSet = Sets.newHashSet(left, right, top, bottom);
					neighborSet.remove(current);
					neighborSet.remove(null);
					searchModelMap.putAll(current, neighborSet);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * ファイルの特別ケース ・Case 1 :空ファイルの場合 ・Case 2 :ファイルに１文字しかない場合
	 */
	private boolean isSpcialCaseCheck(int loopSize) {
		if (loopSize == 0) {
			return true;
		}
		if (loopSize == 1 && charArrayList.get(0).length == 1) {
			answerMap.put(String.valueOf(charArrayList.get(0)), COLOR_MARK.get(0));
			return true;
		}
		return false;
	}

	/*
	 * 探索するブロックの順番を決める
	 */
	private List<String> getSearchOrderList() {
		return searchModelMap.keySet().stream().collect(Collectors.toList());
	}

	/*
	 * 解を探索する
	 * 
	 * @return 探索結果を返す。解がある場合：True 解がない場合： False
	 */
	private boolean searchAnswer() {

		List<String> searchOrderList = getSearchOrderList();
		int searchOrderListSize = searchOrderList.size();

		// ４種類のブロック以下の場合、探索せずに解を作成する
		if (noMoreThanFourBlocks(searchOrderList)) {
			return true;
		}

		int blockIndex = 0;
		while (blockIndex < searchOrderListSize) {
			String blockName = searchOrderList.get(blockIndex);
			String availableColor = searchAvailableColor(blockName);

			// 解がなければ、一つ前のブロックの答えを変える
			if (Strings.isNullOrEmpty(availableColor)) {
				answerMap.remove(blockName);
				blockIndex--;
				if (blockIndex < 0) {
					return false;
				}
				continue;
			}

			// 解を記録する
			answerMap.put(blockName, availableColor);
			blockIndex++;
		}

		return true;
	}

	private boolean noMoreThanFourBlocks(List<String> searchOrderList) {
		int searchOrderListSize = searchOrderList.size();
		if (searchOrderListSize <= 4) {
			for (int i = 0; i < searchOrderListSize; i++) {
				answerMap.put(searchOrderList.get(i), COLOR_MARK.get(i));
			}
			return true;
		}
		return false;
	}

	/*
	 * 渡されたブロックに適用可能な色を探索する
	 * 
	 * @param blockName
	 * 
	 * @return 利用できる色を返す。 ・周囲のブロックが全ての色を利用し尽くした場合：null ・すでにCOLOR_MARK最後の解を試した場合
	 */
	private String searchAvailableColor(String blockName) {
		Set<String> usedColorSet = getUsedColor(blockName);

		List<String> answerCandidates = Lists.newArrayList(COLOR_MARK);

		// 解の候補からすでに利用済みの色を排除
		Iterator<String> usedColorIterator = usedColorSet.iterator();
		while (usedColorIterator.hasNext()) {
			String color = (String) usedColorIterator.next();
			answerCandidates.remove(color);
		}

		// COLOR_MARKの最初から解を探索
		int nextAnswerIdx = 0;
		String currentAnswer = answerMap.get(blockName);

		// blockNameにすでに探索した解がある場合,すでにある解の次の解を使う
		if (!Strings.isNullOrEmpty(currentAnswer)) {
			nextAnswerIdx = answerCandidates.indexOf(currentAnswer) + 1;
		}

		// 候補解なし、または候補リストの最後の項目が探索済み
		if (answerCandidates.isEmpty() || nextAnswerIdx > answerCandidates.size() - 1) {
			return null;
		}

		return answerCandidates.get(nextAnswerIdx);
	}

	/*
	 * 指定されたブロックと隣接ブロックで 利用されている色のリストを取得する
	 * 
	 * @param blockName
	 * 
	 * @return 色のリスト
	 */
	private Set<String> getUsedColor(String blockName) {
		Set<String> neighborBlockSet = searchModelMap.get(blockName);
		Set<String> usedColorSet = Sets.newHashSet();
		Iterator<String> nerghborIter = neighborBlockSet.iterator();
		while (nerghborIter.hasNext()) {
			String neighborBlock = (String) nerghborIter.next();
			String neighborColor = answerMap.get(neighborBlock);
			if (Strings.isNullOrEmpty(neighborColor)) {
				continue;
			}
			usedColorSet.add(neighborColor);
		}
		return usedColorSet;
	}

	/*
	 * 解を出力する
	 */
	private void printoutResult(boolean searchResult) {
		// consoleに結果出力
		if (!searchResult) {
			System.out.println("There is no answer for this question.");
			return;
		} else if (answerMap.isEmpty()) {
			System.out.println("Target file is empty.");
			return;
		} else {
			System.out.println("DONE");
		}

		// ファイルに解を出力する
		List<String> finalResult = convertModelToList();
		String answerFolderPath = Utils.createAnswerFolder(inputFileName);
		String outputFileName = Utils.getOutputFileName(answerFolderPath, inputFileName);
		try (OutputStream out = new FileOutputStream(outputFileName); PrintWriter writer = new PrintWriter(out);) {
			finalResult.forEach(writer::println);
			System.out.println("**** FINISH ****");
			System.out.println("Answer file path: " + outputFileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * 解モデルをファイルに書き出すための変換処理
	 * 
	 * @return answerMapをList<String>に変換した結果
	 */
	private List<String> convertModelToList() {
		List<String> finalResult = Lists.newArrayList();
		charArrayList.forEach(array -> {
			Set<String> keySet = Sets.newHashSet();
			for (char key : array) {
				keySet.add(String.valueOf(key));
			}
			String resultString = new String(array);
			Iterator<String> itr = keySet.iterator();
			while (itr.hasNext()) {
				String key = itr.next();
				String color = answerMap.get(key);
				if (Strings.isNullOrEmpty(color)) {
					continue;
				}
				resultString = resultString.replaceAll(key, color);
			}
			finalResult.add(resultString);
		});
		return finalResult;
	}

	/**
	 * main 関数
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length < 1) {
			System.out.println("Please set the input file path for the first parameter");
			return;
		}

		FourColor fouColor = new FourColor(args[0]);

		fouColor.run();
	}
}
