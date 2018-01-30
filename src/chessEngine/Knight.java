package chessEngine;

import java.util.LinkedList;

public class Knight extends Piece {
	static int[] rowDirs = {-2,-2,-1,-1,1, 1,2,2};
	static int[] colDirs = {1, -1, 2,-2,2,-2,1,-1};
	@Override
	LinkedList<Move> getMoves(Board board, int row, int col) {
		LinkedList<Move> moves = new LinkedList<Move>();
		for(int direction = 0; direction < rowDirs.length; direction++){
			int newRow = row+rowDirs[direction];
			int newCol = col+colDirs[direction];
			
			if(cylinderChess) newCol = (newCol+8)%8;
			
			if(newRow >= 0 && newRow < 8 && newCol >= 0 && newCol < 8 &&
					(board.pieces[newRow][newCol].isPassable() || board.pieces[newRow][newCol].color != color)){
				moves.add(new Move(row,col,newRow,newCol));
			}
		}
		return moves;
	}
	@Override
	int gethash() {
		return 2;
	}
	@Override
	int getPointValue() {
		return 300;
	}
	@Override
	char getFenLet() {
		return 'n';
	}
	
}
