//Gabriel Pita
package chessEngine;

import java.util.HashMap;
import java.util.LinkedList;


public class Minimax implements ComputerPlayer {
	static final int inf = (int)(1e9);
	HashMap<Board,Integer> countVisited;
	HashMap<Board,Move>[] hashMemo;
	boolean nullMovePruning = true;
	int maxDepth;
	
	public Minimax(int maxDepth) {
		this.maxDepth = maxDepth;
		hashMemo = new HashMap[maxDepth];
		for(int i =0; i < hashMemo.length; i++) hashMemo[i] = new HashMap<Board,Move>();
	}
	
	public Move getCpuMove(Board curBoard, HashMap<Board,Integer> countVisited){
		this.countVisited= countVisited;
		int defaultAncestorValue = curBoard.nextMoveColor == Piece.black ? -inf : inf;
    	Move best = calculateMove(curBoard, 0,defaultAncestorValue,false);
       	
		System.out.println("calcuated evaluation: " + best.evaluation);
    	
    	return best;
	}
	
	//bestAncestor and canPrune are for alpha beta prunning
	Move calculateMove(Board board, int depth, int bestAncestor, boolean canPrune){
		if(depth == maxDepth){
			return new Move(board.evaluate(Piece.white)-board.evaluate(Piece.black));
		}
		if(board.thereAreNoMoves()){
			 //current color is checkmated
			if(board.kingAttacked()) return new Move(board.nextMoveColor == Piece.white ? -inf : inf);
			
			//stalemate
			else return new Move(0); 
		}
		
		if(hashMemo[depth].get(board) != null){
			return hashMemo[depth].get(board);
		}
		
		
		//White wants to find the move that leads to the most positive evaluation, so the default value is 
		//negative infinity. Vice versa for black.
		Move bestSoFar = new Move(board.nextMoveColor == Piece.white ? -inf : inf);
		boolean foundMove = false;
		
		
		for(int row = 0; row < 8; row++){
			for(int col =0; col < 8; col++){
				if(board.pieces[row][col].color == board.nextMoveColor){
					LinkedList<Move> moves =board.pieces[row][col].getMoves(board, row, col);
					for(Move move : moves){
						//checking for illegal moves
						Board result = board.isIllegal(move);
						if(result != null){
														
							Move contenderMove = calculateMove(result,depth+1,bestSoFar.evaluation,foundMove);
														
							if(contenderMove != null){
								
								//if(depth == 0) System.out.println("not null: " + bestContender.evaluation);
								
								Integer threeFold= countVisited.get(result);
								if(threeFold != null && threeFold >= 2 || result.movesSinceProgress >= 100){
									//draws by three fold repetition or 50 move rule
									contenderMove.evaluation = 0;
								}
																
								if(board.nextMoveColor == Piece.black && contenderMove.evaluation <= bestSoFar.evaluation ||
								   board.nextMoveColor == Piece.white && contenderMove.evaluation >= bestSoFar.evaluation){
									bestSoFar = move;
									bestSoFar.evaluation = contenderMove.evaluation;
									foundMove = true;
									//bestSoFar.debugNext = contenderMove;
									//bestSoFar.boardNext = result;
								}
								if(canPrune && 
								(board.nextMoveColor == Piece.black && bestSoFar.evaluation <= bestAncestor ||
								board.nextMoveColor == Piece.white && contenderMove.evaluation >= bestAncestor)){
									//alpha beta pruning
									return null;
								}
							}

						}
					}
				}
			}
		}
		
		hashMemo[depth].put(new Board(board), bestSoFar);
		
		
		return bestSoFar;
	}
	

}
