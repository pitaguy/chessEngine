package chessEngine;

import java.util.LinkedList;

public abstract class DirectionalPiece extends Piece {
	abstract int[] getRowDirections();
	abstract int[] getColDirections();
	
	@Override
	LinkedList<Move> getMoves(Board board, int row, int col) {
		int[] rowDirs = getRowDirections();
		int[] colDirs = getColDirections();
		
		assert rowDirs.length == colDirs.length;
		LinkedList<Move> moves= new LinkedList<Move>();
		for(int direction = 0; direction < rowDirs.length; direction++){
			int curRow = row+rowDirs[direction];
			int curCol = col+colDirs[direction];
			
			if(cylinderChess) curCol = (curCol+8)%8;
			
			while(curRow >= 0 && curRow < 8 && curCol >= 0 && curCol < 8
					&& (board.pieces[curRow][curCol].isPassable() || board.pieces[curRow][curCol].color != color)){
				
				moves.add(new Move(row,col,curRow,curCol));
				if(!board.pieces[curRow][curCol].isPassable() && board.pieces[curRow][curCol].color != color) break;
				curRow +=rowDirs[direction];
				curCol += colDirs[direction];
				
				if(cylinderChess) curCol = (curCol+8)%8;
				
			}
			
		}
		
		return moves;
		
	}
	
}
