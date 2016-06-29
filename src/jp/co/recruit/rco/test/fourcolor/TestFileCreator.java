package jp.co.recruit.rco.test.fourcolor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
/**
 * 問題1
 * テストファイルを生成する
 * 
 * @author jyotaku
 * @since 2016.5.25
 *
 */
public class TestFileCreator {
	public static void main(String[] args) {
		int testFileNumber = 100;
		for (int i = 0; i < testFileNumber; i++) {
			TestFileCreator testFileCreator = new TestFileCreator();
			testFileCreator.createTestData(i);
		}
	}

	private final static int MAX_LENGTH = 30;
	private final static int MAX_DEPTH = 30;
	private final static String EMPTY_SPACE = "_";
	private final static int MAX_BLOCK_NUMBER = 62;
	// private final static int MIN_BLOCK_NUMBER = 15;

	private final List<String> keyList = Lists.newArrayList();
	private String[][] worldMap = new String[MAX_LENGTH][MAX_DEPTH];
	private ListMultimap<String, Coordinate> boundaryMap = ArrayListMultimap.create();

	public TestFileCreator() {
		// 0-9
		for (int i = 48; i <= 57; i++) {
			String number = Character.toString((char) i);
			keyList.add(number);
		}
		// A-Z
		for (int i = 65; i <= 90; i++) {
			String upperCase = Character.toString((char) i);
			keyList.add(upperCase);
		}
		// a-z
		for (int i = 97; i <= 122; i++) {
			String lowerCase = Character.toString((char) i);
			keyList.add(lowerCase);
		}

		for (int i = 0; i < MAX_LENGTH; i++) {
			for (int j = 0; j < MAX_DEPTH; j++) {
				worldMap[i][j] = EMPTY_SPACE;
			}
		}
	}

	public void createTestData(int fileIdx) {

		int blockNumber = MAX_BLOCK_NUMBER;

		initializeBoundary(blockNumber);

		while (true) {
			if (isFinish()) {
				break;
			}

			List<String> currentKeyList = boundaryMap.keySet().stream().collect(Collectors.toList());
			int targetBlockIdx = getRandomIdx(currentKeyList.size());
			String targetKey = currentKeyList.get(targetBlockIdx);

			expandTerritory(targetKey);
		}

		outputResultFile(fileIdx);
		// printOutWorldMap();
	}

	private int getRandomIdx(int maxValue) {
		return (int) (Math.random() * maxValue);
	}

	private void expandTerritory(String targetKey) {

		List<Coordinate> targetBoundary = boundaryMap.get(targetKey);

		Coordinate expandPoint = targetBoundary.get(getRandomIdx(targetBoundary.size()));

		List<Coordinate> availableCoordinateList = getAvailableCoordinate(expandPoint);

		Coordinate newCoordinate = availableCoordinateList.get(getRandomIdx(availableCoordinateList.size()));

		registerNewSpace(targetKey, newCoordinate);
	}

	private void initializeBoundary(int blockNumber) {
		for (int i = 0; i < blockNumber; i++) {
			String key = keyList.get(i);
			Coordinate coordinate = new Coordinate(getRandomIdx(MAX_LENGTH), getRandomIdx(MAX_DEPTH));
			while (!EMPTY_SPACE.equals(worldMap[coordinate.x][coordinate.y])) {
				coordinate = new Coordinate(getRandomIdx(MAX_LENGTH), getRandomIdx(MAX_DEPTH));
			}
			registerNewSpace(key, coordinate);
		}
	}

	private void registerNewSpace(String key, Coordinate coordinate) {
		worldMap[coordinate.x][coordinate.y] = key;
		boundaryMap.put(key, coordinate);
		adjustBoundary(coordinate);

		// For search bug
		// boundaryMap.keySet().forEach(testKey->{
		// List<Coordinate> boundary = boundaryMap.get(testKey);
		// boundary.forEach(testCoordinate->{
		// List<Coordinate> availableCoordinateList =
		// getAvailableCoordinate(testCoordinate);
		// if(availableCoordinateList.isEmpty()){
		// printOutWorldMap();
		// System.out.println("Error");
		// }
		// });
		// });
	}

	private boolean isFinish() {
		return boundaryMap.isEmpty();
	}

	private void adjustBoundary(Coordinate coordinate) {

		List<Coordinate> checkList = coordinate.getNeighborCoordinateList(MAX_LENGTH, MAX_DEPTH);
		checkList.add(coordinate);
		checkList.forEach(neighbor -> {
			String key = worldMap[neighbor.x][neighbor.y];
			if (EMPTY_SPACE.equals(key)) {
				return;
			}
			List<Coordinate> neighborList = neighbor.getNeighborCoordinateList(MAX_LENGTH, MAX_DEPTH);
			boolean isAavailable = neighborList.stream().anyMatch(checkCoordinate -> {
				return EMPTY_SPACE.equals(worldMap[checkCoordinate.x][checkCoordinate.y]);
			});

			if (!isAavailable) {
				boolean removeResult = boundaryMap.remove(key, neighbor);
				if (!removeResult) {
					printOutWorldMap();
					System.out.println("Remove failed!");
				}
				List<Coordinate> boundary = boundaryMap.get(key);
				if (boundary.isEmpty()) {
					boundaryMap.removeAll(key);
				}
			}

		});
	}

	private List<Coordinate> getAvailableCoordinate(Coordinate expandPoint) {
		List<Coordinate> neighborList = expandPoint.getNeighborCoordinateList(MAX_LENGTH, MAX_DEPTH);

		List<Coordinate> result = neighborList.stream().filter(neighbor -> {
			return EMPTY_SPACE.equals(worldMap[neighbor.x][neighbor.y]);
		}).collect(Collectors.toList());

		return result;
	}

	private void outputResultFile(int fileIdx) {
		String dir = System.getProperty("user.dir");
		String outputFileName = dir + File.separator + "testData" 
		        + File.separator 
		        + "_" + MAX_LENGTH 
		        + "_" + MAX_DEPTH
		        + "_" + MAX_BLOCK_NUMBER + "_" + fileIdx + ".txt";

		try (OutputStream out = new FileOutputStream(outputFileName); PrintWriter writer = new PrintWriter(out);) {
			for (int i = 0; i < MAX_LENGTH; i++) {
				String line = Joiner.on("").join(worldMap[i]);
				writer.println(line);
			}
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	private void printOutWorldMap() {
		for (int i = 0; i < MAX_LENGTH; i++) {
			String line = Joiner.on("").join(worldMap[i]);
			System.out.println(line);
		}
	}
}
