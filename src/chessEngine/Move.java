package chessEngine;

public class Move {
	int sourceRow, sourceCol, endRow, endCol;
	int castleRookECol =-1, castleRookSCol=-1;
	int evaluation;
//	Move debugNext = null;
//	Board boardNext = null;
	public Move(int sourceRow, int sourceCol, int endRow, int endCol) {
		this.sourceRow = sourceRow;
		this.sourceCol = sourceCol;
		this.endRow = endRow;
		this.endCol = endCol;
	}
	
	public Move(int sourceRow, int sourceCol, int endRow, int endCol, int evaluation) {
		this.sourceRow = sourceRow;
		this.sourceCol = sourceCol;
		this.endRow = endRow;
		this.endCol = endCol;
		this.evaluation = evaluation;
	}
	
	public Move(int evaluation) {
		sourceRow = -2;
		this.evaluation = evaluation;
	}
	
	@Override
	public String toString() {
		return "("+sourceRow+", "+sourceCol+") to " + "("+ endRow+", "+endCol+")";
	}	
	
//	public void printDebug(){
//		System.out.println("Starting a print: ");
//		System.out.println(this);
//		System.out.println("eval: " + evaluation);
//		if(boardNext != null) System.out.println(boardNext.getFen());
//		System.out.println();
//		if(debugNext != null) debugNext.printDebug();
//	}
	
}
