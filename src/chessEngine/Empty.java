package chessEngine;

import java.util.LinkedList;

public class Empty extends Piece {
		
	@Override
	boolean isPassable() {
		return true;
	}

	@Override
	LinkedList<Move> getMoves(Board board, int row, int col) {
		return new LinkedList<Move>();
	}

	@Override
	int gethash() {
		return 0;
	}

	@Override
	int getPointValue() {
		return 0;
	}

	@Override
	char getFenLet() {
		return '1';
	}
	

}
