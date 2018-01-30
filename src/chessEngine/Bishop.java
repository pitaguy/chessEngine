package chessEngine;

public class Bishop extends DirectionalPiece {

	@Override
	int[] getColDirections() {
		return new int[]{-1,1,-1,1};
	}
	
	@Override
	int[] getRowDirections() {
		return new int[]{-1,1,1,-1};
	}

	@Override
	int gethash() {
		return 3;
	}

	@Override
	int getPointValue() {
		if(cylinderChess){
			return 500;
		}
		else return 300;
	}

	@Override
	char getFenLet() {
		return 'b';
	}

}
