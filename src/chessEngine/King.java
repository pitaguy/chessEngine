package chessEngine;

import java.util.LinkedList;

public class King extends Piece {

	static int[] rowDirs = {-1,-1,-1,0,0,1,1,1};
	static int[] colDirs = {-1,0,1,-1,1,-1,0,1};
	@Override
	LinkedList<Move> getMoves(Board board, int row, int col) {
		LinkedList<Move> moves = new LinkedList<Move>();
		
		for(int direction = 0; direction < rowDirs.length; direction++){
			int newRow = row+rowDirs[direction];
			int newCol = col+colDirs[direction];
			if(newRow >= 0 && newRow < 8 && newCol >= 0 && newCol < 8 &&
					(board.pieces[newRow][newCol].isPassable() || board.pieces[newRow][newCol].color != color)){
				moves.add(new Move(row,col,newRow,newCol));
			}
		}
		
		
		addCastleMove(row,col,1,board,moves);
		addCastleMove(row,col,-1,board,moves);
		
		
		return moves;
	}
	
	 void addCastleMove(int row, int col, int dir, Board board, LinkedList<Move> moves){
		if(row % 7 != 0) return;
		 
		int finalCol = 4+2*dir;
		int minCol= Math.min(col, finalCol);
		int maxCol = Math.max(col, finalCol);
		
		
		int rookCol= -1;
		
		for(int curCol = col; curCol < 8 && curCol >= 0; curCol += dir){
			if(board.pieces[row][curCol] instanceof Rook && !board.pieces[row][curCol].hasMoved){
				rookCol = curCol;
			}
		}
		
		if(rookCol == -1) return;
		
		for(int curCol=minCol; curCol <= maxCol; curCol++){
			Piece look= board.pieces[row][curCol];
			if(look.isAttacked[oppositeColor()]) return;
			if(!(look instanceof King || look instanceof Empty || curCol == rookCol)) return;
		}
		
		int newRookCol = 4 + dir;
		
		if(!(board.pieces[row][newRookCol] instanceof Empty) && !(board.pieces[row][newRookCol] instanceof King)) return;
		
		Move toadd = new Move(row,col,row,finalCol);
		toadd.castleRookECol= newRookCol;
		toadd.castleRookSCol = rookCol;
		moves.add(toadd);
		
	}
	
	
	@Override
	int gethash() {
		return 6;
	}
	@Override
	int getPointValue() {
		return 1000000;
	}
	@Override
	char getFenLet() {
		return 'k';
	}

}
