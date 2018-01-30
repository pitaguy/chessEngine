//Gabriel Pita
package chessEngine;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Scanner;

public class Board {
	Piece[][] pieces;
	int nextMoveColor;
	int[] kingRow;
	int[] kingCol;
	
	int enPassRow = -1;
	int enPassCol = -1;
	int movesSinceProgress = 0;
	int turn = 0;
	
	public Board(){
		pieces = new Piece[8][8];
	}
	
	public Board(Board toCopy){
		pieces = new Piece[8][8];
		for(int i =0; i < 8; i++){
			for(int j =0; j < 8; j++){
				pieces[i][j] = toCopy.pieces[i][j].getClone();
			}
		}
		kingRow = Arrays.copyOf(toCopy.kingRow, 2);
		kingCol = Arrays.copyOf(toCopy.kingCol, 2);
		nextMoveColor = toCopy.nextMoveColor;
		movesSinceProgress = toCopy.movesSinceProgress;
		turn = toCopy.turn;
	}
	
	void updateIsAttacked(){
		
		for(int row = 0; row < 8; row++){
			for(int col=0; col < 8; col++){
				for(int color = 0; color < 2; color++)
				pieces[row][col].isAttacked[color]= false;
			}
		}
		
		for(int row = 0; row < 8; row++){
			for(int col=0; col < 8; col++){
				for(Move move : pieces[row][col].getMoves(this, row, col)){
					if(!(pieces[row][col] instanceof Pawn) || move.endCol != col){ //Some pawn moves are not attacks
							pieces[move.endRow][move.endCol].isAttacked[ pieces[row][col].color ] = true;
					}
				}
			}
		}
		
	}
	
	@Override
	public int hashCode(){
		int hash = nextMoveColor;
		
		for(int row =0; row < 8; row++){
			for(int col = 0; col < 8; col++){
				hash = hash*31 + pieces[row][col].gethash();
			}
		}
		
		return hash;
	}
	
	@Override
	public boolean equals(Object o) {
		Board other = (Board) o;
		
		if(nextMoveColor != other.nextMoveColor) return false;
		
		for(int row =0; row < 8; row++){
			for(int col = 0; col < 8; col++){
				if(!pieces[row][col].equals(other.pieces[row][col])){
					return false;
				}
			}
		}
		
		return true;
	}
	
	//Returns null if the board is illegal, otherwise returns the resulting board
	Board isIllegal(Move move){
		Board copyBoard = new Board(this);
		copyBoard.makeMove(move);

		
		Piece king = copyBoard.pieces[ copyBoard.kingRow[nextMoveColor] ][ copyBoard.kingCol[nextMoveColor] ];
		assert king instanceof King;
		
		return king.isAttacked[king.oppositeColor()] ? null : copyBoard;
		

	}
	
	void setKingPositions(){
		kingRow = new int[2];
		kingCol = new int[2];
		
		for(int row = 0; row < 8; row++){
			for(int col = 0; col < 8; col++){
				if(pieces[row][col] instanceof King){
					int color = pieces[row][col].color;
					kingRow[color] = row;
					kingCol[color] = col;
				}
			}
		}
	}
	
