package chessEngine;

import javax.swing.ImageIcon;
import javax.swing.JButton;

public class PieceJButton extends JButton {
	//generated arbitrarily
	private static final long serialVersionUID = -1792284307294021079L;
	Move highlightedMove;
	int row,col;
	Piece curPiece;
	public PieceJButton(int row, int col){
		super();
		this.row = row;
		this.col = col;
		//the row and column will index a piece in ChessGui.curBoard
	}
	
	//This constructor is used by the board editor screen
	public PieceJButton(Piece piece){
		curPiece = piece;
	}
	
	void updateImage(Piece piece){
		this.setIcon(new ImageIcon(piece.getImage(row, col)));
	}
	
}
