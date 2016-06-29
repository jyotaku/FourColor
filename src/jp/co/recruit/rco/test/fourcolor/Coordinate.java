package jp.co.recruit.rco.test.fourcolor;

import java.util.List;

import com.google.common.collect.Lists;

public class Coordinate {

	int x;
	int y;

	public Coordinate(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public List<Coordinate> getNeighborCoordinateList(int maxLength, int maxDepth) {
		List<Coordinate> availableCellList = Lists.newArrayList();
		if (x > 0) {
			availableCellList.add(new Coordinate(x - 1, y));
		}
		if (x + 1 < maxLength) {
			availableCellList.add(new Coordinate(x + 1, y));
		}
		if (y > 0) {
			availableCellList.add(new Coordinate(x, y - 1));
		}
		if (y + 1 < maxDepth) {
			availableCellList.add(new Coordinate(x, y + 1));
		}
		return availableCellList;
	}
	
	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	@Override
	public String toString() {
		return "Coordinate [x=" + x + ", y=" + y + "]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Coordinate other = (Coordinate) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

}