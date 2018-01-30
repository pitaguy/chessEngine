package chessEngine;

import java.util.LinkedList;

public class Pawn extends Piece {
	
		@Override
		LinkedList<Move> getMoves(Board board, int row, int col) {
			LinkedList<Move> moves= new LinkedList<Move>();
			
			int rowDirection = 2*color-1; //white (0) is -1, black (1) is 1
			int toRow = row + rowDirection;
			//Note: due to promotion, a pawn will not be able to move off the board via rows
			
			for(int attackCol = col-1; attackCol <= col +1; attackCol += 2){
				
				int newCol = attackCol;
				if(cylinderChess) newCol = (newCol+8)%8;
				
				if(newCol >= 0 && newCol < 8 && (!board.pieces[toRow][newCol].isPassable() && board.pieces[toRow][newCol].color != color) || 
						toRow == board.enPassRow && newCol == board.enPassCol){
					moves.add(new Move(row,col,toRow,newCol));
				}
				
			}
			
			
			if(board.pieces[toRow][col].isPassable()){
				moves.add(new Move(row,col,toRow,col));
				
				//pawn moving twice
				toRow += rowDirection;
				if((row == 1 || row == 6) && toRow >= 0 && toRow < 8 && board.pieces[toRow][col].isPassable()){
					moves.add(new Move(row,col,toRow,col));
				}
				
			}
			
			
			
			return moves;
		}

		@Override
		int gethash() {
			return 1;
		}

		@Override
		int getPointValue() {
			return 100;
		}

		@Override
		char getFenLet() {
			return 'p';
		}

}
