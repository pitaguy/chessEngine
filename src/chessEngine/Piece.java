package chessEngine;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import javax.imageio.ImageIO;

public abstract class Piece {
	
	//default values
	boolean[] isAttacked = new boolean[2];
	int color=1; //0 is white, 1 is black. Used as an array index
	static final int white = 0;
	static final int black = 1;
	
	static boolean cylinderChess= false;
	
	boolean hasMoved = false;
	
	
	boolean isPassable(){
		return false;
	}
	
	abstract int gethash();
	abstract int getPointValue();
	abstract char getFenLet();
	
	abstract LinkedList<Move> getMoves(Board board, int row, int col);
	
	int oppositeColor(){
		return 1-color;
	}
	
	Piece getClone() {
		try{
			Piece newlyMade = this.getClass().newInstance();
			newlyMade.color = color;
			newlyMade.hasMoved = hasMoved;
			//newlyMade.isAttacked = Arrays.copyOf(isAttacked, 2);
			return newlyMade;
		}
		catch(InstantiationException | IllegalAccessException e){
			e.printStackTrace();
			return null;
		}
		
	}
	
	@Override
	public boolean equals(Object obj) {
		Piece other = (Piece) obj;
		return getFenLet() == other.getFenLet() &&
				color == other.color &&
				hasMoved == other.hasMoved;
	}
	
	BufferedImage getImage(int row, int col){
		String fileName = "pieceIcons/"+this.getClass().getSimpleName();
		if(color == 0) fileName += "White";
		else fileName += "Black";
		
		if((row + col) % 2 == 0) fileName += "OnWhite";
		else fileName += "OnBlue";
		
		fileName += ".png";
		
		BufferedImage icon=null;
		try{
			icon = ImageIO.read(new File(fileName));
		}
		catch(IOException e){
			System.err.println("file not found: " + fileName);
		}
		return icon;
	}

}
