//Gabriel Pita
package chessEngine;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

public class ChessGui {
	boolean gameOver= false;
	
	//the different screens the player interacts with
	JFrame playScreen,mainMenu,playOptionsMenu,boardEditor;
	
	// UI variables
	PieceJButton[][] playButtons;
	Board curBoard,curEditorBoard;
	Piece editorPlacePiece, pieceSelected;
	PieceJButton prevEditorSelected;
	
	//button border info
	static final int highlightBoarderWeight =9;
	Border yellowBorder = BorderFactory.createLineBorder(Color.yellow, highlightBoarderWeight);
	
	//game modes
	boolean chess960 = false;
	boolean vsCpu = true;
	
	//The computer player determines the moves for the computer to play
	ComputerPlayer computerPlayer;
	
	Board[] boardHistory; 				 //used for the undo button
	HashMap<Board,Integer> countVisited; //used to enforce three-fold repetition
	
	public static void main(String[] args) {
		int depth = 4;
		ComputerPlayer computerPlayer = new Minimax(depth);
		new ChessGui(computerPlayer); 
	}
	
	public ChessGui(ComputerPlayer computerPlayer){
		this.computerPlayer = computerPlayer;
		
		imageSetup();
		
		playScreen = new JFrame("Gpita Chess");
		playScreen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		mainMenu = new JFrame("Main Menu");
		mainMenu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		playOptionsMenu = new JFrame("Options");
		playOptionsMenu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		
		boardEditor = new JFrame("Board Editor");
		boardEditor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JButton play = new JButton();
		play.setText("Play");
		
		play.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				mainMenu.setVisible(false);
				playOptionsMenuSetup();
				playOptionsMenu.setVisible(true);
			}
		});
		
		
		JButton edit = new JButton();
		edit.setText("Board Editor");
		
		edit.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				mainMenu.setVisible(false);
				boardEditorSetup();
				boardEditor.setVisible(true);
			}
		});
		
		mainMenu.setLayout(new GridLayout(2, 1));
		JLabel title = new JLabel("Gpita Chess",SwingConstants.CENTER);
		title.setFont(new Font("Serif", Font.BOLD, 36));
		mainMenu.add(title);
		
		JPanel buttons = new JPanel();
		buttons.add(play);
		buttons.add(edit);

		mainMenu.add(buttons);
		mainMenu.setBounds(700, 400, 300, 200);
		mainMenu.setVisible(true);
	}
	
	public void playOptionsMenuSetup(){
		Font optionFont = new Font("Calibri",Font.PLAIN,20);
		JCheckBox chess960Box = new JCheckBox("chess 960");
		chess960Box.setFont(optionFont);
		chess960Box.addItemListener(new ItemListener(){
			@Override
			public void  itemStateChanged(ItemEvent e){
				if(e.getStateChange() == ItemEvent.SELECTED){
					chess960 = true;
				}
				else chess960 = false;
			}
		});
		
		JCheckBox cylinderChessBox = new JCheckBox("cylinder chess (rows wrap around but not for king)");
		cylinderChessBox.setFont(optionFont);
		cylinderChessBox.addItemListener(new ItemListener(){
			@Override
			public void  itemStateChanged(ItemEvent e){
				if(e.getStateChange() == ItemEvent.SELECTED){
					Piece.cylinderChess = true;
				}
				else Piece.cylinderChess = false;
			}
		});
		
		JCheckBox vsCpuBox = new JCheckBox("play against computer");
		vsCpuBox.setFont(optionFont);
		vsCpuBox.setSelected(true);
		vsCpuBox.addItemListener(new ItemListener(){
			@Override
			public void  itemStateChanged(ItemEvent e){
				if(e.getStateChange() == ItemEvent.SELECTED){
					vsCpu = true;
				}
				else vsCpu = false;
			}
		});
		
		JButton start = new JButton();
		start.setText("Start");
		
		start.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				playOptionsMenu.setVisible(false);
				initCurBoard();
				
				playBoardSetup();
				playScreen.setVisible(true);
			}
		});
		
		playOptionsMenu.setLayout(new GridLayout(4,1));
		
		playOptionsMenu.add(chess960Box);
		playOptionsMenu.add(cylinderChessBox);
		playOptionsMenu.add(vsCpuBox);
		playOptionsMenu.add(start);
		playOptionsMenu.pack();
		playOptionsMenu.setLocation(300, 200);
		playOptionsMenu.setResizable(false);
	}
	
	public void boardEditorSetup(){
		boardEditor.setLayout(new FlowLayout());
		JPanel guiBoard = new JPanel(new GridLayout(8, 8));
		PieceJButton[][] buttons= new PieceJButton[8][8];
		editorPlacePiece = new Empty();
		curEditorBoard= new Board();
		
		//filling the array of buttons
		for(int row = 0; row < 8; row++){
			for(int col = 0; col < 8; col++){
				PieceJButton button = new PieceJButton(row, col);
				buttons[row][col] = button;
				button.updateImage(new Empty());
				curEditorBoard.pieces[row][col] = new Empty();
				
				buttons[row][col].addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if(curEditorBoard.pieces[button.row][button.col].getClass() ==  editorPlacePiece.getClass() &&
								curEditorBoard.pieces[button.row][button.col].color == editorPlacePiece.color){
							curEditorBoard.pieces[button.row][button.col] = new Empty();
						}
						else{
							curEditorBoard.pieces[button.row][button.col] = editorPlacePiece.getClone();
						}
						curEditorBoard.updateIsAttacked();
						updateAllIcons(buttons,curEditorBoard);
					}
				});
				guiBoard.add(button);
			}
		}
		
		guiBoard.setPreferredSize(new Dimension(853,853));
		boardEditor.add(guiBoard);
		
		//Setting up the piece type selection for the editor
		JPanel editSelectGui = new JPanel(new GridLayout(2, 5));
		Piece[] editBar = {new King(), new Queen(), new Rook(), new Knight(), new Bishop(), new Pawn(),
				new King(), new Queen(), new Rook(), new Knight(), new Bishop(), new Pawn()};
		
		for(int i = 6; i < editBar.length; i++) editBar[i].color = editBar[i].oppositeColor();
		
		for (int i = 0; i < editBar.length; i++) {
			PieceJButton button  = new PieceJButton(editBar[i]);
			button.setIcon(new ImageIcon(editBar[i].getImage(0, 0)));
			button.setBorder(yellowBorder);
			button.setBorderPainted(false);
			button.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					if(prevEditorSelected != null) prevEditorSelected.setBorderPainted(false);
					editorPlacePiece = button.curPiece;
					button.setBorderPainted(true);
					prevEditorSelected = button;
				}
			});
			
			editSelectGui.add(button);
		}
		
		editSelectGui.setPreferredSize(new Dimension(639, 214));
		
		
		JRadioButton whiteToPlay = new JRadioButton("white to play and human");
		whiteToPlay.setActionCommand("w");
		whiteToPlay.setSelected(true);
		
		JRadioButton blackToPlay = new JRadioButton("black to play and human");
		blackToPlay.setActionCommand("b");
		
		Font optionFont = new Font("Calibri",Font.PLAIN,20);
		whiteToPlay.setFont(optionFont);
		blackToPlay.setFont(optionFont);
		
		ActionListener turnToPlayAction = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(e.getActionCommand().equals("w")) curEditorBoard.nextMoveColor = Piece.white;
				else curEditorBoard.nextMoveColor = Piece.black;
			}
		};
		
		whiteToPlay.addActionListener(turnToPlayAction);
		blackToPlay.addActionListener(turnToPlayAction);
		
		ButtonGroup turnToPlay = new ButtonGroup();
		turnToPlay.add(whiteToPlay);
		turnToPlay.add(blackToPlay);
		
		JMenuBar menuBar = new JMenuBar();
		
		JMenuItem playMenuItem = new JMenuItem("Play");
		playMenuItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if(curEditorBoard.isValid()){
					curBoard = curEditorBoard;				
					boardEditor.setVisible(false);
					
					playBoardSetup();
					playScreen.setVisible(true);
				}
				else{
					JOptionPane.showMessageDialog(boardEditor, "This is an invalid board. Make sure there is one of each king and that there are no king capturing moves.");
				}
				
			}
			
		});
		menuBar.add(playMenuItem);
		
		JMenuItem importMenuItem = new JMenuItem("Import from clipboard");
		importMenuItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
				String importFen = "default import";
				try{
					importFen = (String) clpbrd.getData(DataFlavor.stringFlavor);
				}
				catch(Exception ex){
					System.err.println("Pulling from clipboard error");
					//ex.printStackTrace();
				}
				
				curEditorBoard = Board.importBoard(importFen);
				if(curEditorBoard.nextMoveColor == Piece.white){
					whiteToPlay.setSelected(true);
				}
				else blackToPlay.setSelected(true);
				
				updateAllIcons(buttons, curEditorBoard);
			}
			
		});
		menuBar.add(importMenuItem);
		
		
		JCheckBox cylinderChessBox = new JCheckBox("cylinder chess");
		cylinderChessBox.setFont(optionFont);
		cylinderChessBox.addItemListener(new ItemListener(){
			@Override
			public void  itemStateChanged(ItemEvent e){
				if(e.getStateChange() == ItemEvent.SELECTED){
					Piece.cylinderChess = true;
				}
				else Piece.cylinderChess = false;
			}
		});
		
		JCheckBox vsCpuBox = new JCheckBox("play against computer");
		vsCpuBox.setFont(optionFont);
		vsCpuBox.setSelected(true);
		vsCpuBox.addItemListener(new ItemListener(){
			@Override
			public void  itemStateChanged(ItemEvent e){
				if(e.getStateChange() == ItemEvent.SELECTED){
					vsCpu = true;
				}
				else vsCpu = false;
			}
		});
		
		boardEditor.setJMenuBar(menuBar);
		
		JPanel editAndOptions = new JPanel(new GridLayout(2,1));
		
		editAndOptions.add(editSelectGui);
		JPanel options = new JPanel(new GridLayout(4,1));
		
		options.add(cylinderChessBox);
		options.add(vsCpuBox);
		options.add(whiteToPlay);
		options.add(blackToPlay);
		
		editAndOptions.add(options);
		
		boardEditor.add(editAndOptions);
		boardEditor.pack();
		boardEditor.setLocation(200, 50);
	}
	
	public void playBoardSetup(){
		JPanel guiBoard = new JPanel(new GridLayout(8, 8));
			
		playButtons= new PieceJButton[8][8];
		countVisited = new HashMap<>();
		boardHistory = new Board[6500];
		
		//setting up the interactive play board
		for(int row = 0; row < 8; row++){
			for(int col =0; col < 8; col++){
				PieceJButton button = new PieceJButton(row,col);
				button.setBorder(yellowBorder);
				button.setBorderPainted(false);
				
				button.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						if(gameOver) return;
						
						if(button.isBorderPainted() && button.getBorder().equals(yellowBorder)){
							curBoard.makeMove(button.highlightedMove);
							
							updateVisited(curBoard);
							boardHistory[curBoard.turn] = new Board(curBoard);
							
							wipeHighlights();
							updateAllIcons(playButtons,curBoard);
							checkIfGameOver(curBoard);
							
							if(vsCpu && !gameOver){
								
								SwingUtilities.invokeLater(new Runnable() {
							        public void run() {
							        	Move best = computerPlayer.getCpuMove(curBoard,countVisited);
							        	doCpuMove(best);
							        }
							    });
								
							}
						}
						else{
							wipeHighlights();
							if(pieceSelected == curBoard.pieces[button.row][button.col]){
								pieceSelected = null;
							}
							else{
								pieceSelected = curBoard.pieces[button.row][button.col];
								
								LinkedList<Move> moves = curBoard.pieces[button.row][button.col].getMoves(curBoard, button.row, button.col);
								
								if(curBoard.pieces[button.row][button.col].color == curBoard.nextMoveColor){
									for(Move move : moves){
										
										if(curBoard.isIllegal(move) != null){
											if(move.castleRookSCol != -1){
												playButtons[move.endRow][move.castleRookSCol].setBorderPainted(true);
												playButtons[move.endRow][move.castleRookSCol].highlightedMove = move;
											}
											else{
												playButtons[move.endRow][move.endCol].setBorderPainted(true);
												playButtons[move.endRow][move.endCol].highlightedMove = move;
											}
											
										}
									}
									
								}else{
									
									for(Move move : moves){
										
										if(move.castleRookSCol != -1){
											playButtons[move.endRow][move.castleRookSCol].setBorder(BorderFactory.createLineBorder(Color.darkGray, highlightBoarderWeight));
											playButtons[move.endRow][move.castleRookSCol].setBorderPainted(true);
										}
										else{
											playButtons[move.endRow][move.endCol].setBorder(BorderFactory.createLineBorder(Color.darkGray, highlightBoarderWeight));
											playButtons[move.endRow][move.endCol].setBorderPainted(true);
										}
										
										
									}
									
								}
							}
							
						}

					}
				});
				
				guiBoard.add(button);
				playButtons[row][col] = button;
			}
		}
		
		curBoard.setKingPositions();
		
		updateAllIcons(playButtons,curBoard);
		
		updateVisited(curBoard);
		boardHistory[0] = new Board(curBoard);
		
		JMenuBar menuBar = new JMenuBar();
		
		JMenuItem undoMenuItem = new JMenuItem("Undo");
		undoMenuItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if(curBoard.turn < 2 || gameOver) return;
				wipeHighlights();
				undoVisited(curBoard);
				undoVisited(boardHistory[curBoard.turn-1]);
				curBoard = new Board(boardHistory[curBoard.turn-2]);
				updateAllIcons(playButtons,curBoard);
			}
			
		});
		menuBar.add(undoMenuItem);
		
		
		if(vsCpu){
			JMenuItem swap = new JMenuItem("Swap with cpu (next move in cpu vs cpu)");
			swap.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					Move best = computerPlayer.getCpuMove(curBoard,countVisited);
		        	doCpuMove(best);
				}
				
			});
			menuBar.add(swap);
		}
		
		JMenuItem exportMenuItem = new JMenuItem("Export");
		exportMenuItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				
				StringSelection boardCode = new StringSelection(curBoard.getFen());
				Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
				clpbrd.setContents(boardCode, null);
				
				JOptionPane.showMessageDialog(playScreen, " Copied to clipboard ");
			}
			
		});
		
		menuBar.add(exportMenuItem);
		
		
		playScreen.setJMenuBar(menuBar);
		
		playScreen.add(guiBoard);
		
		playScreen.setSize(853, 853);
		playScreen.setResizable(false);
		playScreen.setLocation(600,50);
	}
	
	public void initCurBoard(){
		curBoard = new Board();

		ArrayList<Piece> backRank = new ArrayList<Piece>();
		backRank.add(new Rook()); backRank.add(new Knight()); backRank.add(new Bishop());
		backRank.add(new Queen()); backRank.add(new King());
		backRank.add(new Bishop()); backRank.add(new Knight()); backRank.add(new Rook());
		
		if(chess960){
			boolean done = false;
			while(!done){
				done = true;
				int rookCountBeforeKing = 0; //king must be between the rooks
				int bishopSum = 0; //bishops must be on opposite colors
				
				Collections.shuffle(backRank);
				for(int i =0; i < backRank.size(); i++){
					if(backRank.get(i) instanceof Bishop){
						bishopSum += i;
					}
					if(backRank.get(i) instanceof Rook){
						rookCountBeforeKing++;
						if(rookCountBeforeKing == 2) done = false;
					}
					if(backRank.get(i) instanceof King){
						rookCountBeforeKing = 0;
					}
				}
				if(bishopSum % 2 == 0){
					done = false;
				}
				
			}
		}
		
		Piece templatePawn = new Pawn();
		
		for(int row = 0; row < 8; row++){
			if(row == 3){
				//past the black pieces, now changing color to white
				for(Piece p: backRank) p.color = Piece.white;
				templatePawn.color= Piece.white;
			}
			
			for(int col =0; col < 8; col++){
				
				if(row == 0 || row == 7){
					curBoard.pieces[row][col] = backRank.get(col).getClone();
				}
				else if(row == 1 || row == 6){
					curBoard.pieces[row][col] = templatePawn.getClone();
				}
				else{
					curBoard.pieces[row][col] = new Empty();
				}
				
				
			}
		}
		
		
	}
	
	public void doCpuMove(Move move){
		
		curBoard.makeMove(move);
		updateVisited(curBoard);
		boardHistory[curBoard.turn] = new Board(curBoard);
		
		wipeHighlights();
		
		playButtons[move.endRow][move.endCol].setBorder(BorderFactory.createLineBorder(Color.red, highlightBoarderWeight));
		playButtons[move.endRow][move.endCol].setBorderPainted(true);
		
		playButtons[move.sourceRow][move.sourceCol].setBorder(BorderFactory.createLineBorder(Color.red, highlightBoarderWeight));
		playButtons[move.sourceRow][move.sourceCol].setBorderPainted(true);
		
		updateAllIcons(playButtons,curBoard);
		pieceSelected = null;
		
		checkIfGameOver(curBoard);
	}
	
	public void checkIfGameOver(Board board){
		if(board.thereAreNoMoves()){
			if(board.kingAttacked()) JOptionPane.showMessageDialog(playScreen, (board.nextMoveColor == 1 ? "Black" : "White")+"  is checkmated!");
			else JOptionPane.showMessageDialog(playScreen, "Stalemate!");
			gameOver = true;
		}
		else if(countVisited.get(board) >= 3){
			JOptionPane.showMessageDialog(playScreen, "Draw by repetition");
			gameOver = true;
		}
		else if(board.movesSinceProgress >= 100){
			JOptionPane.showMessageDialog(playScreen, "Draw by 50 move rule");
			gameOver = true;
		}
		
	}
	
	void updateVisited(Board board){
		Board copyboard = new Board(board);
		if(!countVisited.containsKey(copyboard)){
			countVisited.put(copyboard, 1);
		}
		else{
			countVisited.put(copyboard, countVisited.get(copyboard)+1);
		}
	}
	
	void undoVisited(Board board){
		Board copyboard = new Board(board);
	
		countVisited.put(copyboard, countVisited.get(copyboard)-1);
		
	}
	
	public void wipeHighlights(){
		for(int row = 0; row < 8; row++){
			for(int col = 0; col < 8; col++){
				playButtons[row][col].setBorderPainted(false);
				playButtons[row][col].setBorder(yellowBorder);
			}
		}
	}
	
	public void updateAllIcons(PieceJButton[][] buttons, Board board){
		for(int row = 0; row < 8; row++){
			for(int col = 0; col < 8; col++){
				buttons[row][col].updateImage(board.pieces[row][col]);
			}
		}
	}
	
	public void imageSetup(){
		int imageSize = 106;
		String[] imageOrder = {"King",	"Queen", "Bishop", "Knight", "Rook", "Pawn", "Empty"};		
		String[] colorsTopToBottom = {"White","Black"};
		String[] backgrounds = {"OnWhite","OnBlue"};
		
		BufferedImage[] chesspiecesWB = null;
		try{
			chesspiecesWB = new BufferedImage[]{ImageIO.read(new File("ChessPiecesArrayWhite.png")),ImageIO.read(new File("ChessPiecesArrayBlue.png"))};
			for(int i =0; i < backgrounds.length; i++){
				for(int j =0; j < colorsTopToBottom.length; j++){
					for(int k =0; k < imageOrder.length; k++){
						
						String fileName = "pieceIcons/"+imageOrder[k]+colorsTopToBottom[j]+backgrounds[i]+".png";
						BufferedImage toWrite = chesspiecesWB[i].getSubimage(imageSize*k, imageSize*j, imageSize, imageSize);
						File outputFile = new File(fileName);
						
						ImageIO.write(toWrite, "png", outputFile);
					}
				}
			}
		}
		catch(IOException e){
			System.err.println("setup error: " + e.getMessage());
		}
		
	}

}
