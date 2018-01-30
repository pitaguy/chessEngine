package chessEngine;

public class Rook extends DirectionalPiece {
	
	@Override
	int[] getColDirections() {
		return new int[]{-1,1,0,0};
	}
	
	@Override
	int[] getRowDirections() {
		return new int[]{0,0,-1,1};
	}

	@Override
	int gethash() {
		return 4;
	}

	@Override
	int getPointValue() {
		return 500;
	}

	@Override
	char getFenLet() {
		return 'r';
	}

}