	void makeMove(Move move){
		enPassRow = -1;
		enPassCol = -1;
		movesSinceProgress++;
		
		//castling
		if(move.castleRookSCol != -1){
			//move.endcol is the position of the rook for the ui's sake
			//to make the castling move unambiguous.
	
				Piece rook = pieces[move.endRow][move.castleRookSCol];
				Piece king = pieces[move.sourceRow][move.sourceCol];
				
				pieces[move.endRow][move.castleRookSCol] = new Empty();
				pieces[move.sourceRow][move.sourceCol] = new Empty();
				
				pieces[move.endRow][move.castleRookECol] = rook;
				rook.hasMoved = true;
				
				pieces[move.endRow][move.endCol] = king;
				king.hasMoved = true;
				
				setKingPositions();
		}
		else{
			if(!(pieces[move.endRow][move.endCol] instanceof Empty)) movesSinceProgress = 0;
			
			boolean enpassantHappening = pieces[move.sourceRow][move.sourceCol] instanceof Pawn && move.endCol != move.sourceCol &&
					pieces[move.endRow][move.endCol] instanceof Empty;
			
			pieces[move.endRow][move.endCol] = pieces[move.sourceRow][move.sourceCol];
			pieces[move.endRow][move.endCol].hasMoved = true;
			
			if(pieces[move.endRow][move.endCol] instanceof Pawn){
				movesSinceProgress = 0;
				//setup for possible enpassant
				if((move.endRow+move.sourceRow)%2 == 0){
					enPassRow = (move.endRow+move.sourceRow)/2;
					enPassCol = move.endCol;
				}
				
				
				//promotion
				if(move.endRow == 0 || move.endRow == 7){
					int color = pieces[move.endRow][move.endCol].color;
					pieces[move.endRow][move.endCol] = new Queen();
					pieces[move.endRow][move.endCol].color = color;
				}
			}
			
			pieces[move.sourceRow][move.sourceCol] = new Empty();
			if(enpassantHappening){
				//safe to clear out both since these squares are either newly captured or where the pawn just moved from
				pieces[move.endRow+1][move.endCol] = new Empty();
				pieces[move.endRow-1][move.endCol] = new Empty();
			}
			
		}
		
		
		if(pieces[move.endRow][move.endCol] instanceof King) setKingPositions();
		
		nextMoveColor = 1-nextMoveColor;
		turn++;
		updateIsAttacked();
	}
	
	
	boolean kingAttacked(){
		return pieces[ kingRow[nextMoveColor] ][ kingCol[nextMoveColor] ].isAttacked[1-nextMoveColor];
	}
	
	boolean thereAreNoMoves(){
		for(int row = 0; row < 8; row++){
			for(int col =0; col < 8; col++){
				if(pieces[row][col].color == nextMoveColor){
					LinkedList<Move> moves =pieces[row][col].getMoves(this, row, col);
					for(Move move : moves){
						//checking for illegal moves
						Board result = isIllegal(move);
						if(result != null){
							return false;
						}
					}
				}
			}
		}
		return true;
	}
	
	
	int evaluate(int color){
		int sum = 0; //100 is equivalent to pawn value
		
		if(thereAreNoMoves()){
			if(kingAttacked()){
				return color == nextMoveColor ? -(int)(5e8) : (int)(5e8); //checkmate
			}
			else return 0; //stalemate
		}
		
		boolean opponentOnlyKing = true;
		
		for(int row = 0; row < 8; row++){
			for(int col = 0; col < 8; col++){
				if(pieces[row][col].color == color){
					
					sum += pieces[row][col].getPointValue();
					
					if(pieces[row][col].hasMoved){
						if(pieces[row][col] instanceof Knight || pieces[row][col] instanceof Bishop){
							sum += 20;
						}
						else if(pieces[row][col] instanceof Queen && turn <= 8){
							sum -= 100;
						}
						else if(pieces[row][col] instanceof Pawn && (col == 3 || col == 4)){
							sum += 20;
						}
							
					}
					
				}
				else{
					if(!(pieces[row][col] instanceof King) && !(pieces[row][col] instanceof Empty)){
						opponentOnlyKing = false;
					}
				}
				
				//more points for controlling squares near the center
				if(pieces[row][col].isAttacked[color]){
					if(Piece.cylinderChess){
						sum += 12 -Math.abs(7-2*row);
					}
					else{
						sum += 15-(Math.abs(7-2*row)+Math.abs(7-2*col)); 
					}
				}
			}
		}
		
		if(opponentOnlyKing) sum += endGameBonus(color);
		else{
			//points for attacking squares around the opponent king
			for(int direction =0; direction < King.rowDirs.length; direction++){
				int checkRow = kingRow[1-color] + King.rowDirs[direction];
				if(checkRow >= 0 && checkRow < 8){
					int checkCol = kingCol[1-color] + King.colDirs[direction];
					if(checkCol >= 0 && checkCol < 8 && pieces[checkRow][checkCol].isAttacked[color]){
						sum += 12;
					}
				}
			}
		}
		
		return sum;
	}
	
