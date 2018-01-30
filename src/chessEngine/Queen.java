package chessEngine;

public class Queen extends DirectionalPiece {

	@Override
	int[] getColDirections() {
		return new int[]{-1,1,0,0,-1,1,-1,1};
	}
	
	@Override
	int[] getRowDirections() {
		return new int[]{0,0,-1,1,-1,1,1,-1};
	}

	@Override
	int gethash() {
		return 5;
	}

	@Override
	int getPointValue() {
		return 900;
	}

	@Override
	char getFenLet() {
		return 'q';
	}

}