	//meant to aid in checkmating
	int endGameBonus(int color){
		int sum = 12*9;
		
		//king away from the center
		sum += 30*(Math.abs(7-2*kingRow[1-color]));
		sum += 15*(Math.abs(7-2*kingCol[1-color]));
		
		//keep the kings close
		sum += 6*24;
		sum -= 16*Math.abs(kingRow[color]-kingRow[1-color]);
		sum -= 8*Math.abs(kingCol[color]-kingCol[1-color]);
		
		
		return sum;
	}
	
	//This is the board position code
	String getFen(){
		String fen = "";
		for(int row = 0; row < 8; row++){
			int run = 0;
			for(int col= 0; col < 8; col++){
				char letter = pieces[row][col].getFenLet();
				if(letter == '1'){
					run++;
				}
				else{
					if(run > 0){
						fen+=(char)('0'+run);
						run = 0;
					}
					if(pieces[row][col].color == Piece.white) letter = Character.toUpperCase(letter);
					fen+=letter;
				}
			}
			if(run > 0){
				fen+=(char)('0'+run);
				run = 0;
			}
			if(row != 7) fen +='/';
		}
		
		if(nextMoveColor == Piece.white){
			fen += " w ";
		}
		else{
			fen += " b ";
		}
		
		fen += "KQkq ";
		
		if(enPassCol != -1){
			fen += (char)('a'+enPassCol);
			fen += (8-enPassRow);
		}
		else fen += "-";
		
		fen += " " + movesSinceProgress + " ";
		fen += turn/2+1;
		
		return fen;
	}
	
	//Used by the board editor
	boolean isValid(){
		int whiteKing = 0;
		int blackKing = 0;
		
		for(int row = 0; row < 8; row++){
			for(int col = 0; col < 8; col++){
				Piece pieceHere = pieces[row][col];
				if(pieceHere instanceof King){
					if(pieceHere.color == Piece.white){
						whiteKing++;
					}
					else{
						blackKing++;
					}
					
					//You can't be in check if it is not your turn to move
					if(pieceHere.color != nextMoveColor && pieceHere.isAttacked[pieceHere.oppositeColor()]){
						return false;
					}
					
				}
			}
		}
		
		return whiteKing == 1 && blackKing == 1;
		
	}
	
	static Board importBoard(String fen){
		Board board = new Board();
		
		String pieceTypes = "rnbqkpRNBQKP";
		Piece[] templates= {new Rook(), new Knight(), new Bishop(), new Queen(), new King(), new Pawn(),
				new Rook(), new Knight(), new Bishop(), new Queen(), new King(), new Pawn()};
		for(int i = 6; i < templates.length; i++) templates[i].color = Piece.white;
		
		Scanner reader = new Scanner(fen);
		String[] locations = reader.next().split("/");
		
		for(int row = 0; row < 8; row++){
			int col = 0;
			for(int i=0; i < locations[row].length(); i++){
				char at = locations[row].charAt(i);
				int index = pieceTypes.indexOf(at);
				if(index != -1){
					board.pieces[row][col++] = templates[index].getClone();
				}
				else{
					int num = at-'0';
					while(num --> 0){
						board.pieces[row][col++] = new Empty();
					}
				}
			}
		}
		
		if(reader.next().charAt(0) == 'w'){
			board.nextMoveColor = Piece.white;
		}
		else{
			board.nextMoveColor = Piece.black;
		}
		
		reader.next();
		
		String enpass = reader.next();
		if(enpass.charAt(0) != '-'){
			board.enPassCol = enpass.charAt(0)-'a';
			board.enPassRow = 8-(enpass.charAt(1)-'0');
		}
		
		board.movesSinceProgress = reader.nextInt();
		
		//I'm resetting the turn on import
		board.turn = 0;
		
		//if you don't want to reset it (breaks undo button)
		//board.turn = (reader.nextInt()-1)*2 + 1-board.nextMoveColor; 
		
		reader.close();
		
		return board;
	}
}
